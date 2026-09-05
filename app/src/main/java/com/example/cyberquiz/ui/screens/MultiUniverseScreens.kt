package com.example.cyberquiz.ui.screens

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.NutritionCategory
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.viewmodel.QuizViewModel

private val MultiPurple = Color(0xFFD652FF)
private val MultiBlue = Color(0xFF19BFFF)
private val MultiCyan = Color(0xFF19F2E5)
private val MultiGreen = Color(0xFF38E69A)
private val MultiOrange = Color(0xFFFFB84A)
private val MultiText = Color(0xFFF5F7FF)
private val MultiMuted = Color(0xFF9FAED3)
private val MultiPanel = Color(0xFF081226)
private val MultiBorder = Color(0xFF244777)

fun QuizType.isPlayableNow(): Boolean =
    this == QuizType.CYBERSECURITY || this == QuizType.NUTRITION

private fun multiBackground() = Brush.verticalGradient(
    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
)

private data class CategoryUi(
    val label: String,
    val symbol: String,
    val accent: Color
)

@Composable
fun CategoriesScreenV3(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onBack: () -> Unit,
    onQuiz: () -> Unit
) {
    val categories = when (selectedQuizType) {
        QuizType.CYBERSECURITY -> Category.entries.mapIndexed { index, category ->
            CategoryUi(
                label = category.label,
                symbol = cyberCategorySymbol(category),
                accent = categoryAccent(index)
            )
        }

        QuizType.NUTRITION -> NutritionCategory.entries.mapIndexed { index, category ->
            CategoryUi(
                label = category.label,
                symbol = nutritionCategorySymbol(category),
                accent = categoryAccent(index)
            )
        }

        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(multiBackground())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        MultiHeader(
            title = "Catégories",
            subtitle = selectedQuizType.label.uppercase(),
            onBack = onBack
        )

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF17133D), Color(0xFF07162D))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, MultiPurple.copy(alpha = .65f), RoundedCornerShape(20.dp))
                .padding(15.dp)
        ) {
            Text(
                "ENTRAÎNEMENT CIBLÉ",
                color = MultiPurple,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Choisis une catégorie",
                color = MultiText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Le quiz utilisera uniquement les questions de cette catégorie.",
                color = MultiMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
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
            items(categories) { category ->
                MultiCategoryCard(category) {
                    vm.startCategory(selectedQuizType.name, category.label)
                    onQuiz()
                }
            }
        }

        MultiAdaptiveButton(selectedQuizType.label) {
            vm.start(quizType = selectedQuizType.name)
            onQuiz()
        }
    }
}

@Composable
private fun MultiCategoryCard(category: CategoryUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .background(
                Brush.linearGradient(
                    listOf(category.accent.copy(alpha = .12f), MultiPanel)
                ),
                RoundedCornerShape(19.dp)
            )
            .border(1.dp, category.accent.copy(alpha = .55f), RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(category.accent.copy(alpha = .14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    category.symbol,
                    color = category.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text("›", color = category.accent, fontSize = 24.sp)
        }
        Text(
            category.label,
            color = MultiText,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MultiAdaptiveButton(quizLabel: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF6B1AA1), Color(0xFF163A7C))
                ),
                RoundedCornerShape(18.dp)
            )
            .border(1.4.dp, MultiPurple, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✦", color = MultiCyan, fontSize = 18.sp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("Quiz adaptatif", color = MultiText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                "Mélange les catégories · $quizLabel",
                color = Color(0xFFC3CCEA),
                fontSize = 11.sp
            )
        }
        Text("›", color = MultiText, fontSize = 27.sp)
    }
}

@Composable
fun ProfileScreenV3(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val showClearDataDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(multiBackground())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MultiHeader(
            title = "Profil",
            subtitle = "TON UNIVERS",
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF15143D), Color(0xFF07172F))
                    ),
                    RoundedCornerShape(23.dp)
                )
                .border(1.2.dp, Color(0xFF416EC4), RoundedCornerShape(23.dp))
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF762EFF), Color(0xFF0877D8))),
                        CircleShape
                    )
                    .border(1.5.dp, MultiCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    selectedQuizType.symbol,
                    color = MultiText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text("Mon profil", color = MultiText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Univers actuel",
                    color = MultiMuted,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    selectedQuizType.label,
                    color = MultiCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF251016), RoundedCornerShape(19.dp))
                .border(1.2.dp, Color(0xFFFF557A).copy(alpha = .70f), RoundedCornerShape(19.dp))
                .clickable { showClearDataDialog.value = true }
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color(0xFFFF557A).copy(alpha = .12f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⌫", color = Color(0xFFFF7A96), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Effacer les données enregistrées",
                    color = Color(0xFFFFA0B3),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Progression, historique, révisions et quiz en cours",
                    color = MultiMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            Text("›", color = Color(0xFFFF7A96), fontSize = 23.sp)
        }

        Text(
            "TYPE DE QUIZ",
            color = MultiBlue,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
        )

        QuizType.entries.forEach { type ->
            MultiUniverseCard(
                type = type,
                selected = type == selectedQuizType,
                playable = type.isPlayableNow(),
                onClick = { onQuizTypeSelected(type) }
            )
        }

        val playable = selectedQuizType.isPlayableNow()
        val statusColor = if (playable) MultiGreen else MultiOrange

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor.copy(alpha = .08f), RoundedCornerShape(17.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (playable) "✓" else "◷",
                color = statusColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (playable) {
                    "${selectedQuizType.label} est prêt à jouer."
                } else {
                    "${selectedQuizType.label} est sélectionné · questions bientôt disponibles."
                },
                color = Color(0xFFD4DCF5),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(6.dp))
    }

    if (showClearDataDialog.value) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog.value = false },
            containerColor = Color(0xFF0B1429),
            title = {
                Text(
                    "Effacer les données ?",
                    color = MultiText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Cette action supprimera toute la progression, l'historique, les révisions et les quiz en cours. L'application sera réinitialisée.",
                    color = MultiMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog.value = false
                        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        activityManager?.clearApplicationUserData()
                    }
                ) {
                    Text("EFFACER", color = Color(0xFFFF557A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog.value = false }) {
                    Text("ANNULER", color = MultiBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun MultiUniverseCard(
    type: QuizType,
    selected: Boolean,
    playable: Boolean,
    onClick: () -> Unit
) {
    val accent = when (type) {
        QuizType.CYBERSECURITY -> MultiBlue
        QuizType.NUTRITION -> MultiGreen
        QuizType.TAROT -> MultiPurple
        QuizType.LITHOTHERAPY -> MultiCyan
        QuizType.GENERAL_KNOWLEDGE -> Color(0xFF8B7CFF)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        if (selected) accent.copy(alpha = .16f) else Color(0xFF091429),
                        Color(0xFF071124)
                    )
                ),
                RoundedCornerShape(19.dp)
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) accent else MultiBorder,
                RoundedCornerShape(19.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(accent.copy(alpha = .13f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(type.symbol, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(type.label, color = MultiText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                if (playable) {
                    Text("PRÊT", color = MultiGreen, fontSize = 8.sp, fontWeight = FontWeight.Black)
                } else {
                    Text("BIENTÔT", color = MultiOrange, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(type.description, color = MultiMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }

        Text(
            if (selected) "✓" else "›",
            color = if (selected) MultiGreen else MultiMuted,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MultiHeader(title: String, subtitle: String, onBack: () -> Unit) {
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
            Text("‹", color = MultiText, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }

        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = MultiText, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = MultiMuted, fontSize = 9.sp, letterSpacing = 1.8.sp)
        }
    }
}

private fun categoryAccent(index: Int): Color = when (index % 4) {
    0 -> MultiPurple
    1 -> MultiBlue
    2 -> MultiCyan
    else -> MultiGreen
}

private fun cyberCategorySymbol(category: Category): String = when (category) {
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

private fun nutritionCategorySymbol(category: NutritionCategory): String = when (category) {
    NutritionCategory.MACRONUTRIENTS -> "M"
    NutritionCategory.MICRONUTRIENTS -> "µ"
    NutritionCategory.HYDRATION -> "◉"
    NutritionCategory.BALANCE -> "◎"
    NutritionCategory.DIGESTION -> "≈"
}
