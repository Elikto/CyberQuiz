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
import androidx.compose.ui.geometry.Size
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

private val QuizPurple = Color(0xFFD652FF)
private val QuizBlue = Color(0xFF19BFFF)
private val QuizCyan = Color(0xFF19F2E5)
private val QuizGreen = Color(0xFF38E69A)
private val QuizRed = Color(0xFFFF557A)
private val QuizText = Color(0xFFF5F7FF)
private val QuizMuted = Color(0xFF9FAED3)
private val QuizPanel = Color(0xFF081226)

@Composable
fun QuizScreenV2(
    vm: QuizViewModel,
    onBack: () -> Unit
) {
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
        QuizHeader(
            xp = progress.xp,
            streak = progress.streak,
            onBack = onBack
        )

        when (val currentState = state) {
            QuizUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = QuizCyan)
                }
            }

            QuizUiState.Finished -> {
                FinishedQuizCard(
                    answered = progress.answered,
                    correct = progress.correct,
                    xp = progress.xp,
                    onReplay = {
                        selected = null
                        vm.start()
                    },
                    onBack = onBack
                )
            }

            is QuizUiState.Ready -> {
                val q = currentState.question
                val answers = listOf(q.answerA, q.answerB, q.answerC, q.answerD)

                QuestionInfoRow(
                    number = currentState.number,
                    category = q.category,
                    difficulty = q.difficulty
                )

                QuestionCard(question = q.question)

                Text(
                    text = "CHOISIS TA RÉPONSE",
                    color = QuizMuted,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                )

                answers.forEachIndexed { index, answer ->
                    val answerState = when {
                        result == null && selected == index -> AnswerVisualState.Selected
                        result != null && index == q.correctIndex -> AnswerVisualState.Correct
                        result != null && selected == index && index != q.correctIndex -> AnswerVisualState.Wrong
                        else -> AnswerVisualState.Idle
                    }

                    QuizAnswerCard(
                        letter = ('A'.code + index).toChar().toString(),
                        text = answer,
                        state = answerState,
                        enabled = result == null,
                        onClick = { selected = index }
                    )
                }

                if (result == null) {
                    QuizPrimaryButton(
                        text = if (selected == null) "Sélectionne une réponse" else "Valider ma réponse",
                        enabled = selected != null,
                        onClick = { selected?.let(vm::answer) }
                    )
                } else {
                    ExplanationCard(
                        correct = result!!.correct,
                        explanation = result!!.explanation,
                        xp = result!!.xp
                    )

                    QuizPrimaryButton(
                        text = "Question suivante",
                        enabled = true,
                        onClick = {
                            selected = null
                            vm.nextQuestion()
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuizHeader(
    xp: Int,
    streak: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = QuizText, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = QuizText, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = QuizPurple, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Text("MODE QUIZ", color = QuizMuted, fontSize = 9.sp, letterSpacing = 1.8.sp)
        }

        HeaderStat("🔥", streak.toString())
        Spacer(Modifier.width(8.dp))
        HeaderStat("XP", xp.toString())
    }
}

@Composable
private fun HeaderStat(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFF0B1730), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF254B86), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = QuizCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = QuizText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuestionInfoRow(
    number: Int,
    category: String,
    difficulty: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF4C167D), Color(0xFF172659))),
                    RoundedCornerShape(50.dp)
                )
                .border(1.dp, QuizPurple.copy(alpha = .8f), RoundedCornerShape(50.dp))
                .padding(horizontal = 13.dp, vertical = 7.dp)
        ) {
            Text(
                "QUESTION ${number.toString().padStart(2, '0')}",
                color = QuizText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = category.uppercase(),
            color = QuizBlue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        DifficultyChip(difficulty)
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val color = when (difficulty.uppercase()) {
        "HARD" -> QuizRed
        "MEDIUM" -> Color(0xFFFFB84A)
        else -> QuizGreen
    }
    val label = when (difficulty.uppercase()) {
        "HARD" -> "DIFFICILE"
        "MEDIUM" -> "MOYEN"
        else -> "FACILE"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = .10f), RoundedCornerShape(50.dp))
            .border(1.dp, color.copy(alpha = .75f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuestionCard(question: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF111538), Color(0xFF081832), Color(0xFF071023))
                ),
                RoundedCornerShape(24.dp)
            )
            .border(1.3.dp, Color(0xFF3A71D8), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
        ) {
            val c = QuizBlue.copy(alpha = .10f)
            repeat(4) { i ->
                val y = 22f + i * 34f
                drawLine(c, Offset(0f, y), Offset(size.width * .23f, y), 1.5f)
                drawLine(c, Offset(size.width * .77f, y), Offset(size.width, y), 1.5f)
            }
        }

        Text(
            text = question,
            color = QuizText,
            fontSize = 21.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private enum class AnswerVisualState { Idle, Selected, Correct, Wrong }

@Composable
private fun QuizAnswerCard(
    letter: String,
    text: String,
    state: AnswerVisualState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = when (state) {
        AnswerVisualState.Selected -> QuizPurple
        AnswerVisualState.Correct -> QuizGreen
        AnswerVisualState.Wrong -> QuizRed
        AnswerVisualState.Idle -> Color(0xFF365A91)
    }
    val background = when (state) {
        AnswerVisualState.Selected -> Color(0xFF25133D)
        AnswerVisualState.Correct -> Color(0xFF092E2B)
        AnswerVisualState.Wrong -> Color(0xFF321326)
        AnswerVisualState.Idle -> QuizPanel
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(background, Color(0xFF071024))),
                RoundedCornerShape(19.dp)
            )
            .border(if (state == AnswerVisualState.Idle) 1.dp else 1.6.dp, accent, RoundedCornerShape(19.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp))
                .border(1.dp, accent, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.width(13.dp))

        Text(
            text = text,
            color = QuizText,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f)
        )

        when (state) {
            AnswerVisualState.Selected -> Text("●", color = QuizPurple, fontSize = 17.sp)
            AnswerVisualState.Correct -> Text("✓", color = QuizGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
            AnswerVisualState.Wrong -> Text("×", color = QuizRed, fontSize = 23.sp, fontWeight = FontWeight.Black)
            AnswerVisualState.Idle -> Text("›", color = Color(0xFF7081A8), fontSize = 23.sp)
        }
    }
}

@Composable
private fun ExplanationCard(
    correct: Boolean,
    explanation: String,
    xp: Int
) {
    val accent = if (correct) QuizGreen else QuizRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = .12f), Color(0xFF071225))
                ),
                RoundedCornerShape(22.dp)
            )
            .border(1.2.dp, accent.copy(alpha = .8f), RoundedCornerShape(22.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = .15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (correct) "✓" else "×", color = accent, fontSize = 21.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (correct) "Bonne réponse" else "Mauvaise réponse",
                    color = accent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("EXPLICATION", color = QuizMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
            if (xp > 0) {
                Text("+$xp XP", color = QuizPurple, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }

        Text(
            explanation,
            color = Color(0xFFD6DDF4),
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun QuizPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val border = if (enabled) QuizPurple else Color(0xFF39415C)
    val start = if (enabled) Color(0xFF6A1DA0) else Color(0xFF171A27)
    val end = if (enabled) Color(0xFF1F1753) else Color(0xFF111521)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Brush.horizontalGradient(listOf(start, end)), RoundedCornerShape(18.dp))
            .border(1.5.dp, border, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) QuizText else Color(0xFF6D7591),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FinishedQuizCard(
    answered: Int,
    correct: Int,
    xp: Int,
    onReplay: () -> Unit,
    onBack: () -> Unit
) {
    val rate = if (answered == 0) 0 else correct * 100 / answered

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF11183A), Color(0xFF071225))),
                RoundedCornerShape(26.dp)
            )
            .border(1.4.dp, QuizBlue, RoundedCornerShape(26.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuizFinishShield()
        Text("Session terminée", color = QuizText, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text("Tu as terminé cette série de questions.", color = QuizMuted, fontSize = 14.sp, textAlign = TextAlign.Center)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinishStat("Réussite", "$rate%", Modifier.weight(1f))
            FinishStat("Réponses", answered.toString(), Modifier.weight(1f))
            FinishStat("XP total", xp.toString(), Modifier.weight(1f))
        }

        QuizPrimaryButton("Rejouer", true, onReplay)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Color(0xFF42547D), RoundedCornerShape(16.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("Retour", color = QuizMuted, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinishStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF08152C), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF254A82), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = QuizText, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = QuizMuted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun QuizFinishShield() {
    Canvas(modifier = Modifier.size(92.dp)) {
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
        drawPath(shield, brush = Brush.linearGradient(listOf(QuizPurple, QuizBlue)), style = Stroke(5f))
        drawCircle(QuizGreen.copy(alpha = .14f), radius = size.minDimension * .22f, center = Offset(cx, size.height * .48f))
        drawLine(QuizGreen, Offset(size.width * .38f, size.height * .49f), Offset(size.width * .47f, size.height * .59f), 6f, StrokeCap.Round)
        drawLine(QuizGreen, Offset(size.width * .47f, size.height * .59f), Offset(size.width * .66f, size.height * .37f), 6f, StrokeCap.Round)
    }
}
