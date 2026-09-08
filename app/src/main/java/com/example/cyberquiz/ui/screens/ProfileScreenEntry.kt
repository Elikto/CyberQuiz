package com.example.cyberquiz.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyberquiz.viewmodel.QuizViewModel

@Composable
fun ProfileScreenV5(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onBack: () -> Unit
) {
    val vm: QuizViewModel = viewModel()
    val progress by vm.progress.collectAsState()
    var showCosmetics by rememberSaveable { mutableStateOf(false) }

    if (showCosmetics) {
        CosmeticsScreen(
            playerLevel = progress.level,
            onBack = { showCosmetics = false }
        )
    } else {
        ProfileScreenV5(
            selectedQuizType = selectedQuizType,
            onQuizTypeSelected = onQuizTypeSelected,
            onCosmetics = { showCosmetics = true },
            onBack = onBack
        )
    }
}
