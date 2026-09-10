package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

/**
 * Keeps the existing home screen stable while exposing the question bank as a
 * first-class home action. The compact floating pill avoids reshuffling the
 * established home menu and stays reachable regardless of scroll position.
 */
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
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 14.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF3D1769), Color(0xFF123D70))
                    ),
                    RoundedCornerShape(50.dp)
                )
                .border(1.2.dp, Color(0xFF19F2E5).copy(alpha = .75f), RoundedCornerShape(50.dp))
                .clickable(onClick = onQuestionBank)
                .padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⌕", color = Color(0xFF19F2E5), fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(
                "  BANQUE",
                color = Color(0xFFF5F7FF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .8.sp
            )
        }
    }
}
