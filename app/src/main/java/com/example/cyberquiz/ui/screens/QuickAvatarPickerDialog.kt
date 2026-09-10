package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementStore

private enum class QuickCosmeticTab { AVATAR, BANNER, FRAME }

@Composable
internal fun PlayerAvatarPickerDialog(
    playerLevel: Int,
    onSelectionChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val achievements = remember { EngagementStore.unlockedAchievementIds(context) }
    val purchasedFrames = remember { EngagementStore.purchasedFrameKeys(context) }
    val purchasedAvatars = remember { EngagementStore.purchasedAvatarKeys(context) }
    val purchasedBanners = remember { EngagementStore.purchasedBannerKeys(context) }
    var selectedTab by rememberSaveable { mutableStateOf(QuickCosmeticTab.AVATAR) }
    var avatar by remember { mutableStateOf(storedPlayerAvatar(context)) }
    var banner by remember { mutableStateOf(storedPlayerBanner(context)) }
    var frame by remember { mutableStateOf(storedPlayerFrame(context)) }
    var shopAvatar by remember { mutableStateOf(storedShopAvatar(context)) }
    var shopBanner by remember { mutableStateOf(storedShopBanner(context)) }

    val availableAvatars = remember(playerLevel, achievements) {
        PlayerAvatarStyle.entries.filter { isAvatarUnlocked(it, playerLevel, achievements) }
    }
    val availableBanners = remember(playerLevel, achievements) {
        PlayerBannerStyle.entries.filter { isBannerUnlocked(it, playerLevel, achievements) }
    }
    val availableFrames = remember(playerLevel, purchasedFrames, achievements) {
        PlayerFrameStyle.entries.filter { isFrameUnlocked(it, playerLevel, purchasedFrames, achievements) }
    }
    val ownedShopAvatars = ShopAvatarStyle.entries.filter { it.storageKey in purchasedAvatars }
    val ownedShopBanners = ShopBannerStyle.entries.filter { it.storageKey in purchasedBanners }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF081225),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Personnalise ton profil", color = Color(0xFFF5F7FF), fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Avatar, bannière et contour au même endroit.", color = Color(0xFF9FAED3), fontSize = 10.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF071329), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickTab("AVATAR", selectedTab == QuickCosmeticTab.AVATAR, Modifier.weight(1f)) { selectedTab = QuickCosmeticTab.AVATAR }
                    QuickTab("BANNIÈRE", selectedTab == QuickCosmeticTab.BANNER, Modifier.weight(1f)) { selectedTab = QuickCosmeticTab.BANNER }
                    QuickTab("CONTOUR", selectedTab == QuickCosmeticTab.FRAME, Modifier.weight(1f)) { selectedTab = QuickCosmeticTab.FRAME }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 390.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        QuickCosmeticTab.AVATAR -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickAvatarGrid(
                                styles = availableAvatars,
                                selected = avatar,
                                banner = banner,
                                frame = frame,
                                shopBanner = shopBanner,
                                shopAvatarActive = shopAvatar != null,
                                onSelect = { style ->
                                    avatar = style
                                    shopAvatar = null
                                    clearShopAvatar(context)
                                    storePlayerAvatar(context, style)
                                    onSelectionChanged()
                                }
                            )
                            if (ownedShopAvatars.isNotEmpty()) {
                                QuickSectionTitle("BOUTIQUE")
                                QuickShopAvatarGrid(
                                    styles = ownedShopAvatars,
                                    selected = shopAvatar,
                                    avatar = avatar,
                                    banner = banner,
                                    frame = frame,
                                    shopBanner = shopBanner,
                                    onSelect = { style ->
                                        shopAvatar = style
                                        storeShopAvatar(context, style)
                                        onSelectionChanged()
                                    }
                                )
                            }
                        }

                        QuickCosmeticTab.BANNER -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickBannerGrid(
                                styles = availableBanners,
                                selected = banner,
                                avatar = avatar,
                                frame = frame,
                                shopAvatar = shopAvatar,
                                shopBannerActive = shopBanner != null,
                                onSelect = { style ->
                                    banner = style
                                    shopBanner = null
                                    clearShopBanner(context)
                                    storePlayerBanner(context, style)
                                    onSelectionChanged()
                                }
                            )
                            if (ownedShopBanners.isNotEmpty()) {
                                QuickSectionTitle("BOUTIQUE")
                                QuickShopBannerGrid(
                                    styles = ownedShopBanners,
                                    selected = shopBanner,
                                    avatar = avatar,
                                    banner = banner,
                                    frame = frame,
                                    shopAvatar = shopAvatar,
                                    onSelect = { style ->
                                        shopBanner = style
                                        storeShopBanner(context, style)
                                        onSelectionChanged()
                                    }
                                )
                            }
                        }

                        QuickCosmeticTab.FRAME -> QuickFrameGrid(
                            styles = availableFrames,
                            selected = frame,
                            avatar = avatar,
                            banner = banner,
                            shopAvatar = shopAvatar,
                            shopBanner = shopBanner,
                            onSelect = { style ->
                                frame = style
                                storePlayerFrame(context, style)
                                onSelectionChanged()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("TERMINÉ", color = Color(0xFF19F2E5), fontWeight = FontWeight.Black) }
        }
    )
}

@Composable
private fun QuickTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(if (selected) Color(0xFFD652FF).copy(alpha=.24f) else Color.Transparent, RoundedCornerShape(10.dp))
            .border(if (selected) 1.dp else 0.dp, if (selected) Color(0xFFD652FF) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (selected) Color.White else Color(0xFF91A3CA), fontSize = 8.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun QuickSectionTitle(label: String) {
    Text(label, color = Color(0xFF19F2E5), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
}

@Composable
private fun QuickAvatarGrid(
    styles: List<PlayerAvatarStyle>,
    selected: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle,
    shopBanner: ShopBannerStyle?,
    shopAvatarActive: Boolean,
    onSelect: (PlayerAvatarStyle) -> Unit
) {
    QuickGrid(styles) { style, modifier ->
        val isSelected = style == selected && !shopAvatarActive
        QuickChoiceCard(style.displayName, if (isSelected) "ÉQUIPÉ" else style.subtitle, isSelected, modifier, { onSelect(style) }) {
            CyberAvatarView(
                style = style,
                banner = banner,
                frame = frame,
                onClick = { onSelect(style) },
                size = 54.dp,
                shopBannerStyle = shopBanner,
                syncShopSelection = false
            )
        }
    }
}

@Composable
private fun QuickShopAvatarGrid(
    styles: List<ShopAvatarStyle>,
    selected: ShopAvatarStyle?,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle,
    shopBanner: ShopBannerStyle?,
    onSelect: (ShopAvatarStyle) -> Unit
) {
    QuickGrid(styles) { style, modifier ->
        QuickChoiceCard(style.displayName, if (style == selected) "ÉQUIPÉ" else style.subtitle, style == selected, modifier, { onSelect(style) }) {
            CyberAvatarView(
                style = avatar,
                banner = banner,
                frame = frame,
                onClick = { onSelect(style) },
                size = 54.dp,
                shopAvatarStyle = style,
                shopBannerStyle = shopBanner,
                syncShopSelection = false
            )
        }
    }
}

@Composable
private fun QuickBannerGrid(
    styles: List<PlayerBannerStyle>,
    selected: PlayerBannerStyle,
    avatar: PlayerAvatarStyle,
    frame: PlayerFrameStyle,
    shopAvatar: ShopAvatarStyle?,
    shopBannerActive: Boolean,
    onSelect: (PlayerBannerStyle) -> Unit
) {
    QuickGrid(styles) { style, modifier ->
        val isSelected = style == selected && !shopBannerActive
        QuickChoiceCard(style.displayName, if (isSelected) "ÉQUIPÉE" else style.subtitle, isSelected, modifier, { onSelect(style) }) {
            CyberAvatarView(
                style = avatar,
                banner = style,
                frame = frame,
                onClick = { onSelect(style) },
                size = 54.dp,
                shopAvatarStyle = shopAvatar,
                syncShopSelection = false
            )
        }
    }
}

@Composable
private fun QuickShopBannerGrid(
    styles: List<ShopBannerStyle>,
    selected: ShopBannerStyle?,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle,
    shopAvatar: ShopAvatarStyle?,
    onSelect: (ShopBannerStyle) -> Unit
) {
    QuickGrid(styles) { style, modifier ->
        QuickChoiceCard(style.displayName, if (style == selected) "ÉQUIPÉE" else style.subtitle, style == selected, modifier, { onSelect(style) }) {
            CyberAvatarView(
                style = avatar,
                banner = banner,
                frame = frame,
                onClick = { onSelect(style) },
                size = 54.dp,
                shopAvatarStyle = shopAvatar,
                shopBannerStyle = style,
                syncShopSelection = false
            )
        }
    }
}

@Composable
private fun QuickFrameGrid(
    styles: List<PlayerFrameStyle>,
    selected: PlayerFrameStyle,
    avatar: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    shopAvatar: ShopAvatarStyle?,
    shopBanner: ShopBannerStyle?,
    onSelect: (PlayerFrameStyle) -> Unit
) {
    QuickGrid(styles) { style, modifier ->
        QuickChoiceCard(style.displayName, if (style == selected) "ÉQUIPÉ" else style.subtitle, style == selected, modifier, { onSelect(style) }) {
            CyberAvatarView(
                style = avatar,
                banner = banner,
                frame = style,
                onClick = { onSelect(style) },
                size = 54.dp,
                shopAvatarStyle = shopAvatar,
                shopBannerStyle = shopBanner,
                syncShopSelection = false
            )
        }
    }
}

@Composable
private fun <T> QuickGrid(items: List<T>, card: @Composable (T, Modifier) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item -> card(item, Modifier.weight(1f)) }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    preview: @Composable () -> Unit
) {
    val accent = if (selected) Color(0xFF38E69A) else Color(0xFF244777)
    Column(
        modifier
            .background(Color(0xFF0A152A), RoundedCornerShape(16.dp))
            .border(if (selected) 1.5.dp else 1.dp, accent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        preview()
        Text(title, color = Color(0xFFF5F7FF), fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
        Text(subtitle, color = if (selected) Color(0xFF38E69A) else Color(0xFF9FAED3), fontSize = 7.sp, textAlign = TextAlign.Center, maxLines = 2)
    }
}