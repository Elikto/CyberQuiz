package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.data.repository.QuestionBankQuizSessionFactory
import com.example.cyberquiz.data.repository.QuestionLibraryStore
import com.example.cyberquiz.data.repository.QuestionReportReason
import com.example.cyberquiz.model.QuestionBankFilter
import com.example.cyberquiz.model.QuestionBankStatus
import com.example.cyberquiz.model.filterQuestionBank
import com.example.cyberquiz.social.QuestionReportDelivery
import com.example.cyberquiz.social.QuestionReportManager
import com.example.cyberquiz.viewmodel.QuizViewModel
import kotlinx.coroutines.launch

private val BankBg = Color(0xFF030712)
private val BankPanel = Color(0xFF081329)
private val BankText = Color(0xFFF4F7FF)
private val BankMuted = Color(0xFF9DADD1)
private val BankCyan = Color(0xFF19F2E5)
private val BankBlue = Color(0xFF28BFFF)
private val BankPurple = Color(0xFFD652FF)
private val BankOrange = Color(0xFFFFB84A)
private val BankGreen = Color(0xFF38E69A)
private val BankRed = Color(0xFFFF657F)

@Composable
fun QuestionBankScreen(
    vm: QuizViewModel,
    onStartFavoriteQuiz: (List<QuestionEntity>) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reviewItems by vm.reviewItems.collectAsState()
    val library = remember(context) { QuestionLibraryStore(context) }
    var questions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf(library.favoriteIds()) }
    var reportedIds by remember { mutableStateOf(library.reportedIds()) }
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(QuestionBankStatus.ALL) }
    var category by remember { mutableStateOf<String?>(null) }
    var difficulty by remember { mutableStateOf<String?>(null) }
    var categoryDialog by remember { mutableStateOf(false) }
    var reportQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var reportInfo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        questions = CyberQuizDatabase.get(context).quizDao().questionsSnapshot("CYBERSECURITY")
    }

    val wrongIds = remember(reviewItems) { reviewItems.map { it.questionId }.filter { it > 0L }.toSet() }
    val filter = QuestionBankFilter(query, category, difficulty, status)
    val filtered = remember(questions, filter, favoriteIds, wrongIds, reportedIds) {
        filterQuestionBank(questions, filter, favoriteIds, wrongIds, reportedIds)
    }
    val favoriteQuestions = remember(questions, favoriteIds) {
        questions.filter { it.id in favoriteIds }
    }
    val categories = remember(questions) { questions.map { it.category }.distinct().sorted() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020610), Color(0xFF071329), BankBg)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(Color(0xFF101A34), CircleShape)
                    .border(1.dp, Color(0xFF718CE2), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Text("‹", color = BankText, fontSize = 25.sp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Banque de questions", color = BankText, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("RECHERCHER · FILTRER · RÉVISER", color = BankCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }
            Box(
                Modifier
                    .background(BankPurple.copy(alpha = .12f), RoundedCornerShape(50.dp))
                    .border(1.dp, BankPurple.copy(alpha = .55f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("${questions.size} Q", color = BankPurple, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it.take(100) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Rechercher une question ou une notion") },
            leadingIcon = { Text("⌕", color = BankCyan, fontSize = 20.sp) }
        )

        Text("STATUT", color = BankMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            QuestionBankStatus.entries.forEach { value ->
                BankFilterChip(value.label, value == status, BankPurple) { status = value }
            }
        }

        Text("DIFFICULTÉ", color = BankMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            BankFilterChip("Toutes", difficulty == null, BankBlue, Modifier.weight(1f)) { difficulty = null }
            BankFilterChip("Facile", difficulty == "EASY", BankGreen, Modifier.weight(1f)) { difficulty = "EASY" }
            BankFilterChip("Moyen", difficulty == "MEDIUM", BankOrange, Modifier.weight(1f)) { difficulty = "MEDIUM" }
            BankFilterChip("Difficile", difficulty == "HARD", BankRed, Modifier.weight(1f)) { difficulty = "HARD" }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .background(BankPanel, RoundedCornerShape(15.dp))
                .border(1.dp, BankBlue.copy(alpha = .35f), RoundedCornerShape(15.dp))
                .clickable { categoryDialog = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("CATÉGORIE", color = BankMuted, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text(category ?: "Toutes les catégories", color = BankText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("⌄", color = BankCyan, fontSize = 20.sp)
        }

        if (favoriteQuestions.isNotEmpty()) {
            val quizCount = minOf(favoriteQuestions.size, QuestionBankQuizSessionFactory.MAX_QUESTIONS)
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF562087), Color(0xFF123D78))), RoundedCornerShape(17.dp))
                    .border(1.2.dp, BankPurple, RoundedCornerShape(17.dp))
                    .clickable { onStartFavoriteQuiz(favoriteQuestions) }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "★ LANCER MES FAVORIS · $quizCount QUESTION${if (quizCount > 1) "S" else ""}",
                    color = BankText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = .7.sp
                )
            }
        }

        reportInfo?.let { message ->
            Text(
                message,
                color = BankCyan,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BankCyan.copy(alpha = .06f), RoundedCornerShape(13.dp))
                    .padding(10.dp)
            )
        }

        Text(
            "${filtered.size} résultat${if (filtered.size > 1) "s" else ""}",
            color = BankMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        if (questions.isEmpty()) {
            Text("Chargement de la banque…", color = BankMuted, modifier = Modifier.padding(vertical = 20.dp))
        } else if (filtered.isEmpty()) {
            Text(
                "Aucune question ne correspond à ces filtres.",
                color = BankMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 25.dp)
            )
        } else {
            filtered.forEach { question ->
                QuestionBankCard(
                    question = question,
                    favorite = question.id in favoriteIds,
                    wrong = question.id in wrongIds,
                    reported = question.id in reportedIds,
                    reportSent = library.reportSent(question.id),
                    onFavorite = {
                        library.toggleFavorite(question.id)
                        favoriteIds = library.favoriteIds()
                    },
                    onReport = { reportQuestion = question }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (categoryDialog) {
        AlertDialog(
            onDismissRequest = { categoryDialog = false },
            containerColor = Color(0xFF0B1429),
            title = { Text("Filtrer par catégorie", color = BankText, fontWeight = FontWeight.Black) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BankPickerRow("Toutes les catégories", category == null) {
                        category = null
                        categoryDialog = false
                    }
                    categories.forEach { value ->
                        BankPickerRow(value, category == value) {
                            category = value
                            categoryDialog = false
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    reportQuestion?.let { question ->
        QuestionReportDialog(
            question = question,
            existingReason = library.reportReason(question.id),
            onDismiss = { reportQuestion = null },
            onSubmit = { reason, comment ->
                reportQuestion = null
                scope.launch {
                    runCatching {
                        QuestionReportManager.submit(context, question, reason, comment)
                    }.onSuccess { delivery ->
                        reportedIds = library.reportedIds()
                        reportInfo = if (delivery == QuestionReportDelivery.SENT) {
                            "Signalement envoyé. Merci : il pourra être examiné pour corriger la banque."
                        } else {
                            "Signalement enregistré sur l'appareil. Il sera envoyé automatiquement après ta prochaine connexion."
                        }
                    }.onFailure {
                        reportedIds = library.reportedIds()
                        reportInfo = "Signalement conservé sur l'appareil ; l'envoi sera retenté après connexion."
                    }
                }
            }
        )
    }
}

@Composable
private fun QuestionBankCard(
    question: QuestionEntity,
    favorite: Boolean,
    wrong: Boolean,
    reported: Boolean,
    reportSent: Boolean,
    onFavorite: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BankPanel, RoundedCornerShape(18.dp))
            .border(1.dp, (if (favorite) BankPurple else BankBlue).copy(alpha = .36f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(question.question, color = BankText, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(
                    "${question.category.uppercase()} · ${difficultyLabelBank(question.difficulty)}",
                    color = BankBlue,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = .8.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(36.dp)
                    .background(BankPurple.copy(alpha = if (favorite) .18f else .05f), CircleShape)
                    .border(1.dp, BankPurple.copy(alpha = .65f), CircleShape)
                    .clickable(onClick = onFavorite),
                contentAlignment = Alignment.Center
            ) {
                Text(if (favorite) "★" else "☆", color = BankPurple, fontSize = 20.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BankTag(if (question.seen) "DÉJÀ VUE" else "JAMAIS VUE", if (question.seen) BankCyan else BankMuted)
            if (wrong) BankTag("RATÉE", BankOrange)
            if (favorite) BankTag("FAVORITE", BankPurple)
            if (reported) BankTag(if (reportSent) "SIGNALÉE" else "À ENVOYER", BankRed)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onReport)
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚑", color = BankRed, fontSize = 14.sp)
            Spacer(Modifier.width(7.dp))
            Text(
                if (reported) "Modifier le signalement" else "Signaler une question incorrecte ou ambiguë",
                color = BankRed,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuestionReportDialog(
    question: QuestionEntity,
    existingReason: QuestionReportReason?,
    onDismiss: () -> Unit,
    onSubmit: (QuestionReportReason, String) -> Unit
) {
    var reason by remember(question.id) { mutableStateOf(existingReason ?: QuestionReportReason.INCORRECT) }
    var comment by remember(question.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B1429),
        title = { Text("Signaler cette question", color = BankText, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(question.question, color = BankMuted, fontSize = 11.sp, lineHeight = 16.sp)
                QuestionReportReason.entries.forEach { value ->
                    BankPickerRow(value.label, reason == value) { reason = value }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it.take(800) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Précision facultative") },
                    minLines = 2,
                    maxLines = 4
                )
                Text(
                    "Le signalement est conservé si tu es hors ligne. Pour l'envoyer au serveur, une connexion CyberQuiz est nécessaire.",
                    color = BankMuted,
                    fontSize = 9.sp,
                    lineHeight = 13.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(reason, comment.trim()) }) {
                Text("ENREGISTRER", color = BankCyan, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ANNULER", color = BankMuted) }
        }
    )
}

@Composable
private fun BankFilterChip(
    text: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .background(accent.copy(alpha = if (selected) .17f else .05f), RoundedCornerShape(50.dp))
            .border(1.dp, accent.copy(alpha = if (selected) .8f else .28f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) accent else BankMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun BankTag(text: String, accent: Color) {
    Box(
        Modifier
            .background(accent.copy(alpha = .09f), RoundedCornerShape(50.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Text(text, color = accent, fontSize = 7.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BankPickerRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (selected) BankCyan.copy(alpha = .08f) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (selected) "●" else "○", color = if (selected) BankCyan else BankMuted, fontSize = 11.sp)
        Spacer(Modifier.width(8.dp))
        Text(text, color = if (selected) BankText else BankMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}

private fun difficultyLabelBank(value: String): String = when (value.uppercase()) {
    "HARD" -> "DIFFICILE"
    "MEDIUM" -> "MOYEN"
    else -> "FACILE"
}
