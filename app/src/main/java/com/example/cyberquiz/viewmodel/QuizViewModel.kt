package com.example.cyberquiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.database.ProgressEntity
import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.data.repository.QuizRepository
import com.example.cyberquiz.model.Category
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

    private val _result = MutableStateFlow<AnswerResult?>(null)
    val result: StateFlow<AnswerResult?> = _result.asStateFlow()

    private var currentQuizType = CYBERSECURITY
    private var currentCategory: String? = null
    private var questionNumber = 0

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
        _result.value = null
        _state.value = QuizUiState.Loading
        viewModelScope.launch { loadNext() }
    }

    fun startCategory(quizType: String, category: String) {
        selectQuizType(quizType)
        currentCategory = category
        questionNumber = 0
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
            val newStreak = if (ok) p.streak + 1 else 0
            val best = maxOf(p.bestStreak, newStreak)
            val gainedXp = if (ok) {
                10 + when (current.difficulty) {
                    "HARD" -> 10
                    "MEDIUM" -> 5
                    else -> 0
                }
            } else {
                0
            }
            val newXp = p.xp + gainedXp
            val newLevel = (newXp / 100) + 1
            val newAnswered = p.answered + 1
            val newCorrect = p.correct + if (ok) 1 else 0

            repo.markSeen(current.id)
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
        viewModelScope.launch { loadNext() }
    }

    private suspend fun loadNext() {
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
