package com.example.cyberquiz.viewmodel

import android.app.Application
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
import com.example.cyberquiz.model.Category
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

    private var currentQuizType = CYBERSECURITY
    private var currentCategory: String? = null
    private var questionNumber = 0
    private var reviewQuestionId: Long? = null

    init {
        viewModelScope.launch {
            repo.init()
            loadNext()
        }
    }

    fun selectQuizType(quizType: String) {
        currentQuizType = quizType
        currentCategory = null
        _activeQuizType.value = quizType
    }

    fun start(category: Category? = null, quizType: String = CYBERSECURITY) {
        selectQuizType(quizType)
        currentCategory = category?.label
        questionNumber = 0
        reviewQuestionId = null
        _reviewMode.value = false
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch { loadNext() }
    }

    fun startCategory(quizType: String, category: String) {
        selectQuizType(quizType)
        currentCategory = category
        questionNumber = 0
        reviewQuestionId = null
        _reviewMode.value = false
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch { loadNext() }
    }

    fun startReviewQuestion(quizType: String, questionId: Long) {
        selectQuizType(quizType)
        currentCategory = null
        questionNumber = 0
        reviewQuestionId = questionId
        _reviewMode.value = true
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch { loadNext() }
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
                isReview && currentQuizType == CYBERSECURITY && concept.isNotBlank()
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
            _result.value = AnswerResult(ok, gainedXp, current.explanation, current)
        }
    }

    fun nextQuestion() {
        _result.value = null
        if (_reviewMode.value) {
            reviewQuestionId = null
            _state.value = QuizUiState.Finished
            return
        }
        viewModelScope.launch { loadNext() }
    }

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
        if (_reviewMode.value) {
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
    }
}
