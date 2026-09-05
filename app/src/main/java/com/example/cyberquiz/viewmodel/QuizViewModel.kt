package com.example.cyberquiz.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.database.ProgressEntity
import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.data.database.ReviewItemEntity
import com.example.cyberquiz.data.database.ReviewItemWithQuestion
import com.example.cyberquiz.data.repository.QuizRepository
import com.example.cyberquiz.model.ActiveQuizSessionSummary
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
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

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = CyberQuizDatabase.get(app).quizDao()
    private val repo = QuizRepository(dao)
    private val initialized = CompletableDeferred<Unit>()
    private val sessionPrefs = app.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private val _activeQuizType = MutableStateFlow(CYBERSECURITY)
    val progress: StateFlow<ProgressEntity> = _activeQuizType
        .flatMapLatest { quizType ->
            repo.progress(quizType).map { stored ->
                stored ?: emptyProgress(quizType)
            }
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

    private val _reviewMode = MutableStateFlow(false)
    val reviewMode: StateFlow<Boolean> = _reviewMode.asStateFlow()

    private val _lastSessionConfig = MutableStateFlow(loadLastSessionConfig())
    val lastSessionConfig: StateFlow<QuizSessionConfig> = _lastSessionConfig.asStateFlow()

    private val _activeSession = MutableStateFlow<ActiveQuizSessionSummary?>(loadActiveSessionSummary())
    val activeSession: StateFlow<ActiveQuizSessionSummary?> = _activeSession.asStateFlow()

    private val _restoredSelection = MutableStateFlow<Int?>(null)
    val restoredSelection: StateFlow<Int?> = _restoredSelection.asStateFlow()

    private var currentQuizType = CYBERSECURITY
    private var currentCategory: String? = null
    private var questionNumber = 0
    private var reviewQuestionId: Long? = null
    private var singleReviewQuestion = false

    private var configuredSessionRuntime = false
    private var configuredSessionConfig: QuizSessionConfig? = null
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
        configuredSessionRuntime = false
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
        configuredSessionRuntime = false
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
        configuredSessionRuntime = false
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
        saveLastSessionConfig(config)
        configuredSessionRuntime = true
        configuredSessionConfig = config
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

        viewModelScope.launch {
            initialized.await()
            if (config.mode == QuizSessionMode.DIFFICULTIES) {
                val question = chooseDifficultyQuestion(config, null)
                if (question == null) {
                    finishConfiguredSession()
                } else {
                    sessionCurrentQuestionId = question.id
                    persistActiveSession()
                    _state.value = QuizUiState.Ready(question, 1)
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
                    _state.value = QuizUiState.Ready(question, 1)
                }
            }
        }
    }

    fun resumeConfiguredQuiz() {
        if (!sessionPrefs.getBoolean(KEY_ACTIVE, false)) return

        configuredSessionRuntime = true
        configuredSessionConfig = loadActiveConfig()
        singleReviewQuestion = false
        selectQuizType(CYBERSECURITY)
        currentCategory = null
        reviewQuestionId = null
        sessionQueue = loadActiveQueue().toMutableList()
        sessionIndex = sessionPrefs.getInt(KEY_ACTIVE_INDEX, 0)
        sessionAnswered = sessionPrefs.getInt(KEY_ACTIVE_ANSWERED, 0)
        sessionCurrentQuestionId = sessionPrefs.getLong(KEY_ACTIVE_CURRENT_ID, -1L).takeIf { it > 0L }
        sessionPendingAnswer = sessionPrefs.getBoolean(KEY_ACTIVE_PENDING, false)
        sessionPendingCorrect = sessionPrefs.getBoolean(KEY_ACTIVE_CORRECT, false)
        sessionPendingXp = sessionPrefs.getInt(KEY_ACTIVE_XP, 0)
        sessionPendingSelected = sessionPrefs.getInt(KEY_ACTIVE_SELECTED, -1).takeIf { it >= 0 }
        _reviewMode.value = configuredSessionConfig?.mode == QuizSessionMode.DIFFICULTIES
        _restoredSelection.value = sessionPendingSelected
        _result.value = null
        _state.value = QuizUiState.Loading
        updateActiveSessionSummary()

        viewModelScope.launch {
            initialized.await()
            val question = sessionCurrentQuestionId?.let { dao.questionById(it) }
            if (question == null) {
                advanceConfiguredSession()
                return@launch
            }
            val number = if (sessionPendingAnswer) {
                sessionAnswered.coerceAtLeast(1)
            } else {
                sessionAnswered + 1
            }
            _state.value = QuizUiState.Ready(question, number)
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

    fun abandonConfiguredQuiz() {
        clearActiveSessionStorage()
        configuredSessionRuntime = false
        configuredSessionConfig = null
        sessionQueue.clear()
        sessionIndex = 0
        sessionAnswered = 0
        sessionCurrentQuestionId = null
        sessionPendingAnswer = false
        sessionPendingCorrect = false
        sessionPendingXp = 0
        sessionPendingSelected = null
        _activeSession.value = null
        _reviewMode.value = false
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Finished
    }

    fun restartLastConfiguredQuiz() {
        startConfiguredQuiz(_lastSessionConfig.value)
    }

    fun answer(index: Int) {
        val current = (_state.value as? QuizUiState.Ready)?.question ?: return
        if (_result.value != null) return

        viewModelScope.launch {
            val p = repo.progressSnapshot(currentQuizType) ?: emptyProgress(currentQuizType)
            val ok = index == current.correctIndex
            val isReview = _reviewMode.value
            val concept = correctAnswer(current).trim()
            val reviewItemBefore = if (
                currentQuizType == CYBERSECURITY && concept.isNotBlank()
            ) {
                dao.reviewItemSnapshot(CYBERSECURITY, concept)
            } else {
                null
            }

            val firstReviewSuccess = isReview &&
                ok &&
                reviewItemBefore != null &&
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

            val newStreak = if (isReview) {
                p.streak
            } else if (ok) {
                p.streak + 1
            } else {
                0
            }
            val best = if (isReview) p.bestStreak else maxOf(p.bestStreak, newStreak)
            val newXp = p.xp + gainedXp
            val newLevel = (newXp / 100) + 1
            val newAnswered = if (isReview) p.answered else p.answered + 1
            val newCorrect = if (isReview) p.correct else p.correct + if (ok) 1 else 0

            if (!isReview) {
                repo.markSeen(current.id)
            }

            if (currentQuizType == CYBERSECURITY) {
                if (isReview) {
                    recordConceptReviewResult(current, ok)
                } else {
                    recordLearningProgress(current, ok)
                }
                recordReviewResult(current, ok)

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
                totalResponseMs = p.totalResponseMs
            )

            _restoredSelection.value = index
            _result.value = AnswerResult(ok, gainedXp, current.explanation, current)

            if (configuredSessionRuntime) {
                sessionAnswered += 1
                sessionCurrentQuestionId = current.id
                sessionPendingAnswer = true
                sessionPendingCorrect = ok
                sessionPendingXp = gainedXp
                sessionPendingSelected = index
                persistActiveSession()
            }
        }
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
            val question = chooseDifficultyQuestion(config, sessionCurrentQuestionId)
            if (question == null) {
                finishConfiguredSession()
                return
            }
            sessionCurrentQuestionId = question.id
            persistActiveSession()
            _state.value = QuizUiState.Ready(question, sessionAnswered + 1)
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
        _state.value = QuizUiState.Ready(question, sessionAnswered + 1)
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
            }
            categoryMatches && difficultyMatches
        }
    }

    private suspend fun chooseDifficultyQuestion(
        config: QuizSessionConfig,
        previousQuestionId: Long?
    ): QuestionEntity? {
        val active = dao.activeReviewQuestionsSnapshot(CYBERSECURITY)
            .filter { it.category in config.categories }
        if (active.isEmpty()) return null
        val alternatives = active.filterNot { it.id == previousQuestionId }
        return (if (alternatives.isNotEmpty()) alternatives else active).random()
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

        dao.insertCategoryProgress(
            CategoryProgressEntity(
                quizType = CYBERSECURITY,
                category = question.category
            )
        )
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

        if (correct) {
            dao.markConceptMasteredByReview(CYBERSECURITY, concept, now)
        } else {
            dao.markConceptNeedsReview(CYBERSECURITY, concept, now)
        }
    }

    private suspend fun recordReviewResult(question: QuestionEntity, correct: Boolean) {
        val concept = correctAnswer(question).trim()
        if (concept.isBlank()) return

        if (correct) {
            dao.recordReviewCorrect(CYBERSECURITY, concept)
            return
        }

        val existing = dao.reviewItemSnapshot(CYBERSECURITY, concept)
        val now = System.currentTimeMillis()
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
                    lastWrongAt = now
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
                _state.value = QuizUiState.Finished
            } else {
                questionNumber = 1
                _state.value = QuizUiState.Ready(question, questionNumber)
            }
            return
        }

        val question = repo.next(currentQuizType, currentCategory)
        if (question == null) {
            repo.resetSeen(currentQuizType, currentCategory)
            _state.value = QuizUiState.Finished
        } else {
            questionNumber++
            _state.value = QuizUiState.Ready(question, questionNumber)
        }
    }

    private fun saveLastSessionConfig(config: QuizSessionConfig) {
        _lastSessionConfig.value = config
        sessionPrefs.edit()
            .putString(KEY_LAST_MODE, config.mode.name)
            .putString(KEY_LAST_CATEGORIES, encodeCategories(config.categories))
            .putInt(KEY_LAST_COUNT, config.questionCount)
            .apply()
    }

    private fun loadLastSessionConfig(): QuizSessionConfig = QuizSessionConfig(
        mode = readMode(KEY_LAST_MODE, QuizSessionMode.RANDOM),
        categories = readCategories(KEY_LAST_CATEGORIES),
        questionCount = sessionPrefs.getInt(KEY_LAST_COUNT, 10)
    )

    private fun loadActiveConfig(): QuizSessionConfig = QuizSessionConfig(
        mode = readMode(KEY_ACTIVE_MODE, QuizSessionMode.RANDOM),
        categories = readCategories(KEY_ACTIVE_CATEGORIES),
        questionCount = sessionPrefs.getInt(KEY_ACTIVE_COUNT, 10)
    )

    private fun loadActiveSessionSummary(): ActiveQuizSessionSummary? {
        if (!sessionPrefs.getBoolean(KEY_ACTIVE, false)) return null
        return ActiveQuizSessionSummary(
            config = loadActiveConfig(),
            answered = sessionPrefs.getInt(KEY_ACTIVE_ANSWERED, 0),
            pendingAnswer = sessionPrefs.getBoolean(KEY_ACTIVE_PENDING, false)
        )
    }

    private fun updateActiveSessionSummary() {
        val config = configuredSessionConfig ?: return
        _activeSession.value = ActiveQuizSessionSummary(
            config = config,
            answered = sessionAnswered,
            pendingAnswer = sessionPendingAnswer
        )
    }

    private fun persistActiveSession() {
        val config = configuredSessionConfig ?: return
        sessionPrefs.edit()
            .putBoolean(KEY_ACTIVE, true)
            .putString(KEY_ACTIVE_MODE, config.mode.name)
            .putString(KEY_ACTIVE_CATEGORIES, encodeCategories(config.categories))
            .putInt(KEY_ACTIVE_COUNT, config.questionCount)
            .putString(KEY_ACTIVE_QUEUE, sessionQueue.joinToString(","))
            .putInt(KEY_ACTIVE_INDEX, sessionIndex)
            .putInt(KEY_ACTIVE_ANSWERED, sessionAnswered)
            .putLong(KEY_ACTIVE_CURRENT_ID, sessionCurrentQuestionId ?: -1L)
            .putBoolean(KEY_ACTIVE_PENDING, sessionPendingAnswer)
            .putBoolean(KEY_ACTIVE_CORRECT, sessionPendingCorrect)
            .putInt(KEY_ACTIVE_XP, sessionPendingXp)
            .putInt(KEY_ACTIVE_SELECTED, sessionPendingSelected ?: -1)
            .apply()
        updateActiveSessionSummary()
    }

    private fun loadActiveQueue(): List<Long> = sessionPrefs
        .getString(KEY_ACTIVE_QUEUE, "")
        .orEmpty()
        .split(',')
        .mapNotNull { it.toLongOrNull() }

    private fun finishConfiguredSession() {
        clearActiveSessionStorage()
        configuredSessionRuntime = false
        sessionPendingAnswer = false
        sessionPendingSelected = null
        sessionCurrentQuestionId = null
        _activeSession.value = null
        _reviewMode.value = false
        _restoredSelection.value = null
        _result.value = null
        _state.value = QuizUiState.Finished
    }

    private fun clearActiveSessionStorage() {
        sessionPrefs.edit()
            .remove(KEY_ACTIVE)
            .remove(KEY_ACTIVE_MODE)
            .remove(KEY_ACTIVE_CATEGORIES)
            .remove(KEY_ACTIVE_COUNT)
            .remove(KEY_ACTIVE_QUEUE)
            .remove(KEY_ACTIVE_INDEX)
            .remove(KEY_ACTIVE_ANSWERED)
            .remove(KEY_ACTIVE_CURRENT_ID)
            .remove(KEY_ACTIVE_PENDING)
            .remove(KEY_ACTIVE_CORRECT)
            .remove(KEY_ACTIVE_XP)
            .remove(KEY_ACTIVE_SELECTED)
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
        private const val CYBERSECURITY = "CYBERSECURITY"
        private const val NUTRITION = "NUTRITION"

        private const val SESSION_PREFS = "cyberquiz_quiz_session"
        private const val CATEGORY_SEPARATOR = "||"

        private const val KEY_LAST_MODE = "last_mode"
        private const val KEY_LAST_CATEGORIES = "last_categories"
        private const val KEY_LAST_COUNT = "last_count"

        private const val KEY_ACTIVE = "active"
        private const val KEY_ACTIVE_MODE = "active_mode"
        private const val KEY_ACTIVE_CATEGORIES = "active_categories"
        private const val KEY_ACTIVE_COUNT = "active_count"
        private const val KEY_ACTIVE_QUEUE = "active_queue"
        private const val KEY_ACTIVE_INDEX = "active_index"
        private const val KEY_ACTIVE_ANSWERED = "active_answered"
        private const val KEY_ACTIVE_CURRENT_ID = "active_current_id"
        private const val KEY_ACTIVE_PENDING = "active_pending"
        private const val KEY_ACTIVE_CORRECT = "active_correct"
        private const val KEY_ACTIVE_XP = "active_xp"
        private const val KEY_ACTIVE_SELECTED = "active_selected"
    }
}
