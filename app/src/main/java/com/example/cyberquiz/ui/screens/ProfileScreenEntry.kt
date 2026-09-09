package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.viewmodel.QuizViewModel

@Composable
fun ProfileScreenV5(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onFriends: () -> Unit,
    onBack: () -> Unit
) {
    val vm: QuizViewModel = viewModel()
    val context = LocalContext.current
    val progress by vm.progress.collectAsState()
    val history by vm.quizHistory.collectAsState()
    val metrics = EngagementMetrics(
        answered = progress.answered,
        correct = progress.correct,
        xp = progress.xp,
        level = progress.level,
        streak = progress.streak,
        bestStreak = progress.bestStreak,
        quizCount = history.size
    )
    var showCosmetics by rememberSaveable { mutableStateOf(false) }
    var showShop by rememberSaveable { mutableStateOf(false) }

    when {
        showCosmetics -> {
            EngagementStore.sync(context, metrics)
            CosmeticsScreen(
                playerLevel = progress.level,
                metrics = metrics,
                onBack = { showCosmetics = false }
            )
        }

        showShop -> {
            CosmeticShopScreen(
                metrics = metrics,
                onBack = { showShop = false }
            )
        }

        else -> {
            val engagement = EngagementStore.sync(context, metrics)
            Box {
                ProfileScreenV6(
                    selectedQuizType = selectedQuizType,
                    onQuizTypeSelected = onQuizTypeSelected,
                    onCosmetics = { showCosmetics = true },
                    onBack = onBack
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 14.dp, end = 18.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF20153B), RoundedCornerShape(50.dp))
                            .border(1.dp, Color(0xFFFFB84A).copy(alpha = .65f), RoundedCornerShape(50.dp))
                            .clickable { showShop = true }
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "◈ ${engagement.coins}",
                            color = Color(0xFFFFC86A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(Color(0xFF0B2542), RoundedCornerShape(50.dp))
                            .border(1.dp, Color(0xFF20E7F2).copy(alpha = .72f), RoundedCornerShape(50.dp))
                            .clickable(onClick = onFriends)
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "👥 CYBERSQUAD",
                            color = Color(0xFF7BF5FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
