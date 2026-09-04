package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

private val Q4Purple = Color(0xFFD652FF)
private val Q4Blue = Color(0xFF19BFFF)
private val Q4Cyan = Color(0xFF19F2E5)
private val Q4Green = Color(0xFF38E69A)
private val Q4Red = Color(0xFFFF557A)
private val Q4Text = Color(0xFFF5F7FF)
private val Q4Muted = Color(0xFF9FAED3)
private val Q4Panel = Color(0xFF081226)

@Composable
fun QuizScreenV4(vm: QuizViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val result by vm.result.collectAsState()
    val progress by vm.progress.collectAsState()
    var selected by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        QuizHeaderV4(onBack)

        when (val s = state) {
            QuizUiState.Loading -> Box(
                Modifier.fillMaxWidth().height(360.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Q4Cyan)
            }

            QuizUiState.Finished -> FinishedQuizCardV4(
                answered = progress.answered,
                correct = progress.correct,
                xp = progress.xp,
                onReplay = {
                    selected = null
                    vm.start()
                },
                onBack = onBack
            )

            is QuizUiState.Ready -> {
                val q = s.question
                val answers = listOf(q.answerA, q.answerB, q.answerC, q.answerD)

                ThemeTitleV4(q.category, q.difficulty)
                QuizStatsRowV4(s.number, progress.xp, progress.streak)
                QuestionCardV4(q.question)

                Text(
                    "CHOISIS TA RÉPONSE",
                    color = Q4Muted,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                )

                answers.forEachIndexed { index, answer ->
                    val visual = when {
                        result == null && selected == index -> AnswerStateV4.Selected
                        result != null && index == q.correctIndex -> AnswerStateV4.Correct
                        result != null && selected == index && index != q.correctIndex -> AnswerStateV4.Wrong
                        else -> AnswerStateV4.Idle
                    }
                    AnswerCardV4(
                        letter = ('A'.code + index).toChar().toString(),
                        text = answer,
                        state = visual,
                        enabled = result == null,
                        onClick = { selected = index }
                    )
                }

                if (result == null) {
                    PrimaryButtonV4(
                        text = if (selected == null) "Sélectionne une réponse" else "Valider ma réponse",
                        enabled = selected != null,
                        onClick = { selected?.let(vm::answer) }
                    )
                } else {
                    ExplanationCardV4(result!!.correct, result!!.explanation, result!!.xp)
                    PrimaryButtonV4("Question suivante", true) {
                        selected = null
                        vm.nextQuestion()
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuizHeaderV4(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = Q4Text, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = Q4Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = Q4Purple, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Text("MODE QUIZ", color = Q4Muted, fontSize = 9.sp, letterSpacing = 1.8.sp)
        }
    }
}

@Composable
private fun ThemeTitleV4(category: String, difficulty: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "THÈME",
                color = Q4Muted,
                fontSize = 9.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(1.dp))
            Text(
                category.uppercase(),
                color = Q4Blue,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .35.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        DifficultyChipV4(difficulty)
    }
}

@Composable
private fun QuizStatsRowV4(number: Int, xp: Int, streak: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OldStyleStatCardV4(
            label = "QUESTION",
            value = number.toString().padStart(2, '0'),
            accent = Q4Purple,
            modifier = Modifier.weight(1f)
        )
        OldStyleStatCardV4(
            label = "XP",
            value = xp.toString(),
            accent = Q4Cyan,
            modifier = Modifier.weight(1f)
        )
        OldStyleStatCardV4(
            label = "🔥 SÉRIE",
            value = streak.toString(),
            accent = Color(0xFFFFA83A),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OldStyleStatCardV4(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(52.dp)
            .background(Color(0xFF0B1730), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .72f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            label,
            color = accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(value, color = Q4Text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DifficultyChipV4(difficulty: String) {
    val color = when (difficulty.uppercase()) {
        "HARD" -> Q4Red
        "MEDIUM" -> Color(0xFFFFB84A)
        else -> Q4Green
    }
    val label = when (difficulty.uppercase()) {
        "HARD" -> "DIFFICILE"
        "MEDIUM" -> "MOYEN"
        else -> "FACILE"
    }
    Box(
        Modifier
            .background(color.copy(alpha = .10f), RoundedCornerShape(50.dp))
            .border(1.dp, color.copy(alpha = .75f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuestionCardV4(question: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFF111538), Color(0xFF081832), Color(0xFF071023))),
                RoundedCornerShape(24.dp)
            )
            .border(1.3.dp, Color(0xFF3A71D8), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val c = Q4Blue.copy(alpha = .10f)
            repeat(4) { i ->
                val y = 22f + i * 34f
                drawLine(c, Offset(0f, y), Offset(size.width * .23f, y), 1.5f)
                drawLine(c, Offset(size.width * .77f, y), Offset(size.width, y), 1.5f)
            }
        }
        Text(
            question,
            color = Q4Text,
            fontSize = 21.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private enum class AnswerStateV4 { Idle, Selected, Correct, Wrong }

@Composable
private fun AnswerCardV4(
    letter: String,
    text: String,
    state: AnswerStateV4,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = when (state) {
        AnswerStateV4.Selected -> Q4Purple
        AnswerStateV4.Correct -> Q4Green
        AnswerStateV4.Wrong -> Q4Red
        AnswerStateV4.Idle -> Color(0xFF365A91)
    }
    val bg = when (state) {
        AnswerStateV4.Selected -> Color(0xFF25133D)
        AnswerStateV4.Correct -> Color(0xFF092E2B)
        AnswerStateV4.Wrong -> Color(0xFF321326)
        AnswerStateV4.Idle -> Q4Panel
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(bg, Color(0xFF071024))), RoundedCornerShape(19.dp))
            .border(if (state == AnswerStateV4.Idle) 1.dp else 1.6.dp, accent, RoundedCornerShape(19.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp))
                .border(1.dp, accent, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(13.dp))
        Text(text, color = Q4Text, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
        when (state) {
            AnswerStateV4.Selected -> Text("●", color = Q4Purple, fontSize = 17.sp)
            AnswerStateV4.Correct -> Text("✓", color = Q4Green, fontSize = 20.sp, fontWeight = FontWeight.Black)
            AnswerStateV4.Wrong -> Text("×", color = Q4Red, fontSize = 23.sp, fontWeight = FontWeight.Black)
            AnswerStateV4.Idle -> Text("›", color = Color(0xFF7081A8), fontSize = 23.sp)
        }
    }
}

@Composable
private fun ExplanationCardV4(correct: Boolean, explanation: String, xp: Int) {
    val accent = if (correct) Q4Green else Q4Red
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = .12f), Color(0xFF071225))),
                RoundedCornerShape(22.dp)
            )
            .border(1.2.dp, accent.copy(alpha = .8f), RoundedCornerShape(22.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(accent.copy(alpha = .15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (correct) "✓" else "×", color = accent, fontSize = 21.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (correct) "Bonne réponse" else "Mauvaise réponse",
                    color = accent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("EXPLICATION", color = Q4Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
            if (xp > 0) Text("+$xp XP", color = Q4Purple, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Text(explanation, color = Color(0xFFD6DDF4), fontSize = 14.sp, lineHeight = 21.sp)
    }
}

@Composable
private fun PrimaryButtonV4(text: String, enabled: Boolean, onClick: () -> Unit) {
    val border = if (enabled) Q4Purple else Color(0xFF39415C)
    val start = if (enabled) Color(0xFF6A1DA0) else Color(0xFF171A27)
    val end = if (enabled) Color(0xFF1F1753) else Color(0xFF111521)
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Brush.horizontalGradient(listOf(start, end)), RoundedCornerShape(18.dp))
            .border(1.5.dp, border, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Q4Text else Color(0xFF6D7591),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FinishedQuizCardV4(
    answered: Int,
    correct: Int,
    xp: Int,
    onReplay: () -> Unit,
    onBack: () -> Unit
) {
    val rate = if (answered == 0) 0 else correct * 100 / answered
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF11183A), Color(0xFF071225))), RoundedCornerShape(26.dp))
            .border(1.4.dp, Q4Blue, RoundedCornerShape(26.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FinishShieldV4()
        Text("Session terminée", color = Q4Text, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text("Tu as terminé cette série de questions.", color = Q4Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinishStatV4("Réussite", "$rate%", Modifier.weight(1f))
            FinishStatV4("Réponses", answered.toString(), Modifier.weight(1f))
            FinishStatV4("XP total", xp.toString(), Modifier.weight(1f))
        }
        PrimaryButtonV4("Rejouer", true, onReplay)
        Box(
            Modifier.fillMaxWidth().height(48.dp).border(1.dp, Color(0xFF42547D), RoundedCornerShape(16.dp)).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("Retour", color = Q4Muted, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinishStatV4(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0xFF08152C), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF254A82), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Q4Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = Q4Muted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinishShieldV4() {
    Canvas(Modifier.size(92.dp)) {
        val cx = size.width / 2f
        val shield = Path().apply {
            moveTo(cx, 4f)
            lineTo(size.width * .86f, size.height * .24f)
            lineTo(size.width * .80f, size.height * .66f)
            quadraticBezierTo(size.width * .70f, size.height * .86f, cx, size.height * .97f)
            quadraticBezierTo(size.width * .30f, size.height * .86f, size.width * .20f, size.height * .66f)
            lineTo(size.width * .14f, size.height * .24f)
            close()
        }
        drawPath(shield, brush = Brush.linearGradient(listOf(Q4Purple, Q4Blue)), style = Stroke(5f))
        drawCircle(Q4Green.copy(alpha = .14f), size.minDimension * .22f, Offset(cx, size.height * .48f))
        drawLine(Q4Green, Offset(size.width * .38f, size.height * .49f), Offset(size.width * .47f, size.height * .59f), 6f, StrokeCap.Round)
        drawLine(Q4Green, Offset(size.width * .47f, size.height * .59f), Offset(size.width * .66f, size.height * .37f), 6f, StrokeCap.Round)
    }
}
