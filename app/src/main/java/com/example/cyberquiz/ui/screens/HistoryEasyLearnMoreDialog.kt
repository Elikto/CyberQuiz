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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val HLearnPurple = Color(0xFFD652FF)
private val HLearnBlue = Color(0xFF19BFFF)
private val HLearnCyan = Color(0xFF19F2E5)
private val HLearnGreen = Color(0xFF38E69A)
private val HLearnOrange = Color(0xFFFFB84A)
private val HLearnText = Color(0xFFF5F7FF)
private val HLearnMuted = Color(0xFF9FAED3)

@Composable
internal fun HistoryEasyLearnMoreDialogV8(
    question: String,
    category: String,
    answers: List<String>,
    correctIndex: Int,
    explanation: String,
    onDismiss: () -> Unit
) {
    var selectedChoice by remember(question) { mutableStateOf<Int?>(null) }
    var courseTerm by remember(question) { mutableStateOf<String?>(null) }
    val correctAnswer = answers.getOrElse(correctIndex) { "Réponse correcte" }
    val correctLetter = ('A'.code + correctIndex).toChar().toString()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF101536), Color(0xFF071327), Color(0xFF050B18))
                    ),
                    RoundedCornerShape(26.dp)
                )
                .border(1.4.dp, HLearnBlue.copy(alpha = .85f), RoundedCornerShape(26.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .background(HLearnPurple.copy(alpha = .16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("i", color = HLearnPurple, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Comprendre tous les choix",
                        color = HLearnText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "HISTORIQUE · QUESTION FACILE · ${category.uppercase()}",
                        color = HLearnBlue,
                        fontSize = 8.sp,
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .background(Color(0xFF121C36), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = HLearnMuted, fontSize = 23.sp)
                }
            }

            Text(question, color = Color(0xFFDDE5FA), fontSize = 13.sp, lineHeight = 19.sp)

            HistoryLearnSectionTitle("LA BONNE RÉPONSE")
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(HLearnGreen.copy(alpha = .09f), RoundedCornerShape(18.dp))
                    .border(1.dp, HLearnGreen.copy(alpha = .65f), RoundedCornerShape(18.dp))
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(HLearnGreen.copy(alpha = .16f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(correctLetter, color = HLearnGreen, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Text(
                    correctAnswer,
                    color = HLearnText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text("✓", color = HLearnGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            HistoryLearnSectionTitle("COMPRENDRE CHAQUE PROPOSITION")
            Text(
                "Chaque choix est repris avec des mots simples : ce que c'est, à quoi ça sert et pourquoi il correspond ou non à cette question.",
                color = HLearnMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            answers.forEachIndexed { index, answer ->
                val beginner = remember(question, answer, category, correctAnswer, index) {
                    buildBeginnerChoiceExplanation(
                        question = question,
                        term = answer,
                        category = category,
                        correctAnswer = correctAnswer,
                        isCorrect = index == correctIndex
                    )
                }
                HistoryChoiceDefinitionCard(
                    index = index,
                    answer = answer,
                    explanation = beginner,
                    correct = index == correctIndex
                )
            }

            HistoryLearnSectionTitle("ANALYSER UNE RÉPONSE")
            Text(
                "Choisis A, B, C ou D pour approfondir une proposition précise avec son fonctionnement, un exemple, un schéma et une démonstration sûre.",
                color = HLearnMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { index ->
                    HistoryChoiceInspectButton(
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
                        .background(HLearnBlue.copy(alpha = .06f), RoundedCornerShape(16.dp))
                        .border(1.dp, HLearnBlue.copy(alpha = .25f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "Sélectionne un choix pour afficher son explication complète.",
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
                val beginner = remember(question, chosen, category, correctAnswer, isCorrect) {
                    buildBeginnerChoiceExplanation(
                        question = question,
                        term = chosen,
                        category = category,
                        correctAnswer = correctAnswer,
                        isCorrect = isCorrect
                    )
                }

                HistoryLearnSectionTitle("EXPLICATION DÉTAILLÉE · CHOIX $letter")
                HistoryBeginnerClarity(beginner)
                HistoryDetailedChoiceCard(chosen, deepDive)

                if (hasCyberExpertCourse(chosen)) {
                    HistoryOpenCourseButton(chosen) { courseTerm = chosen }
                }

                HistoryLearnSectionTitle("SCHÉMA · $chosen")
                Text(
                    "Lis le schéma du haut vers le bas : chaque bloc représente une étape ou une action liée à cette notion.",
                    color = HLearnMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                HistoryLearningSchema(deepDive.schema)

                HistoryLearnSectionTitle("CONCLUSION POUR CETTE QUESTION")
                val conclusionAccent = if (isCorrect) HLearnGreen else HLearnOrange
                val conclusion = if (isCorrect) {
                    "« $chosen » est bien la bonne réponse. $explanation"
                } else {
                    "« $chosen » n'était pas la bonne réponse ici. La réponse attendue était « $correctAnswer ». $explanation"
                }
                Text(
                    conclusion,
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

            HistoryLearnSectionTitle("À RETENIR")
            Text(
                "Le but est de comprendre les quatre choix, pas seulement de mémoriser la bonne lettre. Tu peux ouvrir le cours d'une notion lorsqu'il est disponible.",
                color = Color(0xFFC9D4F3),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HLearnPurple.copy(alpha = .07f), RoundedCornerShape(17.dp))
                    .padding(14.dp)
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF542084), Color(0xFF163D78))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, HLearnPurple, RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("J'ai compris", color = HLearnText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
private fun HistoryChoiceDefinitionCard(
    index: Int,
    answer: String,
    explanation: BeginnerChoiceExplanation,
    correct: Boolean
) {
    val accent = if (correct) HLearnGreen else HLearnBlue
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
                Modifier
                    .size(31.dp)
                    .background(accent.copy(alpha = .14f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(letter, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(9.dp))
            Text(answer, color = HLearnText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (correct) {
                Text("BONNE", color = HLearnGreen, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }

        HistoryBeginnerMiniSection("CE QUE C'EST", explanation.what, HLearnCyan)
        HistoryBeginnerMiniSection("À QUOI ÇA SERT", explanation.purpose, HLearnBlue)
        Text(
            explanation.distinction,
            color = if (correct) Color(0xFFC9F5DD) else Color(0xFFFFD8A2),
            fontSize = 11.sp,
            lineHeight = 17.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    (if (correct) HLearnGreen else HLearnOrange).copy(alpha = .07f),
                    RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
        )
    }
}

@Composable
private fun HistoryBeginnerMiniSection(title: String, body: String, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
        Text(body, color = Color(0xFFD6DFF5), fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun HistoryBeginnerClarity(explanation: BeginnerChoiceExplanation) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HLearnGreen.copy(alpha = .055f), RoundedCornerShape(17.dp))
            .border(1.dp, HLearnGreen.copy(alpha = .30f), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text("D'ABORD, EN CLAIR", color = HLearnGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        HistoryBeginnerMiniSection("CE QUE C'EST", explanation.what, HLearnCyan)
        HistoryBeginnerMiniSection("À QUOI ÇA SERT", explanation.purpose, HLearnBlue)
        HistoryBeginnerMiniSection("DANS CETTE QUESTION", explanation.distinction, HLearnOrange)
    }
}

@Composable
private fun HistoryChoiceInspectButton(
    index: Int,
    selectedChoice: Int?,
    correctIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val selected = selectedChoice == index
    val accent = when {
        selected && index == correctIndex -> HLearnGreen
        selected -> HLearnPurple
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
        Text(letter, color = if (selected) HLearnText else HLearnMuted, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun HistoryDetailedChoiceCard(term: String, lesson: CyberChoiceDeepDive) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(18.dp))
            .border(1.dp, HLearnCyan.copy(alpha = .35f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(term, color = HLearnText, fontSize = 17.sp, fontWeight = FontWeight.Black)
        HistoryLessonParagraph("1 · DÉFINITION", lesson.definition, HLearnCyan)
        HistoryLessonParagraph("2 · À QUOI ÇA SERT ?", lesson.action, HLearnBlue)
        HistoryActionList(lesson.actions)
        HistoryLessonParagraph("3 · COMMENT ÇA FONCTIONNE ?", lesson.details, HLearnPurple)
        HistoryLessonParagraph("4 · EXEMPLE CONCRET", lesson.example, HLearnGreen)
        HistoryCodeDemoCard(lesson.demoTitle, lesson.demo)
        HistoryAnalogyCard(lesson.analogy)
    }
}

@Composable
private fun HistoryActionList(actions: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HLearnBlue.copy(alpha = .055f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("ACTIONS CONCRÈTES", color = HLearnBlue, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        actions.forEach { action ->
            Row(verticalAlignment = Alignment.Top) {
                Text("•", color = HLearnCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(7.dp))
                Text(action, color = Color(0xFFD3DCF4), fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HistoryCodeDemoCard(title: String, code: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF030812), RoundedCornerShape(15.dp))
            .border(1.dp, HLearnGreen.copy(alpha = .35f), RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, color = HLearnMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
        Text(code, color = Color(0xFFB8F7DA), fontSize = 11.sp, lineHeight = 17.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun HistoryAnalogyCard(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HLearnOrange.copy(alpha = .06f), RoundedCornerShape(15.dp))
            .border(1.dp, HLearnOrange.copy(alpha = .28f), RoundedCornerShape(15.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text("💡 ANALOGIE POUR MÉMORISER", color = HLearnOrange, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(text, color = Color(0xFFD9E0F4), fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun HistoryLessonParagraph(title: String, body: String, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Text(body, color = Color(0xFFD6DFF5), fontSize = 12.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun HistoryLearningSchema(steps: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF061225), RoundedCornerShape(18.dp))
            .border(1.dp, HLearnBlue.copy(alpha = .32f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        steps.forEachIndexed { index, step ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(HLearnPurple.copy(alpha = .10f), HLearnBlue.copy(alpha = .10f))
                        ),
                        RoundedCornerShape(13.dp)
                    )
                    .border(1.dp, HLearnBlue.copy(alpha = .28f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    step,
                    color = HLearnText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            if (index < steps.lastIndex) {
                Text("↓", color = HLearnCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun HistoryOpenCourseButton(term: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF30104D), Color(0xFF0C2C55))),
                RoundedCornerShape(18.dp)
            )
            .border(1.3.dp, HLearnCyan.copy(alpha = .72f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("TOUJOURS PAS COMPRIS ?", color = HLearnMuted, fontSize = 8.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Black)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ouvrir le cours · $term", color = HLearnCyan, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("›", color = HLearnCyan, fontSize = 24.sp)
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
private fun HistoryLearnSectionTitle(text: String) {
    Text(text, color = HLearnCyan, fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
}
