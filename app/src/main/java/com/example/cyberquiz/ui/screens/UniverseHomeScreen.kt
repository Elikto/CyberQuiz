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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.cyberquiz.viewmodel.QuizViewModel
import kotlin.math.cos
import kotlin.math.sin

private val NutritionGreen = Color(0xFF42E59D)
private val NutritionCyan = Color(0xFF28DDE2)
private val NutritionBlue = Color(0xFF2D9BFF)
private val NutritionText = Color(0xFFF3FFF9)
private val NutritionMuted = Color(0xFFA9C9BF)

@Composable
fun UniverseHomeScreen(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onReview: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    when (selectedQuizType) {
        QuizType.CYBERSECURITY -> HomeScreenV2(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onQuiz = onQuiz,
            onStats = onStats,
            onCategories = onCategories,
            onReview = onReview,
            onProfile = onProfile,
            onSettings = onSettings
        )

        QuizType.NUTRITION -> NutritionHomeScreen(
            vm = vm,
            onQuiz = onQuiz,
            onStats = onStats,
            onCategories = onCategories,
            onProfile = onProfile,
            onSettings = onSettings
        )

        else -> FutureUniverseHomeScreen(
            quizType = selectedQuizType,
            onQuiz = onQuiz,
            onStats = onStats,
            onCategories = onCategories,
            onProfile = onProfile,
            onSettings = onSettings
        )
    }
}

@Composable
private fun NutritionHomeScreen(
    vm: QuizViewModel,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    val p by vm.progress.collectAsState()
    val currentXp = p.xp % 100
    val xpProgress = (currentXp / 100f).coerceIn(0f, 1f)
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered
    val levelTitle = when {
        p.level >= 15 -> "Maître de l'équilibre"
        p.level >= 10 -> "Connaisseur Nutrition"
        p.level >= 5 -> "Explorateur Nutrition"
        else -> "Curieux Nutrition"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020D0B),
                        Color(0xFF061B18),
                        Color(0xFF071411),
                        Color(0xFF020A09)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NutritionTopButton(settings = true, onClick = onSettings)
            NutritionTopButton(settings = false, onClick = onProfile)
        }

        Spacer(Modifier.height(4.dp))
        NutritionHeroLogo()

        Row(verticalAlignment = Alignment.Bottom) {
            Text("Nutrition", color = NutritionText, fontSize = 37.sp, fontWeight = FontWeight.Black)
            Text("Quiz", color = NutritionGreen, fontSize = 37.sp, fontWeight = FontWeight.Black)
        }
        Text(
            "APPRENDS  ·  JOUE  ·  ÉQUILIBRE",
            color = NutritionMuted,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp
        )

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(Color(0xFF0B2822), RoundedCornerShape(50.dp))
                .border(1.dp, NutritionGreen.copy(alpha = .55f), RoundedCornerShape(50.dp))
                .padding(horizontal = 13.dp, vertical = 6.dp)
        ) {
            Text(
                "UNIVERS : NUTRITION",
                color = Color(0xFFC8FFE8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        NutritionLevelCard(
            level = p.level,
            title = levelTitle,
            xp = currentXp,
            progress = xpProgress
        )

        Spacer(Modifier.height(14.dp))
        Text(
            "« Comprendre ce que l'on mange,\nc'est déjà mieux choisir. »",
            color = Color(0xFFC8E4DB),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(18.dp))
        NutritionMenuCard(
            title = "Commencer",
            subtitle = "Lancer un nouveau quiz Nutrition",
            accent = NutritionGreen,
            dark = Color(0xFF103C2C),
            icon = NutritionAction.PLAY,
            onClick = onQuiz
        )
        Spacer(Modifier.height(10.dp))
        NutritionMenuCard(
            title = "Statistiques",
            subtitle = "Suis ta progression Nutrition",
            accent = NutritionCyan,
            dark = Color(0xFF07383A),
            icon = NutritionAction.BARS,
            onClick = onStats
        )
        Spacer(Modifier.height(10.dp))
        NutritionMenuCard(
            title = "Catégories",
            subtitle = "Choisis ton thème Nutrition",
            accent = Color(0xFF79E96A),
            dark = Color(0xFF173A16),
            icon = NutritionAction.GRID,
            onClick = onCategories
        )

        Spacer(Modifier.height(15.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionStatCard("🔥", p.streak.toString(), "Série", Modifier.weight(1f), Color(0xFFFFB84A))
            NutritionStatCard("★", p.xp.toString(), "XP", Modifier.weight(1f), NutritionGreen)
            NutritionStatCard("▥", p.level.toString(), "Niveau", Modifier.weight(1f), NutritionCyan)
            NutritionStatCard("✓", "$accuracy%", "Réussite", Modifier.weight(1f), Color(0xFF8BE86D))
        }

        Spacer(Modifier.height(10.dp))
        NutritionFooter()
    }
}

@Composable
private fun NutritionTopButton(settings: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF16463B), Color(0xFF071A17))
                ),
                CircleShape
            )
            .border(1.3.dp, NutritionCyan.copy(alpha = .8f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(25.dp)) {
            if (settings) {
                val center = Offset(size.width / 2f, size.height / 2f)
                repeat(8) { i ->
                    val angle = Math.toRadians(i * 45.0)
                    val x = cos(angle).toFloat()
                    val y = sin(angle).toFloat()
                    drawLine(
                        color = NutritionText,
                        start = Offset(center.x + x * size.minDimension * .38f, center.y + y * size.minDimension * .38f),
                        end = Offset(center.x + x * size.minDimension * .47f, center.y + y * size.minDimension * .47f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(NutritionText, size.minDimension * .29f, center, style = Stroke(3f))
                drawCircle(Color(0xFF071A17), size.minDimension * .10f, center)
            } else {
                drawCircle(
                    color = NutritionText,
                    radius = size.minDimension * .17f,
                    center = Offset(size.width / 2f, size.height * .31f),
                    style = Stroke(3f)
                )
                drawArc(
                    color = NutritionText,
                    startAngle = 198f,
                    sweepAngle = 144f,
                    useCenter = false,
                    topLeft = Offset(size.width * .17f, size.height * .46f),
                    size = Size(size.width * .66f, size.height * .48f),
                    style = Stroke(3f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun NutritionHeroLogo() {
    Box(
        modifier = Modifier.fillMaxWidth().height(188.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * .56f)

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NutritionGreen.copy(alpha = .24f), Color.Transparent),
                    center = center,
                    radius = 150f
                ),
                radius = 150f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NutritionCyan.copy(alpha = .14f), Color.Transparent),
                    center = center,
                    radius = 205f
                ),
                radius = 205f,
                center = center
            )

            val line = NutritionGreen.copy(alpha = .42f)
            repeat(5) { i ->
                val y = h * (.25f + i * .13f)
                drawLine(line, Offset(20f, y), Offset(w * .29f, y), 1.7f, StrokeCap.Round)
                drawLine(line, Offset(w * .71f, y), Offset(w - 20f, y), 1.7f, StrokeCap.Round)
                drawCircle(NutritionCyan.copy(alpha = .72f), 3.3f, Offset(20f, y))
                drawCircle(NutritionCyan.copy(alpha = .72f), 3.3f, Offset(w - 20f, y))
            }

            val leaf = Path().apply {
                moveTo(center.x, h * .18f)
                cubicTo(w * .76f, h * .24f, w * .76f, h * .63f, center.x, h * .84f)
                cubicTo(w * .24f, h * .63f, w * .24f, h * .24f, center.x, h * .18f)
                close()
            }
            drawPath(
                path = leaf,
                brush = Brush.linearGradient(listOf(NutritionGreen, NutritionCyan)),
                style = Stroke(6f, cap = StrokeCap.Round)
            )

            val vein = Path().apply {
                moveTo(center.x, h * .75f)
                quadraticBezierTo(center.x - 6f, h * .50f, center.x + 3f, h * .27f)
            }
            drawPath(vein, NutritionText.copy(alpha = .9f), style = Stroke(4f, cap = StrokeCap.Round))
            drawLine(
                NutritionText.copy(alpha = .72f),
                Offset(center.x, h * .52f),
                Offset(w * .39f, h * .40f),
                3f,
                StrokeCap.Round
            )
            drawLine(
                NutritionText.copy(alpha = .72f),
                Offset(center.x, h * .58f),
                Offset(w * .62f, h * .45f),
                3f,
                StrokeCap.Round
            )
        }
    }
}

@Composable
private fun NutritionLevelCard(level: Int, title: String, xp: Int, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0C2923), Color(0xFF08251F), Color(0xFF071B18))
                ),
                RoundedCornerShape(22.dp)
            )
            .border(1.35.dp, NutritionGreen.copy(alpha = .8f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NutritionLevelBadge(level)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = NutritionText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$xp / 100 XP", color = NutritionCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text("◒", color = NutritionGreen, fontSize = 30.sp)
        }
        Spacer(Modifier.height(11.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFF1C443A), RoundedCornerShape(50.dp))
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(listOf(NutritionGreen, NutritionCyan)),
                            RoundedCornerShape(50.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun NutritionLevelBadge(level: Int) {
    Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val shape = Path().apply {
                moveTo(w * .5f, h * .06f)
                lineTo(w * .84f, h * .26f)
                lineTo(w * .84f, h * .74f)
                lineTo(w * .5f, h * .94f)
                lineTo(w * .16f, h * .74f)
                lineTo(w * .16f, h * .26f)
                close()
            }
            drawPath(
                shape,
                brush = Brush.linearGradient(listOf(NutritionGreen, NutritionCyan)),
                style = Stroke(5f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NIV.", color = Color(0xFFD9F7ED), fontSize = 9.sp)
            Text(level.toString(), color = NutritionText, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

private enum class NutritionAction { PLAY, BARS, GRID }

@Composable
private fun NutritionMenuCard(
    title: String,
    subtitle: String,
    accent: Color,
    dark: Color,
    icon: NutritionAction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(dark, accent.copy(alpha = .15f), Color(0xFF071C18))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.6.dp, accent, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(62.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(31.dp)) {
                when (icon) {
                    NutritionAction.PLAY -> drawPath(
                        Path().apply {
                            moveTo(size.width * .25f, size.height * .15f)
                            lineTo(size.width * .8f, size.height * .5f)
                            lineTo(size.width * .25f, size.height * .85f)
                            close()
                        },
                        NutritionText
                    )
                    NutritionAction.BARS -> {
                        val bw = size.width * .18f
                        drawRoundRect(accent, Offset(size.width * .08f, size.height * .52f), Size(bw, size.height * .28f))
                        drawRoundRect(accent, Offset(size.width * .4f, size.height * .35f), Size(bw, size.height * .45f))
                        drawRoundRect(accent, Offset(size.width * .72f, size.height * .16f), Size(bw, size.height * .64f))
                    }
                    NutritionAction.GRID -> {
                        val cell = size.width * .28f
                        val gap = size.width * .12f
                        listOf(0f, cell + gap).forEach { x ->
                            listOf(0f, cell + gap).forEach { y ->
                                drawRoundRect(accent, Offset(x, y), Size(cell, cell))
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxHeight(.66f).width(1.dp).background(accent.copy(alpha = .28f)))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = NutritionText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFC5DDD6), fontSize = 12.sp)
        }
        Text("›", color = NutritionText, fontSize = 31.sp)
    }
}

@Composable
private fun NutritionStatCard(
    symbol: String,
    value: String,
    label: String,
    modifier: Modifier,
    accent: Color
) {
    Column(
        modifier = modifier
            .height(92.dp)
            .background(Color(0xFF071B17), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1C5A49), RoundedCornerShape(16.dp))
            .padding(3.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 18.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = NutritionText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(label, color = NutritionMuted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NutritionFooter() {
    Box(
        modifier = Modifier.fillMaxWidth().height(132.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * 1.05f)
            val radius = w * .44f
            val main = NutritionGreen.copy(alpha = .82f)
            val dim = NutritionCyan.copy(alpha = .38f)

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NutritionGreen.copy(alpha = .17f), Color.Transparent),
                    center = Offset(w / 2f, h * .88f),
                    radius = w * .46f
                ),
                radius = w * .46f,
                center = Offset(w / 2f, h * .88f)
            )

            drawArc(
                color = main,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(3f, cap = StrokeCap.Round)
            )

            repeat(4) { i ->
                val inset = 18f + i * 18f
                drawArc(
                    color = dim,
                    startAngle = 198f,
                    sweepAngle = 144f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius + inset, center.y - radius + inset),
                    size = Size((radius - inset) * 2f, (radius - inset) * 2f),
                    style = Stroke(1.4f)
                )
            }

            val leaf = Path().apply {
                moveTo(w * .50f, h * .92f)
                cubicTo(w * .38f, h * .64f, w * .57f, h * .49f, w * .69f, h * .54f)
                cubicTo(w * .70f, h * .70f, w * .62f, h * .84f, w * .50f, h * .92f)
            }
            drawPath(leaf, NutritionGreen, style = Stroke(2.5f, cap = StrokeCap.Round))
        }

        Text(
            "UN MEILLEUR ÉQUILIBRE\nCOMMENCE PAR TOI",
            color = Color(0xFFD0EADF),
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            letterSpacing = 2.2.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun FutureUniverseHomeScreen(
    quizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    val accent = when (quizType) {
        QuizType.TAROT -> Color(0xFFD980FF)
        QuizType.LITHOTHERAPY -> Color(0xFF65D9FF)
        QuizType.GENERAL_KNOWLEDGE -> Color(0xFFFFC75C)
        else -> Color(0xFF8B7CFF)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF050713), Color(0xFF0A1020), Color(0xFF040711))))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("⚙", color = Color.White, fontSize = 25.sp, modifier = Modifier.clickable(onClick = onSettings))
            Text("◯", color = Color.White, fontSize = 25.sp, modifier = Modifier.clickable(onClick = onProfile))
        }

        Spacer(Modifier.weight(.7f))
        Text(quizType.symbol, color = accent, fontSize = 78.sp)
        Spacer(Modifier.height(16.dp))
        Text(quizType.label, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("UNIVERS CYBERQUIZ", color = accent, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            quizType.description,
            color = Color(0xFFB9C3E0),
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .background(accent.copy(alpha = .10f), RoundedCornerShape(50.dp))
                .border(1.dp, accent.copy(alpha = .7f), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("BIENTÔT DISPONIBLE", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        }
        Spacer(Modifier.weight(1f))

        FutureAction("Commencer", accent, onQuiz)
        Spacer(Modifier.height(10.dp))
        FutureAction("Statistiques", accent, onStats)
        Spacer(Modifier.height(10.dp))
        FutureAction("Catégories", accent, onCategories)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun FutureAction(title: String, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color(0xFF0B1225), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .55f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("›", color = accent, fontSize = 27.sp)
    }
}
