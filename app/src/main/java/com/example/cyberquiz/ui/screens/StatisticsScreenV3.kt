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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity
import com.example.cyberquiz.model.categoryLevelProgress
import com.example.cyberquiz.viewmodel.QuizViewModel

private val S3Purple = Color(0xFFD652FF)
private val S3Blue = Color(0xFF19BFFF)
private val S3Cyan = Color(0xFF19F2E5)
private val S3Green = Color(0xFF38E69A)
private val S3Orange = Color(0xFFFFB84A)
private val S3Red = Color(0xFFFF657F)
private val S3Text = Color(0xFFF5F7FF)
private val S3Muted = Color(0xFF9FAED3)
private val S3Panel = Color(0xFF081226)

@Composable
fun StatisticsScreenV3(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onReviewConcept: (String) -> Unit,
    onThemeQuiz: (String, Int) -> Unit
) {
    val progress by vm.progress.collectAsState()
    val categories by vm.categoryProgress.collectAsState()
    val concepts by vm.conceptProgress.collectAsState()
    val reviewItems by vm.reviewItems.collectAsState()
    val accuracy = if (progress.answered == 0) 0 else progress.correct * 100 / progress.answered
    val reviewQuestionsByConcept = remember(reviewItems) {
        reviewItems.associate { it.concept to it.question }
    }

    var selectedTheme by remember { mutableStateOf<CategoryProgressEntity?>(null) }
    var themesExpanded by rememberSaveable { mutableStateOf(false) }
    var conceptsExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF050916), Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StatsHeaderV3(onBack)
        GlobalProgressCardV3(
            accuracy = accuracy,
            level = progress.level,
            xp = progress.xp,
            answered = progress.answered,
            correct = progress.correct,
            streak = progress.streak,
            bestStreak = progress.bestStreak
        )

        CollapsibleStatsSectionV3(
            text = "PROGRESSION PAR THÈME",
            detail = if (categories.isEmpty()) "Aucune donnée" else "${categories.size} thèmes",
            expanded = themesExpanded,
            onToggle = { themesExpanded = !themesExpanded }
        )
        if (themesExpanded) {
            if (categories.isEmpty()) {
                StatsInfoV3(
                    "Réponds à quelques questions pour créer tes premiers niveaux par thème. Chaque réponse fait progresser l'XP de la catégorie."
                )
            } else {
                Text(
                    "Chaque réponse donne 5 XP dans son thème, avec 5 XP bonus si elle est correcte. Le niveau augmente tous les 100 XP.",
                    color = S3Muted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )

                categories.sortedBy { it.category }.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            CategoryProgressCardV3(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedTheme = item }
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        val needsWork = concepts.filter { !it.lastResultCorrect && !it.reviewMastered }
        val acquired = concepts.filter { it.lastResultCorrect || it.reviewMastered }
        CollapsibleStatsSectionV3(
            text = "MAÎTRISE DES NOTIONS",
            detail = if (concepts.isEmpty()) "Aucune donnée" else "${concepts.size} notions",
            expanded = conceptsExpanded,
            onToggle = { conceptsExpanded = !conceptsExpanded }
        )
        if (conceptsExpanded) {
            if (concepts.isEmpty()) {
                StatsInfoV3(
                    "Les notions apparaîtront ici au fil de tes réponses. Une révision réussie peut ensuite les faire passer en maîtrisées."
                )
            } else {
                if (needsWork.isNotEmpty()) {
                    Text(
                        "À RENFORCER",
                        color = S3Orange,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                    needsWork.take(8).forEach { item ->
                        ConceptProgressCardV3(
                            item = item,
                            title = reviewQuestionsByConcept[item.concept]
                                ?.takeIf { it.isNotBlank() }
                                ?: item.concept,
                            onReviewClick = { onReviewConcept(item.concept) }
                        )
                    }
                }

                if (acquired.isNotEmpty()) {
                    Text(
                        "ACQUISES / EN COURS",
                        color = S3Green,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp
                    )
                    acquired.take(12).forEach { item ->
                        ConceptProgressCardV3(
                            item = item,
                            title = item.concept,
                            onReviewClick = null
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    selectedTheme?.let { item ->
        StatsThemeDetailDialog(
            item = item,
            concepts = concepts,
            onQuiz = { questionCount ->
                selectedTheme = null
                onThemeQuiz(item.category, questionCount)
            },
            onDismiss = { selectedTheme = null }
        )
    }
}

@Composable
private fun StatsHeaderV3(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = S3Text, fontSize = 30.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Statistiques", color = S3Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("TA PROGRESSION CYBER", color = S3Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun GlobalProgressCardV3(
    accuracy: Int,
    level: Int,
    xp: Int,
    answered: Int,
    correct: Int,
    streak: Int,
    bestStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF11183A), Color(0xFF071225))),
                RoundedCornerShape(22.dp)
            )
            .border(1.2.dp, Color(0xFF3B6FD1), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(S3Purple.copy(alpha = .10f), CircleShape)
                    .border(4.dp, S3Purple.copy(alpha = .60f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$accuracy%", color = S3Text, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Text("RÉUSSITE", color = S3Muted, fontSize = 7.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("NIVEAU $level", color = S3Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("Progression globale", color = S3Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$xp XP accumulés", color = S3Muted, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                StatsBarV3((xp % 100) / 100f, S3Purple)
                Text("${xp % 100} / 100 XP vers le niveau suivant", color = Color(0xFFB9C5E8), fontSize = 9.sp)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniMetricV3(correct.toString(), "Bonnes", S3Green, Modifier.weight(1f))
            MiniMetricV3(answered.toString(), "Réponses", S3Blue, Modifier.weight(1f))
            MiniMetricV3(streak.toString(), "Série", S3Orange, Modifier.weight(1f))
            MiniMetricV3(bestStreak.toString(), "Record", S3Purple, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniMetricV3(value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF08152B), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .28f), RoundedCornerShape(14.dp))
            .padding(vertical = 9.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = S3Muted, fontSize = 8.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CategoryProgressCardV3(
    item: CategoryProgressEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accuracy = if (item.answered == 0) 0 else item.correct * 100 / item.answered
    val categoryProgress = categoryLevelProgress(item.answered, item.correct)
    val accent = statsThemeAccent(item.category)

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = .11f), S3Panel)),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, accent.copy(alpha = .48f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = .14f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    statsThemeSymbol(item.category),
                    color = accent,
                    fontSize = if (item.category.equals("Active Directory", true)) 9.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "NIV. ${categoryProgress.level}",
                color = accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                item.category,
                color = S3Text,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2
            )
            Text(
                "${item.correct}/${item.answered} bonnes · $accuracy%",
                color = S3Muted,
                fontSize = 8.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${categoryProgress.xp}", color = S3Text, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text("XP", color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            StatsBarV3(categoryProgress.progress, accent, height = 7.dp)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${categoryProgress.xpIntoLevel}/100 XP",
                    color = S3Muted,
                    fontSize = 8.sp,
                    modifier = Modifier.weight(1f)
                )
                Text("›", color = accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConceptProgressCardV3(
    item: ConceptProgressEntity,
    title: String,
    onReviewClick: (() -> Unit)?
) {
    val accuracy = if (item.attempts == 0) 0 else item.correct * 100 / item.attempts
    val mastered = item.reviewMastered || (item.lastResultCorrect && accuracy >= 80)
    val accent = when {
        mastered -> S3Green
        item.lastResultCorrect -> S3Cyan
        else -> S3Orange
    }
    val status = when {
        item.reviewMastered -> "Maîtrisée après révision"
        mastered -> "Bien acquise"
        item.lastResultCorrect -> "En cours"
        else -> "À revoir"
    }

    val clickableModifier = if (onReviewClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewClick)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = clickableModifier
            .background(
                if (onReviewClick != null) {
                    Brush.horizontalGradient(
                        listOf(S3Orange.copy(alpha = .10f), Color(0xFF071225), S3Purple.copy(alpha = .05f))
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF071225), Color(0xFF071225)))
                },
                RoundedCornerShape(17.dp)
            )
            .border(
                if (onReviewClick != null) 1.4.dp else 1.dp,
                accent.copy(alpha = if (onReviewClick != null) .70f else .35f),
                RoundedCornerShape(17.dp)
            )
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(accent.copy(alpha = .12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (mastered) "✓" else "!", color = accent, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = S3Text,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(statsThemeSymbol(item.category), color = S3Blue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        item.category.uppercase(),
                        color = S3Blue,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            if (onReviewClick != null) {
                Text("›", color = S3Orange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(status, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            when {
                onReviewClick != null -> Text("OUVRIR À REVOIR", color = S3Orange, fontSize = 8.sp, fontWeight = FontWeight.Black)
                item.attempts > 0 -> Text("Quiz : ${item.correct}/${item.attempts}", color = S3Muted, fontSize = 9.sp)
                item.reviewMastered -> Text("Retest validé", color = S3Muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun CollapsibleStatsSectionV3(
    text: String,
    detail: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(S3Cyan.copy(alpha = .045f), RoundedCornerShape(14.dp))
            .border(1.dp, S3Cyan.copy(alpha = .22f), RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text,
                color = S3Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            Text(detail, color = S3Muted, fontSize = 8.sp)
        }
        Text(
            if (expanded) "⌃" else "⌄",
            color = S3Cyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun StatsInfoV3(text: String) {
    Text(
        text,
        color = Color(0xFFC9D4EE),
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(S3Blue.copy(alpha = .06f), RoundedCornerShape(18.dp))
            .border(1.dp, S3Blue.copy(alpha = .25f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    )
}

@Composable
private fun StatsBarV3(progress: Float, accent: Color, height: Dp = 9.dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .background(Color(0xFF20365E), RoundedCornerShape(50.dp))
    ) {
        if (progress > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(
                        Brush.horizontalGradient(listOf(accent.copy(alpha = .75f), accent)),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

private fun statsThemeAccent(category: String): Color = when ((category.hashCode() and Int.MAX_VALUE) % 4) {
    0 -> S3Purple
    1 -> S3Blue
    2 -> S3Cyan
    else -> S3Green
}

private fun statsThemeSymbol(category: String): String = when (category.lowercase()) {
    "réseaux" -> "⌁"
    "linux" -> ">_"
    "windows" -> "▦"
    "cryptographie" -> "◇"
    "sécurité web" -> "◎"
    "malware" -> "!"
    "ingénierie sociale" -> "◌"
    "osint" -> "⌖"
    "forensics" -> "⌕"
    "pentest" -> "⚡"
    "active directory" -> "AD"
    "cloud security" -> "☁"
    "mobile security" -> "▯"
    "sécurité système" -> "⚙"
    else -> "◆"
}
