package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.ui.theme.CyberBackground

internal const val MAX_PLAYER_LEVEL = 30

internal enum class LevelRewardKind { AVATAR, BANNER, FRAME }

internal data class LevelRewardItem(
    val kind: LevelRewardKind,
    val name: String,
    val avatar: PlayerAvatarStyle? = null,
    val banner: PlayerBannerStyle? = null,
    val frame: PlayerFrameStyle? = null
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
    playerLevelRoles[(level.coerceIn(1, MAX_PLAYER_LEVEL) - 1)]

internal fun roadmapAvatarForLevel(level: Int): PlayerAvatarStyle {
    val styles = roadmapAvatars
    return styles[(level.coerceIn(1, MAX_PLAYER_LEVEL) - 1) % styles.size]
}

internal fun roadmapBannerForLevel(level: Int): PlayerBannerStyle {
    val styles = roadmapBanners
    return styles[((level.coerceIn(1, MAX_PLAYER_LEVEL) - 1) * 3) % styles.size]
}

internal fun roadmapFrameForLevel(level: Int): PlayerFrameStyle {
    val styles = roadmapFrames
    return styles[((level.coerceIn(1, MAX_PLAYER_LEVEL) - 1) * 2) % styles.size]
}

internal fun levelRewards(level: Int): List<LevelRewardItem> {
    val safeLevel = level.coerceIn(1, MAX_PLAYER_LEVEL)
    val rewards = mutableListOf<LevelRewardItem>()

    PlayerAvatarStyle.entries
        .filter { !it.mystery && it.unlockLevel == safeLevel }
        .forEach { style ->
            rewards += LevelRewardItem(
                kind = LevelRewardKind.AVATAR,
                name = style.displayName,
                avatar = style
            )
        }

    PlayerBannerStyle.entries
        .filter { !it.mystery && it.unlockLevel == safeLevel }
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
                it.unlockLevel == safeLevel
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
    onClick: () -> Unit
) {
    val displayLevel = level.coerceIn(1, MAX_PLAYER_LEVEL)
    val safeProgress = if (level >= MAX_PLAYER_LEVEL) 1f else progress.coerceIn(0f, 1f)
    val displayXp = if (level >= MAX_PLAYER_LEVEL) 100 else xpIntoLevel.coerceIn(0, 100)
    val barShape = RoundedCornerShape(4.dp)

    Column(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "NIV. $displayLevel",
                color = Color(0xFF63EFFF),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$displayXp/100 XP",
                    color = Color(0xFF91A5CE),
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(1.dp))
                Box(
                    Modifier
                        .width(106.dp)
                        .height(8.dp)
                        .clip(barShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF17223B), Color(0xFF070B14))
                            )
                        )
                        .border(1.dp, Color(0xFF536E9C), barShape)
                        .padding(1.dp)
                ) {
                    if (safeProgress > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(safeProgress)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF6647FF),
                                            Color(0xFFD74CFF),
                                            Color(0xFF2FE8FF)
                                        )
                                    ),
                                    RoundedCornerShape(3.dp)
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
                            .background(Color.White.copy(alpha = .16f))
                    )
                }
            }
        }
        Text(
            playerRoleForLevel(displayLevel),
            color = Color(0xFFDCE6FF),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun LevelProgressionScreen(
    currentLevel: Int,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val displayLevel = currentLevel.coerceIn(1, MAX_PLAYER_LEVEL)
    val roadmap = levelRoadmap()
    val currentIndex = (MAX_PLAYER_LEVEL - displayLevel).coerceIn(0, roadmap.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex)

    LaunchedEffect(displayLevel) {
        listState.scrollToItem(currentIndex)
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
            modifier = Modifier.fillMaxSize()
        ) {
            items(roadmap, key = { it.level }) { entry ->
                LevelRoadmapNode(
                    entry = entry,
                    currentLevel = displayLevel,
                    showConnector = entry.level != 1
                )
            }
        }
    }
}

@Composable
private fun LevelRoadmapNode(
    entry: LevelRoadmapEntry,
    currentLevel: Int,
    showConnector: Boolean
) {
    val current = entry.level == currentLevel
    val unlocked = entry.level < currentLevel
    val future = entry.level > currentLevel
    val accent = when {
        current -> Color(0xFF2DE9FF)
        unlocked -> Color(0xFF45D89A)
        else -> Color(0xFF536A94)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LevelIdentityCard(
            entry = entry,
            current = current,
            unlocked = unlocked,
            future = future,
            accent = accent
        )

        if (entry.rewards.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                entry.rewards.forEach { reward ->
                    LevelRewardPreviewCard(
                        reward = reward,
                        fallbackAvatar = entry.avatar,
                        fallbackBanner = entry.banner,
                        fallbackFrame = entry.frame,
                        future = future,
                        accent = accent
                    )
                }
            }
        }

        if (showConnector) {
            Spacer(Modifier.height(7.dp))
            Box(
                Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(accent.copy(alpha = .62f), Color(0xFF314563).copy(alpha = .35f))
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
            Spacer(Modifier.height(7.dp))
        } else {
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LevelIdentityCard(
    entry: LevelRoadmapEntry,
    current: Boolean,
    unlocked: Boolean,
    future: Boolean,
    accent: Color
) {
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .size(166.dp)
            .background(
                if (current) {
                    Brush.radialGradient(
                        listOf(Color(0xFF1C1A52), Color(0xFF091528), Color(0xFF07101E))
                    )
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF0B162B), Color(0xFF060D19)))
                },
                shape
            )
            .border(if (current) 1.8.dp else 1.dp, accent.copy(alpha = if (current) .95f else .48f), shape)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CyberAvatarView(
            style = entry.avatar,
            banner = entry.banner,
            frame = entry.frame,
            onClick = {},
            size = if (current) 78.dp else 72.dp,
            showEditBadge = false,
            syncShopSelection = false
        )
        Spacer(Modifier.height(7.dp))
        Text(
            "NIV. ${entry.level}",
            color = accent,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black
        )
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
                current -> "ACTUEL"
                unlocked -> "DÉBLOQUÉ"
                else -> "À VENIR"
            },
            color = accent.copy(alpha = .88f),
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
    future: Boolean,
    accent: Color
) {
    val shape = RoundedCornerShape(13.dp)

    Column(
        modifier = Modifier
            .width(104.dp)
            .height(98.dp)
            .background(Color(0xFF081226), shape)
            .border(1.dp, accent.copy(alpha = .28f), shape)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (reward.kind) {
            LevelRewardKind.AVATAR -> {
                CyberAvatarView(
                    style = reward.avatar ?: fallbackAvatar,
                    banner = fallbackBanner,
                    frame = PlayerFrameStyle.CYAN_PULSE,
                    onClick = {},
                    size = 49.dp,
                    showEditBadge = false,
                    syncShopSelection = false
                )
            }

            LevelRewardKind.BANNER -> {
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF050914))
                        .border(1.dp, accent.copy(alpha = .32f), RoundedCornerShape(10.dp))
                ) {
                    PlayerBannerBackdrop(
                        style = reward.banner ?: fallbackBanner,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            LevelRewardKind.FRAME -> {
                CyberAvatarView(
                    style = fallbackAvatar,
                    banner = fallbackBanner,
                    frame = reward.frame ?: fallbackFrame,
                    onClick = {},
                    size = 49.dp,
                    showEditBadge = false,
                    syncShopSelection = false
                )
            }
        }

        Text(
            reward.name,
            color = if (future) Color(0xFFAAB7D4) else Color(0xFFE4EBFF),
            fontSize = 7.4.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
