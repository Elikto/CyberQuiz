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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Ready(val question: QuestionEntity, val number: Int) : QuizUiState
    data object Finished : QuizUiState
}

data class AnswerResult(val correct: Boolean, val xp: Int, val explanation: String, val question: QuestionEntity)

class QuizViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = QuizRepository(CyberQuizDatabase.get(app).quizDao())
    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val state: StateFlow<QuizUiState> = _state.asStateFlow()
    private val _progress = MutableStateFlow(ProgressEntity())
    val progress: StateFlow<ProgressEntity> = _progress.asStateFlow()
    private val _result = MutableStateFlow<AnswerResult?>(null)
    val result: StateFlow<AnswerResult?> = _result.asStateFlow()
    private var currentCategory: Category? = null
    private var questionNumber = 0

    init {
        viewModelScope.launch {
            repo.init()
            launch { repo.progress().collect { if (it != null) _progress.value = it } }
            loadNext()
        }
    }

    fun start(category: Category? = null) {
        currentCategory = category
        questionNumber = 0
        _result.value = null
        viewModelScope.launch { loadNext() }
    }

    fun answer(index: Int) {
        val current = (_state.value as? QuizUiState.Ready)?.question ?: return
        if (_result.value != null) return
        viewModelScope.launch {
            val p = _progress.value
            val ok = index == current.correctIndex
            val newStreak = if (ok) p.streak + 1 else 0
            val best = maxOf(p.bestStreak, newStreak)
            val xp = if (ok) 10 + if (current.difficulty == "HARD") 10 else if (current.difficulty == "MEDIUM") 5 else 0 else 2
            val newXp = p.xp + if (ok) xp else 0
            val newLevel = (newXp / 100) + 1
            val newAnswered = p.answered + 1
            val newCorrect = p.correct + if (ok) 1 else 0
            repo.markSeen(current.id)
            CyberQuizDatabase.get(getApplication()).quizDao().updateProgress(newXp,newLevel,newAnswered,newCorrect,newStreak,best,p.totalResponseMs)
            _result.value = AnswerResult(ok, if (ok) xp else 0, current.explanation, current)
        }
    }

    fun nextQuestion() {
        _result.value = null
        viewModelScope.launch { loadNext() }
    }

    private suspend fun loadNext() {
        val q = repo.next(currentCategory)
        if (q == null) { repo.resetSeen(); _state.value = QuizUiState.Finished }
        else { questionNumber++; _state.value = QuizUiState.Ready(q, questionNumber) }
    }
}
