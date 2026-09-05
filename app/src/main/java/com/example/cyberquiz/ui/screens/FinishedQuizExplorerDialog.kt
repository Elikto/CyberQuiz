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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cyberquiz.model.QuizHistoryEntry
import com.example.cyberquiz.model.QuizHistoryQuestion

private val FinishExplorePurple = Color(0xFFD652FF)
private val FinishExploreBlue = Color(0xFF19BFFF)
private val FinishExploreCyan = Color(0xFF19F2E5)
private val FinishExploreGreen = Color(0xFF38E69A)
private val FinishExploreRed = Color(0xFFFF557A)
private val FinishExploreText = Color(0xFFF5F7FF)
private val FinishExploreMuted = Color(0xFF9FAED3)
private val FinishExplorePanel = Color(0xFF081226)

@Composable
internal fun FinishedQuizExplorerDialog(
    entry: QuizHistoryEntry,
    onDismiss: () -> Unit
) {
    var learnMoreIndex by rememberSaveable(entry.id) { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .background(Color(0xFF101A34), CircleShape)
                        .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = FinishExploreText, fontSize = 32.sp, fontWeight = FontWeight.Light)
                }
                Spacer(Modifier.size(11.dp))
                Column {
                    Text("Explorer les réponses", color = FinishExploreText, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text("QUIZ QUE TU VIENS DE TERMINER", color = FinishExploreMuted, fontSize = 8.sp, letterSpacing = 1.2.sp)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FinishExploreStat("NOTE", "${entry.correct}/${entry.answered}", FinishExploreCyan, Modifier.weight(1f))
                FinishExploreStat("RÉUSSITE", "${entry.percent}%", FinishExploreGreen, Modifier.weight(1f))
                FinishExploreStat("XP", "+${entry.xpGained}", FinishExplorePurple, Modifier.weight(1f))
            }

            Text("Tes réponses", color = FinishExploreText, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text(
                "Ta réponse sélectionnée et la bonne réponse sont affichées pour chaque question. Les questions faciles donnent aussi accès à l'explication détaillée.",
                color = FinishExploreMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            entry.questions.forEachIndexed { index, question ->
                FinishedQuestionCard(
                    number = index + 1,
                    item = question,
                    onLearnMore = if (question.difficulty.equals("EASY", ignoreCase = true)) {
                        { learnMoreIndex = index }
                    } else {
                        null
                    }
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(FinishExploreBlue.copy(alpha = .10f), RoundedCornerShape(16.dp))
                    .border(1.2.dp, FinishExploreBlue.copy(alpha = .75f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("RETOUR AU RÉSULTAT", color = FinishExploreBlue, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(8.dp))
        }
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
private fun FinishedQuestionCard(
    number: Int,
    item: QuizHistoryQuestion,
    onLearnMore: (() -> Unit)?
) {
    val resultAccent = if (item.correct) FinishExploreGreen else FinishExploreRed

    Column(
        Modifier
            .fillMaxWidth()
            .background(FinishExplorePanel, RoundedCornerShape(20.dp))
            .border(1.dp, resultAccent.copy(alpha = .48f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "QUESTION ${number.toString().padStart(2, '0')}",
                color = FinishExploreBlue,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (item.correct) "✓ JUSTE" else "× FAUX",
                color = resultAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(item.question, color = FinishExploreText, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
        Text(
            "${item.category} · ${finishedDifficultyLabel(item.difficulty)}",
            color = FinishExploreMuted,
            fontSize = 9.sp
        )

        item.answers.forEachIndexed { index, answer ->
            val selected = index == item.selectedIndex
            val correct = index == item.correctIndex
            val accent = when {
                correct -> FinishExploreGreen
                selected -> FinishExploreRed
                else -> Color(0xFF35517E)
            }
            val background = when {
                correct -> FinishExploreGreen.copy(alpha = .08f)
                selected -> FinishExploreRed.copy(alpha = .08f)
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
                Text(answer, color = FinishExploreText, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                if (selected) {
                    Text("TON CHOIX", color = if (correct) FinishExploreGreen else FinishExploreRed, fontSize = 7.sp, fontWeight = FontWeight.Black)
                } else if (correct) {
                    Text("BONNE", color = FinishExploreGreen, fontSize = 7.sp, fontWeight = FontWeight.Black)
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
                    .background(FinishExploreBlue.copy(alpha = .055f), RoundedCornerShape(13.dp))
                    .padding(11.dp)
            )
        }

        if (onLearnMore != null && item.difficulty.equals("EASY", ignoreCase = true)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(FinishExploreCyan.copy(alpha = .055f), RoundedCornerShape(14.dp))
                    .border(1.dp, FinishExploreCyan.copy(alpha = .30f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onLearnMore)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(31.dp)
                        .background(FinishExploreBlue.copy(alpha = .12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = FinishExploreCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("En savoir plus", color = FinishExploreCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Comprendre les 4 choix et approfondir la réponse", color = FinishExploreMuted, fontSize = 9.sp)
                }
                Text("›", color = FinishExploreCyan, fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun FinishExploreStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier
) {
    Column(
        modifier
            .background(Color(0xFF07152C), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .30f), RoundedCornerShape(14.dp))
            .padding(horizontal = 5.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = FinishExploreMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)
    }
}

private fun finishedDifficultyLabel(value: String): String = when (value.uppercase()) {
    "HARD" -> "Difficile"
    "MEDIUM" -> "Moyen"
    else -> "Facile"
}
