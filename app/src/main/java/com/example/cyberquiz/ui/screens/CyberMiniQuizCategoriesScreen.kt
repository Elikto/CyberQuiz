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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.categoryQuizAttempts
import com.example.cyberquiz.viewmodel.QuizViewModel

private val MiniCatPurple = Color(0xFFD652FF)
private val MiniCatBlue = Color(0xFF19BFFF)
private val MiniCatCyan = Color(0xFF19F2E5)
private val MiniCatGreen = Color(0xFF38E69A)
private val MiniCatOrange = Color(0xFFFFB84A)
private val MiniCatText = Color(0xFFF5F7FF)
private val MiniCatMuted = Color(0xFF9FAED3)
private val MiniCatPanel = Color(0xFF081226)
private val MiniCatBorder = Color(0xFF284B7A)

@Composable
fun CyberMiniQuizCategoriesScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onCategoryQuiz: (String, Int) -> Unit,
    onAdaptiveQuiz: () -> Unit
) {
    val history by vm.quizHistory.collectAsState()
    val activeSessions by vm.activeSessions.collectAsState()
    var blockedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
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
            Text(
                "QUIZ PAR CATÉGORIE",
                color = MiniCatPurple,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            Spacer(Modifier.height(4.dp))
            Text("Choisis ton domaine", color = MiniCatText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                "Sélectionne une catégorie, puis choisis le nombre de questions que tu veux jouer.",
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
                    categoryQuizAttempts(history, category.label)
                }
                MiniCategoryCard(
                    category = category,
                    attemptCount = attempts.size,
                    lastPercent = attempts.lastOrNull()?.percent,
                    onClick = {
                        if (hasFreeSlot) {
                            selectedCategory = category
                        } else {
                            blockedCategory = category.label
                        }
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

    selectedCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { selectedCategory = null },
            containerColor = Color(0xFF0B1429),
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        miniCategorySymbol(category),
                        color = miniCategoryAccent(category),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(category.label, color = MiniCatText, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Combien de questions veux-tu pour ce quiz ?",
                        color = MiniCatMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    QUIZ_QUESTION_COUNT_OPTIONS.chunked(3).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            rowOptions.forEach { count ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .background(
                                            miniCategoryAccent(category).copy(alpha = .11f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            miniCategoryAccent(category).copy(alpha = .55f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedCategory = null
                                            onCategoryQuiz(category.label, count)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        quizQuestionCountLabel(count),
                                        color = MiniCatText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            repeat(3 - rowOptions.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedCategory = null }) {
                    Text("ANNULER", color = MiniCatMuted, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    blockedCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { blockedCategory = null },
            containerColor = Color(0xFF0B1429),
            title = { Text("Limite de quiz en cours", color = MiniCatText, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Tu as déjà ${QuizViewModel.MAX_ACTIVE_SESSIONS} quiz enregistrés. Reprends ou arrête une session avant de lancer un nouveau quiz $category.",
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
            Text("CYBERSÉCURITÉ", color = MiniCatMuted, fontSize = 8.sp, letterSpacing = 1.2.sp)
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
    val accent = miniCategoryAccent(category)
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = .13f), RoundedCornerShape(10.dp))
                    .border(1.dp, accent.copy(alpha = .32f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    miniCategorySymbol(category),
                    color = accent,
                    fontSize = if (category == Category.AD) 11.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.weight(1f))
            Text("›", color = accent, fontSize = 22.sp)
        }
        Text(category.label, color = MiniCatText, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Black)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (attemptCount == 0) "Aucun quiz terminé" else "$attemptCount quiz terminé${if (attemptCount > 1) "s" else ""}",
                color = MiniCatMuted,
                fontSize = 8.sp
            )
            if (lastPercent != null) {
                Text(
                    "Dernier : $lastPercent%",
                    color = if (lastPercent >= 80) MiniCatGreen else MiniCatOrange,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun miniCategoryAccent(category: Category): Color = when (category.ordinal % 4) {
    0 -> MiniCatPurple
    1 -> MiniCatBlue
    2 -> MiniCatCyan
    else -> MiniCatGreen
}

private fun miniCategorySymbol(category: Category): String = when (category) {
    Category.RESEAUX -> "⌁"
    Category.LINUX -> ">_"
    Category.WINDOWS -> "▦"
    Category.CRYPTO -> "◇"
    Category.WEB -> "◎"
    Category.MALWARE -> "!"
    Category.SOCIAL -> "◌"
    Category.OSINT -> "⌖"
    Category.FORENSICS -> "⌕"
    Category.PENTEST -> "⚡"
    Category.AD -> "AD"
    Category.CLOUD -> "☁"
    Category.MOBILE -> "▯"
    Category.SYSTEM -> "⚙"
}
