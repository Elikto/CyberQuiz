package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.viewmodel.QuizViewModel

/** Keeps the established home layout stable while exposing advanced learning tools. */
@Composable
fun HomeScreenWithQuestionBank(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onReview: () -> Unit,
    onHistory: () -> Unit,
    onQuestionBank: () -> Unit,
    onLearningPaths: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        HomeScreenV2(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onQuiz = onQuiz,
            onStats = onStats,
            onCategories = onCategories,
            onReview = onReview,
            onHistory = onHistory,
            onProfile = onProfile,
            onSettings = onSettings
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeLearningPill(
                icon = "◎",
                label = "PARCOURS",
                accent = Color(0xFFD652FF),
                modifier = Modifier.weight(1f),
                onClick = onLearningPaths
            )
            HomeLearningPill(
                icon = "⌕",
                label = "BANQUE",
                accent = Color(0xFF19F2E5),
                modifier = Modifier.weight(1f),
                onClick = onQuestionBank
            )
        }
    }
}

@Composable
private fun HomeLearningPill(
    icon: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = .22f), Color(0xFF123D70).copy(alpha = .88f))
                ),
                RoundedCornerShape(50.dp)
            )
            .border(1.2.dp, accent.copy(alpha = .75f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(icon, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Text(
            "  $label",
            color = Color(0xFFF5F7FF),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .7.sp
        )
    }
}
