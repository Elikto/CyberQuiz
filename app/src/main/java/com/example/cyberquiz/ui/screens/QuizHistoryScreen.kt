package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.cyberquiz.model.QuizHistoryEntry
import com.example.cyberquiz.model.QuizHistoryQuestion
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.viewmodel.QuizViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HistoryPurple = Color(0xFFD652FF)
private val HistoryBlue = Color(0xFF19BFFF)
private val HistoryCyan = Color(0xFF19F2E5)
private val HistoryGreen = Color(0xFF38E69A)
private val HistoryOrange = Color(0xFFFFB84A)
private val HistoryRed = Color(0xFFFF557A)
private val HistoryText = Color(0xFFF5F7FF)
private val HistoryMuted = Color(0xFF9FAED3)
private val HistoryPanel = Color(0xFF081226)

@Composable
fun QuizHistoryScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onReplay: (String) -> Unit
) {
    val history by vm.quizHistory.collectAsState()
    val activeSessions by vm.activeSessions.collectAsState()
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var exploringId by rememberSaveable { mutableStateOf<String?>(null) }
    val historyScrollState = rememberScrollState()

    val exploring = history.firstOrNull { it.id == exploringId }
    if (exploring != null) {
        BackHandler {
            exploringId = null
        }
        HistoryExplorer(
            entry = exploring,
            onBack = { exploringId = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF050916))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(historyScrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        HistoryHeader("Historique", "25 DERNIERS QUIZ TERMINÉS", onBack)

        Text(
            "Tes quiz terminés",
            color = HistoryText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "Les quiz abandonnés ne sont pas ajoutés ici. Tu peux revoir un résultat, relancer sa configuration ou explorer toutes les réponses que tu avais choisies.",
            color = HistoryMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        if (history.isEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(HistoryPanel, RoundedCornerShape(22.dp))
                    .border(1.dp, HistoryBlue.copy(alpha = .35f), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("◷", color = HistoryCyan, fontSize = 34.sp)
                Text("Aucun quiz terminé", color = HistoryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Ton premier quiz terminé apparaîtra ici automatiquement.",
                    color = HistoryMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            history.forEachIndexed { index, entry ->
                HistoryCard(
                    index = index,
                    entry = entry,
                    expanded = expandedId == entry.id,
                    replayEnabled = activeSessions.size < QuizViewModel.MAX_ACTIVE_SESSIONS,
                    onToggleInfo = {
                        expandedId = if (expandedId == entry.id) null else entry.id
                    },
                    onReplay = { onReplay(entry.id) },
                    onExplore = { exploringId = entry.id }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HistoryCard(
    index: Int,
    entry: QuizHistoryEntry,
    expanded: Boolean,
    replayEnabled: Boolean,
    onToggleInfo: () -> Unit,
    onReplay: () -> Unit,
    onExplore: () -> Unit
) {
    val percent = entry.percent
    val accent = when {
        percent >= 95 -> Color(0xFFFFD166)
        percent >= 85 -> HistoryPurple
        percent >= 65 -> HistoryBlue
        percent >= 51 -> HistoryGreen
        else -> HistoryOrange
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = .10f), HistoryPanel, Color(0xFF071024))
                ),
                RoundedCornerShape(21.dp)
            )
            .border(1.1.dp, accent.copy(alpha = .55f), RoundedCornerShape(21.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "QUIZ #${index + 1}",
                    color = accent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    historyModeLabel(entry),
                    color = HistoryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    formatDate(entry.endedAt),
                    color = HistoryMuted,
                    fontSize = 10.sp
                )
            }

            Box(
                Modifier
                    .size(36.dp)
                    .background(HistoryBlue.copy(alpha = .10f), CircleShape)
                    .border(1.dp, HistoryBlue.copy(alpha = .55f), CircleShape)
                    .clickable(onClick = onToggleInfo),
                contentAlignment = Alignment.Center
            ) {
                Text("i", color = HistoryCyan, fontSize = 17.sp, fontWeight = FontWeight.Black)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryStat("NOTE", "${entry.correct}/${entry.answered}", accent, Modifier.weight(1f))
            HistoryStat("RÉUSSITE", "${entry.percent}%", accent, Modifier.weight(1f))
            HistoryStat("XP", "+${entry.xpGained}", HistoryPurple, Modifier.weight(1f))
        }

        if (expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF061329), RoundedCornerShape(15.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoLine("Début", formatDateTime(entry.startedAt))
                InfoLine("Fin", formatDateTime(entry.endedAt))
                InfoLine("Durée", formatDuration(entry.startedAt, entry.endedAt))
                InfoLine("Difficulté", modeName(entry.config.mode))
                InfoLine("Catégories", categorySummary(entry))
                InfoLine("Questions enregistrées", entry.questions.size.toString())
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryActionButton(
                text = if (replayEnabled) "RELANCER" else "6 QUIZ EN COURS",
                accent = HistoryPurple,
                enabled = replayEnabled,
                modifier = Modifier.weight(1f),
                onClick = onReplay
            )
            HistoryActionButton(
                text = "EXPLORER",
                accent = HistoryCyan,
                enabled = entry.questions.isNotEmpty(),
                modifier = Modifier.weight(1f),
                onClick = onExplore
            )
        }
    }
}

@Composable
private fun HistoryExplorer(entry: QuizHistoryEntry, onBack: () -> Unit) {
    var learnMoreIndex by rememberSaveable(entry.id) { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF050916))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        HistoryHeader("Explorer", "QUIZ DU ${formatDate(entry.startedAt).uppercase()}", onBack)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryStat("NOTE", "${entry.correct}/${entry.answered}", HistoryCyan, Modifier.weight(1f))
            HistoryStat("RÉUSSITE", "${entry.percent}%", HistoryGreen, Modifier.weight(1f))
            HistoryStat("XP", "+${entry.xpGained}", HistoryPurple, Modifier.weight(1f))
        }

        Text(
            "Tes réponses",
            color = HistoryText,
            fontSize = 23.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "La réponse que tu avais sélectionnée est conservée. La bonne réponse est également indiquée pour pouvoir revoir le quiz tranquillement.",
            color = HistoryMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        entry.questions.forEachIndexed { index, question ->
            HistoryQuestionCard(
                number = index + 1,
                item = question,
                onLearnMore = if (question.difficulty.equals("EASY", ignoreCase = true)) {
                    { learnMoreIndex = index }
                } else {
                    null
                }
            )
        }

        Spacer(Modifier.height(8.dp))
    }

    val learnMoreQuestion = learnMoreIndex?.let { entry.questions.getOrNull(it) }
    if (learnMoreQuestion != null && learnMoreQuestion.difficulty.equals("EASY", ignoreCase = true)) {
        HistoryEasyLearnMoreDialogV8(
            question = learnMoreQuestion.question,
            category = learnMoreQuestion.category,
            answers = learnMoreQuestion.answers,
            correctIndex = learnMoreQuestion.correctIndex,
            explanation = learnMoreQuestion.explanation,
            onDismiss = { learnMoreIndex = null }
        )
    }
}

@Composable
private fun HistoryQuestionCard(
    number: Int,
    item: QuizHistoryQuestion,
    onLearnMore: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HistoryPanel, RoundedCornerShape(20.dp))
            .border(
                1.dp,
                (if (item.correct) HistoryGreen else HistoryRed).copy(alpha = .48f),
                RoundedCornerShape(20.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "QUESTION ${number.toString().padStart(2, '0')}",
                color = HistoryBlue,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (item.correct) "✓ JUSTE" else "× FAUX",
                color = if (item.correct) HistoryGreen else HistoryRed,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(item.question, color = HistoryText, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
        Text(
            "${item.category} · ${difficultyLabel(item.difficulty)}",
            color = HistoryMuted,
            fontSize = 9.sp
        )

        item.answers.forEachIndexed { index, answer ->
            val selected = index == item.selectedIndex
            val correct = index == item.correctIndex
            val accent = when {
                correct -> HistoryGreen
                selected -> HistoryRed
                else -> Color(0xFF35517E)
            }
            val background = when {
                correct -> HistoryGreen.copy(alpha = .08f)
                selected -> HistoryRed.copy(alpha = .08f)
                else -> Color(0xFF071329)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(background, RoundedCornerShape(14.dp))
                    .border(1.dp, accent.copy(alpha = .55f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 11.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    ('A'.code + index).toChar().toString(),
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.size(9.dp))
                Text(answer, color = HistoryText, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                if (selected) {
                    Text("TON CHOIX", color = if (correct) HistoryGreen else HistoryRed, fontSize = 7.sp, fontWeight = FontWeight.Black)
                } else if (correct) {
                    Text("BONNE", color = HistoryGreen, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        if (item.explanation.isNotBlank()) {
            Text(
                item.explanation,
                color = Color(0xFFC9D4F0),
                fontSize = 11.sp,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HistoryBlue.copy(alpha = .055f), RoundedCornerShape(13.dp))
                    .padding(11.dp)
            )
        }

        if (onLearnMore != null && item.difficulty.equals("EASY", ignoreCase = true)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HistoryCyan.copy(alpha = .055f), RoundedCornerShape(14.dp))
                    .border(1.dp, HistoryCyan.copy(alpha = .30f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onLearnMore)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(31.dp)
                        .background(HistoryBlue.copy(alpha = .12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = HistoryCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("En savoir plus", color = HistoryCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Comprendre les 4 choix et approfondir la réponse",
                        color = HistoryMuted,
                        fontSize = 9.sp
                    )
                }
                Text("›", color = HistoryCyan, fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun HistoryHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(42.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = HistoryText, fontSize = 32.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.size(11.dp))
        Column {
            Text(title, color = HistoryText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = HistoryMuted, fontSize = 8.sp, letterSpacing = 1.3.sp)
        }
    }
}

@Composable
private fun HistoryStat(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0xFF07152C), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .30f), RoundedCornerShape(14.dp))
            .padding(horizontal = 5.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = HistoryMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = HistoryMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
        Text(value, color = HistoryText, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
private fun HistoryActionButton(
    text: String,
    accent: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(45.dp)
            .background(
                if (enabled) accent.copy(alpha = .10f) else Color(0xFF111725),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (enabled) accent.copy(alpha = .65f) else Color(0xFF38425B),
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) accent else Color(0xFF677089),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

private fun historyModeLabel(entry: QuizHistoryEntry): String =
    "${modeName(entry.config.mode)} · ${categorySummary(entry)}"

private fun modeName(mode: QuizSessionMode): String = when (mode) {
    QuizSessionMode.EASY -> "Facile"
    QuizSessionMode.MEDIUM -> "Moyen"
    QuizSessionMode.HARD -> "Difficile"
    QuizSessionMode.RANDOM -> "Aléatoire"
    QuizSessionMode.DIFFICULTIES -> "Mes difficultés"
}

private fun categorySummary(entry: QuizHistoryEntry): String = when {
    entry.config.categories.size == com.example.cyberquiz.model.Category.entries.size -> "Toutes les catégories"
    entry.config.categories.size == 1 -> entry.config.categories.first()
    else -> "${entry.config.categories.size} catégories"
}

private fun difficultyLabel(value: String): String = when (value.uppercase()) {
    "HARD" -> "Difficile"
    "MEDIUM" -> "Moyen"
    else -> "Facile"
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatDuration(start: Long, end: Long): String {
    val totalSeconds = ((end - start).coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (minutes > 0) "${minutes} min ${seconds.toString().padStart(2, '0')} s" else "${seconds} s"
}
