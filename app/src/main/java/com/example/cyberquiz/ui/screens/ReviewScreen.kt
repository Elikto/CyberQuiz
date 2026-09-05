package com.example.cyberquiz.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.database.ReviewItemEntity
import com.example.cyberquiz.data.database.ReviewItemWithQuestion
import com.example.cyberquiz.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

private val ReviewPurple = Color(0xFFD652FF)
private val ReviewBlue = Color(0xFF19BFFF)
private val ReviewCyan = Color(0xFF19F2E5)
private val ReviewGreen = Color(0xFF38E69A)
private val ReviewOrange = Color(0xFFFFB84A)
private val ReviewText = Color(0xFFF5F7FF)
private val ReviewMuted = Color(0xFF9FAED3)

@Composable
fun ReviewScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onPractice: (ReviewItemEntity) -> Unit,
    onCategoryQuiz: (String, Int) -> Unit,
    highlightedConcept: String? = null
) {
    val items by vm.reviewItemsWithQuestions.collectAsState()
    val activeSessions by vm.activeSessions.collectAsState()
    val scrollState = rememberScrollState()
    var courseTerm by remember { mutableStateOf<String?>(null) }
    var courseCategory by remember { mutableStateOf("") }

    val activeRaw = items.filterNot { it.review.mastered }
    val highlightedItem = highlightedConcept?.let { concept ->
        activeRaw.firstOrNull { it.review.concept.equals(concept, ignoreCase = true) }
    }
    val active = if (highlightedItem == null) {
        activeRaw
    } else {
        listOf(highlightedItem) + activeRaw.filterNot { it.review.id == highlightedItem.review.id }
    }
    val mastered = items.filter { it.review.mastered }
    val activeByCategory = active.groupBy { it.review.category }
    val highlightedCategory = highlightedItem?.review?.category
    val orderedActiveCategories = activeByCategory.keys.sortedWith(
        compareBy<String> {
            if (highlightedCategory != null && it.equals(highlightedCategory, ignoreCase = true)) 0 else 1
        }.thenBy { it.lowercase() }
    )
    val categoryQuizEnabled = activeSessions.size < QuizViewModel.MAX_ACTIVE_SESSIONS

    var highlightedCardY by remember(highlightedItem?.review?.id) { mutableStateOf<Int?>(null) }
    var highlightPulseReady by remember(highlightedItem?.review?.id) { mutableStateOf(false) }

    LaunchedEffect(highlightedItem?.review?.id) {
        if (highlightedItem == null) return@LaunchedEffect
        highlightPulseReady = false

        val cardY = snapshotFlow { highlightedCardY }
            .filterNotNull()
            .first()

        scrollState.animateScrollTo(
            value = cardY.coerceIn(0, scrollState.maxValue),
            animationSpec = tween(durationMillis = 330)
        )
        delay(5)
        highlightPulseReady = true
    }

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
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ReviewHeader(onBack)

        Text(
            "À revoir",
            color = ReviewText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "CyberQuiz mémorise les questions que tu rates sans afficher leur réponse. Elles sont maintenant regroupées par catégorie pour te permettre de travailler un domaine entier d'un coup.",
            color = ReviewMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Text(
            "Premier retest réussi : +5 XP une seule fois par notion. Les retests suivants ne donnent plus d'XP.",
            color = ReviewGreen,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewSummaryCard(
                value = active.size.toString(),
                label = "À retravailler",
                accent = ReviewOrange,
                modifier = Modifier.weight(1f)
            )
            ReviewSummaryCard(
                value = mastered.size.toString(),
                label = "Maîtrisées",
                accent = ReviewGreen,
                modifier = Modifier.weight(1f)
            )
        }

        if (items.isEmpty()) {
            EmptyReviewCard()
        } else {
            if (active.isNotEmpty()) {
                SectionTitleReview("PRIORITÉ · À RETRAVAILLER")
                orderedActiveCategories.forEach { category ->
                    val categoryItems = activeByCategory[category].orEmpty()
                    ReviewCategoryCard(
                        category = category,
                        questionCount = categoryItems.size,
                        quizEnabled = categoryQuizEnabled,
                        onLaunchQuiz = { onCategoryQuiz(category, categoryItems.size) }
                    )

                    categoryItems.forEach { item ->
                        val isHighlighted = highlightedItem?.review?.id == item.review.id
                        ReviewItemCard(
                            item = item,
                            highlighted = isHighlighted,
                            flashHighlight = isHighlighted && highlightPulseReady,
                            modifier = if (isHighlighted) {
                                Modifier.onGloballyPositioned { coordinates ->
                                    if (highlightedCardY == null) {
                                        highlightedCardY = coordinates.positionInParent().y.roundToInt()
                                    }
                                }
                            } else {
                                Modifier
                            },
                            onCourse = if (hasCyberExpertCourse(item.review.concept)) {
                                {
                                    courseTerm = item.review.concept
                                    courseCategory = item.review.category
                                }
                            } else {
                                null
                            },
                            onPractice = { onPractice(item.review) }
                        )
                    }
                }
            }

            if (mastered.isNotEmpty()) {
                SectionTitleReview("MAÎTRISÉES APRÈS RÉVISION")
                mastered.forEach { item ->
                    ReviewItemCard(
                        item = item,
                        highlighted = false,
                        flashHighlight = false,
                        onCourse = if (hasCyberExpertCourse(item.review.concept)) {
                            {
                                courseTerm = item.review.concept
                                courseCategory = item.review.category
                            }
                        } else {
                            null
                        },
                        onPractice = { onPractice(item.review) }
                    )
                }
            }
        }

        Spacer(Modifier.height(if (highlightedItem != null) 720.dp else 8.dp))
    }

    courseTerm?.let { term ->
        CyberCourseDialog(
            term = term,
            category = courseCategory,
            onDismiss = { courseTerm = null }
        )
    }
}

@Composable
private fun ReviewHeader(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = ReviewText, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = ReviewText, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = ReviewPurple, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Text("CARNET D'APPRENTISSAGE", color = ReviewMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun ReviewSummaryCard(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color(0xFF081226), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .45f), RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text(label, color = ReviewMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ReviewCategoryCard(
    category: String,
    questionCount: Int,
    quizEnabled: Boolean,
    onLaunchQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(ReviewOrange.copy(alpha = .12f), Color(0xFF081226), ReviewPurple.copy(alpha = .06f))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.2.dp, ReviewOrange.copy(alpha = .55f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(ReviewOrange.copy(alpha = .13f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("#", color = ReviewOrange, fontSize = 17.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    category,
                    color = ReviewText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "$questionCount question${if (questionCount > 1) "s" else ""} à retravailler",
                    color = ReviewMuted,
                    fontSize = 10.sp
                )
            }
            Text(
                questionCount.toString(),
                color = ReviewOrange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(
                    if (quizEnabled) ReviewPurple.copy(alpha = .12f) else Color(0xFF111725),
                    RoundedCornerShape(14.dp)
                )
                .border(
                    1.1.dp,
                    if (quizEnabled) ReviewPurple.copy(alpha = .80f) else Color(0xFF38425B),
                    RoundedCornerShape(14.dp)
                )
                .clickable(enabled = quizEnabled, onClick = onLaunchQuiz),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (quizEnabled) "LANCER LE QUIZ · $questionCount" else "6 QUIZ EN COURS",
                color = if (quizEnabled) ReviewPurple else Color(0xFF677089),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .6.sp
            )
        }
    }
}

@Composable
private fun EmptyReviewCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF081226), RoundedCornerShape(22.dp))
            .border(1.dp, ReviewGreen.copy(alpha = .45f), RoundedCornerShape(22.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("✓", color = ReviewGreen, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("Rien à revoir pour l'instant", color = ReviewText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Les questions apparaîtront ici automatiquement dès qu'une question Cyber sera ratée.",
            color = ReviewMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionTitleReview(text: String) {
    Text(
        text,
        color = ReviewCyan,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp
    )
}

@Composable
private fun ReviewItemCard(
    item: ReviewItemWithQuestion,
    highlighted: Boolean,
    flashHighlight: Boolean,
    modifier: Modifier = Modifier,
    onCourse: (() -> Unit)?,
    onPractice: () -> Unit
) {
    val review = item.review
    val question = item.question
    val accent = when {
        highlighted -> ReviewPurple
        review.mastered -> ReviewGreen
        else -> ReviewOrange
    }
    val borderWidth = if (highlighted) 2.dp else 1.2.dp
    val glow = remember(review.id) { Animatable(0f) }

    LaunchedEffect(flashHighlight) {
        if (flashHighlight) {
            glow.snapTo(0f)
            repeat(2) {
                glow.animateTo(1f, animationSpec = tween(durationMillis = 450))
                glow.animateTo(0f, animationSpec = tween(durationMillis = 280))
            }
        } else {
            glow.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .drawBehind {
                if (highlighted && glow.value > 0f) {
                    val pulse = glow.value.coerceIn(0f, 1f)
                    val inset = 7.dp.toPx()
                    val radius = 22.dp.toPx()
                    val rectSize = Size(
                        width = size.width - inset * 2,
                        height = size.height - inset * 2
                    )
                    val topLeft = Offset(inset, inset)

                    drawRoundRect(
                        color = Color.White.copy(alpha = .12f * pulse),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 14.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = .28f * pulse),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = .96f * pulse),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 7.dp, vertical = 7.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (highlighted) {
                            listOf(
                                ReviewPurple.copy(alpha = .18f),
                                ReviewBlue.copy(alpha = .09f),
                                Color(0xFF071022)
                            )
                        } else {
                            listOf(accent.copy(alpha = .08f), Color(0xFF081226), Color(0xFF071022))
                        }
                    ),
                    RoundedCornerShape(21.dp)
                )
                .border(
                    borderWidth,
                    accent.copy(alpha = if (highlighted) .95f else .55f),
                    RoundedCornerShape(21.dp)
                )
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (highlighted) {
                Box(
                    Modifier
                        .background(ReviewPurple.copy(alpha = .17f), RoundedCornerShape(50.dp))
                        .border(1.dp, ReviewCyan.copy(alpha = .65f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "SÉLECTIONNÉE DEPUIS STATISTIQUES",
                        color = ReviewCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(accent.copy(alpha = .13f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (review.mastered) "✓" else "!", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        question.question,
                        color = ReviewText,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "${review.category.uppercase()} · ${difficultyLabel(review.difficulty)}",
                        color = ReviewBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (review.mastered) "MAÎTRISÉ" else "À REVOIR",
                        color = accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${review.wrongCount} erreur${if (review.wrongCount > 1) "s" else ""}",
                        color = ReviewMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    "CHOIX POSSIBLES",
                    color = ReviewMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                ReviewAnswerChoice("A", question.answerA, highlighted)
                ReviewAnswerChoice("B", question.answerB, highlighted)
                ReviewAnswerChoice("C", question.answerC, highlighted)
                ReviewAnswerChoice("D", question.answerD, highlighted)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onCourse != null) {
                    SmallReviewButton(
                        text = "Réviser la notion",
                        accent = ReviewCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onCourse
                    )
                }
                SmallReviewButton(
                    text = "Me retester",
                    accent = ReviewPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onPractice
                )
            }
        }
    }
}

@Composable
private fun ReviewAnswerChoice(letter: String, text: String, highlighted: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (highlighted) ReviewPurple.copy(alpha = .07f) else Color(0xFF09162D),
                RoundedCornerShape(13.dp)
            )
            .border(
                1.dp,
                if (highlighted) ReviewPurple.copy(alpha = .28f) else Color(0xFF263B64),
                RoundedCornerShape(13.dp)
            )
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(25.dp)
                .background(ReviewBlue.copy(alpha = .13f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, color = ReviewCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(9.dp))
        Text(
            text,
            color = Color(0xFFD7E0F5),
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun difficultyLabel(difficulty: String): String = when (difficulty.uppercase()) {
    "HARD" -> "DIFFICILE"
    "MEDIUM" -> "MOYEN"
    else -> "FACILE"
}

@Composable
private fun SmallReviewButton(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(44.dp)
            .background(accent.copy(alpha = .09f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .65f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
