package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

private val ExitDialogBackground = Color(0xFF0B1429)
private val ExitDialogText = Color(0xFFF5F7FF)
private val ExitDialogMuted = Color(0xFFA7B4D4)
private val ExitDialogCyan = Color(0xFF19F2E5)
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
    val currentState by vm.state.collectAsState()
    var showExitConfirmation by remember { mutableStateOf(false) }

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

    QuizScreenV8(
        vm = vm,
        configuredSession = configuredSession,
        questionTotal = questionTotal,
        onBack = { requestExit() },
        onOtherQuiz = onOtherQuiz,
        onHome = onHome
    )

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
                        "Ta progression est déjà enregistrée. Tu pourras reprendre ce quiz exactement là où tu l'as laissé depuis l'écran Commencer."
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
                        onBack()
                    }
                ) {
                    Text("QUITTER", color = ExitDialogRed, fontWeight = FontWeight.Black)
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
