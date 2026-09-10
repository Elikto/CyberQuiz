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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.model.LEARNING_PATH_MIN_ANSWERS
import com.example.cyberquiz.model.LEARNING_PATH_REQUIRED_ACCURACY
import com.example.cyberquiz.model.LearningPathProgress
import com.example.cyberquiz.model.LearningPathStepProgress
import com.example.cyberquiz.model.buildLearningPathProgress
import com.example.cyberquiz.viewmodel.QuizViewModel

private val PathBg = Color(0xFF030712)
private val PathCard = Color(0xFF08142A)
private val PathText = Color(0xFFF5F7FF)
private val PathMuted = Color(0xFF9FAED3)
private val PathCyan = Color(0xFF19F2E5)
private val PathBlue = Color(0xFF19BFFF)
private val PathPurple = Color(0xFFD652FF)
private val PathGreen = Color(0xFF38E69A)
private val PathOrange = Color(0xFFFFB84A)

@Composable
internal fun LearningPathsScreen(
    vm: QuizViewModel,
    onLaunchCategory: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val categoryProgress by vm.categoryProgress.collectAsState()
    val paths = buildLearningPathProgress(categoryProgress = categoryProgress)
    val totalSteps = paths.sumOf { it.steps.size }
    val completedSteps = paths.sumOf { it.completedSteps }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PathBg, Color(0xFF07142B), Color(0xFF120921))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF111C34), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = PathCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("PARCOURS GUIDÉS", color = PathCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
                Text("Ta route vers la maîtrise", color = PathText, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Box(
                Modifier
                    .background(PathPurple.copy(alpha = .12f), RoundedCornerShape(50.dp))
                    .border(1.dp, PathPurple.copy(alpha = .55f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text("$completedSteps/$totalSteps", color = PathPurple, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }

        Text(
            "Chaque parcours avance étape par étape. Une étape est maîtrisée après au moins $LEARNING_PATH_MIN_ANSWERS réponses dans sa catégorie avec $LEARNING_PATH_REQUIRED_ACCURACY % de réussite ou plus.",
            color = PathMuted,
            fontSize = 11.sp,
            lineHeight = 17.sp
        )

        paths.forEach { path ->
            LearningPathCard(path = path, onLaunchCategory = onLaunchCategory)
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun LearningPathCard(
    path: LearningPathProgress,
    onLaunchCategory: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(PathPurple.copy(alpha = .08f), PathCard, PathBlue.copy(alpha = .05f))
                ),
                RoundedCornerShape(22.dp)
            )
            .border(
                1.2.dp,
                if (path.completed) PathGreen.copy(alpha = .70f) else PathBlue.copy(alpha = .35f),
                RoundedCornerShape(22.dp)
            )
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(
                        (if (path.completed) PathGreen else PathPurple).copy(alpha = .14f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (path.completed) "✓" else "◎", color = if (path.completed) PathGreen else PathPurple, fontSize = 19.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(path.definition.title, color = PathText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(path.definition.subtitle, color = PathMuted, fontSize = 10.sp, lineHeight = 15.sp)
            }
            Text("${path.progressPercent}%", color = if (path.completed) PathGreen else PathCyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }

        PathProgressBar(path.progressPercent)

        path.steps.forEachIndexed { index, step ->
            PathStepCard(
                step = step,
                isLast = index == path.steps.lastIndex,
                onLaunch = { onLaunchCategory(step.step.category) }
            )
        }
    }
}

@Composable
private fun PathProgressBar(percent: Int) {
    val fraction = (percent.coerceIn(0, 100) / 100f)
    Box(
        Modifier
            .fillMaxWidth()
            .height(7.dp)
            .background(Color(0xFF152541), RoundedCornerShape(50.dp))
    ) {
        if (fraction > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(7.dp)
                    .background(
                        Brush.horizontalGradient(listOf(PathPurple, PathBlue, PathCyan)),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
private fun PathStepCard(
    step: LearningPathStepProgress,
    isLast: Boolean,
    onLaunch: () -> Unit
) {
    val accent = when {
        step.completed -> PathGreen
        step.unlocked -> PathOrange
        else -> Color(0xFF566582)
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent.copy(alpha = if (step.unlocked) .08f else .035f), RoundedCornerShape(16.dp))
                .border(1.dp, accent.copy(alpha = if (step.unlocked) .42f else .18f), RoundedCornerShape(16.dp))
                .clickable(enabled = step.unlocked, onClick = onLaunch)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = .13f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        step.completed -> "✓"
                        step.unlocked -> "▶"
                        else -> "•"
                    },
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(step.step.title, color = if (step.unlocked) PathText else PathMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(step.step.objective, color = PathMuted, fontSize = 9.5.sp, lineHeight = 14.sp)
                Text(
                    when {
                        step.completed -> "MAÎTRISÉE · ${step.accuracy}% sur ${step.answered} réponses"
                        step.unlocked && step.answered > 0 -> "EN COURS · ${step.accuracy}% sur ${step.answered} réponses"
                        step.unlocked -> "À COMMENCER · quiz de 5 questions"
                        else -> "VERROUILLÉE · maîtrise l'étape précédente"
                    },
                    color = accent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = .5.sp
                )
            }
            if (step.unlocked) {
                Text(if (step.completed) "REJOUER" else "JOUER", color = accent, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
            }
        }
        if (!isLast) {
            Text("↓", color = PathBlue.copy(alpha = .55f), fontSize = 15.sp, modifier = Modifier.padding(start = 9.dp))
        }
    }
}
