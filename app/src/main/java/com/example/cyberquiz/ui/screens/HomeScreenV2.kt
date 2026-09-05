package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.R
import com.example.cyberquiz.viewmodel.QuizViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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

    val activeReviewCount = reviewItems.count { !it.mastered }
    val xpInLevel = p.xp % 100
    val levelProgress = (xpInLevel / 100f).coerceIn(0f, 1f)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF01040D))
    ) {
        HomeCircuitBackground()

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeTopButton(TopIcon.SETTINGS, onSettings)
                Spacer(Modifier.width(12.dp))
                HomeLevelCard(
                    level = p.level,
                    xp = xpInLevel,
                    progress = levelProgress,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                HomeTopButton(TopIcon.PROFILE, onProfile)
            }

            Spacer(Modifier.height(18.dp))

            Image(
                painter = painterResource(R.drawable.cyberquiz_home_logo),
                contentDescription = "CyberQuiz",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(12.dp))

            MainQuizButton(
                subtitle = if (selectedQuizType == QuizType.CYBER) {
                    "Testez vos connaissances"
                } else {
                    "Quiz ${selectedQuizType.label}"
                },
                onClick = onQuiz
            )

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavCard(
                    title = "Mes statistiques",
                    subtitle = "Suivez vos progrès",
                    icon = HomeActionIcon.BARS,
                    accent = Color(0xFF9B5CFF),
                    modifier = Modifier.weight(1f),
                    onClick = onStats
                )
                HomeNavCard(
                    title = "Catégories",
                    subtitle = "Explorez les thèmes",
                    icon = HomeActionIcon.GRID,
                    accent = Color(0xFF12DDF3),
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
                    subtitle = if (activeReviewCount == 0) {
                        "Rien à retravailler"
                    } else {
                        "$activeReviewCount notion${if (activeReviewCount > 1) "s" else ""} à revoir"
                    },
                    icon = HomeActionIcon.REVIEW,
                    accent = Color(0xFF18D9E8),
                    modifier = Modifier.weight(1f),
                    onClick = onReview
                )
                HomeNavCard(
                    title = "Historique",
                    subtitle = if (history.isEmpty()) {
                        "Aucun quiz terminé"
                    } else {
                        "${history.size} quiz enregistré${if (history.size > 1) "s" else ""}"
                    },
                    icon = HomeActionIcon.HISTORY,
                    accent = Color(0xFF9B5CFF),
                    modifier = Modifier.weight(1f),
                    onClick = onHistory
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "“ Une meilleure cybersécurité\ncommence par de meilleures connaissances ”",
                color = Color(0xFFAFC4FF),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(8.dp))
            DigitalPlanet()
        }
    }
}

@Composable
private fun HomeCircuitBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF01040D),
                    Color(0xFF03102A),
                    Color(0xFF020713),
                    Color(0xFF01030A)
                )
            )
        )

        val cyan = Color(0xFF00CFFF).copy(alpha = .15f)
        val blue = Color(0xFF1677FF).copy(alpha = .13f)
        val purple = Color(0xFF8D39FF).copy(alpha = .13f)
        val w = size.width
        val h = size.height

        val leftYs = listOf(.11f, .18f, .27f, .37f, .51f, .66f, .79f)
        leftYs.forEachIndexed { i, yf ->
            val c = when (i % 3) {
                0 -> cyan
                1 -> purple
                else -> blue
            }
            val y = h * yf
            val p = Path().apply {
                moveTo(0f, y)
                lineTo(w * .07f, y)
                lineTo(w * .12f, y + if (i % 2 == 0) 34f else -34f)
                lineTo(w * .23f, y + if (i % 2 == 0) 34f else -34f)
                lineTo(w * .27f, y + if (i % 2 == 0) 10f else -10f)
            }
            drawPath(p, c, style = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(c.copy(alpha = .9f), 3.5f, Offset(w * .27f, y + if (i % 2 == 0) 10f else -10f))
        }

        val rightYs = listOf(.13f, .23f, .33f, .45f, .59f, .72f, .86f)
        rightYs.forEachIndexed { i, yf ->
            val c = when (i % 3) {
                0 -> purple
                1 -> cyan
                else -> blue
            }
            val y = h * yf
            val p = Path().apply {
                moveTo(w, y)
                lineTo(w * .93f, y)
                lineTo(w * .88f, y + if (i % 2 == 0) -34f else 34f)
                lineTo(w * .77f, y + if (i % 2 == 0) -34f else 34f)
                lineTo(w * .73f, y + if (i % 2 == 0) -10f else 10f)
            }
            drawPath(p, c, style = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(c.copy(alpha = .9f), 3.5f, Offset(w * .73f, y + if (i % 2 == 0) -10f else 10f))
        }

        repeat(18) { i ->
            val xx = w * ((i * 37 % 100) / 100f)
            val yy = h * ((i * 53 % 92) / 100f)
            val c = if (i % 2 == 0) cyan else purple
            drawCircle(c.copy(alpha = .5f), 2.4f, Offset(xx, yy))
        }
    }
}

private enum class TopIcon { SETTINGS, PROFILE }

@Composable
private fun HomeTopButton(icon: TopIcon, onClick: () -> Unit) {
    Box(
        Modifier
            .size(56.dp)
            .background(Color(0xFF06112A).copy(alpha = .94f), RoundedCornerShape(18.dp))
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(Color(0xFF7D38FF), Color(0xFF0DAEFF))),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(28.dp)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            when (icon) {
                TopIcon.SETTINGS -> {
                    repeat(8) { i ->
                        val a = Math.toRadians((i * 45).toDouble())
                        val dx = cos(a).toFloat()
                        val dy = sin(a).toFloat()
                        drawLine(
                            Color(0xFF9BD9FF),
                            Offset(c.x + dx * 9f, c.y + dy * 9f),
                            Offset(c.x + dx * 13f, c.y + dy * 13f),
                            3f,
                            StrokeCap.Round
                        )
                    }
                    drawCircle(Color(0xFF9BD9FF), 8.5f, c, style = Stroke(3f))
                    drawCircle(Color(0xFF06112A), 3f, c)
                }

                TopIcon.PROFILE -> {
                    drawCircle(
                        Color(0xFF71DFFF),
                        radius = 5.5f,
                        center = Offset(c.x, c.y - 6f)
                    )
                    drawArc(
                        Color(0xFF71DFFF),
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(4f, 12f),
                        size = Size(20f, 15f),
                        style = Stroke(5f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLevelCard(level: Int, xp: Int, progress: Float, modifier: Modifier = Modifier) {
    Column(
        modifier
            .height(70.dp)
            .background(Color(0xFF06112A).copy(alpha = .94f), RoundedCornerShape(22.dp))
            .border(1.2.dp, Color(0xFF315DCE), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Niveau ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(level.toString(), color = Color(0xFFD04BFF), fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("$xp / 100 XP", color = Color(0xFF9AB9FA), fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(9.dp)
                .background(Color(0xFF142A58), RoundedCornerShape(50.dp))
        ) {
            if (progress > 0f) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF9D27FF), Color(0xFF00D7F5))),
                            RoundedCornerShape(50.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun MainQuizButton(subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(108.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF9B28FF), Color(0xFF315BFF), Color(0xFF06C6F4))
                ),
                RoundedCornerShape(25.dp)
            )
            .border(1.7.dp, Color(0xFFBEEFFF), RoundedCornerShape(25.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(46.dp)) {
            val p = Path().apply {
                moveTo(size.width * .25f, size.height * .15f)
                lineTo(size.width * .82f, size.height * .5f)
                lineTo(size.width * .25f, size.height * .85f)
                close()
            }
            drawPath(p, Color.White.copy(alpha = .95f))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Commencer un quiz",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                color = Color(0xFFD4E5FF),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("›", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Light)
    }
}

private enum class HomeActionIcon { BARS, GRID, REVIEW, HISTORY }

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
            .height(112.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF07132D), Color(0xFF030A1B))),
                RoundedCornerShape(22.dp)
            )
            .border(1.25.dp, accent.copy(alpha = .9f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeActionIconCanvas(icon, accent)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color(0xFF8DBAF8), fontSize = 11.sp, lineHeight = 14.sp)
        }
        Text("›", color = Color(0xFF75C8FF), fontSize = 28.sp)
    }
}

@Composable
private fun HomeActionIconCanvas(icon: HomeActionIcon, accent: Color) {
    Canvas(Modifier.size(38.dp)) {
        when (icon) {
            HomeActionIcon.BARS -> {
                val bw = size.width * .18f
                drawRoundRect(accent.copy(alpha = .65f), Offset(size.width * .08f, size.height * .55f), Size(bw, size.height * .28f))
                drawRoundRect(accent.copy(alpha = .8f), Offset(size.width * .39f, size.height * .36f), Size(bw, size.height * .47f))
                drawRoundRect(accent, Offset(size.width * .70f, size.height * .16f), Size(bw, size.height * .67f))
            }

            HomeActionIcon.GRID -> {
                val s = size.width * .27f
                val gap = size.width * .15f
                listOf(0f, s + gap).forEach { x ->
                    listOf(0f, s + gap).forEach { y ->
                        drawRoundRect(accent, Offset(x + 2f, y + 2f), Size(s, s), style = Stroke(3f))
                    }
                }
            }

            HomeActionIcon.REVIEW -> {
                drawRoundRect(
                    accent,
                    Offset(size.width * .15f, size.height * .08f),
                    Size(size.width * .68f, size.height * .82f),
                    CornerRadius(5f, 5f),
                    style = Stroke(3f)
                )
                repeat(3) { i ->
                    val yy = size.height * (.31f + i * .18f)
                    drawLine(accent, Offset(size.width * .30f, yy), Offset(size.width * .67f, yy), 3f, StrokeCap.Round)
                }
            }

            HomeActionIcon.HISTORY -> {
                val center = Offset(size.width * .52f, size.height * .52f)
                drawCircle(accent, size.minDimension * .34f, center, style = Stroke(3f))
                drawLine(accent, center, Offset(center.x, size.height * .28f), 3f, StrokeCap.Round)
                drawLine(accent, center, Offset(size.width * .69f, size.height * .61f), 3f, StrokeCap.Round)
                drawArc(
                    accent,
                    startAngle = 150f,
                    sweepAngle = 75f,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width * .45f, size.height * .45f),
                    style = Stroke(3f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun DigitalPlanet() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * 1.10f)
            val rx = w * .62f
            val ry = h * .80f
            val cyan = Color(0xFF1BBFFF).copy(alpha = .92f)
            val blue = Color(0xFF1D66FF).copy(alpha = .64f)
            val purple = Color(0xFF9B5CFF).copy(alpha = .65f)

            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF006CFF).copy(alpha = .33f), Color.Transparent),
                    center = Offset(w / 2f, h * .82f),
                    radius = w * .62f
                ),
                radius = w * .62f,
                center = Offset(w / 2f, h * .82f)
            )

            drawArc(
                cyan,
                startAngle = 184f,
                sweepAngle = 172f,
                useCenter = false,
                topLeft = Offset(center.x - rx, center.y - ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(3.2f, cap = StrokeCap.Round)
            )

            for (i in 1..5) {
                val insetX = rx * (.05f * i)
                val insetY = ry * (.08f * i)
                drawArc(
                    if (i % 2 == 0) purple else blue,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(center.x - rx + insetX, center.y - ry + insetY),
                    size = Size((rx - insetX) * 2f, (ry - insetY) * 2f),
                    style = Stroke(1.2f)
                )
            }

            for (i in -6..6) {
                val sx = center.x + i * (w * .075f)
                val ex = center.x + i * (w * .018f)
                drawLine(
                    if (i % 3 == 0) purple else blue,
                    Offset(sx, h * .35f + abs(i) * 3f),
                    Offset(ex, h),
                    1.2f
                )
            }

            val nodes = listOf(
                Offset(w * .08f, h * .66f),
                Offset(w * .22f, h * .46f),
                Offset(w * .36f, h * .37f),
                Offset(w * .50f, h * .31f),
                Offset(w * .64f, h * .38f),
                Offset(w * .78f, h * .48f),
                Offset(w * .92f, h * .67f)
            )
            nodes.forEachIndexed { i, node ->
                val c = if (i % 3 == 0) purple else cyan
                drawCircle(c.copy(alpha = .18f), 10f, node)
                drawCircle(c, 3.2f, node)
                if (i < nodes.lastIndex) {
                    drawLine(c.copy(alpha = .38f), node, nodes[i + 1], 1.1f)
                }
            }
        }
    }
}
