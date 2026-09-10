package com.example.cyberquiz.viewmodel

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.database.ProgressEntity
import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.data.database.ReviewItemEntity
import com.example.cyberquiz.data.database.ReviewItemWithQuestion
import com.example.cyberquiz.data.repository.QuizHistoryStore
import com.example.cyberquiz.data.repository.QuizRepository
import com.example.cyberquiz.engagement.DAILY_CHALLENGE_SIZE
import com.example.cyberquiz.engagement.DailyChallengeStore
import com.example.cyberquiz.model.ActiveQuizSessionSummary
import com.example.cyberquiz.model.AdaptiveReviewEngine
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.QuizHistoryEntry
import com.example.cyberquiz.model.QuizHistoryQuestion
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.model.examDeadlineEpochMs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Ready(val question: QuestionEntity, val number: Int) : QuizUiState
    data object Finished : QuizUiState
}

data class AnswerResult(
    val correct: Boolean,
    val xp: Int,
    val explanation: String,
    val question: QuestionEntity
)

data class QuizFinishSummary(
    val answered: Int = 0,
    val correct: Int = 0,
    val xpGained: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = CyberQuizDatabase.get(app).quizDao()
    private val repo = QuizRepository(dao)
    private val initialized = CompletableDeferred<Unit>()
    private val sessionPrefs = app.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
    private val historyStore = QuizHistoryStore(app)

    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private val _activeQuizType = MutableStateFlow(CYBERSECURITY)
    val progress: StateFlow<ProgressEntity> = _activeQuizType
        .flatMapLatest { quizType ->
            repo.progress(quizType).map { stored -> stored ?: emptyProgress(quizType) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyProgress(CYBERSECURITY)
        )

    val reviewItems: StateFlow<List<ReviewItemEntity>> = _activeQuizType
        .flatMapLatest { quizType -> dao.reviewItems(quizType) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val reviewItemsWithQuestions: StateFlow<List<ReviewItemWithQuestion>> = _activeQuizType
        .flatMapLatest { quizType -> dao.reviewItemsWithQuestions(quizType) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val categoryProgress: StateFlow<List<CategoryProgressEntity>> = _activeQuizType
        .flatMapLatest { quizType -> dao.categoryProgress(quizType) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val conceptProgress: StateFlow<List<ConceptProgressEntity>> = _activeQuizType
        .flatMapLatest { quizType -> dao.conceptProgress(quizType) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _result = MutableStateFlow<AnswerResult?>(null)
    val result: StateFlow<AnswerResult?> = _result.asStateFlow()

    private val _finishSummary = MutableStateFlow<QuizFinishSummary?>(null)
    val finishSummary: StateFlow<QuizFinishSummary?> = _finishSummary.asStateFlow()

    private val _quizHistory = MutableStateFlow(historyStore.load())
    val quizHistory: StateFlow<List<QuizHistoryEntry>> = _quizHistory.asStateFlow()

    private val _reviewMode = MutableStateFlow(false)
    val reviewMode: StateFlow<Boolean> = _reviewMode.asStateFlow()

    private val _lastSessionConfig = MutableStateFlow(loadLastSessionConfig())
    val lastSessionConfig: StateFlow<QuizSessionConfig> = _lastSessionConfig.asStateFlow()

    private val _activeSessions = MutableStateFlow(loadActiveSessionsWithLegacyMigration())
    val activeSessions: StateFlow<List<ActiveQuizSessionSummary>> = _activeSessions.asStateFlow()

    val activeSession: StateFlow<ActiveQuizSessionSummary?> = _activeSessions
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _activeSessions.value.firstOrNull()
        )

    private val _restoredSelection = MutableStateFlow<Int?>(null)
    val restoredSelection: StateFlow<Int?> = _restoredSelection.asStateFlow()

    private val _currentSessionConfig = MutableStateFlow<QuizSessionConfig?>(null)
    val currentSessionConfig: StateFlow<QuizSessionConfig?> = _currentSessionConfig.asStateFlow()

    private val _examDeadline = MutableStateFlow<Long?>(null)
    val examDeadline: StateFlow<Long?> = _examDeadline.asStateFlow()

    private var currentQuizType = CYBERSECURITY
    private var currentCategory: String? = null
    private var questionNumber = 0
    private var reviewQuestionId: Long? = null
    private var singleReviewQuestion = false
    private var questionPresentedAtElapsed = 0L

    private var runAnswered = 0
    private var runCorrect = 0
    private var runXpGained = 0
    private var runStartedAt = 0L
    private var runReplayConfig: QuizSessionConfig? = null
    private var runHistorySaved = false
    private val runHistoryQuestions = mutableListOf<QuizHistoryQuestion>()

    private var configuredSessionRuntime = false
    private var configuredSessionId: String? = null
    private var configuredSessionConfig: QuizSessionConfig? = null
    private var dailyChallengeDay: String? = null
    private var sessionQueue = mutableListOf<Long>()
    private var sessionIndex = 0
    private var sessionAnswered = 0
    private var sessionCurrentQuestionId: Long? = null
    private var sessionPendingAnswer = false
    private var sessionPendingCorrect = false
    private var sessionPendingXp = 0
    private var sessionPendingSelected: Int? = null

    init {
        viewModelScope.launch {
            try {
                repo.init()
            } finally {
                initialized.complete(Unit)
            }
        }
    }

    fun selectQuizType(quizType: String) {
        currentQuizType = quizType
        currentCategory = null
        _activeQuizType.value = quizType
    }

    fun start(category: Category? = null, quizType: String = CYBERSECURITY) {
        leaveConfiguredRuntime()
        resetRunScore(
            if (quizType == CYBERSECURITY) {
                QuizSessionConfig(
                    mode = QuizSessionMode.RANDOM,
                    categories = category?.let { setOf(it.label) }
                        ?: Category.entries.map { it.label }.toSet(),
                    questionCount = 0
                )
            } else null
        )
        singleReviewQuestion = false
        selectQuizType(quizType)
        currentCategory = category?.label
        questionNumber = 0
        reviewQuestionId = null
        _reviewMode.value = false
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch {
            initialized.await()
            loadNext()
        }
    }

    fun startCategory(quizType: String, category: String) {
        leaveConfiguredRuntime()
        resetRunScore(
            if (quizType == CYBERSECURITY) {
                QuizSessionConfig(
                    mode = QuizSessionMode.RANDOM,
                    categories = setOf(category),
                    questionCount = 0
                )
            } else null
        )
        singleReviewQuestion = false
        selectQuizType(quizType)
        currentCategory = category
        questionNumber = 0
        reviewQuestionId = null
        _reviewMode.value = false
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch {
            initialized.await()
            loadNext()
        }
    }

    fun startReviewQuestion(quizType: String, questionId: Long) {
        leaveConfiguredRuntime()
        resetRunScore(null)
        singleReviewQuestion = true
        selectQuizType(quizType)
        currentCategory = null
        questionNumber = 0
        reviewQuestionId = questionId
        _reviewMode.value = true
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch {
            initialized.await()
            loadNext()
        }
    }

    fun startConfiguredQuiz(config: QuizSessionConfig) {
        if (_activeSessions.value.size >= MAX_ACTIVE_SESSIONS) return
        prepareConfiguredSession(config, dailyDay = null)

        viewModelScope.launch {
            initialized.await()
            if (config.mode == QuizSessionMode.DIFFICULTIES) {
                val question = chooseDifficultyQuestion(config, emptySet())
                if (question == null) {
                    finishConfiguredSession()
                } else {
                    sessionQueue = mutableListOf(question.id)
                    sessionCurrentQuestionId = question.id
                    persistActiveSession()
                    showQuestion(question, 1)
                }
            } else {
                val base = configuredQuestionPool(config)
                if (base.isEmpty()) {
                    finishConfiguredSession()
                    return@launch
                }
                sessionQueue = if (config.infinite) {
                    shuffledCycle(base.map { it.id }).toMutableList()
                } else {
                    buildFiniteQueue(base.map { it.id }, config.questionCount).toMutableList()
                }
                sessionIndex = 0
                val question = sessionQueue.firstOrNull()?.let { dao.questionById(it) }
                if (question == null) {
                    finishConfiguredSession()
                } else {
                    sessionCurrentQuestionId = question.id
                    persistActiveSession()
                    showQuestion(question, 1)
                }
            }
        }
    }

    fun startDailyChallenge(): Boolean {
        if (_activeSessions.value.size >= MAX_ACTIVE_SESSIONS) return false
        val day = DailyChallengeStore.today()
        if (DailyChallengeStore.snapshot(getApplication(), day).completedToday) return false
        val config = QuizSessionConfig(
            mode = QuizSessionMode.RANDOM,
            categories = Category.entries.map { it.label }.toSet(),
            questionCount = DAILY_CHALLENGE_SIZE
        )
        prepareConfiguredSession(config, dailyDay = day)
        viewModelScope.launch {
            initialized.await()
            val ids = DailyChallengeStore.selectQuestionIds(
                dao.questionsSnapshot(CYBERSECURITY),
                day,
                DAILY_CHALLENGE_SIZE
            )
            if (ids.isEmpty()) {
                finishConfiguredSession()
                return@launch
            }
            sessionQueue = ids.toMutableList()
            sessionIndex = 0
            val question = dao.questionById(ids.first())
            if (question == null) {
                finishConfiguredSession()
            } else {
                sessionCurrentQuestionId = question.id
                persistActiveSession()
                showQuestion(question, 1)
            }
        }
        return true
    }

    private fun prepareConfiguredSession(config: QuizSessionConfig, dailyDay: String?) {
        saveLastSessionConfig(config)
        resetRunScore(config)
        configuredSessionRuntime = true
        configuredSessionId = nextSessionId()
        configuredSessionConfig = config
        _currentSessionConfig.value = config
        _examDeadline.value = if (config.timedExam) {
            examDeadlineEpochMs(runStartedAt, config.timeLimitMinutes)
        } else null
        dailyChallengeDay = dailyDay
        singleReviewQuestion = false
        selectQuizType(CYBERSECURITY)
        currentCategory = null
        questionNumber = 0
        reviewQuestionId = null
        sessionQueue.clear()
        sessionIndex = 0
        sessionAnswered = 0
        sessionCurrentQuestionId = null
        sessionPendingAnswer = false
        sessionPendingCorrect = false
        sessionPendingXp = 0
        sessionPendingSelected = null
        _reviewMode.value = config.mode == QuizSessionMode.DIFFICULTIES
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Loading
        persistActiveSession()
    }

    fun resumeConfiguredQuiz(sessionId: String) {
        if (sessionId !in loadSessionIds()) return

        configuredSessionRuntime = true
        configuredSessionId = sessionId
        configuredSessionConfig = loadSessionConfig(sessionId)
        val resumedConfig = configuredSessionConfig
        _currentSessionConfig.value = resumedConfig
        dailyChallengeDay = sessionPrefs.getString(sessionKey(sessionId, FIELD_DAILY_DAY), null)
        singleReviewQuestion = false
        selectQuizType(CYBERSECURITY)
        currentCategory = null
        reviewQuestionId = null
        sessionQueue = loadSessionQueue(sessionId).toMutableList()
        sessionIndex = sessionPrefs.getInt(sessionKey(sessionId, FIELD_INDEX), 0)
        sessionAnswered = sessionPrefs.getInt(sessionKey(sessionId, FIELD_ANSWERED), 0)
        sessionCurrentQuestionId = sessionPrefs
            .getLong(sessionKey(sessionId, FIELD_CURRENT_ID), -1L)
            .takeIf { it > 0L }
        sessionPendingAnswer = sessionPrefs.getBoolean(sessionKey(sessionId, FIELD_PENDING), false)
        sessionPendingCorrect = sessionPrefs.getBoolean(sessionKey(sessionId, FIELD_CORRECT), false)
        sessionPendingXp = sessionPrefs.getInt(sessionKey(sessionId, FIELD_XP), 0)
        sessionPendingSelected = sessionPrefs
            .getInt(sessionKey(sessionId, FIELD_SELECTED), -1)
            .takeIf { it >= 0 }

        runAnswered = sessionAnswered
        runCorrect = sessionPrefs.getInt(sessionKey(sessionId, FIELD_CORRECT_COUNT), 0)
        runXpGained = sessionPrefs.getInt(sessionKey(sessionId, FIELD_XP_GAINED), 0)
        runStartedAt = sessionPrefs
            .getLong(sessionKey(sessionId, FIELD_STARTED_AT), System.currentTimeMillis())
        _examDeadline.value = resumedConfig?.takeIf { it.timedExam }?.let {
            examDeadlineEpochMs(runStartedAt, it.timeLimitMinutes)
        }
        runReplayConfig = configuredSessionConfig
        runHistorySaved = false
        runHistoryQuestions.clear()
        runHistoryQuestions += historyStore.decodeQuestions(
            sessionPrefs.getString(sessionKey(sessionId, FIELD_ANSWER_HISTORY), null)
        )
        _finishSummary.value = null

        _reviewMode.value = configuredSessionConfig?.mode == QuizSessionMode.DIFFICULTIES
        _restoredSelection.value = if (resumedConfig?.exam == true) null else sessionPendingSelected
        _result.value = null
        _state.value = QuizUiState.Loading
        refreshActiveSessions()

        viewModelScope.launch {
            initialized.await()
            if (isExamExpired()) {
                finishConfiguredSession()
                return@launch
            }
            if (resumedConfig?.exam == true && sessionPendingAnswer) {
                advanceConfiguredSession()
                return@launch
            }
            val question = sessionCurrentQuestionId?.let { dao.questionById(it) }
            if (question == null) {
                advanceConfiguredSession()
                return@launch
            }
            val number = if (sessionPendingAnswer) sessionAnswered.coerceAtLeast(1) else sessionAnswered + 1
            showQuestion(question, number)
            if (sessionPendingAnswer) {
                _result.value = AnswerResult(
                    correct = sessionPendingCorrect,
                    xp = sessionPendingXp,
                    explanation = question.explanation,
                    question = question
                )
            }
        }
    }

    fun abandonConfiguredQuiz(sessionId: String) {
        removeSessionStorage(sessionId)
        if (configuredSessionId == sessionId) {
            leaveConfiguredRuntime()
            resetRunScore(null)
            _reviewMode.value = false
            _restoredSelection.value = null
            _result.value = null
            _state.value = QuizUiState.Finished
        }
    }

    fun restartLastConfiguredQuiz() {
        startConfiguredQuiz(_lastSessionConfig.value)
    }

    fun restartHistoryQuiz(historyId: String): Boolean {
        if (_activeSessions.value.size >= MAX_ACTIVE_SESSIONS) return false
        val entry = _quizHistory.value.firstOrNull { it.id == historyId } ?: return false
        startConfiguredQuiz(entry.config)
        return true
    }

    fun answer(index: Int) {
        val current = (_state.value as? QuizUiState.Ready)?.question ?: return
        if (_result.value != null) return
        val examMode = configuredSessionRuntime && configuredSessionConfig?.exam == true
        if (examMode && isExamExpired()) {
            finishConfiguredSession()
            return
        }
        if (examMode) _state.value = QuizUiState.Loading
        val responseMs = if (questionPresentedAtElapsed > 0L) {
            (SystemClock.elapsedRealtime() - questionPresentedAtElapsed).coerceIn(0L, 120_000L)
        } else 0L

        viewModelScope.launch {
            val p = repo.progressSnapshot(currentQuizType) ?: emptyProgress(currentQuizType)
            val ok = index == current.correctIndex
            val isReview = _reviewMode.value
            val concept = correctAnswer(current).trim()
            val reviewItemBefore = if (currentQuizType == CYBERSECURITY && concept.isNotBlank()) {
                dao.reviewItemSnapshot(CYBERSECURITY, concept)
            } else null

            val firstReviewSuccess = isReview && ok && reviewItemBefore != null &&
                reviewItemBefore.correctAfterWrongCount == 0

            val gainedXp = when {
                isReview -> if (firstReviewSuccess) 5 else 0
                ok -> 10 + when (current.difficulty) {
                    "HARD" -> 10
                    "MEDIUM" -> 5
                    else -> 0
                }
                else -> 0
            }

            val newStreak = if (isReview) p.streak else if (ok) p.streak + 1 else 0
            val best = if (isReview) p.bestStreak else maxOf(p.bestStreak, newStreak)
            val newXp = p.xp + gainedXp
            val newLevel = (newXp / 100) + 1
            val newAnswered = if (isReview) p.answered else p.answered + 1
            val newCorrect = if (isReview) p.correct else p.correct + if (ok) 1 else 0
            val newTotalResponseMs = if (isReview) p.totalResponseMs else p.totalResponseMs + responseMs

            if (!isReview) repo.markSeen(current.id)

            if (currentQuizType == CYBERSECURITY) {
                if (isReview) recordConceptReviewResult(current, ok) else recordLearningProgress(current, ok)
                recordReviewResult(current, ok, responseMs)

                if (!isReview && ok && reviewItemBefore != null && !reviewItemBefore.mastered) {
                    dao.markConceptMasteredByReview(
                        quizType = CYBERSECURITY,
                        concept = concept,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }

            dao.updateProgress(
                quizType = currentQuizType,
                xp = newXp,
                level = newLevel,
                answered = newAnswered,
                correct = newCorrect,
                streak = newStreak,
                bestStreak = best,
                totalResponseMs = newTotalResponseMs
            )

            runAnswered += 1
            if (ok) runCorrect += 1
            runXpGained += gainedXp

            if (currentQuizType == CYBERSECURITY && !singleReviewQuestion) {
                runHistoryQuestions += QuizHistoryQuestion(
                    questionId = current.id,
                    category = current.category,
                    difficulty = current.difficulty,
                    question = current.question,
                    answers = listOf(current.answerA, current.answerB, current.answerC, current.answerD),
                    correctIndex = current.correctIndex,
                    selectedIndex = index,
                    explanation = current.explanation
                )
            }

            if (configuredSessionRuntime) {
                sessionAnswered += 1
                sessionCurrentQuestionId = current.id
                sessionPendingAnswer = true
                sessionPendingCorrect = ok
                sessionPendingXp = gainedXp
                sessionPendingSelected = index
                persistActiveSession()
            }

            if (examMode) {
                _restoredSelection.value = null
                _result.value = null
                if (isExamExpired()) {
                    finishConfiguredSession()
                } else {
                    advanceConfiguredSession()
                }
            } else {
                _restoredSelection.value = index
                _result.value = AnswerResult(ok, gainedXp, current.explanation, current)
            }
        }
    }

    fun finishExamOnTimeout(nowEpochMs: Long = System.currentTimeMillis()) {
        if (_state.value !is QuizUiState.Ready) return
        if (isExamExpired(nowEpochMs)) finishConfiguredSession()
    }

    private fun isExamExpired(nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        val config = configuredSessionConfig ?: return false
        if (!configuredSessionRuntime || !config.timedExam) return false
        val deadline = _examDeadline.value ?: return false
        return nowEpochMs >= deadline
    }

    fun nextQuestion() {
        _result.value = null
        _restoredSelection.value = null
        if (configuredSessionRuntime) {
            viewModelScope.launch { advanceConfiguredSession() }
            return
        }
        if (_reviewMode.value && singleReviewQuestion) {
            reviewQuestionId = null
            publishFinishSummary()
            _state.value = QuizUiState.Finished
            return
        }
        viewModelScope.launch { loadNext() }
    }

    private suspend fun advanceConfiguredSession() {
        val config = configuredSessionConfig ?: run {
            finishConfiguredSession()
            return
        }

        sessionPendingAnswer = false
        sessionPendingCorrect = false
        sessionPendingXp = 0
        sessionPendingSelected = null

        if (!config.infinite && sessionAnswered >= config.questionCount) {
            finishConfiguredSession()
            return
        }

        if (config.mode == QuizSessionMode.DIFFICULTIES) {
            val question = chooseDifficultyQuestion(config, sessionQueue.toSet())
            if (question == null) {
                if (config.infinite) sessionQueue.clear()
                val retry = if (config.infinite) chooseDifficultyQuestion(config, emptySet()) else null
                if (retry == null) {
                    finishConfiguredSession()
                    return
                }
                sessionQueue += retry.id
                sessionCurrentQuestionId = retry.id
                persistActiveSession()
                showQuestion(retry, sessionAnswered + 1)
                return
            }
            sessionQueue += question.id
            sessionCurrentQuestionId = question.id
            persistActiveSession()
            showQuestion(question, sessionAnswered + 1)
            return
        }

        sessionIndex += 1
        if (sessionIndex >= sessionQueue.size) {
            if (!config.infinite) {
                finishConfiguredSession()
                return
            }
            val base = configuredQuestionPool(config)
            if (base.isEmpty()) {
                finishConfiguredSession()
                return
            }
            sessionQueue = shuffledCycle(base.map { it.id }).toMutableList()
            sessionIndex = 0
        }

        val question = sessionQueue.getOrNull(sessionIndex)?.let { dao.questionById(it) }
        if (question == null) {
            finishConfiguredSession()
            return
        }
        sessionCurrentQuestionId = question.id
        persistActiveSession()
        showQuestion(question, sessionAnswered + 1)
    }

    private suspend fun configuredQuestionPool(config: QuizSessionConfig): List<QuestionEntity> {
        val all = dao.questionsSnapshot(CYBERSECURITY)
        return all.filter { question ->
            val categoryMatches = question.category in config.categories
            val difficultyMatches = when (config.mode) {
                QuizSessionMode.EASY -> question.difficulty.equals("EASY", ignoreCase = true)
                QuizSessionMode.MEDIUM -> question.difficulty.equals("MEDIUM", ignoreCase = true)
                QuizSessionMode.HARD -> question.difficulty.equals("HARD", ignoreCase = true)
                QuizSessionMode.RANDOM -> true
                QuizSessionMode.DIFFICULTIES -> true
                QuizSessionMode.EXAM -> true
            }
            categoryMatches && difficultyMatches
        }
    }

    private suspend fun chooseDifficultyQuestion(
        config: QuizSessionConfig,
        excludedQuestionIds: Set<Long>
    ): QuestionEntity? {
        val reviewItems = dao.reviewItemsSnapshot(CYBERSECURITY)
            .filter { it.category in config.categories }
        val ranked = AdaptiveReviewEngine.rankDue(
            items = reviewItems,
            now = System.currentTimeMillis(),
            excludedQuestionIds = excludedQuestionIds
        )
        return ranked.firstOrNull()?.questionId?.let { dao.questionById(it) }
    }

    private fun buildFiniteQueue(baseIds: List<Long>, count: Int): List<Long> {
        if (baseIds.isEmpty() || count <= 0) return emptyList()
        val output = mutableListOf<Long>()
        while (output.size < count) {
            val cycle = shuffledCycle(baseIds).toMutableList()
            if (output.isNotEmpty() && cycle.size > 1 && cycle.first() == output.last()) {
                val first = cycle.removeAt(0)
                cycle.add(first)
            }
            output += cycle.take(count - output.size)
        }
        return output
    }

    private fun shuffledCycle(ids: List<Long>): List<Long> = ids.shuffled()

    private suspend fun recordLearningProgress(question: QuestionEntity, correct: Boolean) {
        val now = System.currentTimeMillis()
        val concept = correctAnswer(question).trim()

        dao.insertCategoryProgress(CategoryProgressEntity(quizType = CYBERSECURITY, category = question.category))
        dao.incrementCategoryProgress(
            quizType = CYBERSECURITY,
            category = question.category,
            correctIncrement = if (correct) 1 else 0,
            timestamp = now
        )

        if (concept.isBlank()) return
        val previousReview = dao.reviewItemSnapshot(CYBERSECURITY, concept)
        val keepReviewMastered = correct && previousReview?.mastered == true

        dao.insertConceptProgress(
            ConceptProgressEntity(
                quizType = CYBERSECURITY,
                concept = concept,
                category = question.category
            )
        )
        dao.incrementConceptProgress(
            quizType = CYBERSECURITY,
            concept = concept,
            category = question.category,
            correctIncrement = if (correct) 1 else 0,
            reviewMastered = keepReviewMastered,
            lastResultCorrect = correct,
            timestamp = now
        )
    }

    private suspend fun recordConceptReviewResult(question: QuestionEntity, correct: Boolean) {
        val concept = correctAnswer(question).trim()
        if (concept.isBlank()) return
        val now = System.currentTimeMillis()
        dao.insertConceptProgress(
            ConceptProgressEntity(
                quizType = CYBERSECURITY,
                concept = concept,
                category = question.category
            )
        )
        if (correct) dao.markConceptMasteredByReview(CYBERSECURITY, concept, now)
        else dao.markConceptNeedsReview(CYBERSECURITY, concept, now)
    }

    private suspend fun recordReviewResult(
        question: QuestionEntity,
        correct: Boolean,
        responseMs: Long
    ) {
        val concept = correctAnswer(question).trim()
        if (concept.isBlank()) return
        val existing = dao.reviewItemSnapshot(CYBERSECURITY, concept)
        val now = System.currentTimeMillis()

        if (correct) {
            if (existing == null) return
            dao.recordReviewCorrect(CYBERSECURITY, concept)
            val decision = AdaptiveReviewEngine.schedule(
                previousStage = existing.reviewStage,
                correct = true,
                wrongCount = existing.wrongCount,
                responseMs = responseMs,
                now = now
            )
            dao.updateReviewSchedule(
                quizType = CYBERSECURITY,
                concept = concept,
                reviewStage = decision.stage,
                nextReviewAt = decision.nextReviewAt,
                lastReviewedAt = now,
                reviewAttempts = existing.reviewAttempts + 1,
                totalReviewResponseMs = existing.totalReviewResponseMs + responseMs
            )
            return
        }

        val decision = AdaptiveReviewEngine.schedule(
            previousStage = existing?.reviewStage ?: 0,
            correct = false,
            wrongCount = (existing?.wrongCount ?: 0) + 1,
            responseMs = responseMs,
            now = now
        )
        if (existing == null) {
            dao.insertReviewItem(
                ReviewItemEntity(
                    quizType = CYBERSECURITY,
                    concept = concept,
                    category = question.category,
                    difficulty = question.difficulty,
                    questionId = question.id,
                    question = question.question,
                    correctAnswer = concept,
                    wrongCount = 1,
                    correctAfterWrongCount = 0,
                    mastered = false,
                    lastWrongAt = now,
                    reviewStage = decision.stage,
                    nextReviewAt = decision.nextReviewAt,
                    lastReviewedAt = now,
                    reviewAttempts = 1,
                    totalReviewResponseMs = responseMs
                )
            )
        } else {
            dao.recordReviewWrong(
                quizType = CYBERSECURITY,
                concept = concept,
                category = question.category,
                difficulty = question.difficulty,
                questionId = question.id,
                question = question.question,
                correctAnswer = concept,
                lastWrongAt = now
            )
            dao.updateReviewSchedule(
                quizType = CYBERSECURITY,
                concept = concept,
                reviewStage = decision.stage,
                nextReviewAt = decision.nextReviewAt,
                lastReviewedAt = now,
                reviewAttempts = existing.reviewAttempts + 1,
                totalReviewResponseMs = existing.totalReviewResponseMs + responseMs
            )
        }
    }

    private fun correctAnswer(question: QuestionEntity): String = when (question.correctIndex) {
        0 -> question.answerA
        1 -> question.answerB
        2 -> question.answerC
        3 -> question.answerD
        else -> ""
    }

    private suspend fun loadNext() {
        if (_reviewMode.value && singleReviewQuestion) {
            val id = reviewQuestionId
            val question = if (id == null) null else dao.questionById(id)
            if (question == null) {
                publishFinishSummary()
                _state.value = QuizUiState.Finished
            } else {
                questionNumber = 1
                showQuestion(question, questionNumber)
            }
            return
        }

        val question = repo.next(currentQuizType, currentCategory)
        if (question == null) {
            repo.resetSeen(currentQuizType, currentCategory)
            publishFinishSummary()
            saveHistoryIfEligible()
            _state.value = QuizUiState.Finished
        } else {
            questionNumber++
            showQuestion(question, questionNumber)
        }
    }

    private fun showQuestion(question: QuestionEntity, number: Int) {
        questionPresentedAtElapsed = SystemClock.elapsedRealtime()
        _state.value = QuizUiState.Ready(question, number)
    }

    private fun resetRunScore(config: QuizSessionConfig?) {
        runAnswered = 0
        runCorrect = 0
        runXpGained = 0
        runStartedAt = System.currentTimeMillis()
        runReplayConfig = config
        runHistorySaved = false
        runHistoryQuestions.clear()
        _finishSummary.value = null
    }

    private fun publishFinishSummary() {
        _finishSummary.value = QuizFinishSummary(
            answered = runAnswered,
            correct = runCorrect,
            xpGained = runXpGained
        )
    }

    private fun saveHistoryIfEligible() {
        if (runHistorySaved) return
        if (currentQuizType != CYBERSECURITY || singleReviewQuestion || runAnswered <= 0) return
        val originalConfig = configuredSessionConfig ?: runReplayConfig ?: return
        val replayConfig = if (!configuredSessionRuntime && originalConfig.questionCount == 0) {
            originalConfig.copy(questionCount = runAnswered)
        } else originalConfig
        val endedAt = System.currentTimeMillis()
        val entry = QuizHistoryEntry(
            id = "history_${endedAt}_${runAnswered}",
            config = replayConfig,
            startedAt = runStartedAt.takeIf { it > 0L } ?: endedAt,
            endedAt = endedAt,
            answered = runAnswered,
            correct = runCorrect,
            xpGained = runXpGained,
            questions = runHistoryQuestions.toList()
        )
        _quizHistory.value = historyStore.add(entry)
        runHistorySaved = true
    }

    private fun saveLastSessionConfig(config: QuizSessionConfig) {
        _lastSessionConfig.value = config
        sessionPrefs.edit()
            .putString(KEY_LAST_MODE, config.mode.name)
            .putString(KEY_LAST_CATEGORIES, encodeCategories(config.categories))
            .putInt(KEY_LAST_COUNT, config.questionCount)
            .putInt(KEY_LAST_TIME_LIMIT, config.timeLimitMinutes)
            .apply()
    }

    private fun loadLastSessionConfig(): QuizSessionConfig = QuizSessionConfig(
        mode = readMode(KEY_LAST_MODE, QuizSessionMode.RANDOM),
        categories = readCategories(KEY_LAST_CATEGORIES),
        questionCount = sessionPrefs.getInt(KEY_LAST_COUNT, 10),
        timeLimitMinutes = sessionPrefs.getInt(KEY_LAST_TIME_LIMIT, 0).coerceAtLeast(0)
    )

    private fun loadActiveSessionsWithLegacyMigration(): List<ActiveQuizSessionSummary> {
        migrateLegacySessionIfNeeded()
        return loadActiveSessions()
    }

    private fun loadActiveSessions(): List<ActiveQuizSessionSummary> = loadSessionIds()
        .mapNotNull { id -> loadSessionSummary(id) }
        .take(MAX_ACTIVE_SESSIONS)

    private fun loadSessionSummary(id: String): ActiveQuizSessionSummary? {
        if (!sessionPrefs.contains(sessionKey(id, FIELD_MODE))) return null
        return ActiveQuizSessionSummary(
            id = id,
            config = loadSessionConfig(id),
            answered = sessionPrefs.getInt(sessionKey(id, FIELD_ANSWERED), 0),
            pendingAnswer = sessionPrefs.getBoolean(sessionKey(id, FIELD_PENDING), false)
        )
    }

    private fun loadSessionConfig(id: String): QuizSessionConfig = QuizSessionConfig(
        mode = readMode(sessionKey(id, FIELD_MODE), QuizSessionMode.RANDOM),
        categories = readCategories(sessionKey(id, FIELD_CATEGORIES)),
        questionCount = sessionPrefs.getInt(sessionKey(id, FIELD_COUNT), 10),
        timeLimitMinutes = sessionPrefs.getInt(sessionKey(id, FIELD_TIME_LIMIT), 0).coerceAtLeast(0)
    )

    private fun loadSessionQueue(id: String): List<Long> = sessionPrefs
        .getString(sessionKey(id, FIELD_QUEUE), "")
        .orEmpty()
        .split(',')
        .mapNotNull { it.toLongOrNull() }

    private fun refreshActiveSessions() {
        _activeSessions.value = loadActiveSessions()
    }

    private fun persistActiveSession() {
        val id = configuredSessionId ?: return
        val config = configuredSessionConfig ?: return
        val ids = loadSessionIds().toMutableList()
        ids.remove(id)
        ids.add(0, id)
        while (ids.size > MAX_ACTIVE_SESSIONS) ids.removeAt(ids.lastIndex)

        sessionPrefs.edit()
            .putString(KEY_ACTIVE_SESSION_IDS, ids.joinToString(","))
            .putString(sessionKey(id, FIELD_MODE), config.mode.name)
            .putString(sessionKey(id, FIELD_CATEGORIES), encodeCategories(config.categories))
            .putInt(sessionKey(id, FIELD_COUNT), config.questionCount)
            .putInt(sessionKey(id, FIELD_TIME_LIMIT), config.timeLimitMinutes)
            .putString(sessionKey(id, FIELD_QUEUE), sessionQueue.joinToString(","))
            .putInt(sessionKey(id, FIELD_INDEX), sessionIndex)
            .putInt(sessionKey(id, FIELD_ANSWERED), sessionAnswered)
            .putInt(sessionKey(id, FIELD_CORRECT_COUNT), runCorrect)
            .putInt(sessionKey(id, FIELD_XP_GAINED), runXpGained)
            .putLong(sessionKey(id, FIELD_STARTED_AT), runStartedAt)
            .putString(sessionKey(id, FIELD_ANSWER_HISTORY), historyStore.encodeQuestions(runHistoryQuestions))
            .putLong(sessionKey(id, FIELD_CURRENT_ID), sessionCurrentQuestionId ?: -1L)
            .putBoolean(sessionKey(id, FIELD_PENDING), sessionPendingAnswer)
            .putBoolean(sessionKey(id, FIELD_CORRECT), sessionPendingCorrect)
            .putInt(sessionKey(id, FIELD_XP), sessionPendingXp)
            .putInt(sessionKey(id, FIELD_SELECTED), sessionPendingSelected ?: -1)
            .apply {
                if (dailyChallengeDay == null) remove(sessionKey(id, FIELD_DAILY_DAY))
                else putString(sessionKey(id, FIELD_DAILY_DAY), dailyChallengeDay)
            }
            .apply()
        refreshActiveSessions()
    }

    private fun finishConfiguredSession() {
        dailyChallengeDay?.let { day ->
            val completion = DailyChallengeStore.complete(
                getApplication(),
                day = day,
                score = runCorrect,
                total = sessionAnswered.coerceAtLeast(1)
            )
            if (completion.newlyCompleted && completion.bonusXp > 0) {
                runXpGained += completion.bonusXp
                viewModelScope.launch { addBonusXp(completion.bonusXp) }
            }
        }
        publishFinishSummary()
        saveHistoryIfEligible()
        val id = configuredSessionId
        if (id != null) removeSessionStorage(id)
        leaveConfiguredRuntime()
        _reviewMode.value = false
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Finished
    }

    private suspend fun addBonusXp(amount: Int) {
        if (amount <= 0) return
        val p = repo.progressSnapshot(CYBERSECURITY) ?: emptyProgress(CYBERSECURITY)
        val newXp = p.xp + amount
        dao.updateProgress(
            quizType = CYBERSECURITY,
            xp = newXp,
            level = (newXp / 100) + 1,
            answered = p.answered,
            correct = p.correct,
            streak = p.streak,
            bestStreak = p.bestStreak,
            totalResponseMs = p.totalResponseMs
        )
    }

    private fun leaveConfiguredRuntime() {
        configuredSessionRuntime = false
        configuredSessionId = null
        configuredSessionConfig = null
        _currentSessionConfig.value = null
        _examDeadline.value = null
        dailyChallengeDay = null
        sessionQueue.clear()
        sessionIndex = 0
        sessionAnswered = 0
        sessionCurrentQuestionId = null
        sessionPendingAnswer = false
        sessionPendingCorrect = false
        sessionPendingXp = 0
        sessionPendingSelected = null
    }

    private fun removeSessionStorage(id: String) {
        val ids = loadSessionIds().filterNot { it == id }
        sessionPrefs.edit()
            .putString(KEY_ACTIVE_SESSION_IDS, ids.joinToString(","))
            .remove(sessionKey(id, FIELD_MODE))
            .remove(sessionKey(id, FIELD_CATEGORIES))
            .remove(sessionKey(id, FIELD_COUNT))
            .remove(sessionKey(id, FIELD_TIME_LIMIT))
            .remove(sessionKey(id, FIELD_QUEUE))
            .remove(sessionKey(id, FIELD_INDEX))
            .remove(sessionKey(id, FIELD_ANSWERED))
            .remove(sessionKey(id, FIELD_CORRECT_COUNT))
            .remove(sessionKey(id, FIELD_XP_GAINED))
            .remove(sessionKey(id, FIELD_STARTED_AT))
            .remove(sessionKey(id, FIELD_ANSWER_HISTORY))
            .remove(sessionKey(id, FIELD_CURRENT_ID))
            .remove(sessionKey(id, FIELD_PENDING))
            .remove(sessionKey(id, FIELD_CORRECT))
            .remove(sessionKey(id, FIELD_XP))
            .remove(sessionKey(id, FIELD_SELECTED))
            .remove(sessionKey(id, FIELD_DAILY_DAY))
            .apply()
        refreshActiveSessions()
    }

    private fun loadSessionIds(): List<String> = sessionPrefs
        .getString(KEY_ACTIVE_SESSION_IDS, "")
        .orEmpty()
        .split(',')
        .filter { it.isNotBlank() }
        .distinct()
        .take(MAX_ACTIVE_SESSIONS)

    private fun nextSessionId(): String {
        val existing = loadSessionIds().toSet()
        var candidate = System.currentTimeMillis().toString()
        var suffix = 1
        while (candidate in existing) {
            candidate = "${System.currentTimeMillis()}_$suffix"
            suffix++
        }
        return candidate
    }

    private fun sessionKey(id: String, field: String): String = "session_${id}_$field"

    private fun migrateLegacySessionIfNeeded() {
        if (loadSessionIds().isNotEmpty()) return
        if (!sessionPrefs.getBoolean(KEY_LEGACY_ACTIVE, false)) return

        val id = "legacy_${System.currentTimeMillis()}"
        sessionPrefs.edit()
            .putString(KEY_ACTIVE_SESSION_IDS, id)
            .putString(
                sessionKey(id, FIELD_MODE),
                sessionPrefs.getString(KEY_LEGACY_MODE, QuizSessionMode.RANDOM.name)
                    ?: QuizSessionMode.RANDOM.name
            )
            .putString(
                sessionKey(id, FIELD_CATEGORIES),
                sessionPrefs.getString(KEY_LEGACY_CATEGORIES, encodeCategories(Category.entries.map { it.label }.toSet()))
                    ?: encodeCategories(Category.entries.map { it.label }.toSet())
            )
            .putInt(sessionKey(id, FIELD_COUNT), sessionPrefs.getInt(KEY_LEGACY_COUNT, 10))
            .putInt(sessionKey(id, FIELD_TIME_LIMIT), 0)
            .putString(sessionKey(id, FIELD_QUEUE), sessionPrefs.getString(KEY_LEGACY_QUEUE, "") ?: "")
            .putInt(sessionKey(id, FIELD_INDEX), sessionPrefs.getInt(KEY_LEGACY_INDEX, 0))
            .putInt(sessionKey(id, FIELD_ANSWERED), sessionPrefs.getInt(KEY_LEGACY_ANSWERED, 0))
            .putInt(sessionKey(id, FIELD_CORRECT_COUNT), 0)
            .putInt(sessionKey(id, FIELD_XP_GAINED), 0)
            .putLong(sessionKey(id, FIELD_STARTED_AT), System.currentTimeMillis())
            .putString(sessionKey(id, FIELD_ANSWER_HISTORY), "[]")
            .putLong(sessionKey(id, FIELD_CURRENT_ID), sessionPrefs.getLong(KEY_LEGACY_CURRENT_ID, -1L))
            .putBoolean(sessionKey(id, FIELD_PENDING), sessionPrefs.getBoolean(KEY_LEGACY_PENDING, false))
            .putBoolean(sessionKey(id, FIELD_CORRECT), sessionPrefs.getBoolean(KEY_LEGACY_CORRECT, false))
            .putInt(sessionKey(id, FIELD_XP), sessionPrefs.getInt(KEY_LEGACY_XP, 0))
            .putInt(sessionKey(id, FIELD_SELECTED), sessionPrefs.getInt(KEY_LEGACY_SELECTED, -1))
            .remove(KEY_LEGACY_ACTIVE)
            .remove(KEY_LEGACY_MODE)
            .remove(KEY_LEGACY_CATEGORIES)
            .remove(KEY_LEGACY_COUNT)
            .remove(KEY_LEGACY_QUEUE)
            .remove(KEY_LEGACY_INDEX)
            .remove(KEY_LEGACY_ANSWERED)
            .remove(KEY_LEGACY_CURRENT_ID)
            .remove(KEY_LEGACY_PENDING)
            .remove(KEY_LEGACY_CORRECT)
            .remove(KEY_LEGACY_XP)
            .remove(KEY_LEGACY_SELECTED)
            .apply()
    }

    private fun readMode(key: String, fallback: QuizSessionMode): QuizSessionMode {
        val raw = sessionPrefs.getString(key, null) ?: return fallback
        return runCatching { QuizSessionMode.valueOf(raw) }.getOrDefault(fallback)
    }

    private fun readCategories(key: String): Set<String> {
        val all = Category.entries.map { it.label }.toSet()
        val raw = sessionPrefs.getString(key, null) ?: return all
        val decoded = raw.split(CATEGORY_SEPARATOR).filter { it.isNotBlank() }.toSet()
        return decoded.ifEmpty { all }
    }

    private fun encodeCategories(categories: Set<String>): String =
        categories.sorted().joinToString(CATEGORY_SEPARATOR)

    private fun emptyProgress(quizType: String): ProgressEntity = ProgressEntity(
        id = when (quizType) {
            NUTRITION -> 2
            else -> 1
        },
        quizType = quizType
    )

    companion object {
        const val MAX_ACTIVE_SESSIONS = 6

        private const val CYBERSECURITY = "CYBERSECURITY"
        private const val NUTRITION = "NUTRITION"

        private const val SESSION_PREFS = "cyberquiz_quiz_session"
        private const val CATEGORY_SEPARATOR = "||"

        private const val KEY_LAST_MODE = "last_mode"
        private const val KEY_LAST_CATEGORIES = "last_categories"
        private const val KEY_LAST_COUNT = "last_count"
        private const val KEY_LAST_TIME_LIMIT = "last_time_limit"
        private const val KEY_ACTIVE_SESSION_IDS = "active_session_ids"

        private const val FIELD_MODE = "mode"
        private const val FIELD_CATEGORIES = "categories"
        private const val FIELD_COUNT = "count"
        private const val FIELD_TIME_LIMIT = "time_limit"
        private const val FIELD_QUEUE = "queue"
        private const val FIELD_INDEX = "index"
        private const val FIELD_ANSWERED = "answered"
        private const val FIELD_CORRECT_COUNT = "correct_count"
        private const val FIELD_XP_GAINED = "xp_gained"
        private const val FIELD_STARTED_AT = "started_at"
        private const val FIELD_ANSWER_HISTORY = "answer_history"
        private const val FIELD_CURRENT_ID = "current_id"
        private const val FIELD_PENDING = "pending"
        private const val FIELD_CORRECT = "correct"
        private const val FIELD_XP = "xp"
        private const val FIELD_SELECTED = "selected"
        private const val FIELD_DAILY_DAY = "daily_day"

        private const val KEY_LEGACY_ACTIVE = "active"
        private const val KEY_LEGACY_MODE = "active_mode"
        private const val KEY_LEGACY_CATEGORIES = "active_categories"
        private const val KEY_LEGACY_COUNT = "active_count"
        private const val KEY_LEGACY_QUEUE = "active_queue"
        private const val KEY_LEGACY_INDEX = "active_index"
        private const val KEY_LEGACY_ANSWERED = "active_answered"
        private const val KEY_LEGACY_CURRENT_ID = "active_current_id"
        private const val KEY_LEGACY_PENDING = "active_pending"
        private const val KEY_LEGACY_CORRECT = "active_correct"
        private const val KEY_LEGACY_XP = "active_xp"
        private const val KEY_LEGACY_SELECTED = "active_selected"
    }
}
