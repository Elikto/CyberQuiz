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
    highlightedConcept: String? = null
) {
    val items by vm.reviewItems.collectAsState()
    val scrollState = rememberScrollState()
    var courseTerm by remember { mutableStateOf<String?>(null) }
    var courseCategory by remember { mutableStateOf("") }

    val activeRaw = items.filterNot { it.mastered }
    val highlightedItem = highlightedConcept?.let { concept ->
        activeRaw.firstOrNull { it.concept.equals(concept, ignoreCase = true) }
    }
    val active = if (highlightedItem == null) {
        activeRaw
    } else {
        listOf(highlightedItem) + activeRaw.filterNot { it.id == highlightedItem.id }
    }
    val mastered = items.filter { it.mastered }

    var highlightedCardY by remember(highlightedItem?.id) { mutableStateOf<Int?>(null) }
    var highlightPulseReady by remember(highlightedItem?.id) { mutableStateOf(false) }

    // Un seul déplacement automatique par ouverture depuis Statistiques.
    // La position est capturée une fois puis le scroll n'est plus jamais relancé,
    // ce qui laisse ensuite le contrôle total au geste de l'utilisateur.
    LaunchedEffect(highlightedItem?.id) {
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
            "CyberQuiz mémorise les questions que tu rates sans afficher leur réponse. Tu peux te retester à l'aveugle ou choisir de réviser la notion avant.",
            color = ReviewMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        if (highlightedItem != null) {
            SelectedFromStatsBanner()
        }

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
                active.forEach { item ->
                    val isHighlighted = highlightedItem?.id == item.id
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
                        onCourse = if (hasCyberExpertCourse(item.concept)) {
                            {
                                courseTerm = item.concept
                                courseCategory = item.category
                            }
                        } else {
                            null
                        },
                        onPractice = { onPractice(item) }
                    )
                }
            }

            if (mastered.isNotEmpty()) {
                SectionTitleReview("MAÎTRISÉES APRÈS RÉVISION")
                mastered.forEach { item ->
                    ReviewItemCard(
                        item = item,
                        highlighted = false,
                        flashHighlight = false,
                        onCourse = if (hasCyberExpertCourse(item.concept)) {
                            {
                                courseTerm = item.concept
                                courseCategory = item.category
                            }
                        } else {
                            null
                        },
                        onPractice = { onPractice(item) }
                    )
                }
            }
        }

        // Permet de placer réellement la carte sélectionnée en haut même si la liste est courte.
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
private fun SelectedFromStatsBanner() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(ReviewPurple.copy(alpha = .15f), ReviewBlue.copy(alpha = .10f))
                ),
                RoundedCornerShape(17.dp)
            )
            .border(1.2.dp, ReviewPurple.copy(alpha = .70f), RoundedCornerShape(17.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(32.dp)
                .background(ReviewPurple.copy(alpha = .16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("↳", color = ReviewCyan, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "QUESTION SÉLECTIONNÉE DEPUIS STATISTIQUES",
            color = ReviewCyan,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.1.sp,
            modifier = Modifier.weight(1f)
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
    item: ReviewItemEntity,
    highlighted: Boolean,
    flashHighlight: Boolean,
    modifier: Modifier = Modifier,
    onCourse: (() -> Unit)?,
    onPractice: () -> Unit
) {
    val accent = when {
        highlighted -> ReviewPurple
        item.mastered -> ReviewGreen
        else -> ReviewOrange
    }
    val borderWidth = if (highlighted) 2.dp else 1.2.dp
    val glow = remember(item.id) { Animatable(0f) }

    LaunchedEffect(flashHighlight) {
        if (flashHighlight) {
            glow.snapTo(0f)
            repeat(2) {
                glow.animateTo(1f, animationSpec = tween(durationMillis = 430))
                glow.animateTo(0f, animationSpec = tween(durationMillis = 260))
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

                    // Halo large et doux.
                    drawRoundRect(
                        color = Color.White.copy(alpha = .12f * pulse),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 14.dp.toPx())
                    )
                    // Halo intermédiaire plus lumineux.
                    drawRoundRect(
                        color = Color.White.copy(alpha = .28f * pulse),
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Trait blanc net au maximum de la pulsation.
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(accent.copy(alpha = .13f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (item.mastered) "✓" else "!", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (item.mastered) "Question maîtrisée" else "Question à retravailler",
                        color = ReviewText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "${item.category.uppercase()} · ${difficultyLabel(item.difficulty)}",
                        color = ReviewBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (item.mastered) "MAÎTRISÉ" else "À REVOIR",
                        color = accent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${item.wrongCount} erreur${if (item.wrongCount > 1) "s" else ""}",
                        color = ReviewMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                item.question,
                color = if (highlighted) ReviewText else Color(0xFFD2DCF4),
                fontSize = if (highlighted) 13.sp else 12.sp,
                lineHeight = 18.sp,
                fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal
            )

            if (!item.mastered && item.correctAfterWrongCount == 0) {
                Text(
                    "Premier retest réussi : +5 XP. Les essais suivants ne donnent plus d'XP.",
                    color = ReviewGreen,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            } else {
                Text(
                    "Révision sans bonus XP.",
                    color = ReviewMuted,
                    fontSize = 10.sp
                )
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
