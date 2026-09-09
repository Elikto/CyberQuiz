package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.model.achievementDefinitions
import com.example.cyberquiz.ui.theme.CyberBackground

private enum class ShopMainTab { SHOP, ACHIEVEMENTS }
private enum class ShopCosmeticTab { AVATARS, BANNERS, FRAMES }

private val ShopText = Color(0xFFF5F7FF)
private val ShopMuted = Color(0xFF96A7CD)
private val ShopBorder = Color(0xFF244777)
private val ShopCyan = Color(0xFF19F2E5)
private val ShopPurple = Color(0xFFD652FF)
private val ShopOrange = Color(0xFFFFB84A)
private val ShopGreen = Color(0xFF38E69A)

@Composable
fun CosmeticShopScreen(
    metrics: EngagementMetrics,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var snapshot by remember(metrics) { mutableStateOf(EngagementStore.sync(context, metrics)) }
    var mainTab by rememberSaveable { mutableStateOf(ShopMainTab.SHOP) }
    var cosmeticTab by rememberSaveable { mutableStateOf(ShopCosmeticTab.AVATARS) }
    var selectedShopAvatarKey by remember { mutableStateOf(storedShopAvatar(context)?.storageKey) }
    var selectedShopBannerKey by remember { mutableStateOf(storedShopBanner(context)?.storageKey) }
    var selectedFrame by remember { mutableStateOf(storedPlayerFrame(context)) }
    val baseAvatar = storedPlayerAvatar(context)
    val baseBanner = storedPlayerBanner(context)

    fun refresh() {
        snapshot = EngagementStore.sync(context, metrics)
        selectedShopAvatarKey = storedShopAvatar(context)?.storageKey
        selectedShopBannerKey = storedShopBanner(context)?.storageKey
        selectedFrame = storedPlayerFrame(context)
    }

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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        ShopHeader(snapshot.coins, onBack)

        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF071329), RoundedCornerShape(17.dp))
                .border(1.dp, ShopBorder, RoundedCornerShape(17.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MainTabButton("BOUTIQUE", mainTab == ShopMainTab.SHOP, Modifier.weight(1f)) { mainTab = ShopMainTab.SHOP }
            MainTabButton("SUCCÈS", mainTab == ShopMainTab.ACHIEVEMENTS, Modifier.weight(1f)) { mainTab = ShopMainTab.ACHIEVEMENTS }
        }

        when (mainTab) {
            ShopMainTab.SHOP -> {
                Text(
                    "Tous les objets de cette boutique coûtent 10 CyberCoins.",
                    color = ShopMuted,
                    fontSize = 10.sp
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShopSubTab("AVATARS", cosmeticTab == ShopCosmeticTab.AVATARS, Modifier.weight(1f)) { cosmeticTab = ShopCosmeticTab.AVATARS }
                    ShopSubTab("BANNIÈRES", cosmeticTab == ShopCosmeticTab.BANNERS, Modifier.weight(1f)) { cosmeticTab = ShopCosmeticTab.BANNERS }
                    ShopSubTab("CONTOURS", cosmeticTab == ShopCosmeticTab.FRAMES, Modifier.weight(1f)) { cosmeticTab = ShopCosmeticTab.FRAMES }
                }

                when (cosmeticTab) {
                    ShopCosmeticTab.AVATARS -> ShopAvatarStyle.entries.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            row.forEach { style ->
                                val owned = style.storageKey in snapshot.purchasedAvatarKeys
                                ShopItemCard(
                                    title = style.displayName,
                                    subtitle = style.subtitle,
                                    owned = owned,
                                    selected = selectedShopAvatarKey == style.storageKey,
                                    coins = snapshot.coins,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (owned) {
                                            storeShopAvatar(context, style)
                                        } else if (EngagementStore.purchaseAvatar(context, style.storageKey, SHOP_COSMETIC_COST)) {
                                            storeShopAvatar(context, style)
                                        }
                                        refresh()
                                    }
                                ) {
                                    CyberAvatarView(
                                        style = baseAvatar,
                                        banner = baseBanner,
                                        frame = selectedFrame,
                                        onClick = {},
                                        size = 62.dp,
                                        shopAvatarStyle = style,
                                        shopBannerStyle = storedShopBanner(context),
                                        syncShopSelection = false
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    ShopCosmeticTab.BANNERS -> ShopBannerStyle.entries.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            row.forEach { style ->
                                val owned = style.storageKey in snapshot.purchasedBannerKeys
                                ShopItemCard(
                                    title = style.displayName,
                                    subtitle = style.subtitle,
                                    owned = owned,
                                    selected = selectedShopBannerKey == style.storageKey,
                                    coins = snapshot.coins,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (owned) {
                                            storeShopBanner(context, style)
                                        } else if (EngagementStore.purchaseBanner(context, style.storageKey, SHOP_COSMETIC_COST)) {
                                            storeShopBanner(context, style)
                                        }
                                        refresh()
                                    }
                                ) {
                                    CyberAvatarView(
                                        style = baseAvatar,
                                        banner = baseBanner,
                                        frame = selectedFrame,
                                        onClick = {},
                                        size = 62.dp,
                                        shopAvatarStyle = storedShopAvatar(context),
                                        shopBannerStyle = style,
                                        syncShopSelection = false
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    ShopCosmeticTab.FRAMES -> shopPlayerFrameStyles.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            row.forEach { style ->
                                val owned = style.storageKey in snapshot.purchasedFrameKeys
                                ShopItemCard(
                                    title = style.displayName,
                                    subtitle = "Contour boutique",
                                    owned = owned,
                                    selected = selectedFrame == style,
                                    coins = snapshot.coins,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (owned) {
                                            storePlayerFrame(context, style)
                                        } else if (EngagementStore.purchaseFrame(context, style.storageKey, SHOP_COSMETIC_COST)) {
                                            storePlayerFrame(context, style)
                                        }
                                        refresh()
                                    }
                                ) {
                                    CyberAvatarView(
                                        style = baseAvatar,
                                        banner = baseBanner,
                                        frame = style,
                                        onClick = {},
                                        size = 62.dp
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            ShopMainTab.ACHIEVEMENTS -> achievementDefinitions.forEach { achievement ->
                val unlocked = achievement.id in snapshot.unlockedAchievementIds
                ProgressRewardCard(
                    title = achievement.title,
                    subtitle = achievement.description,
                    reward = achievement.rewardCoins,
                    progress = achievementCosmeticProgress(achievement.id, metrics),
                    completed = unlocked
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ShopHeader(coins: Int, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) { Text("‹", color = ShopText, fontSize = 29.sp) }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("Boutique", color = ShopText, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("COSMÉTIQUES · SUCCÈS", color = ShopMuted, fontSize = 8.sp, letterSpacing = 1.1.sp)
        }
        Box(
            Modifier
                .background(Color(0xFF21163A), RoundedCornerShape(50.dp))
                .border(1.dp, ShopOrange.copy(alpha = .65f), RoundedCornerShape(50.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) { Text("◈ $coins", color = Color(0xFFFFC86A), fontSize = 10.sp, fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun MainTabButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(if (selected) ShopPurple.copy(alpha = .23f) else Color.Transparent, RoundedCornerShape(12.dp))
            .border(if (selected) 1.dp else 0.dp, if (selected) ShopPurple else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else ShopMuted, fontSize = 7.4.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ShopSubTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(if (selected) Color(0xFF123047) else Color(0xFF081329), RoundedCornerShape(13.dp))
            .border(1.dp, if (selected) ShopCyan.copy(alpha=.62f) else ShopBorder, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (selected) ShopCyan else ShopMuted, fontSize = 8.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun ShopItemCard(
    title: String,
    subtitle: String,
    owned: Boolean,
    selected: Boolean,
    coins: Int,
    modifier: Modifier,
    onClick: () -> Unit,
    preview: @Composable () -> Unit
) {
    val actionable = owned || coins >= SHOP_COSMETIC_COST
    Column(
        modifier
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(if (selected) 1.6.dp else 1.dp, if (selected) ShopGreen else ShopBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = actionable, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        preview()
        Text(title, color = ShopText, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
        Text(subtitle, color = ShopMuted, fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 2)
        Text(
            when {
                selected -> "ÉQUIPÉ"
                owned -> "ÉQUIPER"
                else -> "ACHETER · 10 ◈"
            },
            color = when {
                selected -> ShopGreen
                owned -> ShopCyan
                actionable -> ShopOrange
                else -> ShopMuted
            },
            fontSize = 8.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ProgressRewardCard(
    title: String,
    subtitle: String,
    reward: Int,
    progress: CosmeticProgress,
    completed: Boolean
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(17.dp))
            .border(1.dp, if (completed) ShopGreen.copy(alpha=.55f) else ShopBorder, RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(title, color = ShopText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = ShopMuted, fontSize = 9.5.sp, lineHeight = 13.sp)
            }
            Text(if (completed) "✓ +$reward ◈" else "+$reward ◈", color = if (completed) ShopGreen else ShopOrange, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        CosmeticProgressBar(progress)
    }
}

@Composable
internal fun CosmeticProgressBar(progress: CosmeticProgress) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF111E35), RoundedCornerShape(50.dp))
        ) {
            if (progress.fraction > 0f) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.fraction)
                        .height(6.dp)
                        .background(Brush.horizontalGradient(listOf(ShopPurple, ShopCyan)), RoundedCornerShape(50.dp))
                )
            }
        }
        Text(progress.label, color = ShopMuted, fontSize = 8.5.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
    }
}
