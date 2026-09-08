package com.example.cyberquiz.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cyberquiz.model.ActiveQuizSessionSummary
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.viewmodel.QuizViewModel

private val SetupUxPurple = Color(0xFFD652FF)
private val SetupUxBlue = Color(0xFF19BFFF)
private val SetupUxCyan = Color(0xFF19F2E5)
private val SetupUxGreen = Color(0xFF38E69A)
private val SetupUxOrange = Color(0xFFFFB84A)
private val SetupUxRed = Color(0xFFFF657F)
private val SetupUxText = Color(0xFFF5F7FF)
private val SetupUxMuted = Color(0xFF9FAED3)
private val SetupUxPanel = Color(0xFF081226)
private val SetupUxBorder = Color(0xFF284B7A)

@Composable
fun QuizSetupScreenUx(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onStart: (QuizSessionConfig) -> Unit,
    onResume: (String) -> Unit,
    onAbandon: (String) -> Unit
) {
    val lastConfig by vm.lastSessionConfig.collectAsState()
    val activeSessions by vm.activeSessions.collectAsState()
    val reviewItems by vm.reviewItems.collectAsState()
    val allCategories = remember { Category.entries.map { it.label } }

    var modeName by rememberSaveable { mutableStateOf(lastConfig.mode.name) }
    var questionCount by rememberSaveable { mutableStateOf(lastConfig.questionCount) }
    var selectedCategories by remember { mutableStateOf(lastConfig.categories.ifEmpty { allCategories.toSet() }) }
    var showQuizForm by rememberSaveable { mutableStateOf(false) }
    var showPreviousConfirmation by rememberSaveable { mutableStateOf(false) }
    var abandonSession by remember { mutableStateOf<ActiveQuizSessionSummary?>(null) }

    val selectedMode = runCatching { QuizSessionMode.valueOf(modeName) }
        .getOrDefault(QuizSessionMode.RANDOM)
    val hasFreeSlot = activeSessions.size < QuizViewModel.MAX_ACTIVE_SESSIONS
    val activeReviewCount = reviewItems.count { !it.mastered && it.category in selectedCategories }
    val canStart = hasFreeSlot && selectedCategories.isNotEmpty() &&
        (selectedMode != QuizSessionMode.DIFFICULTIES || activeReviewCount > 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF050916), Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SetupUxHeader(onBack)

        Text("Choisis ton prochain quiz", color = SetupUxText, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text(
            "Lance une nouvelle session ou reprends un quiz déjà commencé.",
            color = SetupUxMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        NewQuizLaunchCard(
            enabled = hasFreeSlot,
            onClick = { showQuizForm = true }
        )

        PreviousChoiceCardUx(
            config = lastConfig,
            enabled = hasFreeSlot,
            onClick = { showPreviousConfirmation = true }
        )

        if (activeSessions.isNotEmpty()) {
            SetupUxSection("QUIZ EN COURS · ${activeSessions.size}/${QuizViewModel.MAX_ACTIVE_SESSIONS}")
            activeSessions.forEachIndexed { index, session ->
                ActiveSessionCardUx(
                    number = index + 1,
                    session = session,
                    onResume = { onResume(session.id) },
                    onAbandon = { abandonSession = session }
                )
            }
        }

        if (!hasFreeSlot) {
            SetupUxInfo(
                "La limite de ${QuizViewModel.MAX_ACTIVE_SESSIONS} quiz en cours est atteinte. Termine ou arrête une session avant d'en créer une autre.",
                SetupUxOrange
            )
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showQuizForm) {
        Dialog(
            onDismissRequest = { showQuizForm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(.94f)
                    .heightIn(max = 720.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF101A36), Color(0xFF071329))),
                        RoundedCornerShape(25.dp)
                    )
                    .border(1.4.dp, SetupUxPurple.copy(alpha = .72f), RoundedCornerShape(25.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "NOUVEAU QUIZ",
                            color = SetupUxPurple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            "Configurer le quiz",
                            color = SetupUxText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF17233A), CircleShape)
                            .border(1.dp, SetupUxBorder, CircleShape)
                            .clickable { showQuizForm = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", color = SetupUxMuted, fontSize = 20.sp)
                    }
                }

                Text("1 · DIFFICULTÉ", color = SetupUxBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ModeChip("FACILE", QuizSessionMode.EASY, selectedMode, SetupUxGreen, Modifier.weight(1f)) { modeName = it.name }
                    ModeChip("MOYEN", QuizSessionMode.MEDIUM, selectedMode, SetupUxOrange, Modifier.weight(1f)) { modeName = it.name }
                    ModeChip("DIFFICILE", QuizSessionMode.HARD, selectedMode, SetupUxRed, Modifier.weight(1f)) { modeName = it.name }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ModeChip("ALÉATOIRE", QuizSessionMode.RANDOM, selectedMode, SetupUxPurple, Modifier.weight(1f)) { modeName = it.name }
                    ModeChip("MES DIFFICULTÉS", QuizSessionMode.DIFFICULTIES, selectedMode, SetupUxCyan, Modifier.weight(1f)) { modeName = it.name }
                }

                if (selectedMode == QuizSessionMode.DIFFICULTIES) {
                    SetupUxInfo(
                        "$activeReviewCount question${if (activeReviewCount > 1) "s" else ""} à revoir dans les catégories choisies.",
                        if (activeReviewCount > 0) SetupUxCyan else SetupUxOrange
                    )
                }

                Text("2 · CATÉGORIES", color = SetupUxBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                CategorySelectorUx(
                    categories = allCategories,
                    selected = selectedCategories,
                    onToggleAll = {
                        selectedCategories = if (selectedCategories.size == allCategories.size) {
                            emptySet()
                        } else {
                            allCategories.toSet()
                        }
                    },
                    onToggle = { category ->
                        selectedCategories = if (category in selectedCategories) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    }
                )

                Text("3 · NOMBRE DE QUESTIONS", color = SetupUxBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                QuestionCountSelectorUx(
                    selected = questionCount,
                    onSelected = { questionCount = it }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            if (canStart) {
                                Brush.horizontalGradient(listOf(Color(0xFF6B1AA1), Color(0xFF18458C)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF252B3D), Color(0xFF182033)))
                            },
                            RoundedCornerShape(17.dp)
                        )
                        .border(
                            1.2.dp,
                            if (canStart) SetupUxPurple else Color(0xFF39445E),
                            RoundedCornerShape(17.dp)
                        )
                        .clickable(enabled = canStart) {
                            showQuizForm = false
                            onStart(
                                QuizSessionConfig(
                                    mode = selectedMode,
                                    categories = selectedCategories,
                                    questionCount = questionCount
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (canStart) "LANCER LE QUIZ" else "CONFIGURATION INCOMPLÈTE",
                        color = if (canStart) SetupUxText else SetupUxMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = .7.sp
                    )
                }
            }
        }
    }

    if (showPreviousConfirmation) {
        AlertDialog(
            onDismissRequest = { showPreviousConfirmation = false },
            containerColor = Color(0xFF0B1429),
            title = { Text("Relancer ton dernier choix ?", color = SetupUxText, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "${sessionDescriptionUx(lastConfig)}\n\nUn nouveau quiz sera créé avec exactement ces réglages. Ta précédente session ou ton historique ne seront pas supprimés.",
                    color = SetupUxMuted,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(
                    enabled = hasFreeSlot,
                    onClick = {
                        showPreviousConfirmation = false
                        onStart(lastConfig)
                    }
                ) {
                    Text("RELANCER", color = if (hasFreeSlot) SetupUxCyan else SetupUxMuted, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviousConfirmation = false }) {
                    Text("ANNULER", color = SetupUxMuted, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    abandonSession?.let { session ->
        AlertDialog(
            onDismissRequest = { abandonSession = null },
            containerColor = Color(0xFF0B1429),
            title = { Text("Arrêter définitivement ce quiz ?", color = SetupUxText, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Cette session en cours sera supprimée et ne pourra plus être reprise. Les statistiques déjà enregistrées restent conservées.",
                    color = SetupUxMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    abandonSession = null
                    onAbandon(session.id)
                }) {
                    Text("ARRÊTER", color = SetupUxRed, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { abandonSession = null }) {
                    Text("GARDER", color = SetupUxCyan, fontWeight = FontWeight.Black)
                }
            }
        )
    }
}

@Composable
private fun NewQuizLaunchCard(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(Color(0xFF5A1A83), Color(0xFF163D80), Color(0xFF081A34)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF242A39), Color(0xFF151D2D)))
                },
                RoundedCornerShape(20.dp)
            )
            .border(
                1.5.dp,
                if (enabled) SetupUxPurple else Color(0xFF39445E),
                RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(SetupUxPurple.copy(alpha = if (enabled) .16f else .06f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = if (enabled) SetupUxText else SetupUxMuted, fontSize = 17.sp)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "COMMENCER UN QUIZ",
                color = if (enabled) SetupUxText else SetupUxMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .6.sp
            )
            Text(
                if (enabled) "Choisir difficulté, catégories et nombre de questions" else "Aucune place disponible",
                color = SetupUxMuted,
                fontSize = 9.sp
            )
        }
        Text("›", color = if (enabled) SetupUxCyan else SetupUxMuted, fontSize = 26.sp)
    }
}

@Composable
private fun SetupUxHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = SetupUxText, fontSize = 23.sp, lineHeight = 23.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("CYBER QUIZ", color = SetupUxCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text("SESSIONS DE QUIZ", color = SetupUxMuted, fontSize = 8.sp, letterSpacing = 1.2.sp)
        }
        Spacer(Modifier.width(42.dp))
    }
}

@Composable
private fun PreviousChoiceCardUx(config: QuizSessionConfig, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(SetupUxPurple.copy(alpha = .12f), SetupUxBlue.copy(alpha = .07f), SetupUxPanel)),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, SetupUxPurple.copy(alpha = if (enabled) .55f else .25f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("TON CHOIX PRÉCÉDENT", color = SetupUxPurple, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            Text(sessionDescriptionUx(config), color = SetupUxText, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 19.sp)
            Text(
                if (enabled) "Appuie pour le relancer" else "Libère d'abord une place parmi les quiz en cours",
                color = SetupUxMuted,
                fontSize = 9.sp
            )
        }
        Text("›", color = if (enabled) SetupUxCyan else SetupUxMuted, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActiveSessionCardUx(
    number: Int,
    session: ActiveQuizSessionSummary,
    onResume: () -> Unit,
    onAbandon: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SetupUxPanel, RoundedCornerShape(18.dp))
            .border(1.dp, SetupUxCyan.copy(alpha = .45f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("QUIZ EN COURS $number", color = SetupUxCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text(
                if (session.config.infinite) "∞" else "${session.answered}/${session.config.questionCount}",
                color = SetupUxPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(sessionDescriptionUx(session.config), color = SetupUxText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (session.pendingAnswer) {
            Text("Réponse et explication prêtes à être restaurées.", color = SetupUxGreen, fontSize = 9.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SetupUxAction("REPRENDRE", SetupUxPurple, Modifier.weight(1f), onResume)
            SetupUxAction("ARRÊTER", SetupUxRed, Modifier.weight(1f), onAbandon)
        }
    }
}

@Composable
private fun SetupUxAction(text: String, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .background(accent.copy(alpha = .09f), RoundedCornerShape(13.dp))
            .border(1.dp, accent.copy(alpha = .65f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CategorySelectorUx(
    categories: List<String>,
    selected: Set<String>,
    onToggleAll: () -> Unit,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF071429), RoundedCornerShape(16.dp))
            .border(1.dp, SetupUxBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 5.dp)
    ) {
        CategoryRowUx("Toutes les catégories", selected.size == categories.size, SetupUxCyan, onToggleAll)
        categories.forEach { category ->
            CategoryRowUx(category, category in selected, SetupUxBlue) { onToggle(category) }
        }
    }
}

@Composable
private fun CategoryRowUx(text: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(if (selected) accent.copy(alpha = .14f) else Color(0xFF08101E), RoundedCornerShape(6.dp))
                .border(1.dp, if (selected) accent else Color(0xFF36517C), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Text("✓", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = if (selected) SetupUxText else SetupUxMuted, fontSize = 11.sp)
    }
}

@Composable
private fun QuestionCountSelectorUx(selected: Int, onSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        QUIZ_QUESTION_COUNT_OPTIONS.chunked(3).forEach { rowOptions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                rowOptions.forEach { count ->
                    val active = selected == count
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(if (active) SetupUxPurple.copy(alpha = .15f) else Color(0xFF08152B), RoundedCornerShape(12.dp))
                            .border(1.dp, if (active) SetupUxPurple else SetupUxBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelected(count) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            quizQuestionCountLabel(count),
                            color = if (active) SetupUxText else SetupUxMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                repeat(3 - rowOptions.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ModeChip(
    text: String,
    mode: QuizSessionMode,
    selectedMode: QuizSessionMode,
    accent: Color,
    modifier: Modifier,
    onSelected: (QuizSessionMode) -> Unit
) {
    val selected = mode == selectedMode
    Box(
        modifier = modifier
            .height(40.dp)
            .background(if (selected) accent.copy(alpha = .14f) else Color(0xFF08152B), RoundedCornerShape(12.dp))
            .border(1.dp, if (selected) accent else SetupUxBorder, RoundedCornerShape(12.dp))
            .clickable { onSelected(mode) },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) accent else SetupUxMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SetupUxSection(text: String) {
    Text(text, color = SetupUxBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
}

@Composable
private fun SetupUxInfo(text: String, accent: Color) {
    Text(
        text,
        color = accent,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .06f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .28f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    )
}

private fun sessionDescriptionUx(config: QuizSessionConfig): String {
    val mode = when (config.mode) {
        QuizSessionMode.EASY -> "Facile"
        QuizSessionMode.MEDIUM -> "Moyen"
        QuizSessionMode.HARD -> "Difficile"
        QuizSessionMode.RANDOM -> "Aléatoire"
        QuizSessionMode.DIFFICULTIES -> "Mes difficultés"
    }
    val categories = when (config.categories.size) {
        0 -> "Aucune catégorie"
        Category.entries.size -> "Toutes les catégories"
        1 -> config.categories.first()
        else -> "${config.categories.size} catégories"
    }
    val count = if (config.infinite) "Infini" else "${config.questionCount} questions"
    return "$mode · $categories · $count"
}
