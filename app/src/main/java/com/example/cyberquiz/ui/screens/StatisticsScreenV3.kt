package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity
import com.example.cyberquiz.viewmodel.QuizViewModel

private val Stat3Purple = Color(0xFFD652FF)
private val Stat3Blue = Color(0xFF19BFFF)
private val Stat3Cyan = Color(0xFF19F2E5)
private val Stat3Green = Color(0xFF38E69A)
private val Stat3Orange = Color(0xFFFFB84A)
private val Stat3Red = Color(0xFFFF657F)
private val Stat3Text = Color(0xFFF5F7FF)
private val Stat3Muted = Color(0xFF9FAED3)
private val Stat3Panel = Color(0xFF081226)

@Composable
fun StatisticsScreenV3(vm: QuizViewModel, onBack: () -> Unit) {
    val p by vm.progress.collectAsState()
    val categories by vm.categoryProgress.collectAsState()
    val concepts by vm.conceptProgress.collectAsState()
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered

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
        Stats3Header(onBack)
        Stats3Hero(accuracy = accuracy, level = p.level, xp = p.xp)

        Stats3Section("VUE D'ENSEMBLE")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stats3Metric("✓", p.correct.toString(), "Bonnes réponses", Stat3Green, Modifier.weight(1f))
            Stats3Metric("?", p.answered.toString(), "Questions répondues", Stat3Blue, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stats3Metric("🔥", p.streak.toString(), "Série actuelle", Stat3Orange, Modifier.weight(1f))
            Stats3Metric("★", p.bestStreak.toString(), "Meilleure série", Stat3Purple, Modifier.weight(1f))
        }

        Stats3Section("PROGRESSION PAR THÈME")
        if (categories.isEmpty()) {
            Stats3EmptyDetail(
                "La progression détaillée commence avec cette version. Réponds à quelques nouvelles questions Cyber pour voir apparaître Réseaux, Linux, Web, Crypto et les autres domaines."
            )
        } else {
            categories.forEach { CategoryProgressCardV3(it) }
        }

        Stats3Section("MAÎTRISE DES NOTIONS")
        if (concepts.isEmpty()) {
            Stats3EmptyDetail(
                "Les notions vont apparaître ici au fur et à mesure de tes prochaines réponses. Les retests du carnet peuvent ensuite les faire passer en maîtrisées."
            )
        } else {
            val needsWork = concepts.filter { !it.lastResultCorrect && !it.reviewMastered }
            val mastered = concepts.filter { it.reviewMastered || it.lastResultCorrect }

            if (needsWork.isNotEmpty()) {
                Text(
                    "À RENFORCER",
                    color = Stat3Orange,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                needsWork.take(8).forEach { ConceptProgressCardV3(it) }
            }

            if (mastered.isNotEmpty()) {
                Text(
                    "ACQUISES / EN COURS",
                    color = Stat3Green,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                mastered.take(12).forEach { ConceptProgressCardV3(it) }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Stats3Header(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .then(Modifier),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.clickable
            Text(
                "‹",
                color = Stat3Text,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .then(androidx.compose.ui.Modifier)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Statistiques", color = Stat3Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("TA PROGRESSION CYBER", color = Stat3Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun Stats3Hero(accuracy: Int, level: Int, xp: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF12133A), Color(0xFF07172F), Color(0xFF071024))),
                RoundedCornerShape(24.dp)
            )
            .border(1.2.dp, Color(0xFF3B6FD1), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 10f
                drawArc(
                    color = Color(0xFF20375D),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                if (accuracy > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Stat3Purple, Stat3Blue, Stat3Cyan)),
                        startAngle = -90f,
                        sweepAngle = 360f * accuracy / 100f,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = Size(size.width - stroke * 2, size.height - stroke * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$accuracy%", color = Stat3Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("RÉUSSITE", color = Stat3Muted, fontSize = 8.sp)
            }
        }
        Spacer(Modifier.width(17.dp))
        Column(Modifier.weight(1f)) {
            Text("NIVEAU $level", color = Stat3Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("Progression globale", color = Stat3Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("$xp XP accumulés", color = Stat3Muted, fontSize = 13.sp)
            Spacer(Modifier.height(11.dp))
            Stats3Bar((xp % 100) / 100f, Stat3Purple)
            Spacer(Modifier.height(5.dp))
            Text("${xp % 100} / 100 XP vers le prochain niveau", color = Color(0xFFB9C5E8), fontSize = 10.sp)
        }
    }
}

@Composable
private fun Stats3Metric(symbol: String, value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier
            .height(105.dp)
            .background(Stat3Panel, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF244777), RoundedCornerShape(18.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Stat3Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(label, color = Stat3Muted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun CategoryProgressCardV3(item: CategoryProgressEntity) {
    val accuracy = if (item.answered == 0) 0 else item.correct * 100 / item.answered
    val accent = when {
        accuracy >= 85 -> Stat3Green
        accuracy >= 70 -> Stat3Cyan
        accuracy >= 50 -> Stat3Orange
        else -> Stat3Red
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
            .background(Stat3Panel, RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .42f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.category, color = Stat3Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${item.correct} bonnes réponses sur ${item.answered}", color = Stat3Muted, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$accuracy%", color = accent, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(status, color = accent, fontSize = 9.sp)
            }
        }
        Stats3Bar(accuracy / 100f, accent)
    }
}

@Composable
private fun ConceptProgressCardV3(item: ConceptProgressEntity) {
    val accuracy = if (item.attempts == 0) 0 else item.correct * 100 / item.attempts
    val mastered = item.reviewMastered || (item.lastResultCorrect && accuracy >= 80)
    val accent = when {
        mastered -> Stat3Green
        item.lastResultCorrect -> Stat3Cyan
        else -> Stat3Orange
    }
    val status = when {
        item.reviewMastered -> "Maîtrisée après révision"
        mastered -> "Bien acquise"
        item.lastResultCorrect -> "En cours"
        else -> "À revoir"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF071225), RoundedCornerShape(17.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (mastered) "✓" else "•", color = accent, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.concept, color = Stat3Text, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
                Text(item.category.uppercase(), color = Stat3Blue, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text(status, color = accent, fontSize = 9.sp, textAlign = TextAlign.End)
        }
        if (item.attempts > 0) {
            Text(
                "Quiz normal : ${item.correct}/${item.attempts} réussite${if (item.attempts > 1) "s" else ""}",
                color = Stat3Muted,
                fontSize = 10.sp
            )
        } else if (item.reviewMastered) {
            Text("Notion validée par un retest du carnet.", color = Stat3Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun Stats3EmptyDetail(text: String) {
    Text(
        text,
        color = Color(0xFFC9D4EE),
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Stat3Blue.copy(alpha = .06f), RoundedCornerShape(18.dp))
            .border(1.dp, Stat3Blue.copy(alpha = .25f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    )
}

@Composable
private fun Stats3Section(text: String) {
    Text(text, color = Stat3Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
}

@Composable
private fun Stats3Bar(progress: Float, accent: Color) {
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
