package com.example.cyberquiz.ui.screens

import androidx.compose.runtime.Composable
import com.example.cyberquiz.viewmodel.QuizViewModel

@Composable
fun HomeScreenV2(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onReview: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    HomeScreenV2(
        vm = vm,
        selectedQuizType = selectedQuizType,
        onQuiz = onQuiz,
        onStats = onStats,
        onCategories = onCategories,
        onReview = onReview,
        onHistory = {},
        onProfile = onProfile,
        onSettings = onSettings
    )
}
