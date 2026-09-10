package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.repository.QuestionLibraryStore
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

private val ExitDialogBackground = Color(0xFF0B1429)
private val ExitDialogText = Color(0xFFF5F7FF)
private val ExitDialogMuted = Color(0xFFA7B4D4)
private val ExitDialogCyan = Color(0xFF19F2E5)
private val ExitDialogPurple = Color(0xFFD652FF)
private val ExitDialogRed = Color(0xFFFF657F)

@Composable
fun ResumableQuizScreen(
    vm: QuizViewModel,
    configuredSession: Boolean,
    questionTotal: Int?,
    onBack: () -> Unit,
    onOtherQuiz: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val library = remember(context) { QuestionLibraryStore(context) }
    val currentState by vm.state.collectAsState()
    val currentQuestion = (currentState as? QuizUiState.Ready)?.question
    var favorite by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(currentQuestion?.id) {
        favorite = currentQuestion?.let { library.isFavorite(it.id) } ?: false
    }

    fun requestExit() {
        if (currentState is QuizUiState.Finished) {
            onBack()
        } else {
            showExitConfirmation = true
        }
    }

    BackHandler {
        requestExit()
    }

    Box {
        QuizScreenV8(
            vm = vm,
            configuredSession = configuredSession,
            questionTotal = questionTotal,
            onBack = { requestExit() },
            onOtherQuiz = onOtherQuiz,
            onHome = onHome
        )

        currentQuestion?.let { question ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 53.dp, end = 18.dp)
                    .size(34.dp)
                    .background(ExitDialogPurple.copy(alpha = if (favorite) .18f else .07f), CircleShape)
                    .border(1.dp, ExitDialogPurple.copy(alpha = .75f), CircleShape)
                    .clickable {
                        favorite = library.toggleFavorite(question.id)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (favorite) "★" else "☆",
                    color = ExitDialogPurple,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = ExitDialogBackground,
            title = {
                Text(
                    "Quitter le quiz ?",
                    color = ExitDialogText,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    if (configuredSession) {
                        "Ta progression est déjà enregistrée. En quittant, tu reviendras à l'écran Commencer et tu pourras reprendre ce quiz exactement là où tu l'as laissé."
                    } else {
                        "Cette session n'est pas enregistrée comme quiz à reprendre. Si tu quittes maintenant, cette série sera interrompue."
                    },
                    color = ExitDialogMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmation = false
                        if (configuredSession) {
                            onOtherQuiz()
                        } else {
                            onBack()
                        }
                    }
                ) {
                    Text(
                        if (configuredSession) "QUITTER ET GARDER" else "QUITTER",
                        color = ExitDialogRed,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("CONTINUER", color = ExitDialogCyan, fontWeight = FontWeight.Black)
                }
            }
        )
    }
}
