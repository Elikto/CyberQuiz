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
import androidx.compose.ui.window.Dialog
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

private val Q5Purple = Color(0xFFD652FF)
private val Q5Blue = Color(0xFF19BFFF)
private val Q5Cyan = Color(0xFF19F2E5)
private val Q5Green = Color(0xFF38E69A)
private val Q5Red = Color(0xFFFF557A)
private val Q5Text = Color(0xFFF5F7FF)
private val Q5Muted = Color(0xFF9FAED3)
private val Q5Panel = Color(0xFF081226)

@Composable
fun QuizScreenV5(vm: QuizViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val result by vm.result.collectAsState()
    val progress by vm.progress.collectAsState()
    var selected by remember { mutableStateOf<Int?>(null) }
    var showLearnMore by remember { mutableStateOf(false) }

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
        QuizHeaderV5(onBack)

        when (val s = state) {
            QuizUiState.Loading -> Box(
                Modifier.fillMaxWidth().height(360.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Q5Cyan)
            }

            QuizUiState.Finished -> FinishedQuizCardV5(
                answered = progress.answered,
                correct = progress.correct,
                xp = progress.xp,
                onReplay = {
                    selected = null
                    showLearnMore = false
                    vm.start()
                },
                onBack = onBack
            )

            is QuizUiState.Ready -> {
                val q = s.question
                val answers = listOf(q.answerA, q.answerB, q.answerC, q.answerD)

                ThemeTitleV5(q.category, q.difficulty)
                QuizStatsRowV5(s.number, progress.xp, progress.streak)
                QuestionCardV5(q.question)

                Text(
                    "CHOISIS TA RÉPONSE",
                    color = Q5Muted,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                )

                answers.forEachIndexed { index, answer ->
                    val visual = when {
                        result == null && selected == index -> AnswerStateV5.Selected
                        result != null && index == q.correctIndex -> AnswerStateV5.Correct
                        result != null && selected == index && index != q.correctIndex -> AnswerStateV5.Wrong
                        else -> AnswerStateV5.Idle
                    }
                    AnswerCardV5(
                        letter = ('A'.code + index).toChar().toString(),
                        text = answer,
                        state = visual,
                        enabled = result == null,
                        onClick = { selected = index }
                    )
                }

                if (result == null) {
                    PrimaryButtonV5(
                        text = if (selected == null) "Sélectionne une réponse" else "Valider ma réponse",
                        enabled = selected != null,
                        onClick = { selected?.let(vm::answer) }
                    )
                } else {
                    ExplanationCardV5(
                        correct = result!!.correct,
                        explanation = result!!.explanation,
                        xp = result!!.xp,
                        onLearnMore = { showLearnMore = true }
                    )

                    PrimaryButtonV5("Question suivante", true) {
                        selected = null
                        showLearnMore = false
                        vm.nextQuestion()
                    }

                    if (showLearnMore) {
                        LearnMoreDialogV5(
                            question = q.question,
                            category = q.category,
                            answers = answers,
                            correctIndex = q.correctIndex,
                            explanation = q.explanation,
                            onDismiss = { showLearnMore = false }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuizHeaderV5(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = Q5Text, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = Q5Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = Q5Purple, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Text("MODE QUIZ", color = Q5Muted, fontSize = 9.sp, letterSpacing = 1.8.sp)
        }
    }
}

@Composable
private fun ThemeTitleV5(category: String, difficulty: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("THÈME", color = Q5Muted, fontSize = 9.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(1.dp))
            Text(
                category.uppercase(),
                color = Q5Blue,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .35.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        DifficultyChipV5(difficulty)
    }
}

@Composable
private fun QuizStatsRowV5(number: Int, xp: Int, streak: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        QuestionBadgeV5(number)
        Spacer(Modifier.weight(1f))
        CompactMetricV5("$xp XP", Q5Cyan)
        Spacer(Modifier.width(8.dp))
        CompactMetricV5("🔥 $streak", Color(0xFFFFA83A))
    }
}

@Composable
private fun QuestionBadgeV5(number: Int) {
    Box(
        Modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF4C167D), Color(0xFF172659))),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "QUESTION ${number.toString().padStart(2, '0')}",
            color = Q5Text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.1.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun CompactMetricV5(text: String, accent: Color) {
    Box(
        Modifier
            .background(accent.copy(alpha = .10f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (accent == Q5Cyan) Q5Cyan else Q5Text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun DifficultyChipV5(difficulty: String) {
    val color = when (difficulty.uppercase()) {
        "HARD" -> Q5Red
        "MEDIUM" -> Color(0xFFFFB84A)
        else -> Q5Green
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
private fun QuestionCardV5(question: String) {
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
            val c = Q5Blue.copy(alpha = .10f)
            repeat(4) { i ->
                val y = 22f + i * 34f
                drawLine(c, Offset(0f, y), Offset(size.width * .23f, y), 1.5f)
                drawLine(c, Offset(size.width * .77f, y), Offset(size.width, y), 1.5f)
            }
        }
        Text(
            question,
            color = Q5Text,
            fontSize = 21.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private enum class AnswerStateV5 { Idle, Selected, Correct, Wrong }

@Composable
private fun AnswerCardV5(
    letter: String,
    text: String,
    state: AnswerStateV5,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = when (state) {
        AnswerStateV5.Selected -> Q5Purple
        AnswerStateV5.Correct -> Q5Green
        AnswerStateV5.Wrong -> Q5Red
        AnswerStateV5.Idle -> Color(0xFF365A91)
    }
    val bg = when (state) {
        AnswerStateV5.Selected -> Color(0xFF25133D)
        AnswerStateV5.Correct -> Color(0xFF092E2B)
        AnswerStateV5.Wrong -> Color(0xFF321326)
        AnswerStateV5.Idle -> Q5Panel
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(bg, Color(0xFF071024))), RoundedCornerShape(19.dp))
            .border(if (state == AnswerStateV5.Idle) 1.dp else 1.6.dp, accent, RoundedCornerShape(19.dp))
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
        Text(text, color = Q5Text, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
        when (state) {
            AnswerStateV5.Selected -> Text("●", color = Q5Purple, fontSize = 17.sp)
            AnswerStateV5.Correct -> Text("✓", color = Q5Green, fontSize = 20.sp, fontWeight = FontWeight.Black)
            AnswerStateV5.Wrong -> Text("×", color = Q5Red, fontSize = 23.sp, fontWeight = FontWeight.Black)
            AnswerStateV5.Idle -> Text("›", color = Color(0xFF7081A8), fontSize = 23.sp)
        }
    }
}

@Composable
private fun ExplanationCardV5(
    correct: Boolean,
    explanation: String,
    xp: Int,
    onLearnMore: () -> Unit
) {
    val accent = if (correct) Q5Green else Q5Red
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
                Text("EXPLICATION", color = Q5Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
            if (xp > 0) Text("+$xp XP", color = Q5Purple, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }

        Text(explanation, color = Color(0xFFD6DDF4), fontSize = 14.sp, lineHeight = 21.sp)

        Box(Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = .20f)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLearnMore)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(31.dp).background(Q5Blue.copy(alpha = .12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = Q5Cyan, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("En savoir plus", color = Q5Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Comprendre la réponse ou analyser un choix", color = Q5Muted, fontSize = 10.sp)
            }
            Text("›", color = Q5Cyan, fontSize = 23.sp)
        }
    }
}

@Composable
private fun LearnMoreDialogV5(
    question: String,
    category: String,
    answers: List<String>,
    correctIndex: Int,
    explanation: String,
    onDismiss: () -> Unit
) {
    var selectedChoice by remember(question) { mutableStateOf<Int?>(null) }
    val correctAnswer = answers.getOrElse(correctIndex) { "Réponse correcte" }
    val correctLetter = ('A'.code + correctIndex).toChar().toString()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 690.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF101536), Color(0xFF071327), Color(0xFF050B18))),
                    RoundedCornerShape(26.dp)
                )
                .border(1.4.dp, Q5Blue.copy(alpha = .85f), RoundedCornerShape(26.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).background(Q5Purple.copy(alpha = .16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("i", color = Q5Purple, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("Comprendre la réponse", color = Q5Text, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Text(category.uppercase(), color = Q5Blue, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .background(Color(0xFF121C36), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = Q5Muted, fontSize = 23.sp)
                }
            }

            Text(question, color = Color(0xFFDDE5FA), fontSize = 13.sp, lineHeight = 19.sp)

            LearnSectionTitleV5("LA BONNE RÉPONSE")
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Q5Green.copy(alpha = .09f), RoundedCornerShape(18.dp))
                    .border(1.dp, Q5Green.copy(alpha = .65f), RoundedCornerShape(18.dp))
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(36.dp).background(Q5Green.copy(alpha = .16f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(correctLetter, color = Q5Green, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Text(correctAnswer, color = Q5Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("✓", color = Q5Green, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            LearnSectionTitleV5("POURQUOI ?")
            Text(
                explanation,
                color = Color(0xFFD6DDF4),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF08152B), RoundedCornerShape(17.dp))
                    .padding(14.dp)
            )

            LearnSectionTitleV5("ANALYSER UN CHOIX")
            Text(
                "Touche une proposition pour comprendre son rôle dans cette question.",
                color = Q5Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceInspectButtonV5(0, selectedChoice, correctIndex, Modifier.weight(1f)) { selectedChoice = 0 }
                ChoiceInspectButtonV5(1, selectedChoice, correctIndex, Modifier.weight(1f)) { selectedChoice = 1 }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceInspectButtonV5(2, selectedChoice, correctIndex, Modifier.weight(1f)) { selectedChoice = 2 }
                ChoiceInspectButtonV5(3, selectedChoice, correctIndex, Modifier.weight(1f)) { selectedChoice = 3 }
            }

            selectedChoice?.let { index ->
                val chosen = answers.getOrElse(index) { "" }
                val isCorrect = index == correctIndex
                val accent = if (isCorrect) Q5Green else Q5Red
                val letter = ('A'.code + index).toChar().toString()

                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(accent.copy(alpha = .08f), RoundedCornerShape(18.dp))
                        .border(1.dp, accent.copy(alpha = .48f), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        if (isCorrect) "$letter · Ce choix est correct" else "$letter · Ce choix est incorrect ici",
                        color = accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("« $chosen »", color = Q5Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isCorrect) {
                            explanation
                        } else {
                            "Cette proposition ne répond pas au mécanisme précis demandé par la question. Ici, la réponse attendue est « $correctAnswer ». $explanation"
                        },
                        color = Color(0xFFD3DCF4),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            LearnSectionTitleV5("À RETENIR")
            Text(
                "Associe « $correctAnswer » à cette notion de $category. Pour vérifier que tu as compris, essaie de reformuler la règle avec tes propres mots avant de passer à la question suivante.",
                color = Color(0xFFC9D4F3),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Q5Purple.copy(alpha = .07f), RoundedCornerShape(17.dp))
                    .padding(14.dp)
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF542084), Color(0xFF163D78))), RoundedCornerShape(16.dp))
                    .border(1.dp, Q5Purple, RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("J'ai compris", color = Q5Text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LearnSectionTitleV5(text: String) {
    Text(text, color = Q5Cyan, fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun ChoiceInspectButtonV5(
    index: Int,
    selectedChoice: Int?,
    correctIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val selected = selectedChoice == index
    val accent = when {
        selected && index == correctIndex -> Q5Green
        selected -> Q5Purple
        else -> Color(0xFF385C91)
    }
    val letter = ('A'.code + index).toChar().toString()

    Box(
        modifier
            .height(43.dp)
            .background(accent.copy(alpha = if (selected) .15f else .07f), RoundedCornerShape(13.dp))
            .border(1.dp, accent.copy(alpha = if (selected) .90f else .45f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("Choix $letter", color = if (selected) Q5Text else Q5Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PrimaryButtonV5(text: String, enabled: Boolean, onClick: () -> Unit) {
    val border = if (enabled) Q5Purple else Color(0xFF39415C)
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
            color = if (enabled) Q5Text else Color(0xFF6D7591),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FinishedQuizCardV5(
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
            .border(1.4.dp, Q5Blue, RoundedCornerShape(26.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FinishShieldV5()
        Text("Session terminée", color = Q5Text, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text("Tu as terminé cette série de questions.", color = Q5Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinishStatV5("Réussite", "$rate%", Modifier.weight(1f))
            FinishStatV5("Réponses", answered.toString(), Modifier.weight(1f))
            FinishStatV5("XP total", xp.toString(), Modifier.weight(1f))
        }
        PrimaryButtonV5("Rejouer", true, onReplay)
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Color(0xFF42547D), RoundedCornerShape(16.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("Retour", color = Q5Muted, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinishStatV5(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0xFF08152C), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF254A82), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Q5Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = Q5Muted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinishShieldV5() {
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
        drawPath(shield, brush = Brush.linearGradient(listOf(Q5Purple, Q5Blue)), style = Stroke(5f))
        drawCircle(Q5Green.copy(alpha = .14f), size.minDimension * .22f, Offset(cx, size.height * .48f))
        drawLine(Q5Green, Offset(size.width * .38f, size.height * .49f), Offset(size.width * .47f, size.height * .59f), 6f, StrokeCap.Round)
        drawLine(Q5Green, Offset(size.width * .47f, size.height * .59f), Offset(size.width * .66f, size.height * .37f), 6f, StrokeCap.Round)
    }
}
