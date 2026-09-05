package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.cyberquiz.R
import com.example.cyberquiz.viewmodel.QuizViewModel
import kotlin.math.*

private val HomeBg = Color(0xFF020611)
private val HomePurple = Color(0xFF9B5CFF)
private val HomeMagenta = Color(0xFFD94CFF)
private val HomeBlue = Color(0xFF25BFFF)
private val HomeCyan = Color(0xFF18E8F3)
private val HomeText = Color(0xFFF5F7FF)
private val HomeMuted = Color(0xFF9FC4F5)

@Composable
fun HomeScreenV2(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onReview: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    val p by vm.progress.collectAsState()
    val reviewItems by vm.reviewItems.collectAsState()
    val history by vm.quizHistory.collectAsState()

    val reviewCount = reviewItems.count { !it.mastered }
    val xpInLevel = p.xp % 100
    val progress = (xpInLevel / 100f).coerceIn(0f, 1f)

    Box(
        Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        HomeCircuitBackground()

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeTopButton(TopIcon.SETTINGS, onSettings)
                HomeLevelCard(
                    level = p.level,
                    xp = xpInLevel,
                    progress = progress,
                    modifier = Modifier.weight(1f)
                )
                HomeTopButton(TopIcon.PROFILE, onProfile)
            }

            Spacer(Modifier.height(18.dp))

            Image(
                painter = painterResource(R.drawable.cyberquiz_home_brand),
                contentDescription = "CyberQuiz",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(10.dp))
            MainQuizButton(onQuiz)
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavCard(
                    title = "Mes statistiques",
                    subtitle = "Suivez vos progrès",
                    icon = HomeActionIcon.BARS,
                    accent = HomePurple,
                    modifier = Modifier.weight(1f),
                    onClick = onStats
                )
                HomeNavCard(
                    title = "Catégories",
                    subtitle = "Explorez les thèmes",
                    icon = HomeActionIcon.GRID,
                    accent = HomeCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onCategories
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavCard(
                    title = "Révisions",
                    subtitle = if (reviewCount == 0) {
                        "Revoir mes quiz"
                    } else {
                        "$reviewCount notion${if (reviewCount > 1) "s" else ""} à revoir"
                    },
                    icon = HomeActionIcon.REVIEW,
                    accent = HomeCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onReview
                )
                HomeNavCard(
                    title = "Historique",
                    subtitle = if (history.isEmpty()) {
                        "Mes derniers quiz"
                    } else {
                        "${history.size} quiz enregistré${if (history.size > 1) "s" else ""}"
                    },
                    icon = HomeActionIcon.HISTORY,
                    accent = HomePurple,
                    modifier = Modifier.weight(1f),
                    onClick = onHistory
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "❝ Une meilleure cybersécurité\ncommence par de meilleures connaissances ❞",
                color = HomeMuted,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))
            DigitalPlanet()
        }
    }
}

private enum class TopIcon { SETTINGS, PROFILE }
private enum class HomeActionIcon { PLAY, BARS, GRID, REVIEW, HISTORY }

@Composable
private fun HomeTopButton(icon: TopIcon, onClick: () -> Unit) {
    Box(
        Modifier
            .size(62.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF071226), Color(0xFF0A1830))),
                RoundedCornerShape(18.dp)
            )
            .border(
                1.4.dp,
                Brush.linearGradient(listOf(HomePurple, HomeBlue)),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(31.dp)) {
            when (icon) {
                TopIcon.SETTINGS -> {
                    val c = Offset(size.width / 2, size.height / 2)
                    repeat(8) { i ->
                        val a = Math.toRadians(i * 45.0)
                        val x = cos(a).toFloat()
                        val y = sin(a).toFloat()
                        drawLine(
                            HomeMuted,
                            Offset(c.x + x * size.minDimension * .38f, c.y + y * size.minDimension * .38f),
                            Offset(c.x + x * size.minDimension * .49f, c.y + y * size.minDimension * .49f),
                            3.5f,
                            StrokeCap.Round
                        )
                    }
                    drawCircle(HomeMuted, size.minDimension * .29f, c, style = Stroke(3.5f))
                    drawCircle(Color(0xFF08162B), size.minDimension * .10f, c)
                }
                TopIcon.PROFILE -> {
                    drawCircle(
                        HomeCyan,
                        size.minDimension * .17f,
                        Offset(size.width / 2, size.height * .30f)
                    )
                    drawArc(
                        HomeCyan,
                        198f,
                        144f,
                        false,
                        Offset(size.width * .16f, size.height * .48f),
                        Size(size.width * .68f, size.height * .44f),
                        style = Stroke(5f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLevelCard(
    level: Int,
    xp: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .height(62.dp)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF071227), Color(0xFF091831))),
                RoundedCornerShape(20.dp)
            )
            .border(1.3.dp, Color(0xFF315EC0), RoundedCornerShape(20.dp))
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Niveau ", color = HomeText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(level.toString(), color = HomeMagenta, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("$xp / 100 XP", color = HomeMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(7.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF15294D), RoundedCornerShape(50.dp))
        ) {
            if (progress > 0f) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(listOf(HomeMagenta, Color(0xFF6E66FF), HomeCyan)),
                            RoundedCornerShape(50.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun MainQuizButton(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFB037FF), Color(0xFF555CFF), Color(0xFF0DCBFF))
                ),
                RoundedCornerShape(28.dp)
            )
            .border(
                1.8.dp,
                Brush.horizontalGradient(listOf(Color(0xFFF08CFF), HomeCyan)),
                RoundedCornerShape(28.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeActionCanvas(HomeActionIcon.PLAY, Color.White, 42.dp)
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Commencer un quiz",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Testez vos connaissances",
                color = Color(0xFFD6E8FF),
                fontSize = 16.sp
            )
        }
        Text("›", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun HomeNavCard(
    title: String,
    subtitle: String,
    icon: HomeActionIcon,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier
            .height(124.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF08152B), Color(0xFF061024))),
                RoundedCornerShape(22.dp)
            )
            .border(
                1.4.dp,
                Brush.linearGradient(listOf(accent, Color(0xFF2959C5))),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeActionCanvas(icon, accent, 39.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = HomeText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                subtitle,
                color = HomeMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Text("›", color = Color(0xFF5DC1FF), fontSize = 29.sp)
    }
}

@Composable
private fun HomeActionCanvas(
    icon: HomeActionIcon,
    accent: Color,
    sizeDp: Dp
) {
    Canvas(Modifier.size(sizeDp)) {
        when (icon) {
            HomeActionIcon.PLAY -> {
                drawPath(
                    Path().apply {
                        moveTo(size.width * .22f, size.height * .13f)
                        lineTo(size.width * .82f, size.height * .5f)
                        lineTo(size.width * .22f, size.height * .87f)
                        close()
                    },
                    accent
                )
            }
            HomeActionIcon.BARS -> {
                val bw = size.width * .18f
                drawRoundRect(accent, Offset(size.width * .08f, size.height * .55f), Size(bw, size.height * .28f), CornerRadius(4f))
                drawRoundRect(accent, Offset(size.width * .39f, size.height * .35f), Size(bw, size.height * .48f), CornerRadius(4f))
                drawRoundRect(accent, Offset(size.width * .70f, size.height * .13f), Size(bw, size.height * .70f), CornerRadius(4f))
            }
            HomeActionIcon.GRID -> {
                val side = size.width * .28f
                val gap = size.width * .12f
                listOf(0f, side + gap).forEach { x ->
                    listOf(0f, side + gap).forEach { y ->
                        drawRoundRect(
                            accent,
                            Offset(x + size.width * .10f, y + size.height * .10f),
                            Size(side, side),
                            CornerRadius(4f)
                        )
                    }
                }
            }
            HomeActionIcon.REVIEW -> {
                val left = size.width * .18f
                val top = size.height * .12f
                drawRoundRect(
                    accent,
                    Offset(left, top),
                    Size(size.width * .64f, size.height * .76f),
                    CornerRadius(7f),
                    style = Stroke(3.5f)
                )
                repeat(3) { i ->
                    val y = size.height * (.34f + i * .17f)
                    drawLine(accent, Offset(size.width * .32f, y), Offset(size.width * .68f, y), 3f, StrokeCap.Round)
                }
            }
            HomeActionIcon.HISTORY -> {
                val c = Offset(size.width * .52f, size.height * .52f)
                drawCircle(accent, size.minDimension * .34f, c, style = Stroke(3.8f))
                drawLine(accent, c, Offset(c.x, size.height * .28f), 3.2f, StrokeCap.Round)
                drawLine(accent, c, Offset(size.width * .68f, size.height * .59f), 3.2f, StrokeCap.Round)
                drawArc(
                    accent,
                    145f,
                    75f,
                    false,
                    Offset(size.width * .05f, size.height * .10f),
                    Size(size.width * .58f, size.height * .58f),
                    style = Stroke(3.5f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun HomeCircuitBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFF020611), Color(0xFF041027), Color(0xFF020611))
            )
        )

        val purple = HomePurple.copy(alpha = .30f)
        val blue = HomeBlue.copy(alpha = .26f)
        val faint = Color(0xFF1E4687).copy(alpha = .16f)

        var x = 28f
        while (x < size.width) {
            drawLine(faint, Offset(x, 0f), Offset(x, size.height), 1f)
            x += 76f
        }

        listOf(.14f, .22f, .30f, .66f, .74f, .82f).forEachIndexed { i, yf ->
            val y = size.height * yf
            val c = if (i % 2 == 0) purple else blue
            val pLeft = Path().apply {
                moveTo(0f, y)
                lineTo(size.width * .08f, y)
                lineTo(size.width * .15f, y + 42f)
                lineTo(size.width * .24f, y + 42f)
            }
            val pRight = Path().apply {
                moveTo(size.width, y)
                lineTo(size.width * .92f, y)
                lineTo(size.width * .85f, y + 42f)
                lineTo(size.width * .76f, y + 42f)
            }
            drawPath(pLeft, c, style = Stroke(2.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(pRight, c, style = Stroke(2.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(c, 4.5f, Offset(size.width * .24f, y + 42f))
            drawCircle(c, 4.5f, Offset(size.width * .76f, y + 42f))
        }

        repeat(18) { i ->
            val xx = size.width * ((i + 1) / 20f)
            val yy = if (i % 2 == 0) size.height * .11f else size.height * .89f
            drawCircle(if (i % 3 == 0) purple else blue, 3.5f, Offset(xx, yy))
        }
    }
}

@Composable
private fun DigitalPlanet() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(185.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h * 1.07f)
            val rx = w * .57f
            val ry = h * .92f
            val cyan = Color(0xFF24C8FF).copy(.88f)
            val dim = Color(0xFF315D9B).copy(.60f)
            val purple = HomePurple.copy(.62f)

            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF0078FF).copy(.28f), Color.Transparent),
                    Offset(w / 2, h * .90f),
                    w * .52f
                ),
                w * .52f,
                Offset(w / 2, h * .90f)
            )

            drawArc(
                cyan,
                188f,
                164f,
                false,
                Offset(center.x - rx, center.y - ry),
                Size(rx * 2, ry * 2),
                style = Stroke(3.4f, cap = StrokeCap.Round)
            )

            for (i in 1..5) {
                val ix = rx * (.055f * i)
                val iy = ry * (.08f * i)
                drawArc(
                    if (i == 2) purple else dim,
                    192f,
                    156f,
                    false,
                    Offset(center.x - rx + ix, center.y - ry + iy),
                    Size((rx - ix) * 2, (ry - iy) * 2),
                    style = Stroke(if (i == 2) 1.8f else 1.2f)
                )
            }

            for (i in -6..6) {
                val sx = center.x + i * (w * .07f)
                val ex = center.x + i * (w * .022f)
                drawLine(
                    if (i % 3 == 0) purple else dim,
                    Offset(sx, h * .43f + abs(i) * 2.2f),
                    Offset(ex, h),
                    1.15f
                )
            }

            val nodes = listOf(
                Offset(w * .12f, h * .60f),
                Offset(w * .25f, h * .47f),
                Offset(w * .42f, h * .54f),
                Offset(w * .58f, h * .45f),
                Offset(w * .76f, h * .55f),
                Offset(w * .90f, h * .48f)
            )
            nodes.zipWithNext().forEach { (a, b) ->
                drawLine(cyan.copy(.55f), a, b, 1.5f)
            }
            nodes.forEachIndexed { i, n ->
                val c = if (i % 2 == 0) cyan else purple
                drawCircle(c, 4.3f, n)
                drawCircle(c.copy(.20f), 10f, n)
            }
        }
    }
}
