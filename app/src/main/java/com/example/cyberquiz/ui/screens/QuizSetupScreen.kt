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
import com.example.cyberquiz.model.ActiveQuizSessionSummary
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.viewmodel.QuizViewModel

private val SetupPurple = Color(0xFFD652FF)
private val SetupBlue = Color(0xFF19BFFF)
private val SetupCyan = Color(0xFF19F2E5)
private val SetupGreen = Color(0xFF38E69A)
private val SetupOrange = Color(0xFFFFB84A)
private val SetupRed = Color(0xFFFF557A)
private val SetupText = Color(0xFFF5F7FF)
private val SetupMuted = Color(0xFF9FAED3)
private val SetupPanel = Color(0xFF081226)

@Composable
fun QuizSetupScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onStart: (QuizSessionConfig) -> Unit,
    onResume: (String) -> Unit,
    onAbandon: (String) -> Unit
) {
    val lastConfig by vm.lastSessionConfig.collectAsState()
    val activeSessions by vm.activeSessions.collectAsState()
    val reviewItems by vm.reviewItems.collectAsState()
    val allCategories = Category.entries.map { it.label }

    var modeName by rememberSaveable(lastConfig.mode.name) { mutableStateOf(lastConfig.mode.name) }
    var questionCount by rememberSaveable(lastConfig.questionCount) { mutableStateOf(lastConfig.questionCount) }
    var selectedCategories by remember { mutableStateOf(allCategories.toSet()) }
    var categoriesExpanded by rememberSaveable { mutableStateOf(false) }
    var showNewQuizForm by rememberSaveable { mutableStateOf(false) }

    val selectedMode = runCatching { QuizSessionMode.valueOf(modeName) }
        .getOrDefault(QuizSessionMode.RANDOM)
    val activeReviewCount = reviewItems.count {
        !it.mastered && it.category in selectedCategories
    }
    val hasFreeSlot = activeSessions.size < QuizViewModel.MAX_ACTIVE_SESSIONS
    val showConfiguration = activeSessions.isEmpty() || (showNewQuizForm && hasFreeSlot)
    val canStart = hasFreeSlot &&
        selectedCategories.isNotEmpty() &&
        (selectedMode != QuizSessionMode.DIFFICULTIES || activeReviewCount > 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), Color(0xFF050916))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SetupHeader(onBack)

        Text(
            "Préparer le quiz",
            color = SetupText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "Reprends une session existante ou prépare un nouveau quiz. CyberQuiz peut conserver jusqu'à 6 quiz en cours.",
            color = SetupMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        PreviousChoiceCard(lastConfig)

        if (activeSessions.isNotEmpty()) {
            SetupSectionTitle("QUIZ EN COURS · ${activeSessions.size}/${QuizViewModel.MAX_ACTIVE_SESSIONS}")

            activeSessions.forEachIndexed { index, session ->
                ActiveSessionCard(
                    number = index + 1,
                    session = session,
                    onResume = { onResume(session.id) },
                    onAbandon = { onAbandon(session.id) }
                )
            }

            if (hasFreeSlot) {
                NewQuizCard(
                    activeCount = activeSessions.size,
                    expanded = showNewQuizForm,
                    onClick = { showNewQuizForm = !showNewQuizForm }
                )
            } else {
                SessionLimitCard()
            }
        }

        if (showConfiguration) {
            if (activeSessions.isNotEmpty()) {
                SetupSectionTitle("NOUVEAU QUIZ")
            }

            QuizConfigurationForm(
                selectedMode = selectedMode,
                onModeSelected = { modeName = it.name },
                selectedCategories = selectedCategories,
                allCategories = allCategories,
                categoriesExpanded = categoriesExpanded,
                onToggleCategoriesExpanded = { categoriesExpanded = !categoriesExpanded },
                onSelectAllCategories = { selectedCategories = allCategories.toSet() },
                onToggleCategory = { category ->
                    selectedCategories = if (category in selectedCategories) {
                        selectedCategories - category
                    } else {
                        selectedCategories + category
                    }
                },
                activeReviewCount = activeReviewCount,
                questionCount = questionCount,
                onQuestionCountSelected = { questionCount = it },
                canStart = canStart,
                onStart = {
                    onStart(
                        QuizSessionConfig(
                            mode = selectedMode,
                            categories = selectedCategories,
                            questionCount = questionCount
                        )
                    )
                }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuizConfigurationForm(
    selectedMode: QuizSessionMode,
    onModeSelected: (QuizSessionMode) -> Unit,
    selectedCategories: Set<String>,
    allCategories: List<String>,
    categoriesExpanded: Boolean,
    onToggleCategoriesExpanded: () -> Unit,
    onSelectAllCategories: () -> Unit,
    onToggleCategory: (String) -> Unit,
    activeReviewCount: Int,
    questionCount: Int,
    onQuestionCountSelected: (Int) -> Unit,
    canStart: Boolean,
    onStart: () -> Unit
) {
    SetupSectionTitle("1 · DIFFICULTÉ")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SetupChoiceChip(
            text = "FACILE",
            selected = selectedMode == QuizSessionMode.EASY,
            accent = SetupGreen,
            modifier = Modifier.weight(1f)
        ) { onModeSelected(QuizSessionMode.EASY) }
        SetupChoiceChip(
            text = "MOYEN",
            selected = selectedMode == QuizSessionMode.MEDIUM,
            accent = SetupOrange,
            modifier = Modifier.weight(1f)
        ) { onModeSelected(QuizSessionMode.MEDIUM) }
        SetupChoiceChip(
            text = "DIFFICILE",
            selected = selectedMode == QuizSessionMode.HARD,
            accent = SetupRed,
            modifier = Modifier.weight(1f)
        ) { onModeSelected(QuizSessionMode.HARD) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SetupChoiceChip(
            text = "ALÉATOIRE",
            selected = selectedMode == QuizSessionMode.RANDOM,
            accent = SetupPurple,
            modifier = Modifier.weight(1f)
        ) { onModeSelected(QuizSessionMode.RANDOM) }
        SetupChoiceChip(
            text = "MES DIFFICULTÉS",
            selected = selectedMode == QuizSessionMode.DIFFICULTIES,
            accent = SetupCyan,
            modifier = Modifier.weight(1f)
        ) { onModeSelected(QuizSessionMode.DIFFICULTIES) }
    }

    if (selectedMode == QuizSessionMode.DIFFICULTIES) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(SetupCyan.copy(alpha = .06f), RoundedCornerShape(16.dp))
                .border(1.dp, SetupCyan.copy(alpha = .30f), RoundedCornerShape(16.dp))
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                "$activeReviewCount question${if (activeReviewCount > 1) "s" else ""} à revoir dans les catégories sélectionnées",
                color = if (activeReviewCount > 0) SetupCyan else SetupOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Une bonne réponse fait passer une question de À revoir à Maîtrisée après révision. Une nouvelle erreur la conserve dans À revoir.",
                color = SetupMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }

    SetupSectionTitle("2 · CATÉGORIES")
    CategoryDropdownV8(
        allCategories = allCategories,
        selectedCategories = selectedCategories,
        expanded = categoriesExpanded,
        onToggleExpanded = onToggleCategoriesExpanded,
        onSelectAll = onSelectAllCategories,
        onToggleCategory = onToggleCategory
    )

    if (selectedCategories.isEmpty()) {
        Text(
            "Sélectionne au moins une catégorie.",
            color = SetupOrange,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }

    SetupSectionTitle("3 · NOMBRE DE QUESTIONS")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(10, 20, 50).forEach { count ->
            SetupChoiceChip(
                text = count.toString(),
                selected = questionCount == count,
                accent = SetupPurple,
                modifier = Modifier.weight(1f)
            ) { onQuestionCountSelected(count) }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(100, 200).forEach { count ->
            SetupChoiceChip(
                text = count.toString(),
                selected = questionCount == count,
                accent = SetupPurple,
                modifier = Modifier.weight(1f)
            ) { onQuestionCountSelected(count) }
        }
        SetupChoiceChip(
            text = "INFINI",
            selected = questionCount == 0,
            accent = SetupCyan,
            modifier = Modifier.weight(1f)
        ) { onQuestionCountSelected(0) }
    }

    if (questionCount >= 100 && selectedMode != QuizSessionMode.DIFFICULTIES) {
        Text(
            "Pour les longues sessions, les questions correspondant à tes filtres sont remélangées lorsqu'un cycle est terminé.",
            color = SetupMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }

    StartQuizButton(enabled = canStart, onClick = onStart)

    if (!canStart && selectedMode == QuizSessionMode.DIFFICULTIES && selectedCategories.isNotEmpty()) {
        Text(
            "Tu n'as actuellement aucune question À revoir dans ces catégories.",
            color = SetupOrange,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NewQuizCard(
    activeCount: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(SetupPurple.copy(alpha = .11f), SetupBlue.copy(alpha = .08f), SetupPanel)
                ),
                RoundedCornerShape(18.dp)
            )
            .border(1.2.dp, SetupPurple.copy(alpha = .62f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(SetupPurple.copy(alpha = .14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = SetupPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (expanded) "NOUVEAU QUIZ EN PRÉPARATION" else "COMMENCER UN NOUVEAU QUIZ",
                color = SetupText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "$activeCount/${QuizViewModel.MAX_ACTIVE_SESSIONS} sessions utilisées",
                color = SetupMuted,
                fontSize = 9.sp
            )
        }
        Text(if (expanded) "⌃" else "⌄", color = SetupCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SessionLimitCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SetupOrange.copy(alpha = .055f), RoundedCornerShape(17.dp))
            .border(1.dp, SetupOrange.copy(alpha = .45f), RoundedCornerShape(17.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "6 QUIZ EN COURS · LIMITE ATTEINTE",
            color = SetupOrange,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "Termine ou arrête définitivement une session pour pouvoir en commencer une nouvelle.",
            color = SetupMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun CategoryDropdownV8(
    allCategories: List<String>,
    selectedCategories: Set<String>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectAll: () -> Unit,
    onToggleCategory: (String) -> Unit
) {
    val allSelected = selectedCategories.size == allCategories.size
    val summary = when {
        allSelected -> "Toutes les catégories"
        selectedCategories.isEmpty() -> "Aucune catégorie"
        selectedCategories.size == 1 -> selectedCategories.first()
        else -> "${selectedCategories.size} catégories sélectionnées"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152A), RoundedCornerShape(17.dp))
            .border(1.dp, SetupBlue.copy(alpha = .48f), RoundedCornerShape(17.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    summary.uppercase(),
                    color = if (selectedCategories.isEmpty()) SetupOrange else SetupBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${selectedCategories.size}/${allCategories.size} sélectionnées",
                    color = SetupMuted,
                    fontSize = 9.sp
                )
            }
            Text(
                if (expanded) "⌃" else "⌄",
                color = SetupCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (expanded) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SetupBlue.copy(alpha = .20f))
            )

            CategoryOptionRowV8(
                text = "Toutes les catégories",
                selected = allSelected,
                accent = SetupCyan,
                onClick = onSelectAll
            )

            allCategories.forEach { category ->
                CategoryOptionRowV8(
                    text = category,
                    selected = category in selectedCategories,
                    accent = SetupBlue,
                    onClick = { onToggleCategory(category) }
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "FERMER LE MENU",
                    color = SetupCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryOptionRowV8(
    text: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(21.dp)
                .background(
                    if (selected) accent.copy(alpha = .16f) else Color(0xFF07101F),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    1.dp,
                    if (selected) accent else Color(0xFF36517C),
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text("✓", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.width(11.dp))
        Text(
            text,
            color = if (selected) SetupText else SetupMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SetupHeader(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = SetupText, fontSize = 29.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text("CYBER QUIZ", color = SetupCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text("CONFIGURATION DE SESSION", color = SetupMuted, fontSize = 8.sp, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
private fun PreviousChoiceCard(config: QuizSessionConfig) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(SetupPurple.copy(alpha = .12f), SetupBlue.copy(alpha = .07f), SetupPanel)
                ),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, SetupPurple.copy(alpha = .38f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("TON CHOIX PRÉCÉDENT", color = SetupPurple, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Text(sessionDescription(config), color = SetupText, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
    }
}

@Composable
private fun ActiveSessionCard(
    number: Int,
    session: ActiveQuizSessionSummary,
    onResume: () -> Unit,
    onAbandon: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SetupPanel, RoundedCornerShape(20.dp))
            .border(1.1.dp, SetupCyan.copy(alpha = .48f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "QUIZ EN COURS $number",
                color = SetupCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (session.config.infinite) "∞" else "${session.answered}/${session.config.questionCount}",
                color = SetupPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            sessionDescription(session.config),
            color = SetupText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 19.sp
        )

        if (session.pendingAnswer) {
            Text(
                "Dernière réponse déjà enregistrée · l'explication sera restaurée.",
                color = SetupGreen,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF55208B), Color(0xFF163E79))),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.2.dp, SetupPurple, RoundedCornerShape(14.dp))
                    .clickable(onClick = onResume),
                contentAlignment = Alignment.Center
            ) {
                Text("REPRENDRE", color = SetupText, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }

            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(SetupRed.copy(alpha = .055f), RoundedCornerShape(14.dp))
                    .border(1.dp, SetupRed.copy(alpha = .50f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onAbandon),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ARRÊTER DÉFINITIVEMENT",
                    color = SetupRed,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SetupSectionTitle(text: String) {
    Text(text, color = SetupCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
}

@Composable
private fun SetupChoiceChip(
    text: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .heightIn(min = 43.dp)
            .background(
                if (selected) accent.copy(alpha = .14f) else Color(0xFF09152A),
                RoundedCornerShape(14.dp)
            )
            .border(
                if (selected) 1.4.dp else 1.dp,
                if (selected) accent.copy(alpha = .92f) else Color(0xFF29436D),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) accent else Color(0xFFC3CDE6),
            fontSize = 9.sp,
            lineHeight = 12.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StartQuizButton(enabled: Boolean, onClick: () -> Unit) {
    val accent = if (enabled) SetupPurple else Color(0xFF424B66)
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(Color(0xFF64209B), Color(0xFF183E79)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF171B28), Color(0xFF111521)))
                },
                RoundedCornerShape(18.dp)
            )
            .border(1.5.dp, accent, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "COMMENCER LE QUIZ",
            color = if (enabled) SetupText else Color(0xFF69718B),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .7.sp
        )
    }
}

private fun sessionDescription(config: QuizSessionConfig): String {
    val mode = when (config.mode) {
        QuizSessionMode.EASY -> "Facile"
        QuizSessionMode.MEDIUM -> "Moyen"
        QuizSessionMode.HARD -> "Difficile"
        QuizSessionMode.RANDOM -> "Aléatoire"
        QuizSessionMode.DIFFICULTIES -> "Mes difficultés"
    }
    val allCount = Category.entries.size
    val categories = when {
        config.categories.size == allCount -> "Toutes les catégories"
        config.categories.size == 1 -> config.categories.first()
        else -> "${config.categories.size} catégories"
    }
    val count = if (config.infinite) "Infini" else "${config.questionCount} questions"
    return "$mode · $categories · $count"
}
