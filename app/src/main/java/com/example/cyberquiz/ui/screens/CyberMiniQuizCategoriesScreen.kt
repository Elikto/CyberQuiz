package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.cyberquiz.model.CATEGORY_MINI_QUIZ_SIZE
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.categoryMiniQuizAttempts
import com.example.cyberquiz.viewmodel.QuizViewModel

private val MiniCatPurple = Color(0xFFD652FF)
private val MiniCatBlue = Color(0xFF19BFFF)
private val MiniCatCyan = Color(0xFF19F2E5)
private val MiniCatGreen = Color(0xFF38E69A)
private val MiniCatOrange = Color(0xFFFFB84A)
private val MiniCatText = Color(0xFFF5F7FF)
private val MiniCatMuted = Color(0xFF9FAED3)
private val MiniCatPanel = Color(0xFF081226)

@Composable
fun CyberMiniQuizCategoriesScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onCategoryQuiz: (String) -> Unit,
    onAdaptiveQuiz: () -> Unit
) {
    val history by vm.quizHistory.collectAsState()
    val activeSessions by vm.activeSessions.collectAsState()
    var blockedCategory by remember { mutableStateOf<String?>(null) }
    val hasFreeSlot = activeSessions.size < QuizViewModel.MAX_ACTIVE_SESSIONS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF030712)))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        MiniCategoryHeader(onBack)
        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF17133D), Color(0xFF07162D))),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, MiniCatPurple.copy(alpha = .65f), RoundedCornerShape(20.dp))
                .padding(15.dp)
        ) {
            Text("MINI-QUIZ PAR CATÉGORIE", color = MiniCatPurple, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(4.dp))
            Text("5 questions à chaque fois", color = MiniCatText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                "Relance une même catégorie autant de fois que tu veux. Chaque série de 5 questions est conservée séparément et apparaît dans tes statistiques.",
                color = MiniCatMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            items(Category.entries) { category ->
                val attempts = remember(history, category.label) {
                    categoryMiniQuizAttempts(history, category.label)
                }
                MiniCategoryCard(
                    category = category,
                    attemptCount = attempts.size,
                    lastPercent = attempts.lastOrNull()?.percent,
                    onClick = {
                        if (hasFreeSlot) onCategoryQuiz(category.label) else blockedCategory = category.label
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF592085), Color(0xFF163A7C))),
                    RoundedCornerShape(17.dp)
                )
                .border(1.2.dp, MiniCatPurple, RoundedCornerShape(17.dp))
                .clickable(onClick = onAdaptiveQuiz)
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✦", color = MiniCatCyan, fontSize = 17.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Quiz adaptatif", color = MiniCatText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Mélange toutes les catégories", color = MiniCatMuted, fontSize = 10.sp)
            }
            Text("›", color = MiniCatText, fontSize = 25.sp)
        }
    }

    blockedCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { blockedCategory = null },
            containerColor = Color(0xFF0B1429),
            title = { Text("Limite de quiz en cours", color = MiniCatText, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Tu as déjà ${QuizViewModel.MAX_ACTIVE_SESSIONS} quiz enregistrés. Reprends ou arrête une session avant de lancer un nouveau mini-quiz $category.",
                    color = MiniCatMuted
                )
            },
            confirmButton = {
                TextButton(onClick = { blockedCategory = null }) {
                    Text("COMPRIS", color = MiniCatCyan, fontWeight = FontWeight.Black)
                }
            }
        )
    }
}

@Composable
private fun MiniCategoryHeader(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = MiniCatText, fontSize = 24.sp, lineHeight = 24.sp)
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text("Catégories", color = MiniCatText, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("CYBERSÉCURITÉ · $CATEGORY_MINI_QUIZ_SIZE QUESTIONS", color = MiniCatMuted, fontSize = 8.sp, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
private fun MiniCategoryCard(
    category: Category,
    attemptCount: Int,
    lastPercent: Int?,
    onClick: () -> Unit
) {
    val accent = when (category.ordinal % 4) {
        0 -> MiniCatPurple
        1 -> MiniCatBlue
        2 -> MiniCatCyan
        else -> Color(0xFF8B7CFF)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = .12f), MiniCatPanel)),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, accent.copy(alpha = .52f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("5 Q", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("›", color = accent, fontSize = 22.sp)
        }
        Text(category.label, color = MiniCatText, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Black)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (attemptCount == 0) "Aucun mini-quiz" else "$attemptCount quiz terminé${if (attemptCount > 1) "s" else ""}",
                color = MiniCatMuted,
                fontSize = 8.sp
            )
            if (lastPercent != null) {
                Text("Dernier : $lastPercent%", color = if (lastPercent >= 80) MiniCatGreen else MiniCatOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
