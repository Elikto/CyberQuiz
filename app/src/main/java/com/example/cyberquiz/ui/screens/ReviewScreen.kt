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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.data.database.ReviewItemEntity
import com.example.cyberquiz.viewmodel.QuizViewModel

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
    onPractice: (ReviewItemEntity) -> Unit
) {
    val items by vm.reviewItems.collectAsState()
    var courseTerm by remember { mutableStateOf<String?>(null) }
    var courseCategory by remember { mutableStateOf("") }

    val active = items.filterNot { it.mastered }
    val mastered = items.filter { it.mastered }

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
            "CyberQuiz mémorise les notions que tu rates. Une bonne réponse plus tard les passe en maîtrisées, mais elles restent visibles pour que tu puisses les réviser.",
            color = ReviewMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp
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
                active.forEach { item ->
                    ReviewItemCard(
                        item = item,
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

        Spacer(Modifier.height(8.dp))
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
            "Les notions apparaîtront ici automatiquement dès qu'une question Cyber sera ratée.",
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
    onCourse: (() -> Unit)?,
    onPractice: () -> Unit
) {
    val accent = if (item.mastered) ReviewGreen else ReviewOrange
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = .08f), Color(0xFF081226), Color(0xFF071022))
                ),
                RoundedCornerShape(21.dp)
            )
            .border(1.2.dp, accent.copy(alpha = .55f), RoundedCornerShape(21.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                Text(item.concept, color = ReviewText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(item.category.uppercase(), color = ReviewBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
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
            color = Color(0xFFD2DCF4),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onCourse != null) {
                SmallReviewButton(
                    text = "Ouvrir le cours",
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
