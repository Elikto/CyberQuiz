package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import com.example.cyberquiz.engagement.LevelRewardState
import com.example.cyberquiz.engagement.LevelRewardStore
import com.example.cyberquiz.engagement.levelCoinReward
import com.example.cyberquiz.ui.theme.CyberBackground

internal const val MAX_PLAYER_LEVEL = 30

internal enum class LevelRewardKind { AVATAR, BANNER, FRAME, COINS }

internal data class LevelRewardItem(
    val kind: LevelRewardKind,
    val name: String,
    val avatar: PlayerAvatarStyle? = null,
    val banner: PlayerBannerStyle? = null,
    val frame: PlayerFrameStyle? = null,
    val coinAmount: Int = 0
)

internal data class LevelRoadmapEntry(
    val level: Int,
    val role: String,
    val avatar: PlayerAvatarStyle,
    val banner: PlayerBannerStyle,
    val frame: PlayerFrameStyle,
    val rewards: List<LevelRewardItem>
)

private val playerLevelRoles = listOf(
    "Recrue cyber",
    "Initié réseau",
    "Opérateur novice",
    "Veilleur numérique",
    "Analyste SOC",
    "Gardien réseau",
    "Traqueur de signaux",
    "Blue Team Elite",
    "Défenseur système",
    "Hacker éthique",
    "Éclaireur Red Team",
    "Red Team Runner",
    "Auditeur offensif",
    "Briseur de failles",
    "Expert sécurité",
    "Crypto Sentinel",
    "Gardien des clés",
    "Ingénieur défense",
    "Commandant SOC",
    "Architecte cyber",
    "Stratège cyber",
    "Maître des incidents",
    "Chasseur de zero-day",
    "Commandant Red Team",
    "Expert cyber senior",
    "Architecte défensif",
    "Maître opérateur",
    "Légende du réseau",
    "Gardien suprême",
    "Cyber Légende"
)

private val roadmapAvatars: List<PlayerAvatarStyle>
    get() = PlayerAvatarStyle.entries.filter { !it.mystery }

private val roadmapBanners: List<PlayerBannerStyle>
    get() = PlayerBannerStyle.entries.filter { !it.mystery }

private val roadmapFrames: List<PlayerFrameStyle>
    get() = PlayerFrameStyle.entries.filter {
        !it.mystery && !it.shopItem && it.coinCost == 0 && it.achievementId == null
    }

internal fun playerRoleForLevel(level: Int): String =
    playerLevelRoles[level.coerceIn(1, MAX_PLAYER_LEVEL) - 1]

internal fun roadmapIndexForLevel(level: Int): Int =
    MAX_PLAYER_LEVEL - level.coerceIn(1, MAX_PLAYER_LEVEL)

internal fun roadmapAvatarForLevel(level: Int): PlayerAvatarStyle {
    val safeLevel = level.coerceIn(1, MAX_PLAYER_LEVEL)
    val exact = PlayerAvatarStyle.entries.firstOrNull {
        !it.mystery && it.unlockLevel == safeLevel && safeLevel > 1
    }
    if (exact != null) return exact
    val styles = roadmapAvatars
    return styles[(safeLevel - 1) % styles.size]
}

internal fun roadmapBannerForLevel(level: Int): PlayerBannerStyle {
    val styles = roadmapBanners
    return styles[((level.coerceIn(1, MAX_PLAYER_LEVEL) - 1) * 3) % styles.size]
}

internal fun roadmapFrameForLevel(level: Int): PlayerFrameStyle {
    val styles = roadmapFrames
    return styles[((level.coerceIn(1, MAX_PLAYER_LEVEL) - 1) * 2) % styles.size]
}

internal fun shouldBlurLevelAvatar(level: Int, claimed: Boolean): Boolean =
    !claimed && level.coerceIn(1, MAX_PLAYER_LEVEL) >= 20

internal fun levelRewards(level: Int): List<LevelRewardItem> {
    val safeLevel = level.coerceIn(1, MAX_PLAYER_LEVEL)
    val levelAvatar = roadmapAvatarForLevel(safeLevel)
    val rewards = mutableListOf(
        LevelRewardItem(
            kind = LevelRewardKind.AVATAR,
            name = levelAvatar.displayName,
            avatar = levelAvatar
        ),
        LevelRewardItem(
            kind = LevelRewardKind.COINS,
            name = "${levelCoinReward(safeLevel)} CyberCoins",
            coinAmount = levelCoinReward(safeLevel)
        )
    )

    PlayerBannerStyle.entries
        .filter { !it.mystery && it.unlockLevel == safeLevel && safeLevel > 1 }
        .forEach { style ->
            rewards += LevelRewardItem(
                kind = LevelRewardKind.BANNER,
                name = style.displayName,
                banner = style
            )
        }

    PlayerFrameStyle.entries
        .filter {
            !it.mystery &&
                !it.shopItem &&
                it.coinCost == 0 &&
                it.achievementId == null &&
                it.unlockLevel == safeLevel &&
                safeLevel > 1
        }
        .forEach { style ->
            rewards += LevelRewardItem(
                kind = LevelRewardKind.FRAME,
                name = style.displayName,
                frame = style
            )
        }

    return rewards
}

internal fun levelRoadmap(): List<LevelRoadmapEntry> =
    (MAX_PLAYER_LEVEL downTo 1).map { level ->
        LevelRoadmapEntry(
            level = level,
            role = playerRoleForLevel(level),
            avatar = roadmapAvatarForLevel(level),
            banner = roadmapBannerForLevel(level),
            frame = roadmapFrameForLevel(level),
            rewards = levelRewards(level)
        )
    }

@Composable
internal fun CompactGameLevelBar(
    level: Int,
    xpIntoLevel: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    hasRewardNotification: Boolean = false,
    onClick: () -> Unit
) {
    val displayLevel = level.coerceIn(1, MAX_PLAYER_LEVEL)
    val safeProgress = if (level >= MAX_PLAYER_LEVEL) 1f else progress.coerceIn(0f, 1f)
    val displayXp = if (level >= MAX_PLAYER_LEVEL) 100 else xpIntoLevel.coerceIn(0, 100)
    val barShape = RoundedCornerShape(5.dp)

    Box(modifier = modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "NIV. $displayLevel",
                    color = Color(0xFF63EFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$displayXp/100",
                            color = Color(0xFF9FB3DC),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.width(3.dp))
                        XpBoltIcon()
                    }
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .width(124.dp)
                            .height(11.dp)
                            .clip(barShape)
                            .background(Brush.verticalGradient(listOf(Color(0xFF17223B), Color(0xFF070B14))))
                            .border(1.dp, Color(0xFF637EAD), barShape)
                            .padding(1.dp)
                    ) {
                        if (safeProgress > 0f) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(safeProgress)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF6647FF), Color(0xFFD74CFF), Color(0xFF2FE8FF))
                                        ),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) {
                                Box(
                                    Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = .13f))
                                )
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = .17f))
                        )
                    }
                }
            }
            Text(
                playerRoleForLevel(displayLevel),
                color = Color(0xFFDCE6FF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        if (hasRewardNotification) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .background(Color(0xFFFF4F6D), CircleShape)
                    .border(1.2.dp, Color(0xFF07101F), CircleShape)
            )
        }
    }
}

@Composable
private fun XpBoltIcon() {
    Canvas(Modifier.size(10.dp)) {
        val bolt = Path().apply {
            moveTo(size.width * .57f, 0f)
            lineTo(size.width * .18f, size.height * .56f)
            lineTo(size.width * .47f, size.height * .56f)
            lineTo(size.width * .35f, size.height)
            lineTo(size.width * .84f, size.height * .39f)
            lineTo(size.width * .55f, size.height * .39f)
            close()
        }
        drawPath(
            bolt,
            Brush.verticalGradient(listOf(Color(0xFFFFF08A), Color(0xFFFFB84A)))
        )
    }
}

@Composable
fun LevelProgressionScreen(
    currentLevel: Int,
    focusLevel: Int = currentLevel,
    onRewardClaimed: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val displayLevel = currentLevel.coerceIn(1, MAX_PLAYER_LEVEL)
    val focusedLevel = focusLevel.coerceIn(1, displayLevel)
    val roadmap = levelRoadmap()
    val currentIndex = roadmapIndexForLevel(focusedLevel).coerceIn(0, roadmap.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex)
    var rewardState by remember(displayLevel) {
        mutableStateOf(LevelRewardStore.sync(context, displayLevel))
    }
    var chestLevel by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedCoins by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(focusedLevel) {
        listState.scrollToItem(currentIndex)
        withFrameNanos { }
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex }
        if (item != null) {
            val viewportCenter = (
                listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset
                ) / 2
            val itemCenter = item.offset + item.size / 2
            listState.scrollBy((itemCenter - viewportCenter).toFloat())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071020), CyberBackground, Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF6F8FEA), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = Color.White, fontSize = 27.sp)
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 190.dp)
        ) {
            items(roadmap, key = { it.level }) { entry ->
                LevelRoadmapNode(
                    entry = entry,
                    currentLevel = displayLevel,
                    rewardState = rewardState,
                    showConnector = entry.level != 1,
                    onOpenChest = {
                        openedCoins = 0
                        chestLevel = entry.level
                    }
                )
            }
        }
    }

    chestLevel?.let { level ->
        val entry = roadmap.first { it.level == level }
        LevelChestDialog(
            entry = entry,
            openedCoins = openedCoins,
            onOpen = {
                val coins = LevelRewardStore.claimLevel(context, level, displayLevel)
                if (coins > 0) {
                    openedCoins = coins
                    rewardState = LevelRewardStore.snapshot(context)
                    onRewardClaimed()
                }
            },
            onDismiss = {
                chestLevel = null
                openedCoins = 0
            }
        )
    }
}

@Composable
private fun LevelRoadmapNode(
    entry: LevelRoadmapEntry,
    currentLevel: Int,
    rewardState: LevelRewardState,
    showConnector: Boolean,
    onOpenChest: () -> Unit
) {
    val current = entry.level == currentLevel
    val reached = entry.level <= currentLevel
    val claimed = entry.level in rewardState.claimedLevels
    val pending = entry.level in rewardState.pendingLevels
    val future = entry.level > currentLevel
    val accent = when {
        pending -> Color(0xFFFFC857)
        current -> Color(0xFF2DE9FF)
        claimed -> Color(0xFF45D89A)
        else -> Color(0xFF536A94)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LevelIdentityCard(
            entry = entry,
            current = current,
            claimed = claimed,
            pending = pending,
            future = future,
            accent = accent,
            onClick = if (reached && pending) onOpenChest else null
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            entry.rewards.forEach { reward ->
                LevelRewardPreviewCard(
                    reward = reward,
                    fallbackAvatar = entry.avatar,
                    fallbackBanner = entry.banner,
                    fallbackFrame = entry.frame,
                    revealed = claimed,
                    accent = accent
                )
            }
        }

        if (showConnector) {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .width(3.dp)
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accent.copy(alpha = .72f),
                                Color(0xFF3C5578).copy(alpha = .42f),
                                Color(0xFF263A58).copy(alpha = .28f)
                            )
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
            Spacer(Modifier.height(10.dp))
        } else {
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun LevelIdentityCard(
    entry: LevelRoadmapEntry,
    current: Boolean,
    claimed: Boolean,
    pending: Boolean,
    future: Boolean,
    accent: Color,
    onClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(22.dp)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val blurAvatar = shouldBlurLevelAvatar(entry.level, claimed)

    Column(
        modifier = Modifier
            .size(166.dp)
            .background(
                if (current || pending) {
                    Brush.radialGradient(listOf(Color(0xFF1C1A52), Color(0xFF091528), Color(0xFF07101E)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF0B162B), Color(0xFF060D19)))
                },
                shape
            )
            .border(if (current || pending) 1.8.dp else 1.dp, accent.copy(alpha = .8f), shape)
            .then(clickModifier)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(if (blurAvatar) Modifier.clip(RoundedCornerShape(18.dp)).blur(4.dp) else Modifier) {
                CyberAvatarView(
                    style = entry.avatar,
                    banner = entry.banner,
                    frame = entry.frame,
                    onClick = {},
                    size = if (current) 78.dp else 72.dp,
                    showEditBadge = false,
                    syncShopSelection = false
                )
            }
            when {
                blurAvatar -> Text("🔒", fontSize = 24.sp)
                pending -> Text(
                    "🎁",
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text("NIV. ${entry.level}", color = accent, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
        Text(
            entry.role,
            color = if (future) Color(0xFFAAB7D4) else Color.White,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(Modifier.height(2.dp))
        Text(
            when {
                pending -> "COFFRE À OUVRIR"
                claimed -> "DÉBLOQUÉ"
                current -> "NIVEAU ACTUEL"
                else -> "À VENIR"
            },
            color = accent.copy(alpha = .9f),
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LevelRewardPreviewCard(
    reward: LevelRewardItem,
    fallbackAvatar: PlayerAvatarStyle,
    fallbackBanner: PlayerBannerStyle,
    fallbackFrame: PlayerFrameStyle,
    revealed: Boolean,
    accent: Color
) {
    val shape = RoundedCornerShape(15.dp)
    val previewShape = RoundedCornerShape(11.dp)

    Column(
        modifier = Modifier
            .width(72.dp)
            .height(92.dp)
            .background(Color(0xFF081226), shape)
            .border(1.dp, accent.copy(alpha = .34f), shape)
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = if (revealed) {
                Modifier.clip(previewShape)
            } else {
                Modifier.clip(previewShape).blur(2.5.dp)
            },
            contentAlignment = Alignment.Center
        ) {
            RewardVisual(
                reward = reward,
                fallbackAvatar = fallbackAvatar,
                fallbackBanner = fallbackBanner,
                fallbackFrame = fallbackFrame,
                accent = accent,
                compact = true
            )
        }

        if (revealed) {
            Text(
                reward.name,
                color = Color(0xFFE4EBFF),
                fontSize = 6.4.sp,
                lineHeight = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        } else {
            Box(
                Modifier
                    .width(34.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF6F7F9E).copy(alpha = .45f))
            )
        }
    }
}

@Composable
private fun RewardVisual(
    reward: LevelRewardItem,
    fallbackAvatar: PlayerAvatarStyle,
    fallbackBanner: PlayerBannerStyle,
    fallbackFrame: PlayerFrameStyle,
    accent: Color,
    compact: Boolean
) {
    val avatarSize = if (compact) 43.dp else 52.dp
    val bannerWidth = if (compact) 60.dp else 92.dp
    val bannerHeight = if (compact) 42.dp else 48.dp
    val coinSize = if (compact) 42.dp else 48.dp

    when (reward.kind) {
        LevelRewardKind.AVATAR -> CyberAvatarView(
            style = reward.avatar ?: fallbackAvatar,
            banner = fallbackBanner,
            frame = PlayerFrameStyle.CYAN_PULSE,
            onClick = {},
            size = avatarSize,
            showEditBadge = false,
            syncShopSelection = false
        )

        LevelRewardKind.BANNER -> Box(
            modifier = Modifier
                .width(bannerWidth)
                .height(bannerHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF050914))
                .border(1.dp, accent.copy(alpha = .42f), RoundedCornerShape(10.dp))
        ) {
            PlayerBannerBackdrop(
                style = reward.banner ?: fallbackBanner,
                modifier = Modifier.fillMaxSize()
            )
        }

        LevelRewardKind.FRAME -> CyberAvatarView(
            style = fallbackAvatar,
            banner = fallbackBanner,
            frame = reward.frame ?: fallbackFrame,
            onClick = {},
            size = avatarSize,
            showEditBadge = false,
            syncShopSelection = false
        )

        LevelRewardKind.COINS -> Box(
            Modifier
                .size(coinSize)
                .background(Color(0xFF2A1E12), CircleShape)
                .border(1.dp, Color(0xFFFFC857), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "◈",
                color = Color(0xFFFFC857),
                fontSize = if (compact) 19.sp else 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun LevelChestDialog(
    entry: LevelRoadmapEntry,
    openedCoins: Int,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    val opened = openedCoins > 0
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071225),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (opened) "COFFRE OUVERT" else "COFFRE NIVEAU ${entry.level}",
                    color = Color(0xFFFFC857),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    entry.role,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedLevelChest(opened = opened)

                if (opened) {
                    Text(
                        "TES RÉCOMPENSES",
                        color = Color(0xFF63EFFF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                AnimatedChestRewards(entry = entry, opened = opened)

                AnimatedVisibility(
                    visible = opened,
                    enter = fadeIn(animationSpec = tween(260, delayMillis = 780)) +
                        scaleIn(animationSpec = tween(420, delayMillis = 780), initialScale = .45f) +
                        slideInVertically(animationSpec = tween(420, delayMillis = 780), initialOffsetY = { it / 2 }),
                    exit = fadeOut() + scaleOut(targetScale = .8f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("◈  +$openedCoins", color = Color(0xFFFFD36F), fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text(
                            "CYBERCOINS AJOUTÉS AU SOLDE",
                            color = Color(0xFF38E69A),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    if (opened) {
                        "Tous les objets de ce niveau sont révélés. Tes CyberCoins ont été ajoutés à ton solde."
                    } else {
                        "Ouvre ce coffre pour révéler l’avatar du niveau, les CyberCoins et ses éventuels cosmétiques."
                    },
                    color = if (opened) Color(0xFF38E69A) else Color(0xFF9FAED3),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (opened) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        confirmButton = {
            TextButton(onClick = if (opened) onDismiss else onOpen) {
                Text(
                    if (opened) "FERMER" else "OUVRIR LE COFFRE",
                    color = Color(0xFF19F2E5),
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = if (opened) null else {
            { TextButton(onClick = onDismiss) { Text("PLUS TARD", color = Color(0xFF9FAED3)) } }
        }
    )
}

@Composable
private fun AnimatedChestRewards(entry: LevelRoadmapEntry, opened: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        entry.rewards.chunked(2).forEachIndexed { rowIndex, rowRewards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                rowRewards.forEachIndexed { columnIndex, reward ->
                    val index = rowIndex * 2 + columnIndex
                    AnimatedVisibility(
                        visible = opened,
                        enter = fadeIn(animationSpec = tween(240, delayMillis = 350 + index * 110)) +
                            scaleIn(
                                animationSpec = tween(360, delayMillis = 350 + index * 110),
                                initialScale = .42f
                            ) +
                            slideInVertically(
                                animationSpec = tween(360, delayMillis = 350 + index * 110),
                                initialOffsetY = { it / 2 }
                            ),
                        exit = fadeOut() + scaleOut(targetScale = .8f)
                    ) {
                        LevelChestRewardCard(
                            reward = reward,
                            fallbackAvatar = entry.avatar,
                            fallbackBanner = entry.banner,
                            fallbackFrame = entry.frame
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelChestRewardCard(
    reward: LevelRewardItem,
    fallbackAvatar: PlayerAvatarStyle,
    fallbackBanner: PlayerBannerStyle,
    fallbackFrame: PlayerFrameStyle
) {
    val shape = RoundedCornerShape(15.dp)
    Column(
        modifier = Modifier
            .width(116.dp)
            .height(94.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF111D38), Color(0xFF081226))),
                shape
            )
            .border(1.dp, Color(0xFFFFC857).copy(alpha = .42f), shape)
            .padding(horizontal = 7.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        RewardVisual(
            reward = reward,
            fallbackAvatar = fallbackAvatar,
            fallbackBanner = fallbackBanner,
            fallbackFrame = fallbackFrame,
            accent = Color(0xFFFFC857),
            compact = false
        )
        Text(
            reward.name,
            color = Color(0xFFF2F5FF),
            fontSize = 7.5.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun AnimatedLevelChest(opened: Boolean) {
    val openProgress by animateFloatAsState(
        targetValue = if (opened) 1f else 0f,
        animationSpec = tween(durationMillis = 720),
        label = "levelChestOpen"
    )

    Canvas(Modifier.size(116.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val gold = Color(0xFFFFC857)
        val brightGold = Color(0xFFFFE59A)
        val purple = Color(0xFF9D46FF)
        val dark = Color(0xFF211526)

        if (openProgress > 0f) {
            drawCircle(
                color = gold.copy(alpha = .18f * openProgress),
                radius = w * (.34f + .18f * openProgress),
                center = center
            )
            repeat(8) { index ->
                val x = w * (.18f + (index % 4) * .21f)
                val top = h * (.10f + (index / 4) * .08f)
                drawLine(
                    color = brightGold.copy(alpha = .55f * openProgress),
                    start = Offset(center.x, h * .38f),
                    end = Offset(x, top - h * .08f * openProgress),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )
            }
        }

        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF784D22), dark)),
            topLeft = Offset(w * .17f, h * .47f),
            size = Size(w * .66f, h * .34f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = gold,
            topLeft = Offset(w * .17f, h * .47f),
            size = Size(w * .66f, h * .34f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(3f)
        )
        drawLine(gold.copy(alpha = .72f), Offset(w * .18f, h * .59f), Offset(w * .82f, h * .59f), 2.2f)

        val lidY = h * .31f - h * .15f * openProgress
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(brightGold, Color(0xFF9B652B))),
            topLeft = Offset(w * .14f, lidY),
            size = Size(w * .72f, h * .22f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = gold,
            topLeft = Offset(w * .14f, lidY),
            size = Size(w * .72f, h * .22f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(2.8f)
        )

        drawRoundRect(
            color = purple.copy(alpha = 1f - .45f * openProgress),
            topLeft = Offset(w * .43f, h * .54f),
            size = Size(w * .14f, h * .16f),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawCircle(brightGold, 3.2f, Offset(w * .50f, h * .60f))
    }
}
