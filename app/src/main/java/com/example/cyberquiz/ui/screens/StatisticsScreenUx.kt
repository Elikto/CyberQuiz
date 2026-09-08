package com.example.cyberquiz.ui.screens

import androidx.compose.runtime.Composable
import com.example.cyberquiz.viewmodel.QuizViewModel

@Composable
fun StatisticsScreenUx(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onReviewConcept: (String) -> Unit,
    onThemeQuiz: (String, Int) -> Unit
) {
    StatisticsScreenV3(
        vm = vm,
        onBack = onBack,
        onReviewConcept = onReviewConcept,
        onThemeQuiz = onThemeQuiz
    )
}
