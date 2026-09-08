package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.ui.theme.CyberBackground

private enum class CosmeticsTab { AVATARS, BANNERS, FRAMES }

private val CosmeticsPurple = Color(0xFFD652FF)
private val CosmeticsBlue = Color(0xFF19BFFF)
private val CosmeticsCyan = Color(0xFF19F2E5)
private val CosmeticsGreen = Color(0xFF38E69A)
private val CosmeticsOrange = Color(0xFFFFB84A)
private val CosmeticsText = Color(0xFFF5F7FF)
private val CosmeticsMuted = Color(0xFF9FAED3)
private val CosmeticsBorder = Color(0xFF244777)

@Composable
fun CosmeticsScreen(
    playerLevel: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) { playerCosmeticsPreferences(context) }
    var selectedTab by rememberSaveable { mutableStateOf(CosmeticsTab.AVATARS) }
    var selectedAvatarKey by rememberSaveable {
        mutableStateOf(
            preferences.getString(
                PLAYER_SELECTED_AVATAR_KEY,
                PlayerAvatarStyle.BEGINNER.storageKey
            ) ?: PlayerAvatarStyle.BEGINNER.storageKey
        )
    }
    var selectedBannerKey by rememberSaveable {
        mutableStateOf(
            preferences.getString(
                PLAYER_SELECTED_BANNER_KEY,
                PlayerBannerStyle.CIRCUIT_BLUE.storageKey
            ) ?: PlayerBannerStyle.CIRCUIT_BLUE.storageKey
        )
    }
    var selectedFrameKey by rememberSaveable {
        mutableStateOf(
            preferences.getString(
                PLAYER_SELECTED_FRAME_KEY,
                PlayerFrameStyle.CYAN_PULSE.storageKey
            ) ?: PlayerFrameStyle.CYAN_PULSE.storageKey
        )
    }
    var coins by remember { mutableIntStateOf(EngagementStore.currentCoins(context)) }
    var purchasedFrames by remember { mutableStateOf(EngagementStore.purchasedFrameKeys(context)) }
    val achievements = remember { EngagementStore.unlockedAchievementIds(context) }

    val selectedAvatar = playerAvatarFromStorage(selectedAvatarKey)
    val selectedBanner = playerBannerFromStorage(selectedBannerKey)
    val selectedFrame = playerFrameFromStorage(selectedFrameKey)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CosmeticsHeader(onBack = onBack, coins = coins)
        CosmeticsPreview(
            playerLevel = playerLevel,
            avatar = selectedAvatar,
            banner = selectedBanner,
            frame = selectedFrame,
            unlockedAchievementIds = achievements
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF071329), RoundedCornerShape(18.dp))
                .border(1.dp, CosmeticsBorder, RoundedCornerShape(18.dp))
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CosmeticsTabButton(
                label = "AVATARS",
                selected = selectedTab == CosmeticsTab.AVATARS,
                modifier = Modifier.weight(1f)
            ) { selectedTab = CosmeticsTab.AVATARS }
            CosmeticsTabButton(
                label = "BANNIÈRES",
                selected = selectedTab == CosmeticsTab.BANNERS,
                modifier = Modifier.weight(1f)
            ) { selectedTab = CosmeticsTab.BANNERS }
            CosmeticsTabButton(
                label = "CONTOURS",
                selected = selectedTab == CosmeticsTab.FRAMES,
                modifier = Modifier.weight(1f)
            ) { selectedTab = CosmeticsTab.FRAMES }
        }

        when (selectedTab) {
            CosmeticsTab.AVATARS -> AvatarCatalog(
                playerLevel = playerLevel,
                selected = selectedAvatar,
                selectedBanner = selectedBanner,
                selectedFrame = selectedFrame,
                unlockedAchievementIds = achievements,
                onSelect = { style ->
                    if (isAvatarUnlocked(style, playerLevel, achievements)) {
                        storePlayerAvatar(context, style)
                        selectedAvatarKey = style.storageKey
                    }
                }
            )

            CosmeticsTab.BANNERS -> BannerCatalog(
                playerLevel = playerLevel,
                selected = selectedBanner,
                selectedAvatar = selectedAvatar,
                selectedFrame = selectedFrame,
                onSelect = { style ->
                    if (isBannerUnlocked(style, playerLevel)) {
                        storePlayerBanner(context, style)
                        selectedBannerKey = style.storageKey
                    }
                }
            )

            CosmeticsTab.FRAMES -> FrameCatalog(
                playerLevel = playerLevel,
                selected = selectedFrame,
                selectedAvatar = selectedAvatar,
                selectedBanner = selectedBanner,
                coins = coins,
                purchasedFrameKeys = purchasedFrames,
                unlockedAchievementIds = achievements,
                onEquip = { style ->
                    storePlayerFrame(context, style)
                    selectedFrameKey = style.storageKey
                },
                onBuy = { style ->
                    if (EngagementStore.purchaseFrame(context, style.storageKey, style.coinCost)) {
                        purchasedFrames = EngagementStore.purchasedFrameKeys(context)
                        coins = EngagementStore.currentCoins(context)
                        storePlayerFrame(context, style)
                        selectedFrameKey = style.storageKey
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CosmeticsHeader(onBack: () -> Unit, coins: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = CosmeticsText, fontSize = 31.sp)
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Cosmétiques", color = CosmeticsText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(
                "AVATARS · BANNIÈRES · CONTOURS",
                color = CosmeticsMuted,
                fontSize = 8.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .background(Color(0xFF20153B), RoundedCornerShape(50.dp))
                .border(1.dp, CosmeticsOrange.copy(alpha = .65f), RoundedCornerShape(50.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text("◈ $coins", color = Color(0xFFFFC86A), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CosmeticsPreview(
    playerLevel: Int,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle,
    unlockedAchievementIds: Set<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF15143D), Color(0xFF07172F))),
                RoundedCornerShape(23.dp)
            )
            .border(1.2.dp, Color(0xFF416EC4), RoundedCornerShape(23.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "TON STYLE",
            color = CosmeticsCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            CyberAvatarView(
                style = avatar,
                banner = banner,
                frame = frame,
                onClick = {},
                size = 82.dp,
                showEditBadge = false
            )
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(avatar.displayName, color = CosmeticsText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(banner.displayName, color = CosmeticsCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(frame.displayName, color = CosmeticsOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Niveau $playerLevel · ${PlayerAvatarStyle.entries.count { isAvatarUnlocked(it, playerLevel, unlockedAchievementIds) }} / ${PlayerAvatarStyle.entries.size} avatars débloqués",
                    color = CosmeticsMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CosmeticsTabButton(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (selected) CosmeticsPurple.copy(alpha = .25f) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .border(
                if (selected) 1.2.dp else 0.dp,
                if (selected) CosmeticsPurple else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else CosmeticsMuted,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .5.sp
        )
    }
}

@Composable
private fun AvatarCatalog(
    playerLevel: Int,
    selected: PlayerAvatarStyle,
    selectedBanner: PlayerBannerStyle,
    selectedFrame: PlayerFrameStyle,
    unlockedAchievementIds: Set<String>,
    onSelect: (PlayerAvatarStyle) -> Unit
) {
    CosmeticsSectionTitle("DISPONIBLES", "5 avatars offerts dès le départ")
    AvatarGrid(starterPlayerAvatarStyles, playerLevel, selected, selectedBanner, selectedFrame, unlockedAchievementIds, onSelect)

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("PAR NIVEAU", "Continue à jouer pour les débloquer")
    AvatarGrid(levelPlayerAvatarStyles, playerLevel, selected, selectedBanner, selectedFrame, unlockedAchievementIds, onSelect)

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("DÉFIS", "Les 4 premiers révèlent leur nom · les suivants gardent le mystère")
    AvatarGrid(mysteryPlayerAvatarStyles, playerLevel, selected, selectedBanner, selectedFrame, unlockedAchievementIds, onSelect)
}

@Composable
private fun AvatarGrid(
    styles: List<PlayerAvatarStyle>,
    playerLevel: Int,
    selected: PlayerAvatarStyle,
    selectedBanner: PlayerBannerStyle,
    selectedFrame: PlayerFrameStyle,
    unlockedAchievementIds: Set<String>,
    onSelect: (PlayerAvatarStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        styles.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rowItems.forEach { style ->
                    AvatarCosmeticCard(
                        style = style,
                        playerLevel = playerLevel,
                        selected = style == selected,
                        selectedBanner = selectedBanner,
                        selectedFrame = selectedFrame,
                        unlockedAchievementIds = unlockedAchievementIds,
                        modifier = Modifier.weight(1f),
                        onSelect = onSelect
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BannerCatalog(
    playerLevel: Int,
    selected: PlayerBannerStyle,
    selectedAvatar: PlayerAvatarStyle,
    selectedFrame: PlayerFrameStyle,
    onSelect: (PlayerBannerStyle) -> Unit
) {
    CosmeticsSectionTitle("DISPONIBLES", "Arrière-plans utilisables immédiatement")
    BannerGrid(starterPlayerBannerStyles, playerLevel, selected, selectedAvatar, selectedFrame, onSelect)

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("PAR NIVEAU", "De nouvelles ambiances avec ta progression")
    BannerGrid(levelPlayerBannerStyles, playerLevel, selected, selectedAvatar, selectedFrame, onSelect)

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("SECRÈTES", "Bannières rares réservées aux prochaines récompenses")
    BannerGrid(mysteryPlayerBannerStyles, playerLevel, selected, selectedAvatar, selectedFrame, onSelect)
}

@Composable
private fun BannerGrid(
    styles: List<PlayerBannerStyle>,
    playerLevel: Int,
    selected: PlayerBannerStyle,
    selectedAvatar: PlayerAvatarStyle,
    selectedFrame: PlayerFrameStyle,
    onSelect: (PlayerBannerStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        styles.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rowItems.forEach { style ->
                    BannerCosmeticCard(
                        style = style,
                        playerLevel = playerLevel,
                        selected = style == selected,
                        selectedAvatar = selectedAvatar,
                        selectedFrame = selectedFrame,
                        modifier = Modifier.weight(1f),
                        onSelect = onSelect
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FrameCatalog(
    playerLevel: Int,
    selected: PlayerFrameStyle,
    selectedAvatar: PlayerAvatarStyle,
    selectedBanner: PlayerBannerStyle,
    coins: Int,
    purchasedFrameKeys: Set<String>,
    unlockedAchievementIds: Set<String>,
    onEquip: (PlayerFrameStyle) -> Unit,
    onBuy: (PlayerFrameStyle) -> Unit
) {
    CosmeticsSectionTitle("CONTOURS", "Même logique que les avatars : gratuits, niveaux, boutique et défis")
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        PlayerFrameStyle.entries.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rowItems.forEach { style ->
                    FrameCosmeticCard(
                        style = style,
                        playerLevel = playerLevel,
                        selected = style == selected,
                        selectedAvatar = selectedAvatar,
                        selectedBanner = selectedBanner,
                        coins = coins,
                        purchasedFrameKeys = purchasedFrameKeys,
                        unlockedAchievementIds = unlockedAchievementIds,
                        modifier = Modifier.weight(1f),
                        onEquip = onEquip,
                        onBuy = onBuy
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CosmeticsSectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = CosmeticsBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Text(subtitle, color = CosmeticsMuted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun AvatarCosmeticCard(
    style: PlayerAvatarStyle,
    playerLevel: Int,
    selected: Boolean,
    selectedBanner: PlayerBannerStyle,
    selectedFrame: PlayerFrameStyle,
    unlockedAchievementIds: Set<String>,
    modifier: Modifier,
    onSelect: (PlayerAvatarStyle) -> Unit
) {
    val unlocked = isAvatarUnlocked(style, playerLevel, unlockedAchievementIds)
    val mystery = style.mystery
    val title = when {
        unlocked -> style.displayName
        mystery -> lockedAvatarTitle(style)
        else -> style.displayName
    }
    val status = when {
        selected -> "ÉQUIPÉ"
        unlocked -> "ÉQUIPER"
        mystery -> lockedAvatarCondition(style)
        else -> "Débloqué au niveau ${style.unlockLevel}"
    }
    val statusColor = when {
        selected -> CosmeticsGreen
        unlocked -> CosmeticsCyan
        else -> CosmeticsOrange
    }

    Column(
        modifier = modifier
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(
                if (selected) 1.6.dp else 1.dp,
                if (selected) CosmeticsGreen else CosmeticsBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = unlocked) { onSelect(style) }
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = if (mystery && !unlocked) Modifier.blur(7.dp) else Modifier) {
                CyberAvatarView(
                    style = style,
                    banner = selectedBanner,
                    frame = selectedFrame,
                    onClick = { if (unlocked) onSelect(style) },
                    size = 68.dp,
                    showEditBadge = false
                )
            }
            if (!unlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(23.dp)
                        .background(Color(0xE70A1020), CircleShape)
                        .border(1.dp, CosmeticsOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 11.sp)
                }
            }
        }
        Text(
            title,
            color = CosmeticsText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            status,
            color = statusColor,
            fontSize = 7.5.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}

@Composable
private fun BannerCosmeticCard(
    style: PlayerBannerStyle,
    playerLevel: Int,
    selected: Boolean,
    selectedAvatar: PlayerAvatarStyle,
    selectedFrame: PlayerFrameStyle,
    modifier: Modifier,
    onSelect: (PlayerBannerStyle) -> Unit
) {
    val unlocked = isBannerUnlocked(style, playerLevel)
    val mystery = style.mystery
    val title = if (mystery && !unlocked) "???" else style.displayName
    val status = when {
        selected -> "ÉQUIPÉE"
        mystery -> "Défi à venir"
        !unlocked -> "Débloquée au niveau ${style.unlockLevel}"
        else -> "ÉQUIPER"
    }
    val statusColor = when {
        selected -> CosmeticsGreen
        unlocked -> CosmeticsCyan
        else -> CosmeticsOrange
    }

    Column(
        modifier = modifier
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(
                if (selected) 1.6.dp else 1.dp,
                if (selected) CosmeticsGreen else CosmeticsBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = unlocked) { onSelect(style) }
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.9f)
                .background(Color(0xFF040811), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = if (mystery && !unlocked) Modifier.blur(7.dp) else Modifier) {
                CyberAvatarView(
                    style = selectedAvatar,
                    banner = style,
                    frame = selectedFrame,
                    onClick = { if (unlocked) onSelect(style) },
                    size = 62.dp,
                    showEditBadge = false
                )
            }
            if (!unlocked) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xDF0A1020), CircleShape)
                        .border(1.dp, CosmeticsOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 12.sp)
                }
            }
        }
        Text(title, color = CosmeticsText, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 2)
        Text(status, color = statusColor, fontSize = 7.5.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FrameCosmeticCard(
    style: PlayerFrameStyle,
    playerLevel: Int,
    selected: Boolean,
    selectedAvatar: PlayerAvatarStyle,
    selectedBanner: PlayerBannerStyle,
    coins: Int,
    purchasedFrameKeys: Set<String>,
    unlockedAchievementIds: Set<String>,
    modifier: Modifier,
    onEquip: (PlayerFrameStyle) -> Unit,
    onBuy: (PlayerFrameStyle) -> Unit
) {
    val unlocked = isFrameUnlocked(style, playerLevel, purchasedFrameKeys, unlockedAchievementIds)
    val canBuy = canPurchaseFrame(style, playerLevel, purchasedFrameKeys, coins)
    val lockedByAchievement = style.achievementId != null && style.achievementId !in unlockedAchievementIds
    val status = when {
        selected -> "ÉQUIPÉ"
        unlocked -> "ÉQUIPER"
        canBuy -> "ACHETER · ◈ ${style.coinCost}"
        style.coinCost > 0 && playerLevel < style.unlockLevel -> "NIVEAU ${style.unlockLevel} · puis ◈ ${style.coinCost}"
        style.coinCost > 0 -> "◈ ${style.coinCost}"
        lockedByAchievement -> style.subtitle
        else -> "NIVEAU ${style.unlockLevel}"
    }

    Column(
        modifier = modifier
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(
                if (selected) 1.6.dp else 1.dp,
                if (selected) CosmeticsGreen else CosmeticsBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = unlocked || canBuy) {
                if (unlocked) onEquip(style) else if (canBuy) onBuy(style)
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(modifier = if (style.mystery && !unlocked) Modifier.blur(5.dp) else Modifier) {
            CyberAvatarView(
                style = selectedAvatar,
                banner = selectedBanner,
                frame = style,
                onClick = {
                    if (unlocked) onEquip(style) else if (canBuy) onBuy(style)
                },
                size = 66.dp,
                showEditBadge = false
            )
        }
        Text(
            if (style.mystery && !unlocked) "???" else style.displayName,
            color = CosmeticsText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            status,
            color = when {
                selected -> CosmeticsGreen
                unlocked -> CosmeticsCyan
                canBuy -> CosmeticsOrange
                else -> CosmeticsMuted
            },
            fontSize = 7.5.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}
