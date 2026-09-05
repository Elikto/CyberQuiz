package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity
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
    onThemeQuiz: (String) -> Unit
) {
    val p by vm.progress.collectAsState()
    val categories by vm.categoryProgress.collectAsState()
    val concepts by vm.conceptProgress.collectAsState()
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered
    var selectedTheme by remember { mutableStateOf<CategoryProgressEntity?>(null) }

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
            level = p.level,
            xp = p.xp,
            answered = p.answered,
            correct = p.correct,
            streak = p.streak,
            bestStreak = p.bestStreak
        )

        StatsSectionV3("PROGRESSION PAR THÈME")
        if (categories.isEmpty()) {
            StatsInfoV3(
                "La progression détaillée commence avec cette version. Réponds à quelques nouvelles questions Cyber pour voir apparaître tes résultats par domaine."
            )
        } else {
            Text(
                "Appuie sur un thème pour comprendre ce que signifie ton pourcentage et obtenir un exercice ou un cours adapté.",
                color = S3Muted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
            categories.forEach { item ->
                CategoryProgressCardV3(
                    item = item,
                    onClick = { selectedTheme = item }
                )
            }
        }

        StatsSectionV3("MAÎTRISE DES NOTIONS")
        if (concepts.isEmpty()) {
            StatsInfoV3(
                "Les notions apparaîtront ici au fil de tes prochaines réponses. Une révision réussie dans le carnet peut ensuite les faire passer en maîtrisées."
            )
        } else {
            val needsWork = concepts.filter { !it.lastResultCorrect && !it.reviewMastered }
            val acquired = concepts.filter { it.lastResultCorrect || it.reviewMastered }

            if (needsWork.isNotEmpty()) {
                Text(
                    "À RENFORCER",
                    color = S3Orange,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                Text(
                    "Appuie sur une notion À revoir pour ouvrir directement la question correspondante dans ton carnet.",
                    color = S3Muted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
                needsWork.take(8).forEach { item ->
                    ConceptProgressCardV3(
                        item = item,
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
                    ConceptProgressCardV3(item = item, onReviewClick = null)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    selectedTheme?.let { item ->
        StatsThemeDetailDialog(
            item = item,
            concepts = concepts,
            onQuiz = {
                selectedTheme = null
                onThemeQuiz(item.category)
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
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = S3Text, fontSize = 34.sp, fontWeight = FontWeight.Light)
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
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF11183A), Color(0xFF071225))),
                RoundedCornerShape(24.dp)
            )
            .border(1.2.dp, Color(0xFF3B6FD1), RoundedCornerShape(24.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(78.dp)
                    .background(S3Purple.copy(alpha = .10f), CircleShape)
                    .border(5.dp, S3Purple.copy(alpha = .60f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$accuracy%", color = S3Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("RÉUSSITE", color = S3Muted, fontSize = 7.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("NIVEAU $level", color = S3Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("Progression globale", color = S3Text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("$xp XP accumulés", color = S3Muted, fontSize = 12.sp)
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
        modifier
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
    onClick: () -> Unit
) {
    val accuracy = if (item.answered == 0) 0 else item.correct * 100 / item.answered
    val accent = when {
        accuracy >= 85 -> S3Green
        accuracy >= 70 -> S3Cyan
        accuracy >= 50 -> S3Orange
        else -> S3Red
    }
    val status = when {
        item.answered < 3 -> "Premières données"
        accuracy >= 85 -> "Très bon niveau"
        accuracy >= 70 -> "En bonne progression"
        accuracy >= 50 -> "À consolider"
        else -> "À retravailler"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(S3Panel, RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .42f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.category, color = S3Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${item.correct} bonnes réponses sur ${item.answered}", color = S3Muted, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$accuracy%", color = accent, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(status, color = accent, fontSize = 9.sp)
            }
        }
        StatsBarV3(accuracy / 100f, accent)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "COMPRENDRE CE RÉSULTAT",
                color = S3Muted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .8.sp,
                modifier = Modifier.weight(1f)
            )
            Text("›", color = accent, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConceptProgressCardV3(
    item: ConceptProgressEntity,
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

    val modifier = if (onReviewClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewClick)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier
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
                Text(item.concept, color = S3Text, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
                Text(item.category.uppercase(), color = S3Blue, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
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
private fun StatsSectionV3(text: String) {
    Text(text, color = S3Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
}

@Composable
private fun StatsBarV3(progress: Float, accent: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(9.dp)
            .background(Color(0xFF20365E), RoundedCornerShape(50.dp))
    ) {
        if (progress > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(accent, RoundedCornerShape(50.dp))
            )
        }
    }
}
