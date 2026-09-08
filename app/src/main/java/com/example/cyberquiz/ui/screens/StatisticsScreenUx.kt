package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.model.CATEGORY_MINI_QUIZ_SIZE
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.CategoryMiniQuizAttempt
import com.example.cyberquiz.model.categoryMiniQuizAttempts
import com.example.cyberquiz.viewmodel.QuizViewModel

private val StatsUxPurple = Color(0xFFD652FF)
private val StatsUxBlue = Color(0xFF19BFFF)
private val StatsUxCyan = Color(0xFF19F2E5)
private val StatsUxGreen = Color(0xFF38E69A)
private val StatsUxOrange = Color(0xFFFFB84A)
private val StatsUxRed = Color(0xFFFF657F)
private val StatsUxText = Color(0xFFF5F7FF)
private val StatsUxMuted = Color(0xFF9FAED3)
private val StatsUxPanel = Color(0xFF081226)

@Composable
fun StatisticsScreenUx(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onReviewConcept: (String) -> Unit,
    onThemeQuiz: (String, Int) -> Unit
) {
    var showDetailedStats by rememberSaveable { mutableStateOf(false) }

    if (showDetailedStats) {
        StatisticsScreenV3(
            vm = vm,
            onBack = { showDetailedStats = false },
            onReviewConcept = onReviewConcept,
            onThemeQuiz = onThemeQuiz
        )
        return
    }

    val progress by vm.progress.collectAsState()
    val history by vm.quizHistory.collectAsState()
    val accuracy = if (progress.answered == 0) 0 else progress.correct * 100 / progress.answered

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF050916), Color(0xFF030712)))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        StatsUxHeader(onBack)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatsUxMetric("$accuracy%", "Réussite", StatsUxPurple, Modifier.weight(1f))
            StatsUxMetric(progress.correct.toString(), "Bonnes", StatsUxGreen, Modifier.weight(1f))
            StatsUxMetric(progress.answered.toString(), "Réponses", StatsUxBlue, Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(StatsUxPurple.copy(alpha = .12f), Color(0xFF07152A))),
                    RoundedCornerShape(18.dp)
                )
                .border(1.dp, StatsUxPurple.copy(alpha = .45f), RoundedCornerShape(18.dp))
                .clickable { showDetailedStats = true }
                .padding(14.dp)
        ) {
            Text("STATISTIQUES DÉTAILLÉES", color = StatsUxPurple, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            Text("Progression par thème et notions", color = StatsUxText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Ouvre la vue complète pour tes pourcentages, notions à renforcer et cours conseillés.  ›", color = StatsUxMuted, fontSize = 10.sp, lineHeight = 15.sp)
        }

        Text(
            "MINI-QUIZ · $CATEGORY_MINI_QUIZ_SIZE QUESTIONS PAR CATÉGORIE",
            color = StatsUxCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Text(
            "Chaque ligne garde les séries de 5 questions séparément. Tu peux donc refaire une catégorie plusieurs fois et comparer tes résultats.",
            color = StatsUxMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )

        Category.entries.forEach { category ->
            val attempts = remember(history, category.label) {
                categoryMiniQuizAttempts(history, category.label)
            }
            MiniQuizStatsCard(
                category = category.label,
                attempts = attempts,
                onLaunch = { onThemeQuiz(category.label, CATEGORY_MINI_QUIZ_SIZE) }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatsUxHeader(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = StatsUxText, fontSize = 24.sp, lineHeight = 24.sp)
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text("Statistiques", color = StatsUxText, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("PROGRESSION ET MINI-QUIZ", color = StatsUxMuted, fontSize = 8.sp, letterSpacing = 1.3.sp)
        }
    }
}

@Composable
private fun StatsUxMetric(value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF08152B), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .30f), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Text(label, color = StatsUxMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MiniQuizStatsCard(
    category: String,
    attempts: List<CategoryMiniQuizAttempt>,
    onLaunch: () -> Unit
) {
    val last = attempts.lastOrNull()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StatsUxPanel, RoundedCornerShape(18.dp))
            .border(1.dp, StatsUxBlue.copy(alpha = .35f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(category, color = StatsUxText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(
                    if (attempts.isEmpty()) "Aucun mini-quiz terminé" else "${attempts.size} mini-quiz terminé${if (attempts.size > 1) "s" else ""}",
                    color = StatsUxMuted,
                    fontSize = 9.sp
                )
            }
            if (last != null) {
                val accent = when {
                    last.percent >= 80 -> StatsUxGreen
                    last.percent >= 60 -> StatsUxCyan
                    last.percent >= 40 -> StatsUxOrange
                    else -> StatsUxRed
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${last.percent}%", color = accent, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text("dernier", color = StatsUxMuted, fontSize = 7.sp)
                }
            }
        }

        if (attempts.isNotEmpty()) {
            attempts.takeLast(6).reversed().forEach { attempt ->
                AttemptRow(attempt)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp)
                .background(StatsUxPurple.copy(alpha = .09f), RoundedCornerShape(12.dp))
                .border(1.dp, StatsUxPurple.copy(alpha = .55f), RoundedCornerShape(12.dp))
                .clickable(onClick = onLaunch),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (attempts.isEmpty()) "LANCER LE QUIZ 1 · 5 QUESTIONS" else "LANCER LE QUIZ ${attempts.size + 1} · 5 QUESTIONS",
                color = StatsUxPurple,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .4.sp
            )
        }
    }
}

@Composable
private fun AttemptRow(attempt: CategoryMiniQuizAttempt) {
    val accent = when {
        attempt.percent >= 80 -> StatsUxGreen
        attempt.percent >= 60 -> StatsUxCyan
        attempt.percent >= 40 -> StatsUxOrange
        else -> StatsUxRed
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF071429), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("QUIZ ${attempt.number}", color = StatsUxMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Text("${attempt.correct}/${attempt.answered}", color = StatsUxText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text("${attempt.percent}%", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
