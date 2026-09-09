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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
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
import com.example.cyberquiz.engagement.LevelRewardStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.model.claimableMissionIds
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.update.CyberQuizUpdateManager
import com.example.cyberquiz.viewmodel.QuizViewModel
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
    var levelRewardState by remember { mutableStateOf(LevelRewardStore.snapshot(context)) }
    var selectedAvatar by remember { mutableStateOf(storedPlayerAvatar(context)) }
    var selectedBanner by remember { mutableStateOf(storedPlayerBanner(context)) }
    var selectedFrame by remember { mutableStateOf(storedPlayerFrame(context)) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var showShop by rememberSaveable { mutableStateOf(false) }
    var showDailyQuests by rememberSaveable { mutableStateOf(false) }
    var showLevels by rememberSaveable { mutableStateOf(false) }
    var levelFocus by rememberSaveable { mutableStateOf(1) }
    var updateAvailable by remember { mutableStateOf(false) }

    val activeReviewCount = reviewItems.count { !it.mastered }
    val xpIntoLevel = p.xp % 100
    val levelProgress = if (p.level >= MAX_PLAYER_LEVEL) 1f else (xpIntoLevel / 100f).coerceIn(0f, 1f)
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered
    val hasClaimableQuest = claimableMissionIds(engagement.missions, engagement.claimedMissionIds).isNotEmpty()

    fun reload() {
        selectedAvatar = storedPlayerAvatar(context)
        selectedBanner = storedPlayerBanner(context)
        selectedFrame = storedPlayerFrame(context)
        engagement = EngagementStore.sync(context, metrics)
        levelRewardState = LevelRewardStore.snapshot(context)
    }

    LaunchedEffect(Unit) {
        updateAvailable = CyberQuizUpdateManager.checkForUpdate() != null
    }

    LaunchedEffect(p.level, p.xp, p.answered) {
        val saved = LevelRewardStore.snapshot(context)
        if (p.xp > 0 || p.answered > 0 || saved.pendingLevels.isNotEmpty() || saved.claimedLevels.isNotEmpty()) {
            levelRewardState = LevelRewardStore.sync(context, p.level)
        }
    }

    if (showShop) {
        CosmeticShopScreen(
            metrics = metrics,
            onBack = {
                showShop = false
                reload()
            }
        )
        return
    }

    if (showDailyQuests) {
        DailyQuestsScreen(
            metrics = metrics,
            onCoinsChanged = {
                engagement = EngagementStore.snapshot(context, metrics)
            },
            onBack = {
                showDailyQuests = false
                reload()
            }
        )
        return
    }

    if (showLevels) {
        LevelProgressionScreen(
            currentLevel = p.level,
            focusLevel = levelFocus,
            onRewardClaimed = { reload() },
            onBack = {
                showLevels = false
                reload()
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
            .padding(horizontal = 18.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeTopButton(HomeTopIcon.SETTINGS, onSettings, updateAvailable)
            HomeTopButton(HomeTopIcon.PROFILE, onProfile)
        }

        Spacer(Modifier.height(7.dp))
        HomePlayerHeader(
            level = p.level,
            xpIntoLevel = xpIntoLevel,
            progress = levelProgress,
            coins = engagement.coins,
            avatar = selectedAvatar,
            banner = selectedBanner,
            frame = selectedFrame,
            hasRewardNotification = levelRewardState.hasPendingReward,
            hasQuestNotification = hasClaimableQuest,
            onLevelClick = {
                levelFocus = p.level.coerceIn(1, MAX_PLAYER_LEVEL)
                showLevels = true
            },
            onCoinsClick = { showShop = true },
            onQuestClick = { showDailyQuests = true },
            onAvatarClick = { showPicker = true }
        )

        Spacer(Modifier.height(8.dp))
        HomeHeroLogo()

        Row(verticalAlignment = Alignment.Bottom) {
            Text("Cyber", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black)
            Text("Quiz", color = Color(0xFFE04FFF), fontSize = 31.sp, fontWeight = FontWeight.Black)
        }
        Text(
            "APPRENDS  ·  JOUE  ·  SÉCURISE",
            color = Color(0xFFAEB9EA),
            fontSize = 8.5.sp,
            letterSpacing = 1.65.sp
        )

        Spacer(Modifier.height(17.dp))
        HomeMenuCard("Commencer", "Lancer un nouveau quiz", Color(0xFFD652FF), onQuiz)
        Spacer(Modifier.height(9.dp))
        HomeMenuCard("Statistiques", "Suis ta progression", Color(0xFF18BFFF), onStats)
        Spacer(Modifier.height(9.dp))
        HomeMenuCard("Catégories", "Choisis ton thème", Color(0xFF19F2E5), onCategories)
        Spacer(Modifier.height(9.dp))
        HomeMenuCard(
            "À revoir",
            if (activeReviewCount == 0) {
                "Aucune notion en attente"
            } else {
                "$activeReviewCount notion${if (activeReviewCount > 1) "s" else ""} à retravailler"
            },
            Color(0xFFFFB84A),
            onReview
        )
        Spacer(Modifier.height(9.dp))
        HomeMenuCard(
            "Historique",
            if (history.isEmpty()) {
                "Aucun quiz terminé"
            } else {
                "${history.size} quiz terminé${if (history.size > 1) "s" else ""}"
            },
            Color(0xFF8B7CFF),
            onHistory
        )

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeStatCard("🔥", p.streak.toString(), "Série", Modifier.weight(1f), Color(0xFFFFA61A))
            HomeStatCard("★", p.xp.toString(), "XP", Modifier.weight(1f), Color(0xFFD64CFF))
            HomeStatCard("▥", p.level.coerceAtMost(MAX_PLAYER_LEVEL).toString(), "Niveau", Modifier.weight(1f), Color(0xFF1AC3FF))
            HomeStatCard("🏆", "$accuracy%", "Réussite", Modifier.weight(1f), Color(0xFFFFCC33))
        }
        Spacer(Modifier.height(10.dp))
        HomeDigitalPlanet()
    }

    if (showPicker) {
        PlayerAvatarPickerDialog(
            playerLevel = p.level,
            onSelectionChanged = { reload() },
            onDismiss = {
                showPicker = false
                reload()
            }
        )
    }

    if (!showPicker) {
        levelRewardState.nextPopupLevel?.let { reachedLevel ->
            LevelReachedDialog(
                level = reachedLevel,
                onViewLevel = {
                    levelRewardState = LevelRewardStore.acknowledgePopup(context, reachedLevel)
                    levelFocus = reachedLevel
                    showLevels = true
                },
                onDismiss = {
                    levelRewardState = LevelRewardStore.acknowledgePopup(context, reachedLevel)
                }
            )
        }
    }
}

@Composable
private fun LevelReachedDialog(
    level: Int,
    onViewLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071225),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "NOUVEAU NIVEAU",
                    color = Color(0xFFFFC857),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
                Spacer(Modifier.height(5.dp))
                Text("NIVEAU $level", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text(
                    playerRoleForLevel(level),
                    color = Color(0xFF63EFFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✨", fontSize = 36.sp)
                Text(
                    "Un coffre de niveau t’attend dans ta progression. Son contenu reste secret tant que tu ne l’as pas ouvert.",
                    color = Color(0xFFAAB7D4),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onViewLevel) {
                Text("VOIR MON NIVEAU", color = Color(0xFF19F2E5), fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("PLUS TARD", color = Color(0xFF9FAED3))
            }
        }
    )
}

@Composable
private fun HomePlayerHeader(
    level: Int,
    xpIntoLevel: Int,
    progress: Float,
    coins: Int,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle,
    hasRewardNotification: Boolean,
    hasQuestNotification: Boolean,
    onLevelClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onQuestClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CyberAvatarView(
            style = avatar,
            banner = banner,
            frame = frame,
            onClick = onAvatarClick,
            size = 42.dp,
            showEditBadge = false
        )
        Spacer(Modifier.width(12.dp))
        CompactGameLevelBar(
            level = level,
            xpIntoLevel = xpIntoLevel,
            progress = progress,
            modifier = Modifier.width(166.dp),
            hasRewardNotification = hasRewardNotification,
            onClick = onLevelClick
        )
        Spacer(Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HomeCoinButton(coins = coins, onClick = onCoinsClick)
            HomeQuestButton(showBadge = hasQuestNotification, onClick = onQuestClick)
        }
    }
}

@Composable
private fun HomeCoinButton(coins: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFF21163A), RoundedCornerShape(50.dp))
            .border(1.2.dp, Color(0xFFFFB84A).copy(alpha = .68f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("◈", color = Color(0xFFFFC86A), fontSize = 14.sp, fontWeight = FontWeight.Black)
        Text(coins.toString(), color = Color(0xFFFFD58A), fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun HomeQuestButton(showBadge: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(37.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF203A54), Color(0xFF081522))), CircleShape)
            .border(1.2.dp, Color(0xFF19F2E5).copy(alpha = .78f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(21.dp)) {
            val ink = Color(0xFFDAFFFB)
            drawRoundRect(
                color = ink,
                topLeft = Offset(size.width * .20f, size.height * .14f),
                size = Size(size.width * .62f, size.height * .72f),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(1.8f)
            )
            drawLine(ink, Offset(size.width * .34f, size.height * .35f), Offset(size.width * .68f, size.height * .35f), 1.6f, StrokeCap.Round)
            drawLine(ink, Offset(size.width * .34f, size.height * .52f), Offset(size.width * .68f, size.height * .52f), 1.6f, StrokeCap.Round)
            drawLine(ink, Offset(size.width * .34f, size.height * .69f), Offset(size.width * .56f, size.height * .69f), 1.6f, StrokeCap.Round)
            drawLine(Color(0xFF19F2E5), Offset(size.width * .63f, size.height * .66f), Offset(size.width * .69f, size.height * .73f), 1.9f, StrokeCap.Round)
            drawLine(Color(0xFF19F2E5), Offset(size.width * .69f, size.height * .73f), Offset(size.width * .82f, size.height * .59f), 1.9f, StrokeCap.Round)
        }
        if (showBadge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .background(Color(0xFFFF4F6D), CircleShape)
                    .border(1.2.dp, Color(0xFF081123), CircleShape)
            )
        }
    }
}

private enum class HomeTopIcon { SETTINGS, PROFILE }

@Composable
private fun HomeTopButton(icon: HomeTopIcon, onClick: () -> Unit, showBadge: Boolean = false) {
    Box(
        Modifier
            .size(42.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF172A57), Color(0xFF081123))), CircleShape)
            .border(1.2.dp, Color(0xFF7898F2), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(24.dp)) {
            when (icon) {
                HomeTopIcon.SETTINGS -> {
                    val c = center
                    repeat(8) { i ->
                        val a = Math.toRadians(i * 45.0)
                        val x = cos(a).toFloat()
                        val y = sin(a).toFloat()
                        drawLine(
                            Color(0xFFE0E7FF),
                            Offset(c.x + x * 8.1f, c.y + y * 8.1f),
                            Offset(c.x + x * 10.5f, c.y + y * 10.5f),
                            2.6f,
                            StrokeCap.Round
                        )
                    }
                    drawCircle(Color(0xFFE0E7FF), 6.5f, c, style = Stroke(2.6f))
                    drawCircle(Color(0xFF081123), 2.5f, c)
                }

                HomeTopIcon.PROFILE -> {
                    drawCircle(
                        Color(0xFFE0E7FF),
                        4.4f,
                        Offset(size.width / 2, size.height * .31f),
                        style = Stroke(2.6f)
                    )
                    drawArc(
                        Color(0xFFE0E7FF),
                        198f,
                        144f,
                        false,
                        Offset(size.width * .17f, size.height * .46f),
                        Size(size.width * .66f, size.height * .48f),
                        style = Stroke(2.6f, cap = StrokeCap.Round)
                    )
                }
            }
        }
        if (showBadge && icon == HomeTopIcon.SETTINGS) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .background(Color(0xFFFF4F6D), CircleShape)
                    .border(1.2.dp, Color(0xFF081123), CircleShape)
            )
        }
    }
}

@Composable
private fun HomeHeroLogo() {
    Box(
        Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .height(224.dp)
            .clip(RoundedCornerShape(26.dp))
            .drawWithCache {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val center = Offset(cx, cy)
                // Density-aware 168 dp shield, scaled down only for unusually narrow windows.
                val unit = minOf(1.dp.toPx(), size.width / 280f)
                val radius = 98f * unit
                val cyan = Color(0xFF35DCFF)
                val purple = Color(0xFFB56CFF)
                val board = Brush.verticalGradient(
                    listOf(Color(0xFF030915), Color(0xFF08172B), Color(0xFF040B18))
                )
                val halo = Brush.radialGradient(
                    listOf(purple.copy(alpha = .22f), cyan.copy(alpha = .09f), Color.Transparent),
                    center, 134f * unit
                )
                val shieldInk = Brush.linearGradient(
                    listOf(Color(0xFFEF9CFF), purple, cyan, Color(0xFF4994FF)),
                    Offset(cx - 72f * unit, cy - 84f * unit),
                    Offset(cx + 72f * unit, cy + 84f * unit)
                )
                val shieldFill = Brush.verticalGradient(
                    listOf(Color(0xFF18284C), Color(0xFF0A1530), Color(0xFF0D2340)),
                    cy - 84f * unit, cy + 84f * unit
                )
                val lockFill = Brush.linearGradient(
                    listOf(Color(0xFFD18CFF), Color(0xFF9B66F0), Color(0xFF428FEF)),
                    Offset(cx - 46f * unit, cy - 23f * unit),
                    Offset(cx + 46f * unit, cy + 39f * unit)
                )
                fun shieldPath(scale: Float) = Path().apply {
                    val u = unit * scale
                    moveTo(cx, cy - 84f * u)
                    lineTo(cx + 72f * u, cy - 52f * u)
                    lineTo(cx + 64f * u, cy + 22f * u)
                    quadraticTo(cx + 48f * u, cy + 60f * u, cx, cy + 84f * u)
                    quadraticTo(cx - 48f * u, cy + 60f * u, cx - 64f * u, cy + 22f * u)
                    lineTo(cx - 72f * u, cy - 52f * u)
                    close()
                }
                val shield = shieldPath(1f)
                val innerShield = shieldPath(.88f)
                val grid = Path().apply {
                    val step = 24f * unit
                    for (i in 0..(size.width / step).toInt()) {
                        val x = cx + (i - (size.width / step).toInt() / 2) * step
                        moveTo(x, 12f * unit)
                        lineTo(x, size.height - 12f * unit)
                    }
                    for (i in -4..4) {
                        moveTo(12f * unit, cy + i * step)
                        lineTo(size.width - 12f * unit, cy + i * step)
                    }
                }
                val traces = buildList {
                    for (side in listOf(-1f, 1f)) {
                        for (vertical in listOf(-1f, 1f)) {
                            repeat(3) { lane ->
                                val y = cy + vertical * (30f + lane * 24f) * unit
                                val innerY = cy + vertical * (14f + lane * 21f) * unit
                                val start = Offset(cx + side * (cx - 16f * unit), y)
                                val end = Offset(cx + side * radius * .94f, innerY)
                                val bendX = cx + side * (radius + (cx - radius) * .48f)
                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    lineTo(bendX, y)
                                    lineTo(bendX - side * kotlin.math.abs(y - innerY), innerY)
                                    lineTo(end.x, end.y)
                                }
                                add(HomeHeroTrace(path, start, end, if (lane == 1) purple else cyan))
                            }
                        }
                    }
                }
                val corners = Path().apply {
                    for (side in listOf(-1f, 1f)) {
                        for (vertical in listOf(-1f, 1f)) {
                            val x = cx + side * (cx - 17f * unit)
                            val y = cy + vertical * (cy - 17f * unit)
                            moveTo(x - side * 18f * unit, y)
                            lineTo(x, y)
                            lineTo(x, y - vertical * 12f * unit)
                        }
                    }
                }
                val fineStroke = Stroke(.8f * unit, cap = StrokeCap.Round)
                val ringTop = center - Offset(radius, radius)
                val ringSize = Size(radius * 2f, radius * 2f)
                val lockTop = Offset(cx - 46f * unit, cy - 23f * unit)
                val lockSize = Size(92f * unit, 62f * unit)
                val lockCorners = CornerRadius(12f * unit)

                // Paths and brushes are cached; no animation, blur, bitmap or offscreen layer.
                onDrawBehind {
                    drawRect(board)
                    drawPath(grid, cyan.copy(alpha = .055f), style = Stroke(.5f * unit))
                    drawCircle(halo, 134f * unit, center)
                    traces.forEach { trace ->
                        drawPath(trace.path, trace.accent.copy(alpha = .30f), style = fineStroke)
                        drawCircle(trace.accent.copy(alpha = .08f), 5f * unit, trace.start)
                        drawCircle(trace.accent.copy(alpha = .8f), 1.7f * unit, trace.start)
                        drawCircle(trace.accent.copy(alpha = .45f), 2f * unit, trace.end, style = fineStroke)
                    }
                    drawPath(corners, cyan.copy(alpha = .35f), style = fineStroke)
                    drawCircle(Color(0xFF071226).copy(alpha = .8f), radius * .94f, center)
                    drawCircle(cyan.copy(alpha = .15f), radius, center, style = fineStroke)
                    repeat(4) { segment ->
                        drawArc(
                            if (segment % 2 == 0) cyan.copy(alpha = .62f) else purple.copy(alpha = .55f),
                            20f + segment * 90f, 50f, false, ringTop, ringSize,
                            style = Stroke(1.5f * unit, cap = StrokeCap.Round)
                        )
                    }
                    drawPath(shield, cyan.copy(alpha = .055f), style = Stroke(14f * unit))
                    drawPath(shield, shieldFill)
                    drawPath(shield, shieldInk, style = Stroke(2.6f * unit, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    drawPath(innerShield, cyan.copy(alpha = .24f), style = fineStroke)
                    drawArc(
                        Color(0xFFDDB0FF), 180f, 180f, false,
                        Offset(cx - 29f * unit, cy - 64f * unit), Size(58f * unit, 60f * unit),
                        style = Stroke(6f * unit, cap = StrokeCap.Round)
                    )
                    for (side in listOf(-1f, 1f)) {
                        drawLine(
                            Color(0xFFDDB0FF), Offset(cx + side * 29f * unit, cy - 34f * unit),
                            Offset(cx + side * 29f * unit, cy - 20f * unit), 6f * unit, StrokeCap.Round
                        )
                    }
                    drawRoundRect(lockFill, lockTop, lockSize, lockCorners)
                    drawRoundRect(Color(0xFFE8D6FF).copy(alpha = .38f), lockTop, lockSize, lockCorners, style = fineStroke)
                    drawCircle(Color(0xFF0A1630), 7f * unit, Offset(cx, cy + 1f * unit))
                    drawLine(Color(0xFF0A1630), Offset(cx, cy + 7f * unit), Offset(cx, cy + 19f * unit), 4.5f * unit, StrokeCap.Round)
                }
            }
    )
}

private data class HomeHeroTrace(val path: Path, val start: Offset, val end: Offset, val accent: Color)

@Composable
private fun HomeMenuCard(title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                Brush.horizontalGradient(listOf(accent.copy(alpha = .13f), Color(0xFF081327))),
                RoundedCornerShape(19.dp)
            )
            .border(1.3.dp, accent.copy(alpha = .84f), RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(39.dp)
                .background(accent.copy(alpha = .13f), CircleShape)
                .border(1.dp, accent.copy(alpha = .45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(title.take(1).uppercase(), color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFCBD4F8), fontSize = 11.sp)
        }
        Text("›", color = Color.White, fontSize = 28.sp)
    }
}

@Composable
private fun HomeStatCard(symbol: String, value: String, label: String, modifier: Modifier, accent: Color) {
    Column(
        modifier
            .height(86.dp)
            .background(Color(0xFF071123), RoundedCornerShape(15.dp))
            .border(1.dp, Color(0xFF214B85), RoundedCornerShape(15.dp))
            .padding(3.dp, 7.dp),
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
    Box(Modifier.fillMaxWidth().height(105.dp), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c = Offset(w / 2, h * 1.18f)
            val main = Color(0xFF21BFFF).copy(alpha = .62f)
            drawArc(
                main,
                188f,
                164f,
                false,
                Offset(c.x - w * .48f, c.y - h * .78f),
                Size(w * .96f, h * 1.56f),
                style = Stroke(2.2f, cap = StrokeCap.Round)
            )
            repeat(7) { i ->
                val x = w * (.16f + i * .11f)
                drawLine(
                    Color(0xFF315D9B).copy(alpha = .42f),
                    Offset(x, h * .55f),
                    Offset(c.x + (x - c.x) * .24f, h),
                    1f
                )
            }
        }
        Text(
            "UN MONDE PLUS SÛR · COMMENCE PAR TOI",
            color = Color(0xFFD6DCF8),
            fontSize = 8.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}
