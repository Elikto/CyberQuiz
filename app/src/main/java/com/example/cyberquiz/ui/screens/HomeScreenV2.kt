package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.ui.theme.CyberBlue
import com.example.cyberquiz.ui.theme.CyberPurple
import com.example.cyberquiz.viewmodel.QuizViewModel

@Composable
fun HomeScreenV2(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    val p by vm.progress.collectAsState()
    val xpInLevel = p.xp % 100
    val xpProgress = xpInLevel / 100f
    val accuracy = if (p.answered == 0) 0 else (p.correct * 100 / p.answered)
    val titleLevel = when {
        p.level >= 15 -> "Architecte Sécurité"
        p.level >= 10 -> "Hacker Éthique"
        p.level >= 5 -> "Analyste SOC"
        else -> "Débutant Cyber"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF060B19), CyberBackground, Color(0xFF030712))
                )
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeTopButton(HomeTopIcon.SETTINGS, onClick = onSettings)
            HomeTopButton(HomeTopIcon.PROFILE, onClick = onProfile)
        }

        Spacer(Modifier.height(2.dp))
        HeroShieldSectionV2()

        Row(verticalAlignment = Alignment.Bottom) {
            Text("Cyber", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
            Text("Quiz", color = Color(0xFFE04FFF), fontSize = 44.sp, fontWeight = FontWeight.Black)
        }
        Text(
            text = "APPRENDS  ·  JOUE  ·  SÉCURISE",
            color = Color(0xFFAEB9EA),
            fontSize = 12.sp,
            letterSpacing = 2.2.sp
        )

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .background(Color(0xFF0D1730), RoundedCornerShape(50.dp))
                .border(1.dp, CyberBlue.copy(alpha = .55f), RoundedCornerShape(50.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = "QUIZ : ${selectedQuizType.label.uppercase()}",
                color = Color(0xFFBFD7FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp
            )
        }

        Spacer(Modifier.height(20.dp))
        LevelProgressCardV2(
            level = p.level,
            levelTitle = titleLevel,
            currentXp = xpInLevel,
            targetXp = 100,
            progress = xpProgress
        )

        Spacer(Modifier.height(18.dp))
        Text(
            text = "« La cybersécurité d'aujourd'hui\nconstruit un meilleur demain »",
            color = Color(0xFFC9D0F3),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(22.dp))
        HomeMenuActionCard(
            title = "Commencer",
            subtitle = "Lancer un nouveau quiz",
            accent = Color(0xFFD652FF),
            accentDark = Color(0xFF381160),
            iconKind = HomeActionIconKind.Play,
            onClick = onQuiz
        )
        Spacer(Modifier.height(12.dp))
        HomeMenuActionCard(
            title = "Statistiques",
            subtitle = "Suis ta progression",
            accent = Color(0xFF18BFFF),
            accentDark = Color(0xFF072B5B),
            iconKind = HomeActionIconKind.Bars,
            onClick = onStats
        )
        Spacer(Modifier.height(12.dp))
        HomeMenuActionCard(
            title = "Catégories",
            subtitle = "Choisis ton thème",
            accent = Color(0xFF19F2E5),
            accentDark = Color(0xFF06393B),
            iconKind = HomeActionIconKind.Grid,
            onClick = onCategories
        )

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeStatMiniCard("🔥", p.streak.toString(), "Série en cours", Modifier.weight(1f), Color(0xFFFFA61A))
            HomeStatMiniCard("★", p.xp.toString(), "Points totaux", Modifier.weight(1f), Color(0xFFD64CFF))
            HomeStatMiniCard("▥", p.level.toString(), "Niveau actuel", Modifier.weight(1f), Color(0xFF1AC3FF))
            HomeStatMiniCard("🏆", "$accuracy%", "Réussite", Modifier.weight(1f), Color(0xFFFFCC33))
        }

        Spacer(Modifier.height(20.dp))
        HomeGlobeFooter()
        Spacer(Modifier.height(6.dp))
    }
}

private enum class HomeTopIcon { SETTINGS, PROFILE }

@Composable
private fun HomeTopButton(icon: HomeTopIcon, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF14244A), Color(0xFF081123))), CircleShape)
            .border(1.4.dp, Color(0xFF829BEB), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(27.dp)) {
            when (icon) {
                HomeTopIcon.SETTINGS -> {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val outer = size.minDimension * .34f
                    val inner = size.minDimension * .13f
                    repeat(8) { i ->
                        val angle = Math.toRadians(i * 45.0)
                        val cos = kotlin.math.cos(angle).toFloat()
                        val sin = kotlin.math.sin(angle).toFloat()
                        drawLine(
                            Color(0xFFD7E0FF),
                            Offset(c.x + cos * outer, c.y + sin * outer),
                            Offset(c.x + cos * (outer + size.minDimension * .13f), c.y + sin * (outer + size.minDimension * .13f)),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    drawCircle(Color(0xFFD7E0FF), radius = outer, center = c, style = Stroke(width = 3.2f))
                    drawCircle(Color(0xFF081123), radius = inner, center = c)
                }
                HomeTopIcon.PROFILE -> {
                    drawCircle(
                        color = Color(0xFFD7E0FF),
                        radius = size.minDimension * .17f,
                        center = Offset(size.width / 2f, size.height * .32f),
                        style = Stroke(width = 3.2f)
                    )
                    drawArc(
                        color = Color(0xFFD7E0FF),
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(size.width * .18f, size.height * .46f),
                        size = Size(size.width * .64f, size.height * .48f),
                        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroShieldSectionV2() {
    Box(
        modifier = Modifier.fillMaxWidth().height(228.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * .47f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B2DFF).copy(alpha = .36f), Color.Transparent),
                    center = Offset(cx, cy), radius = 190f
                ),
                radius = 190f, center = Offset(cx, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00C8FF).copy(alpha = .17f), Color.Transparent),
                    center = Offset(cx, cy), radius = 240f
                ),
                radius = 240f, center = Offset(cx, cy)
            )

            val circuitA = Color(0xFF08C8FF).copy(alpha = .82f)
            val circuitB = Color(0xFF9D3CFF).copy(alpha = .72f)
            val ys = listOf(h * .28f, h * .40f, h * .52f, h * .64f, h * .76f)
            ys.forEachIndexed { i, y ->
                val color = if (i % 2 == 0) circuitA else circuitB
                val bend = 20f + i * 6f
                val left = Path().apply {
                    moveTo(cx - 104f, y)
                    lineTo(cx - 145f, y)
                    lineTo(cx - 165f, y - bend * .35f)
                    lineTo(42f + i * 7f, y - bend * .35f)
                }
                val right = Path().apply {
                    moveTo(cx + 104f, y)
                    lineTo(cx + 145f, y)
                    lineTo(cx + 165f, y - bend * .35f)
                    lineTo(w - 42f - i * 7f, y - bend * .35f)
                }
                drawPath(left, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                drawPath(right, color = color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                drawCircle(color, radius = 4.5f, center = Offset(42f + i * 7f, y - bend * .35f))
                drawCircle(color, radius = 4.5f, center = Offset(w - 42f - i * 7f, y - bend * .35f))
            }

            val shieldW = 198f
            val top = 18f
            val outer = Path().apply {
                moveTo(cx, top)
                lineTo(cx + shieldW * .48f, top + 48f)
                lineTo(cx + shieldW * .43f, top + 142f)
                quadraticBezierTo(cx + shieldW * .34f, top + 188f, cx, top + 211f)
                quadraticBezierTo(cx - shieldW * .34f, top + 188f, cx - shieldW * .43f, top + 142f)
                lineTo(cx - shieldW * .48f, top + 48f)
                close()
            }
            val inner = Path().apply {
                moveTo(cx, top + 18f)
                lineTo(cx + shieldW * .36f, top + 56f)
                lineTo(cx + shieldW * .32f, top + 132f)
                quadraticBezierTo(cx + shieldW * .25f, top + 166f, cx, top + 187f)
                quadraticBezierTo(cx - shieldW * .25f, top + 166f, cx - shieldW * .32f, top + 132f)
                lineTo(cx - shieldW * .36f, top + 56f)
                close()
            }

            drawPath(
                outer,
                brush = Brush.linearGradient(listOf(Color(0xFFE95FFF), Color(0xFF27D8FF), Color(0xFF2787FF))),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            drawPath(inner, color = Color(0xFF3AA4FF), style = Stroke(width = 3.5f))

            drawArc(
                color = Color(0xFFEA7CFF), startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(cx - 31f, top + 68f), size = Size(62f, 66f),
                style = Stroke(width = 9f, cap = StrokeCap.Round)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFFB65CFF), Color(0xFF3A8EFF))),
                topLeft = Offset(cx - 38f, top + 101f), size = Size(76f, 70f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawCircle(Color(0xFF08162F), radius = 11f, center = Offset(cx, top + 134f))
            drawLine(Color(0xFF08162F), Offset(cx, top + 143f), Offset(cx, top + 157f), strokeWidth = 6f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun LevelProgressCardV2(
    level: Int,
    levelTitle: String,
    currentXp: Int,
    targetXp: Int,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF0B1430), Color(0xFF07152E), Color(0xFF061024))),
                RoundedCornerShape(26.dp)
            )
            .border(1.5.dp, Color(0xFF2AAEFF), RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HomeHexLevelBadge(level)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(levelTitle, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("$currentXp / $targetXp XP", color = Color(0xFF21E3FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            HomeBarsMiniIcon()
        }

        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(11.dp).clip(RoundedCornerShape(20.dp)),
            color = CyberPurple,
            trackColor = Color(0xFF223A67)
        )
    }
}

@Composable
private fun HomeHexLevelBadge(level: Int) {
    Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * .50f, h * .06f)
                lineTo(w * .84f, h * .26f)
                lineTo(w * .84f, h * .74f)
                lineTo(w * .50f, h * .94f)
                lineTo(w * .16f, h * .74f)
                lineTo(w * .16f, h * .26f)
                close()
            }
            drawPath(path, brush = Brush.linearGradient(listOf(Color(0xFFE163FF), Color(0xFF00CFFF))), style = Stroke(width = 6f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NIV.", color = Color(0xFFE8ECFF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(level.toString(), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun HomeBarsMiniIcon() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(40.dp)) {
        Box(Modifier.width(7.dp).height(15.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
        Box(Modifier.width(7.dp).height(23.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
        Box(Modifier.width(7.dp).height(32.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
    }
}

private enum class HomeActionIconKind { Play, Bars, Grid }

@Composable
private fun HomeMenuActionCard(
    title: String,
    subtitle: String,
    accent: Color,
    accentDark: Color,
    iconKind: HomeActionIconKind,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(Brush.horizontalGradient(listOf(accentDark, accent.copy(alpha = .18f), Color(0xFF081327))), RoundedCornerShape(24.dp))
            .border(2.dp, accent, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(76.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            HomeActionIcon(iconKind, accent)
        }
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(accent.copy(alpha = .22f)))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFCBD4F8), fontSize = 13.sp)
        }
        Text("›", color = Color.White, fontSize = 36.sp)
    }
}

@Composable
private fun HomeActionIcon(iconKind: HomeActionIconKind, accent: Color) {
    Canvas(modifier = Modifier.size(34.dp)) {
        when (iconKind) {
            HomeActionIconKind.Play -> {
                val path = Path().apply {
                    moveTo(size.width * .25f, size.height * .15f)
                    lineTo(size.width * .80f, size.height * .50f)
                    lineTo(size.width * .25f, size.height * .85f)
                    close()
                }
                drawPath(path, color = Color.White)
            }
            HomeActionIconKind.Bars -> {
                val barW = size.width * .18f
                drawRoundRect(accent, topLeft = Offset(size.width * .08f, size.height * .52f), size = Size(barW, size.height * .28f))
                drawRoundRect(accent, topLeft = Offset(size.width * .40f, size.height * .35f), size = Size(barW, size.height * .45f))
                drawRoundRect(accent, topLeft = Offset(size.width * .72f, size.height * .16f), size = Size(barW, size.height * .64f))
            }
            HomeActionIconKind.Grid -> {
                val cell = size.width * .28f
                val gap = size.width * .12f
                listOf(0f, cell + gap).forEach { x ->
                    listOf(0f, cell + gap).forEach { y ->
                        drawRoundRect(accent, topLeft = Offset(x, y), size = Size(cell, cell))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStatMiniCard(symbol: String, value: String, label: String, modifier: Modifier, accent: Color) {
    Column(
        modifier = modifier
            .height(108.dp)
            .background(Color(0xFF071123), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF214B85), RoundedCornerShape(18.dp))
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color(0xFFC9D0F3), fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
    }
}

@Composable
private fun HomeGlobeFooter() {
    Box(modifier = Modifier.fillMaxWidth().height(155.dp), contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val arcTop = h * .35f
            val arcSize = Size(w * 1.1f, h * 1.25f)
            val arcLeft = -w * .05f
            for (i in 0..4) {
                drawArc(
                    color = if (i == 0) Color(0xFF2AC6FF) else Color(0xFF315D9B),
                    startAngle = 200f, sweepAngle = 140f, useCenter = false,
                    topLeft = Offset(arcLeft, arcTop + i * 10f),
                    size = Size(arcSize.width, arcSize.height - i * 22f),
                    style = Stroke(width = if (i == 0) 3f else 1.5f)
                )
            }
            val centerX = w / 2f
            for (i in -3..3) {
                drawLine(
                    color = Color(0xFF2E5A93),
                    start = Offset(centerX + i * 28f, h * .62f),
                    end = Offset(centerX + i * 18f, h * .98f),
                    strokeWidth = 1.4f
                )
            }
        }
        Text(
            text = "UN MONDE PLUS SÛR\nCOMMENCE PAR TOI",
            color = Color(0xFFD6DCF8),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            letterSpacing = 3.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
