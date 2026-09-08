package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.CategoryQuizAttempt
import com.example.cyberquiz.model.categoryQuizAttempts
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
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

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

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsUxMetric("$accuracy%", "Réussite", "◎", StatsUxPurple, Modifier.weight(1f))
            StatsUxMetric(progress.correct.toString(), "Bonnes réponses", "✓", StatsUxGreen, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsUxMetric(progress.answered.toString(), "Questions répondues", "?", StatsUxBlue, Modifier.weight(1f))
            StatsUxMetric(progress.streak.toString(), "Série actuelle", "🔥", StatsUxOrange, Modifier.weight(1f))
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
            Text(
                "Ouvre la vue complète pour tes pourcentages, notions à renforcer et cours conseillés.  ›",
                color = StatsUxMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }

        Text(
            "QUIZ PAR CATÉGORIE",
            color = StatsUxCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Text(
            "Chaque carte regroupe tes quiz terminés dans cette catégorie, quel que soit le nombre de questions choisi.",
            color = StatsUxMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )

        Category.entries.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCategories.forEach { category ->
                    val attempts = remember(history, category.label) {
                        categoryQuizAttempts(history, category.label)
                    }
                    CategoryStatsSquare(
                        category = category,
                        attempts = attempts,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = category }
                    )
                }
                if (rowCategories.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    selectedCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { selectedCategory = null },
            containerColor = Color(0xFF0B1429),
            title = {
                Text(
                    "Nouveau quiz · ${category.label}",
                    color = StatsUxText,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Choisis le nombre de questions.",
                        color = StatsUxMuted,
                        fontSize = 12.sp
                    )
                    QUIZ_QUESTION_COUNT_OPTIONS.chunked(3).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            rowOptions.forEach { count ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .background(StatsUxPurple.copy(alpha = .10f), RoundedCornerShape(12.dp))
                                        .border(1.dp, StatsUxPurple.copy(alpha = .55f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedCategory = null
                                            onThemeQuiz(category.label, count)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        quizQuestionCountLabel(count),
                                        color = StatsUxText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            repeat(3 - rowOptions.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedCategory = null }) {
                    Text("ANNULER", color = StatsUxMuted, fontWeight = FontWeight.Bold)
                }
            }
        )
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
            Text("TA PROGRESSION", color = StatsUxMuted, fontSize = 8.sp, letterSpacing = 1.3.sp)
        }
    }
}

@Composable
private fun StatsUxMetric(
    value: String,
    label: String,
    symbol: String,
    accent: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = .10f), Color(0xFF08152B))),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accent.copy(alpha = .12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Column {
            Text(value, color = StatsUxText, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(label, color = StatsUxMuted, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun CategoryStatsSquare(
    category: Category,
    attempts: List<CategoryQuizAttempt>,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val accent = statsCategoryAccent(category)
    val last = attempts.lastOrNull()

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = .10f), StatsUxPanel)),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, accent.copy(alpha = .40f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(accent.copy(alpha = .12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    statsCategorySymbol(category),
                    color = accent,
                    fontSize = if (category == Category.AD) 10.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.weight(1f))
            Text("+", color = accent, fontSize = 20.sp, fontWeight = FontWeight.Light)
        }

        Text(
            category.label,
            color = StatsUxText,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Black
        )

        if (last == null) {
            Text("Aucun quiz terminé", color = StatsUxMuted, fontSize = 9.sp, lineHeight = 12.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${last.percent}%",
                    color = resultAccent(last.percent),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Dernier · ${last.questionCount} questions",
                    color = StatsUxMuted,
                    fontSize = 8.sp
                )
                Text(
                    "${attempts.size} quiz terminé${if (attempts.size > 1) "s" else ""}",
                    color = StatsUxMuted,
                    fontSize = 8.sp
                )
            }
        }
    }
}

private fun resultAccent(percent: Int): Color = when {
    percent >= 80 -> StatsUxGreen
    percent >= 60 -> StatsUxCyan
    percent >= 40 -> StatsUxOrange
    else -> StatsUxRed
}

private fun statsCategoryAccent(category: Category): Color = when (category.ordinal % 4) {
    0 -> StatsUxPurple
    1 -> StatsUxBlue
    2 -> StatsUxCyan
    else -> StatsUxGreen
}

private fun statsCategorySymbol(category: Category): String = when (category) {
    Category.RESEAUX -> "⌁"
    Category.LINUX -> ">_"
    Category.WINDOWS -> "▦"
    Category.CRYPTO -> "◇"
    Category.WEB -> "◎"
    Category.MALWARE -> "!"
    Category.SOCIAL -> "◌"
    Category.OSINT -> "⌖"
    Category.FORENSICS -> "⌕"
    Category.PENTEST -> "⚡"
    Category.AD -> "AD"
    Category.CLOUD -> "☁"
    Category.MOBILE -> "▯"
    Category.SYSTEM -> "⚙"
}
