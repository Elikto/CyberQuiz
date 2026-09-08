package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.update.CyberQuizUpdateManager
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
    val context = LocalContext.current

    val metrics = EngagementMetrics(
        answered = p.answered,
        correct = p.correct,
        xp = p.xp,
        level = p.level,
        streak = p.streak,
        bestStreak = p.bestStreak,
        quizCount = history.size
    )
    var engagement by remember(metrics) { mutableStateOf(EngagementStore.sync(context, metrics)) }
    var selectedAvatar by remember { mutableStateOf(storedPlayerAvatar(context)) }
    var selectedBanner by remember { mutableStateOf(storedPlayerBanner(context)) }
    var selectedFrame by remember { mutableStateOf(storedPlayerFrame(context)) }
    var coins by remember { mutableIntStateOf(engagement.coins) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var showShop by rememberSaveable { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf(false) }

    val activeReviewCount = reviewItems.count { !it.mastered }
    val xpIntoLevel = p.xp % 100
    val levelProgress = (xpIntoLevel / 100f).coerceIn(0f, 1f)
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered
    val title = when {
        p.level >= 15 -> "Architecte Sécurité"
        p.level >= 10 -> "Hacker Éthique"
        p.level >= 5 -> "Analyste SOC"
        else -> "Débutant Cyber"
    }

    fun reloadCosmeticsAndRewards() {
        selectedAvatar = storedPlayerAvatar(context)
        selectedBanner = storedPlayerBanner(context)
        selectedFrame = storedPlayerFrame(context)
        engagement = EngagementStore.sync(context, metrics)
        coins = engagement.coins
    }

    LaunchedEffect(Unit) {
        updateAvailable = CyberQuizUpdateManager.checkForUpdate() != null
    }

    if (showShop) {
        EngagementScreen(
            metrics = metrics,
            onBack = {
                showShop = false
                reloadCosmeticsAndRewards()
            }
        )
        return
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
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeTopButton(HomeTopIcon.SETTINGS, onSettings, showBadge = updateAvailable)
            HomeTopButton(HomeTopIcon.PROFILE, onProfile)
        }

        Spacer(Modifier.height(8.dp))
        HomePlayerHeader(
            level = p.level,
            title = title,
            xpIntoLevel = xpIntoLevel,
            progress = levelProgress,
            coins = coins,
            avatar = selectedAvatar,
            banner = selectedBanner,
            frame = selectedFrame,
            onAvatarClick = { showPicker = true },
            onCoinsClick = { showShop = true }
        )

        Spacer(Modifier.height(8.dp))
        HomeHeroLogo()

        Row(verticalAlignment = Alignment.Bottom) {
            Text("Cyber", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text("Quiz", color = Color(0xFFE04FFF), fontSize = 40.sp, fontWeight = FontWeight.Black)
        }
        Text(
            "APPRENDS  ·  JOUE  ·  SÉCURISE",
            color = Color(0xFFAEB9EA),
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .background(Color(0xFF0D1730), RoundedCornerShape(50.dp))
                .border(1.dp, Color(0xFF18BFFF).copy(alpha = .55f), RoundedCornerShape(50.dp))
                .padding(horizontal = 13.dp, vertical = 6.dp)
        ) {
            Text(
                "QUIZ : ${selectedQuizType.label.uppercase()}",
                color = Color(0xFFBFD7FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        HomeMenuCard("Commencer", "Lancer un nouveau quiz", Color(0xFFD652FF), onQuiz)
        Spacer(Modifier.height(9.dp))
        HomeMenuCard("Statistiques", "Suis ta progression", Color(0xFF18BFFF), onStats)
        Spacer(Modifier.height(9.dp))
        HomeMenuCard("Catégories", "Choisis ton thème", Color(0xFF19F2E5), onCategories)
        Spacer(Modifier.height(9.dp))
        HomeMenuCard(
            "À revoir",
            if (activeReviewCount == 0) "Aucune notion en attente" else "$activeReviewCount notion${if (activeReviewCount > 1) "s" else ""} à retravailler",
            Color(0xFFFFB84A),
            onReview
        )
        Spacer(Modifier.height(9.dp))
        HomeMenuCard(
            "Historique",
            if (history.isEmpty()) "Aucun quiz terminé" else "${history.size} quiz terminé${if (history.size > 1) "s" else ""}",
            Color(0xFF8B7CFF),
            onHistory
        )

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeStatCard("🔥", p.streak.toString(), "Série", Modifier.weight(1f), Color(0xFFFFA61A))
            HomeStatCard("★", p.xp.toString(), "XP", Modifier.weight(1f), Color(0xFFD64CFF))
            HomeStatCard("▥", p.level.toString(), "Niveau", Modifier.weight(1f), Color(0xFF1AC3FF))
            HomeStatCard("🏆", "$accuracy%", "Réussite", Modifier.weight(1f), Color(0xFFFFCC33))
        }

        Spacer(Modifier.height(12.dp))
        HomeDigitalPlanet()
    }

    if (showPicker) {
        PlayerAvatarPickerDialog(
            playerLevel = p.level,
            onSelectionChanged = { reloadCosmeticsAndRewards() },
            onDismiss = {
                showPicker = false
                reloadCosmeticsAndRewards()
            }
        )
    }
}

@Composable
private fun HomePlayerHeader(
    level: Int,
    title: String,
    xpIntoLevel: Int,
    progress: Float,
    coins: Int,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle,
    onAvatarClick: () -> Unit,
    onCoinsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.width(192.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0D2038), RoundedCornerShape(50.dp))
                        .border(1.dp, Color(0xFF25DFFF).copy(alpha = .65f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        "NIV. $level",
                        color = Color(0xFF2DE8FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.width(7.dp))
                Text(
                    title,
                    color = Color(0xFFF1F4FF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(132.dp)
                        .height(13.dp)
                        .background(Color(0xFF071323), RoundedCornerShape(50.dp))
                        .border(1.dp, Color(0xFF3F69A2), RoundedCornerShape(50.dp))
                        .padding(2.dp)
                ) {
                    if (progress > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6A34FF), Color(0xFFD54EFF), Color(0xFF27DFFF))
                                    ),
                                    RoundedCornerShape(50.dp)
                                )
                        )
                    }
                }
                Spacer(Modifier.width(7.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF21163A), RoundedCornerShape(50.dp))
                        .border(1.dp, Color(0xFFFFB84A).copy(alpha = .75f), RoundedCornerShape(50.dp))
                        .clickable(onClick = onCoinsClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "◈ $coins",
                        color = Color(0xFFFFC86A),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                "$xpIntoLevel / 100 XP",
                color = Color(0xFF8EA3CA),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(10.dp))
        CyberAvatarView(
            style = avatar,
            banner = banner,
            frame = frame,
            onClick = onAvatarClick,
            size = 64.dp,
            showEditBadge = true
        )
    }
}

private enum class HomeTopIcon { SETTINGS, PROFILE }

@Composable
private fun HomeTopButton(icon: HomeTopIcon, onClick: () -> Unit, showBadge: Boolean = false) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF172A57), Color(0xFF081123))), CircleShape)
            .border(1.3.dp, Color(0xFF7898F2), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(25.dp)) {
            when (icon) {
                HomeTopIcon.SETTINGS -> {
                    val c = Offset(size.width / 2, size.height / 2)
                    repeat(8) { i ->
                        val a = Math.toRadians(i * 45.0)
                        val x = cos(a).toFloat()
                        val y = sin(a).toFloat()
                        drawLine(
                            Color(0xFFE0E7FF),
                            Offset(c.x + x * size.minDimension * .39f, c.y + y * size.minDimension * .39f),
                            Offset(c.x + x * size.minDimension * .48f, c.y + y * size.minDimension * .48f),
                            3f,
                            StrokeCap.Round
                        )
                    }
                    drawCircle(Color(0xFFE0E7FF), size.minDimension * .30f, c, style = Stroke(3f))
                    drawCircle(Color(0xFF081123), size.minDimension * .11f, c)
                }
                HomeTopIcon.PROFILE -> {
                    drawCircle(Color(0xFFE0E7FF), size.minDimension * .17f, Offset(size.width / 2, size.height * .31f), style = Stroke(3f))
                    drawArc(
                        Color(0xFFE0E7FF),
                        198f,
                        144f,
                        false,
                        Offset(size.width * .17f, size.height * .46f),
                        Size(size.width * .66f, size.height * .48f),
                        style = Stroke(3f, cap = StrokeCap.Round)
                    )
                }
            }
        }
        if (showBadge && icon == HomeTopIcon.SETTINGS) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(11.dp)
                    .background(Color(0xFFFF4F6D), CircleShape)
                    .border(1.5.dp, Color(0xFF081123), CircleShape)
            )
        }
    }
}

@Composable
private fun HomeHeroLogo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val cyan = Color(0xFF23DFFF)
            val purple = Color(0xFFAA47FF)

            drawCircle(
                Brush.radialGradient(
                    listOf(purple.copy(alpha = .24f), cyan.copy(alpha = .10f), Color.Transparent),
                    Offset(cx, cy),
                    150f
                ),
                150f,
                Offset(cx, cy)
            )

            repeat(8) { i ->
                val y = h * (.12f + i * .11f)
                val start = w * if (i % 2 == 0) .04f else .12f
                val elbow = w * (.26f + (i % 3) * .025f)
                val accent = if (i % 2 == 0) cyan else purple
                val left = Path().apply {
                    moveTo(start, y)
                    lineTo(elbow, y)
                    lineTo(elbow + 16f, cy)
                    lineTo(cx - 82f, cy)
                }
                val right = Path().apply {
                    moveTo(w - start, y)
                    lineTo(w - elbow, y)
                    lineTo(w - elbow - 16f, cy)
                    lineTo(cx + 82f, cy)
                }
                drawPath(left, accent.copy(alpha = .48f), style = Stroke(1.7f, cap = StrokeCap.Round))
                drawPath(right, accent.copy(alpha = .48f), style = Stroke(1.7f, cap = StrokeCap.Round))
                drawCircle(accent.copy(alpha = .78f), 2.7f, Offset(start, y))
                drawCircle(accent.copy(alpha = .78f), 2.7f, Offset(w - start, y))
            }

            val shield = Path().apply {
                moveTo(cx, cy - 66f)
                lineTo(cx + 62f, cy - 40f)
                lineTo(cx + 55f, cy + 25f)
                quadraticBezierTo(cx + 36f, cy + 58f, cx, cy + 73f)
                quadraticBezierTo(cx - 36f, cy + 58f, cx - 55f, cy + 25f)
                lineTo(cx - 62f, cy - 40f)
                close()
            }
            drawPath(
                shield,
                Brush.linearGradient(listOf(Color(0xFFF16EFF), Color(0xFF55E7FF), Color(0xFF347DFF))),
                style = Stroke(6f, cap = StrokeCap.Round)
            )

            val shackleTop = cy - 32f
            drawArc(
                color = Color(0xFFEE83FF),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 21f, shackleTop),
                size = Size(42f, 44f),
                style = Stroke(6f, cap = StrokeCap.Round)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFFC065FF), Color(0xFF3D94FF))),
                topLeft = Offset(cx - 27f, cy - 8f),
                size = Size(54f, 48f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawCircle(Color(0xFF08162F), 7f, Offset(cx, cy + 14f))
            drawLine(Color(0xFF08162F), Offset(cx, cy + 20f), Offset(cx, cy + 31f), 4.5f, StrokeCap.Round)
        }
    }
}

@Composable
private fun HomeMenuCard(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = .18f), Color(0xFF081327), Color(0xFF071021))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.3.dp, accent.copy(alpha = .90f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(accent.copy(alpha = .12f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("◈", color = accent, fontSize = 19.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFCBD4F8), fontSize = 11.sp)
        }
        Text("›", color = Color.White, fontSize = 29.sp)
    }
}

@Composable
private fun HomeStatCard(
    symbol: String,
    value: String,
    label: String,
    modifier: Modifier,
    accent: Color
) {
    Column(
        modifier = modifier
            .height(86.dp)
            .background(Color(0xFF071123), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF214B85), RoundedCornerShape(16.dp))
            .padding(4.dp, 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 17.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFFC9D0F3), fontSize = 8.5.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HomeDigitalPlanet() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * 1.10f)
            val rx = w * .54f
            val ry = h * .86f
            val main = Color(0xFF21BFFF).copy(alpha = .80f)
            val dim = Color(0xFF315D9B).copy(alpha = .62f)
            val purple = Color(0xFF8F45FF).copy(alpha = .50f)
            drawArc(main, 188f, 164f, false, Offset(center.x - rx, center.y - ry), Size(rx * 2, ry * 2), style = Stroke(3f))
            for (i in -5..5) {
                val sx = center.x + i * (w * .075f)
                val ex = center.x + i * (w * .025f)
                drawLine(if (i % 3 == 0) purple else dim, Offset(sx, h * .44f + abs(i) * 2.8f), Offset(ex, h), 1.2f)
            }
        }
        Text(
            "UN MONDE PLUS SÛR\nCOMMENCE PAR TOI",
            color = Color(0xFFD6DCF8),
            textAlign = TextAlign.Center,
            fontSize = 9.5.sp,
            letterSpacing = 2.4.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
