package com.example.cyberquiz.ui.screens

import android.content.Context
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
import com.example.cyberquiz.ui.theme.CyberBackground

private enum class CosmeticsTab { AVATARS, BANNERS }

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
    val preferences = remember(context) {
        context.getSharedPreferences(PLAYER_COSMETICS_PREFERENCES, Context.MODE_PRIVATE)
    }
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

    val selectedAvatar = playerAvatarFromStorage(selectedAvatarKey)
    val selectedBanner = playerBannerFromStorage(selectedBannerKey)

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
        CosmeticsHeader(onBack = onBack)

        CosmeticsPreview(
            playerLevel = playerLevel,
            avatar = selectedAvatar,
            banner = selectedBanner
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF071329), RoundedCornerShape(18.dp))
                .border(1.dp, CosmeticsBorder, RoundedCornerShape(18.dp))
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
        }

        when (selectedTab) {
            CosmeticsTab.AVATARS -> AvatarCatalog(
                playerLevel = playerLevel,
                selected = selectedAvatar,
                selectedBanner = selectedBanner,
                onSelect = { style ->
                    if (isAvatarUnlocked(style, playerLevel)) {
                        storePlayerAvatar(context, style)
                        selectedAvatarKey = style.storageKey
                    }
                }
            )
            CosmeticsTab.BANNERS -> BannerCatalog(
                playerLevel = playerLevel,
                selected = selectedBanner,
                onSelect = { style ->
                    if (isBannerUnlocked(style, playerLevel)) {
                        storePlayerBanner(context, style)
                        selectedBannerKey = style.storageKey
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CosmeticsHeader(onBack: () -> Unit) {
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
        Column {
            Text("Cosmétiques", color = CosmeticsText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(
                "COLLECTION & PERSONNALISATION",
                color = CosmeticsMuted,
                fontSize = 8.sp,
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CosmeticsPreview(
    playerLevel: Int,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle
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
            "TON PROFIL",
            color = CosmeticsCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatarButton(
                style = avatar,
                onClick = {},
                size = 82.dp,
                syncWithStoredSelection = false,
                bannerStyle = banner,
                syncBannerWithStoredSelection = false,
                showEditBadge = false
            )
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(avatar.displayName, color = CosmeticsText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(banner.displayName, color = CosmeticsCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Niveau $playerLevel · ${PlayerAvatarStyle.entries.count { isAvatarUnlocked(it, playerLevel) }} / ${PlayerAvatarStyle.entries.size} avatars disponibles",
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
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else CosmeticsMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .8.sp
        )
    }
}

@Composable
private fun AvatarCatalog(
    playerLevel: Int,
    selected: PlayerAvatarStyle,
    selectedBanner: PlayerBannerStyle,
    onSelect: (PlayerAvatarStyle) -> Unit
) {
    CosmeticsSectionTitle("DISPONIBLES", "5 avatars offerts dès le départ")
    CosmeticsGrid {
        starterPlayerAvatarStyles.forEach { style ->
            AvatarCosmeticCard(
                style = style,
                playerLevel = playerLevel,
                selected = style == selected,
                selectedBanner = selectedBanner,
                onSelect = onSelect
            )
        }
    }

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("PAR NIVEAU", "Continue à jouer pour les débloquer")
    CosmeticsGrid {
        levelPlayerAvatarStyles.forEach { style ->
            AvatarCosmeticCard(
                style = style,
                playerLevel = playerLevel,
                selected = style == selected,
                selectedBanner = selectedBanner,
                onSelect = onSelect
            )
        }
    }

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("SECRETS", "10 avatars rares à découvrir")
    CosmeticsGrid {
        mysteryPlayerAvatarStyles.forEach { style ->
            AvatarCosmeticCard(
                style = style,
                playerLevel = playerLevel,
                selected = false,
                selectedBanner = selectedBanner,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun BannerCatalog(
    playerLevel: Int,
    selected: PlayerBannerStyle,
    onSelect: (PlayerBannerStyle) -> Unit
) {
    CosmeticsSectionTitle("DISPONIBLES", "Arrière-plans utilisables immédiatement")
    CosmeticsGrid {
        starterPlayerBannerStyles.forEach { style ->
            BannerCosmeticCard(style, playerLevel, style == selected, onSelect)
        }
    }

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("PAR NIVEAU", "De nouvelles ambiances avec ta progression")
    CosmeticsGrid {
        levelPlayerBannerStyles.forEach { style ->
            BannerCosmeticCard(style, playerLevel, style == selected, onSelect)
        }
    }

    Spacer(Modifier.height(7.dp))
    CosmeticsSectionTitle("SECRÈTES", "Bannières rares pour les futures récompenses")
    CosmeticsGrid {
        mysteryPlayerBannerStyles.forEach { style ->
            BannerCosmeticCard(style, playerLevel, false, onSelect)
        }
    }
}

@Composable
private fun CosmeticsSectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = CosmeticsBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Text(subtitle, color = CosmeticsMuted, fontSize = 10.sp)
    }
}

@Composable
private fun CosmeticsGrid(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        val marker = remember { mutableStateOf(0) }
        marker.value = 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(Modifier.weight(1f)) { content() }
        }
    }
}

@Composable
private fun AvatarCosmeticCard(
    style: PlayerAvatarStyle,
    playerLevel: Int,
    selected: Boolean,
    selectedBanner: PlayerBannerStyle,
    onSelect: (PlayerAvatarStyle) -> Unit
) {
    val unlocked = isAvatarUnlocked(style, playerLevel)
    val mystery = style.mystery
    val title = if (mystery) "???" else style.displayName
    val status = when {
        selected -> "ÉQUIPÉ"
        mystery -> "RÉCOMPENSE SECRÈTE"
        !unlocked -> "NIVEAU ${style.unlockLevel}"
        else -> "ÉQUIPER"
    }
    val statusColor = when {
        selected -> CosmeticsGreen
        unlocked -> CosmeticsCyan
        else -> CosmeticsOrange
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            PlayerAvatarButton(
                style = style,
                onClick = { if (unlocked) onSelect(style) },
                size = 68.dp,
                syncWithStoredSelection = false,
                bannerStyle = selectedBanner,
                syncBannerWithStoredSelection = false,
                showEditBadge = false,
                modifier = Modifier
            )
            if (!unlocked) {
                if (mystery) {
                    Box(
                        Modifier
                            .size(68.dp)
                            .blur(7.dp)
                            .background(Color(0x55000000), RoundedCornerShape(20.dp))
                    )
                }
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
        Text(title, color = CosmeticsText, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
        Text(status, color = statusColor, fontSize = 7.5.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BannerCosmeticCard(
    style: PlayerBannerStyle,
    playerLevel: Int,
    selected: Boolean,
    onSelect: (PlayerBannerStyle) -> Unit
) {
    val unlocked = isBannerUnlocked(style, playerLevel)
    val mystery = style.mystery
    val title = if (mystery) "???" else style.displayName
    val status = when {
        selected -> "ÉQUIPÉE"
        mystery -> "RÉCOMPENSE SECRÈTE"
        !unlocked -> "NIVEAU ${style.unlockLevel}"
        else -> "ÉQUIPER"
    }
    val statusColor = when {
        selected -> CosmeticsGreen
        unlocked -> CosmeticsCyan
        else -> CosmeticsOrange
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(
                if (selected) 1.6.dp else 1.dp,
                if (selected) CosmeticsGreen else CosmeticsBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = unlocked) { onSelect(style) }
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.9f)
                .background(Color(0xFF040811), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            PlayerBannerBackdrop(
                style = style,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (mystery) Modifier.blur(7.dp) else Modifier)
            )
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
        Text(status, color = statusColor, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
    }
}
