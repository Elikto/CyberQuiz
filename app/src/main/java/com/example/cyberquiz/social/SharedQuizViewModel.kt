package com.example.cyberquiz.social

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.database.QuestionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SharedQuizState {
    data object Idle : SharedQuizState
    data object Loading : SharedQuizState
    data class Playing(
        val question: QuestionEntity,
        val index: Int,
        val total: Int,
        val selectedIndex: Int? = null,
        val correct: Boolean? = null,
        val score: Int = 0,
        val room: SocialQuizRoom
    ) : SharedQuizState
    data class Finished(
        val score: Int,
        val total: Int,
        val room: SocialQuizRoom?
    ) : SharedQuizState
    data class Error(val message: String) : SharedQuizState
}

class SharedQuizViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = CyberQuizDatabase.get(app).quizDao()
    private val _state = MutableStateFlow<SharedQuizState>(SharedQuizState.Idle)
    val state: StateFlow<SharedQuizState> = _state.asStateFlow()

    private var questions: List<QuestionEntity> = emptyList()
    private var token: String? = null
    private var roomId: String? = null
    private var currentRoom: SocialQuizRoom? = null
    private var pollJob: Job? = null

    suspend fun prepareQuestionIds(count: Int = 10): List<Long> {
        val all = dao.questionsSnapshot("CYBERSECURITY")
        if (all.isEmpty()) return emptyList()
        return all.shuffled().take(count.coerceAtLeast(1)).map { it.id }
    }

    fun begin(room: SocialQuizRoom, sessionToken: String) {
        token = sessionToken
        roomId = room.id
        currentRoom = room
        pollJob?.cancel()
        _state.value = SharedQuizState.Loading
        viewModelScope.launch {
            val loaded = room.questionIds.mapNotNull { dao.questionById(it) }
            if (loaded.size != room.questionIds.size || loaded.isEmpty()) {
                _state.value = SharedQuizState.Error(
                    "Ce quiz n'est pas disponible dans cette version de CyberQuiz. Mets l'application à jour."
                )
                return@launch
            }
            questions = loaded
            _state.value = SharedQuizState.Playing(
                question = loaded.first(),
                index = 0,
                total = loaded.size,
                score = 0,
                room = room
            )
            startRoomPolling()
        }
    }

    fun answer(index: Int) {
        val playing = _state.value as? SharedQuizState.Playing ?: return
        if (playing.selectedIndex != null || index !in 0..3) return
        val isCorrect = index == playing.question.correctIndex
        val newScore = playing.score + if (isCorrect) 1 else 0
        _state.value = playing.copy(
            selectedIndex = index,
            correct = isCorrect,
            score = newScore
        )
        postProgress(
            answered = playing.index + 1,
            correct = newScore,
            finished = false
        )
    }

    fun next() {
        val playing = _state.value as? SharedQuizState.Playing ?: return
        if (playing.selectedIndex == null) return
        val nextIndex = playing.index + 1
        if (nextIndex >= questions.size) {
            pollJob?.cancel()
            _state.value = SharedQuizState.Finished(
                score = playing.score,
                total = questions.size,
                room = playing.room
            )
            postProgress(
                answered = questions.size,
                correct = playing.score,
                finished = true
            )
            return
        }
        _state.value = SharedQuizState.Playing(
            question = questions[nextIndex],
            index = nextIndex,
            total = questions.size,
            score = playing.score,
            room = playing.room
        )
    }

    fun leaveRemoteRoom(onComplete: () -> Unit) {
        val activeToken = token
        val activeRoom = roomId
        pollJob?.cancel()
        viewModelScope.launch {
            if (!activeToken.isNullOrBlank() && !activeRoom.isNullOrBlank()) {
                runCatching { SocialLifecycleApiClient.leaveRoom(activeToken, activeRoom) }
            }
            reset()
            onComplete()
        }
    }

    fun reset() {
        pollJob?.cancel()
        questions = emptyList()
        token = null
        roomId = null
        currentRoom = null
        _state.value = SharedQuizState.Idle
    }

    private fun postProgress(answered: Int, correct: Int, finished: Boolean) {
        val activeToken = token ?: return
        val activeRoom = roomId ?: return
        viewModelScope.launch {
            runCatching {
                SocialApiClient.updateRoomProgress(
                    token = activeToken,
                    roomId = activeRoom,
                    answered = answered,
                    correct = correct,
                    finished = finished
                )
            }.onSuccess { room ->
                currentRoom = room
                updateRoomInState(room)
            }
        }
    }

    private fun startRoomPolling() {
        val activeToken = token ?: return
        val activeRoom = roomId ?: return
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(2_000)
                runCatching { SocialApiClient.room(activeToken, activeRoom) }
                    .onSuccess { room ->
                        currentRoom = room
                        updateRoomInState(room)
                    }
            }
        }
    }

    private fun updateRoomInState(room: SocialQuizRoom) {
        when (val current = _state.value) {
            is SharedQuizState.Playing -> _state.value = current.copy(room = room)
            is SharedQuizState.Finished -> _state.value = current.copy(room = room)
            else -> Unit
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
