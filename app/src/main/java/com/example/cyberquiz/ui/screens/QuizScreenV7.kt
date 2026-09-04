package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

private val CourseLauncherPurple = Color(0xFFD652FF)
private val CourseLauncherBlue = Color(0xFF19BFFF)
private val CourseLauncherCyan = Color(0xFF19F2E5)
private val CourseLauncherText = Color(0xFFF5F7FF)
private val CourseLauncherMuted = Color(0xFF9FAED3)

@Composable
fun QuizScreenV7(vm: QuizViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val result by vm.result.collectAsState()
    var showCoursePicker by remember { mutableStateOf(false) }
    var courseTerm by remember { mutableStateOf<String?>(null) }
    var courseCategory by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        QuizScreenV6(vm = vm, onBack = onBack)

        val ready = state as? QuizUiState.Ready
        if (result != null && ready != null) {
            CourseLauncherButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 82.dp),
                onClick = { showCoursePicker = true }
            )
        }
    }

    val ready = state as? QuizUiState.Ready
    if (showCoursePicker && ready != null) {
        val q = ready.question
        CourseChoicePickerDialog(
            answers = listOf(q.answerA, q.answerB, q.answerC, q.answerD),
            category = q.category,
            correctIndex = q.correctIndex,
            onChoice = { term ->
                courseTerm = term
                courseCategory = q.category
                showCoursePicker = false
            },
            onDismiss = { showCoursePicker = false }
        )
    }

    val selectedTerm = courseTerm
    val selectedCategory = courseCategory
    if (selectedTerm != null && selectedCategory != null) {
        CyberCourseDialog(
            term = selectedTerm,
            category = selectedCategory,
            onDismiss = {
                courseTerm = null
                courseCategory = null
            }
        )
    }
}

@Composable
private fun CourseLauncherButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF4E1B78), Color(0xFF123D72))
                ),
                RoundedCornerShape(50.dp)
            )
            .border(1.2.dp, CourseLauncherCyan.copy(alpha = .75f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .background(CourseLauncherCyan.copy(alpha = .12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("C", color = CourseLauncherCyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                "Toujours pas compris ?",
                color = CourseLauncherMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ouvrir un cours",
                color = CourseLauncherText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun CourseChoicePickerDialog(
    answers: List<String>,
    category: String,
    correctIndex: Int,
    onChoice: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF101536), Color(0xFF071327), Color(0xFF050B18))
                    ),
                    RoundedCornerShape(26.dp)
                )
                .border(1.3.dp, CourseLauncherBlue.copy(alpha = .75f), RoundedCornerShape(26.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Ouvrir un cours", color = CourseLauncherText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        "Choisis la notion que tu veux reprendre depuis zéro",
                        color = CourseLauncherMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .background(Color(0xFF121C36), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = CourseLauncherMuted, fontSize = 23.sp)
                }
            }

            Text(
                category.uppercase(),
                color = CourseLauncherBlue,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.3.sp
            )

            answers.forEachIndexed { index, answer ->
                val correct = index == correctIndex
                val accent = if (correct) Color(0xFF38E69A) else CourseLauncherBlue
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF08152B), RoundedCornerShape(17.dp))
                        .border(1.dp, accent.copy(alpha = .38f), RoundedCornerShape(17.dp))
                        .clickable { onChoice(answer) }
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(accent.copy(alpha = .12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ('A'.code + index).toChar().toString(),
                            color = accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(answer, color = CourseLauncherText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            summarizeCyberChoice(answer, category),
                            color = CourseLauncherMuted,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            maxLines = 2
                        )
                    }
                    Text("›", color = CourseLauncherCyan, fontSize = 22.sp)
                }
            }

            Text(
                "Tu peux ouvrir un cours sur une mauvaise réponse aussi : le but est de comprendre les quatre notions, pas seulement de retenir la bonne lettre.",
                color = Color(0xFFC7D2EE),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CourseLauncherPurple.copy(alpha = .07f), RoundedCornerShape(14.dp))
                    .padding(11.dp)
            )
        }
    }
}
