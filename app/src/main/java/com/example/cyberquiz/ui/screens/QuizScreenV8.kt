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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

private val Q8Purple = Color(0xFFD652FF)
private val Q8Blue = Color(0xFF19BFFF)
private val Q8Cyan = Color(0xFF19F2E5)
private val Q8Green = Color(0xFF38E69A)
private val Q8Red = Color(0xFFFF557A)
private val Q8Orange = Color(0xFFFFB84A)
private val Q8Text = Color(0xFFF5F7FF)
private val Q8Muted = Color(0xFF9FAED3)
private val Q8Panel = Color(0xFF081226)

@Composable
fun QuizScreenV8(vm: QuizViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val result by vm.result.collectAsState()
    val progress by vm.progress.collectAsState()
    val reviewMode by vm.reviewMode.collectAsState()
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
        QuizHeaderV8(onBack)

        when (val s = state) {
            QuizUiState.Loading -> Box(
                Modifier.fillMaxWidth().height(360.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Q8Cyan)
            }

            QuizUiState.Finished -> FinishedQuizCardV8(
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

                ThemeTitleV8(q.category, q.difficulty)
                QuizStatsRowV8(s.number, progress.xp, progress.streak)
                QuestionCardV8(q.question)

                Text(
                    "CHOISIS TA RÉPONSE",
                    color = Q8Muted,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                )

                answers.forEachIndexed { index, answer ->
                    val visual = when {
                        result == null && selected == index -> AnswerStateV8.Selected
                        result != null && index == q.correctIndex -> AnswerStateV8.Correct
                        result != null && selected == index && index != q.correctIndex -> AnswerStateV8.Wrong
                        else -> AnswerStateV8.Idle
                    }
                    AnswerCardV8(
                        letter = ('A'.code + index).toChar().toString(),
                        text = answer,
                        state = visual,
                        enabled = result == null,
                        onClick = { selected = index }
                    )
                }

                if (result == null) {
                    PrimaryButtonV8(
                        text = if (selected == null) "Sélectionne une réponse" else "Valider ma réponse",
                        enabled = selected != null,
                        onClick = { selected?.let(vm::answer) }
                    )
                } else {
                    ExplanationCardV8(
                        correct = result!!.correct,
                        explanation = result!!.explanation,
                        xp = result!!.xp,
                        onLearnMore = { showLearnMore = true }
                    )

                    if (reviewMode) {
                        PrimaryButtonV8("Retour aux notions à revoir", true) {
                            selected = null
                            showLearnMore = false
                            onBack()
                        }
                    } else {
                        PrimaryButtonV8("Question suivante", true) {
                            selected = null
                            showLearnMore = false
                            vm.nextQuestion()
                        }
                    }

                    if (showLearnMore) {
                        DetailedLearnMoreDialogV8(
                            question = q.question,
                            category = q.category,
                            difficulty = q.difficulty,
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
private fun QuizHeaderV8(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = Q8Text, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = Q8Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = Q8Purple, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Text("MODE QUIZ", color = Q8Muted, fontSize = 9.sp, letterSpacing = 1.8.sp)
        }
    }
}

@Composable
private fun ThemeTitleV8(category: String, difficulty: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("THÈME", color = Q8Muted, fontSize = 9.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(1.dp))
            Text(
                category.uppercase(),
                color = Q8Blue,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .35.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        DifficultyChipV8(difficulty)
    }
}

@Composable
private fun QuizStatsRowV8(number: Int, xp: Int, streak: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                color = Q8Text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                maxLines = 1
            )
        }
        Spacer(Modifier.weight(1f))
        CompactMetricV8("$xp XP", Q8Cyan)
        Spacer(Modifier.width(8.dp))
        CompactMetricV8("🔥 $streak", Q8Orange)
    }
}

@Composable
private fun CompactMetricV8(text: String, accent: Color) {
    Box(
        Modifier
            .background(accent.copy(alpha = .10f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (accent == Q8Cyan) Q8Cyan else Q8Text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun DifficultyChipV8(difficulty: String) {
    val color = when (difficulty.uppercase()) {
        "HARD" -> Q8Red
        "MEDIUM" -> Q8Orange
        else -> Q8Green
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
private fun QuestionCardV8(question: String) {
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
            val c = Q8Blue.copy(alpha = .10f)
            repeat(4) { i ->
                val y = 22f + i * 34f
                drawLine(c, Offset(0f, y), Offset(size.width * .23f, y), 1.5f)
                drawLine(c, Offset(size.width * .77f, y), Offset(size.width, y), 1.5f)
            }
        }
        Text(
            question,
            color = Q8Text,
            fontSize = 21.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private enum class AnswerStateV8 { Idle, Selected, Correct, Wrong }

@Composable
private fun AnswerCardV8(
    letter: String,
    text: String,
    state: AnswerStateV8,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = when (state) {
        AnswerStateV8.Selected -> Q8Purple
        AnswerStateV8.Correct -> Q8Green
        AnswerStateV8.Wrong -> Q8Red
        AnswerStateV8.Idle -> Color(0xFF365A91)
    }
    val bg = when (state) {
        AnswerStateV8.Selected -> Color(0xFF25133D)
        AnswerStateV8.Correct -> Color(0xFF092E2B)
        AnswerStateV8.Wrong -> Color(0xFF321326)
        AnswerStateV8.Idle -> Q8Panel
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(bg, Color(0xFF071024))), RoundedCornerShape(19.dp))
            .border(if (state == AnswerStateV8.Idle) 1.dp else 1.6.dp, accent, RoundedCornerShape(19.dp))
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
        Text(text, color = Q8Text, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
        when (state) {
            AnswerStateV8.Selected -> Text("●", color = Q8Purple, fontSize = 17.sp)
            AnswerStateV8.Correct -> Text("✓", color = Q8Green, fontSize = 20.sp, fontWeight = FontWeight.Black)
            AnswerStateV8.Wrong -> Text("×", color = Q8Red, fontSize = 23.sp, fontWeight = FontWeight.Black)
            AnswerStateV8.Idle -> Text("›", color = Color(0xFF7081A8), fontSize = 23.sp)
        }
    }
}

@Composable
private fun ExplanationCardV8(
    correct: Boolean,
    explanation: String,
    xp: Int,
    onLearnMore: () -> Unit
) {
    val accent = if (correct) Q8Green else Q8Red
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
                Text("EXPLICATION", color = Q8Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
            if (xp > 0) Text("+$xp XP", color = Q8Purple, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }

        Text(explanation, color = Color(0xFFD6DDF4), fontSize = 14.sp, lineHeight = 21.sp)
        Box(Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = .20f)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLearnMore)
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(32.dp).background(Q8Blue.copy(alpha = .12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = Q8Cyan, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("En savoir plus", color = Q8Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Comprendre tous les choix puis approfondir celui que tu veux", color = Q8Muted, fontSize = 10.sp)
            }
            Text("›", color = Q8Cyan, fontSize = 23.sp)
        }
    }
}

@Composable
private fun DetailedLearnMoreDialogV8(
    question: String,
    category: String,
    difficulty: String,
    answers: List<String>,
    correctIndex: Int,
    explanation: String,
    onDismiss: () -> Unit
) {
    var selectedChoice by remember(question) { mutableStateOf<Int?>(null) }
    var courseTerm by remember(question) { mutableStateOf<String?>(null) }
    val correctAnswer = answers.getOrElse(correctIndex) { "Réponse correcte" }
    val correctLetter = ('A'.code + correctIndex).toChar().toString()
    val beginnerMode = difficulty.uppercase() == "EASY"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF101536), Color(0xFF071327), Color(0xFF050B18))),
                    RoundedCornerShape(26.dp)
                )
                .border(1.4.dp, Q8Blue.copy(alpha = .85f), RoundedCornerShape(26.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).background(Q8Purple.copy(alpha = .16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("i", color = Q8Purple, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("Comprendre tous les choix", color = Q8Text, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Text(category.uppercase(), color = Q8Blue, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .background(Color(0xFF121C36), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = Q8Muted, fontSize = 23.sp)
                }
            }

            Text(question, color = Color(0xFFDDE5FA), fontSize = 13.sp, lineHeight = 19.sp)

            LearnSectionTitleV8("LA BONNE RÉPONSE")
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Q8Green.copy(alpha = .09f), RoundedCornerShape(18.dp))
                    .border(1.dp, Q8Green.copy(alpha = .65f), RoundedCornerShape(18.dp))
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(36.dp).background(Q8Green.copy(alpha = .16f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(correctLetter, color = Q8Green, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Text(correctAnswer, color = Q8Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("✓", color = Q8Green, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            LearnSectionTitleV8("COMPRENDRE CHAQUE PROPOSITION")
            Text(
                if (beginnerMode) {
                    "Question facile : chaque choix est expliqué avec des mots simples. Lis d'abord ce que c'est, puis à quoi ça sert, avant de regarder pourquoi il correspond ou non à cette question."
                } else {
                    "Résumé rapide : ce que signifie chaque terme et à quoi il sert. Sélectionne ensuite un choix si tu veux vraiment l'étudier."
                },
                color = Q8Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            answers.forEachIndexed { index, answer ->
                ChoiceDefinitionCardV8(
                    index = index,
                    answer = answer,
                    question = question,
                    category = category,
                    beginnerMode = beginnerMode,
                    correctAnswer = correctAnswer,
                    correct = index == correctIndex
                )
            }

            LearnSectionTitleV8("ANALYSER UNE RÉPONSE")
            Text(
                "Choisis A, B, C ou D. L'explication ci-dessous concerne uniquement cette proposition, qu'elle soit bonne ou mauvaise.",
                color = Q8Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { index ->
                    ChoiceInspectButtonV8(
                        index = index,
                        selectedChoice = selectedChoice,
                        correctIndex = correctIndex,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedChoice = index }
                    )
                }
            }

            if (selectedChoice == null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Q8Blue.copy(alpha = .06f), RoundedCornerShape(16.dp))
                        .border(1.dp, Q8Blue.copy(alpha = .25f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "Sélectionne un choix pour afficher sa définition complète, ses actions, son fonctionnement, un exemple, un terminal, une analogie et son schéma.",
                        color = Color(0xFFC8D4F2),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            selectedChoice?.let { index ->
                val chosen = answers.getOrElse(index) { "" }
                val isCorrect = index == correctIndex
                val letter = ('A'.code + index).toChar().toString()
                val deepDive = remember(chosen, category) { buildCyberChoiceDeepDive(chosen, category) }

                LearnSectionTitleV8("EXPLICATION DÉTAILLÉE · CHOIX $letter")

                if (beginnerMode) {
                    val beginner = remember(question, chosen, category, correctAnswer, isCorrect) {
                        buildBeginnerChoiceExplanation(
                            question = question,
                            term = chosen,
                            category = category,
                            correctAnswer = correctAnswer,
                            isCorrect = isCorrect
                        )
                    }
                    BeginnerChoiceClarityV8(beginner)
                }

                DetailedChoiceCardV8(chosen, deepDive)

                if (hasCyberExpertCourse(chosen)) {
                    OpenExpertCourseButtonV8(chosen) { courseTerm = chosen }
                }

                LearnSectionTitleV8("SCHÉMA · $chosen")
                Text(
                    "Lis le schéma du haut vers le bas : chaque bloc représente une étape ou une action liée à la notion.",
                    color = Q8Muted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                LearningSchemaV8(deepDive.schema)

                LearnSectionTitleV8("CONCLUSION POUR CETTE QUESTION")
                val conclusionAccent = if (isCorrect) Q8Green else Q8Orange
                Text(
                    buildCyberChoiceConclusion(
                        chosen = chosen,
                        category = category,
                        correctAnswer = correctAnswer,
                        isCorrect = isCorrect,
                        explanation = explanation
                    ),
                    color = Color(0xFFD7E0F7),
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(conclusionAccent.copy(alpha = .08f), RoundedCornerShape(17.dp))
                        .border(1.dp, conclusionAccent.copy(alpha = .32f), RoundedCornerShape(17.dp))
                        .padding(14.dp)
                )
            }

            LearnSectionTitleV8("À RETENIR")
            Text(
                "Le but est de comprendre les quatre notions. Si l'explication détaillée d'un terme ne suffit pas, ouvre son cours pour repartir de zéro et mieux comprendre la notion.",
                color = Color(0xFFC9D4F3),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Q8Purple.copy(alpha = .07f), RoundedCornerShape(17.dp))
                    .padding(14.dp)
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF542084), Color(0xFF163D78))), RoundedCornerShape(16.dp))
                    .border(1.dp, Q8Purple, RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("J'ai compris", color = Q8Text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    courseTerm?.let { term ->
        CyberCourseDialog(
            term = term,
            category = category,
            onDismiss = { courseTerm = null }
        )
    }
}

@Composable
private fun OpenExpertCourseButtonV8(term: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF30104D), Color(0xFF0C2C55))),
                RoundedCornerShape(18.dp)
            )
            .border(1.3.dp, Q8Cyan.copy(alpha = .72f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("TOUJOURS PAS COMPRIS ?", color = Q8Muted, fontSize = 8.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Black)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ouvrir le cours · $term", color = Q8Cyan, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("›", color = Q8Cyan, fontSize = 24.sp)
        }
        Text(
            "Repartir de zéro avec une explication simple, des schémas et des exemples concrets.",
            color = Color(0xFFC5D2ED),
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ChoiceDefinitionCardV8(
    index: Int,
    answer: String,
    question: String,
    category: String,
    beginnerMode: Boolean,
    correctAnswer: String,
    correct: Boolean
) {
    val accent = if (correct) Q8Green else Q8Blue
    val letter = ('A'.code + index).toChar().toString()
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(17.dp))
            .border(1.dp, accent.copy(alpha = if (correct) .65f else .30f), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(31.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(letter, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(9.dp))
            Text(answer, color = Q8Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (correct) Text("BONNE", color = Q8Green, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        if (beginnerMode) {
            val beginner = remember(question, answer, category, correctAnswer, correct) {
                buildBeginnerChoiceExplanation(
                    question = question,
                    term = answer,
                    category = category,
                    correctAnswer = correctAnswer,
                    isCorrect = correct
                )
            }
            BeginnerMiniSectionV8("CE QUE C'EST", beginner.what, Q8Cyan)
            BeginnerMiniSectionV8("À QUOI ÇA SERT", beginner.purpose, Q8Blue)
            Text(
                beginner.distinction,
                color = if (correct) Color(0xFFC9F5DD) else Color(0xFFFFD8A2),
                fontSize = 11.sp,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        (if (correct) Q8Green else Q8Orange).copy(alpha = .07f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            )
        } else {
            Text(
                summarizeCyberChoice(answer, category),
                color = Color(0xFFC9D4F0),
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun BeginnerMiniSectionV8(title: String, body: String, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
        Text(body, color = Color(0xFFD6DFF5), fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun BeginnerChoiceClarityV8(explanation: BeginnerChoiceExplanation) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Q8Green.copy(alpha = .055f), RoundedCornerShape(17.dp))
            .border(1.dp, Q8Green.copy(alpha = .30f), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text("D'ABORD, EN CLAIR", color = Q8Green, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        BeginnerMiniSectionV8("CE QUE C'EST", explanation.what, Q8Cyan)
        BeginnerMiniSectionV8("À QUOI ÇA SERT", explanation.purpose, Q8Blue)
        BeginnerMiniSectionV8("DANS CETTE QUESTION", explanation.distinction, Q8Orange)
    }
}

@Composable
private fun ChoiceInspectButtonV8(
    index: Int,
    selectedChoice: Int?,
    correctIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val selected = selectedChoice == index
    val accent = when {
        selected && index == correctIndex -> Q8Green
        selected -> Q8Purple
        else -> Color(0xFF385C91)
    }
    val letter = ('A'.code + index).toChar().toString()
    Box(
        modifier
            .height(42.dp)
            .background(accent.copy(alpha = if (selected) .15f else .07f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = if (selected) .90f else .45f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, color = if (selected) Q8Text else Q8Muted, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DetailedChoiceCardV8(term: String, lesson: CyberChoiceDeepDive) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(18.dp))
            .border(1.dp, Q8Cyan.copy(alpha = .35f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(term, color = Q8Text, fontSize = 17.sp, fontWeight = FontWeight.Black)
        LessonParagraphV8("1 · DÉFINITION", lesson.definition, Q8Cyan)
        LessonParagraphV8("2 · À QUOI ÇA SERT ?", lesson.action, Q8Blue)
        ActionListV8(lesson.actions)
        LessonParagraphV8("3 · COMMENT ÇA FONCTIONNE ?", lesson.details, Q8Purple)
        LessonParagraphV8("4 · EXEMPLE CONCRET", lesson.example, Q8Green)
        CodeDemoCardV8(lesson.demoTitle, lesson.demo)
        AnalogyCardV8(lesson.analogy)
    }
}

@Composable
private fun ActionListV8(actions: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Q8Blue.copy(alpha = .055f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("ACTIONS CONCRÈTES", color = Q8Blue, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        actions.forEach { action ->
            Row(verticalAlignment = Alignment.Top) {
                Text("•", color = Q8Cyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(7.dp))
                Text(action, color = Color(0xFFD3DCF4), fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CodeDemoCardV8(title: String, code: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF030812), RoundedCornerShape(15.dp))
            .border(1.dp, Q8Green.copy(alpha = .35f), RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("●", color = Q8Red, fontSize = 8.sp)
            Spacer(Modifier.width(4.dp))
            Text("●", color = Q8Orange, fontSize = 8.sp)
            Spacer(Modifier.width(4.dp))
            Text("●", color = Q8Green, fontSize = 8.sp)
            Spacer(Modifier.width(9.dp))
            Text(title, color = Q8Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
        }
        Text(code, color = Color(0xFFB8F7DA), fontSize = 11.sp, lineHeight = 17.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun AnalogyCardV8(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Q8Orange.copy(alpha = .06f), RoundedCornerShape(15.dp))
            .border(1.dp, Q8Orange.copy(alpha = .28f), RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text("💡 ANALOGIE POUR MÉMORISER", color = Q8Orange, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(text, color = Color(0xFFD9E0F4), fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun LessonParagraphV8(title: String, body: String, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Text(body, color = Color(0xFFD6DFF5), fontSize = 12.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun LearningSchemaV8(steps: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF061225), RoundedCornerShape(18.dp))
            .border(1.dp, Q8Blue.copy(alpha = .32f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        steps.forEachIndexed { index, step ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Q8Purple.copy(alpha = .10f), Q8Blue.copy(alpha = .10f))),
                        RoundedCornerShape(13.dp)
                    )
                    .border(1.dp, Q8Blue.copy(alpha = .28f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(step, color = Q8Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
            if (index < steps.lastIndex) Text("↓", color = Q8Cyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LearnSectionTitleV8(text: String) {
    Text(text, color = Q8Cyan, fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun PrimaryButtonV8(text: String, enabled: Boolean, onClick: () -> Unit) {
    val border = if (enabled) Q8Purple else Color(0xFF39415C)
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
        Text(text, color = if (enabled) Q8Text else Color(0xFF6D7591), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FinishedQuizCardV8(
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
            .border(1.4.dp, Q8Blue, RoundedCornerShape(26.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FinishShieldV8()
        Text("Session terminée", color = Q8Text, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text("Tu as terminé cette série de questions.", color = Q8Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FinishStatV8("Réussite", "$rate%", Modifier.weight(1f))
            FinishStatV8("Réponses", answered.toString(), Modifier.weight(1f))
            FinishStatV8("XP total", xp.toString(), Modifier.weight(1f))
        }
        PrimaryButtonV8("Rejouer", true, onReplay)
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Color(0xFF42547D), RoundedCornerShape(16.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("Retour", color = Q8Muted, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinishStatV8(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0xFF08152C), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF254A82), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Q8Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = Q8Muted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinishShieldV8() {
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
        drawPath(shield, brush = Brush.linearGradient(listOf(Q8Purple, Q8Blue)), style = Stroke(5f))
        drawCircle(Q8Green.copy(alpha = .14f), size.minDimension * .22f, Offset(cx, size.height * .48f))
        drawLine(Q8Green, Offset(size.width * .38f, size.height * .49f), Offset(size.width * .47f, size.height * .59f), 6f, StrokeCap.Round)
        drawLine(Q8Green, Offset(size.width * .47f, size.height * .59f), Offset(size.width * .66f, size.height * .37f), 6f, StrokeCap.Round)
    }
}
