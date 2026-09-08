package com.example.cyberquiz.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val PLAYER_SELECTED_FRAME_KEY = "selected_frame"

internal enum class PlayerFrameStyle(
    val storageKey: String,
    val displayName: String,
    val subtitle: String,
    val unlockLevel: Int = 1,
    val coinCost: Int = 0,
    val achievementId: String? = null,
    val mystery: Boolean = false
) {
    CYAN_PULSE("cyan_pulse", "Pulse cyan", "Cadre de départ"),
    PURPLE_NODE("purple_node", "Nœud violet", "Cadre de départ"),
    SOC_HEX("soc_hex", "Hexagone SOC", "Niveau 5", unlockLevel = 5),
    RED_TRACE("red_trace", "Trace rouge", "Niveau 10", unlockLevel = 10),
    QUANTUM_EDGE("quantum_edge", "Bord quantique", "Niveau 15", unlockLevel = 15),
    LEGEND_CORE("legend_core", "Noyau légendaire", "Niveau 20", unlockLevel = 20),
    CHROME_PACKET("chrome_packet", "Chrome Packet", "200 CyberCoins", coinCost = 200),
    FIREWALL_RING("firewall_ring", "Anneau Firewall", "350 CyberCoins · niveau 5", unlockLevel = 5, coinCost = 350),
    VOID_MATRIX("void_matrix", "Void Matrix", "550 CyberCoins · niveau 10", unlockLevel = 10, coinCost = 550),
    COMBO_TEN("combo_ten", "Combo x10", "Succès secret", achievementId = "streak_10", mystery = true),
    CENTURION("centurion", "Centurion", "Succès secret", achievementId = "questions_100", mystery = true),
    ETHICAL_MASTER("ethical_master", "Maître éthique", "Succès secret", achievementId = "level_10", mystery = true)
}

internal fun playerFrameFromStorage(value: String?): PlayerFrameStyle =
    PlayerFrameStyle.entries.firstOrNull { it.storageKey == value } ?: PlayerFrameStyle.CYAN_PULSE

internal fun storedPlayerFrame(context: Context): PlayerFrameStyle =
    playerFrameFromStorage(
        playerCosmeticsPreferences(context).getString(
            PLAYER_SELECTED_FRAME_KEY,
            PlayerFrameStyle.CYAN_PULSE.storageKey
        )
    )

internal fun storePlayerFrame(context: Context, style: PlayerFrameStyle) {
    playerCosmeticsPreferences(context)
        .edit()
        .putString(PLAYER_SELECTED_FRAME_KEY, style.storageKey)
        .apply()
}

internal fun isFrameUnlocked(
    style: PlayerFrameStyle,
    playerLevel: Int,
    purchasedFrameKeys: Set<String>,
    unlockedAchievementIds: Set<String>
): Boolean {
    if (playerLevel < style.unlockLevel) return false
    if (style.achievementId != null) return style.achievementId in unlockedAchievementIds
    if (style.coinCost > 0) return style.storageKey in purchasedFrameKeys
    return true
}

internal fun canPurchaseFrame(
    style: PlayerFrameStyle,
    playerLevel: Int,
    purchasedFrameKeys: Set<String>,
    coins: Int
): Boolean = style.coinCost > 0 &&
    style.storageKey !in purchasedFrameKeys &&
    playerLevel >= style.unlockLevel &&
    coins >= style.coinCost

private val framePrimary = listOf(
    0xFF27E9FF, 0xFFD652FF, 0xFF19F2E5, 0xFFFF557A,
    0xFF8B7CFF, 0xFFFFC857, 0xFFE5F0FF, 0xFFFF7A4F,
    0xFF7657FF, 0xFF35F2A0, 0xFFFFC857, 0xFF8FD9FF
).map(::Color)

private val frameSecondary = listOf(
    0xFF6A34FF, 0xFF27DFFF, 0xFF2B7FFF, 0xFFD652FF,
    0xFF19F2E5, 0xFFFF5D8F, 0xFF6EA8FF, 0xFFFFD166,
    0xFF181D2D, 0xFF1A80FF, 0xFFFF5D8F, 0xFFD652FF
).map(::Color)

@Composable
internal fun PlayerAvatarWithFrame(
    style: PlayerAvatarStyle,
    onClick: () -> Unit,
    size: Dp = 62.dp,
    syncWithStoredSelection: Boolean = true,
    bannerStyle: PlayerBannerStyle? = null,
    syncBannerWithStoredSelection: Boolean = true,
    frameStyle: PlayerFrameStyle? = null,
    syncFrameWithStoredSelection: Boolean = true,
    showEditBadge: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember(context) { playerCosmeticsPreferences(context) }
    var storedFrameKey by remember {
        mutableStateOf(
            preferences.getString(
                PLAYER_SELECTED_FRAME_KEY,
                PlayerFrameStyle.CYAN_PULSE.storageKey
            ) ?: PlayerFrameStyle.CYAN_PULSE.storageKey
        )
    }

    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == PLAYER_SELECTED_FRAME_KEY) {
                storedFrameKey = prefs.getString(
                    PLAYER_SELECTED_FRAME_KEY,
                    PlayerFrameStyle.CYAN_PULSE.storageKey
                ) ?: PlayerFrameStyle.CYAN_PULSE.storageKey
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val effectiveFrame = when {
        syncFrameWithStoredSelection -> playerFrameFromStorage(storedFrameKey)
        frameStyle != null -> frameStyle
        else -> PlayerFrameStyle.CYAN_PULSE
    }

    Box(
        modifier = Modifier.size(size + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        PlayerFrameDecoration(
            style = effectiveFrame,
            modifier = Modifier.fillMaxSize()
        )
        PlayerAvatarButton(
            style = style,
            onClick = onClick,
            size = size,
            syncWithStoredSelection = syncWithStoredSelection,
            bannerStyle = bannerStyle,
            syncBannerWithStoredSelection = syncBannerWithStoredSelection,
            showEditBadge = showEditBadge
        )
    }
}

@Composable
internal fun PlayerFrameDecoration(
    style: PlayerFrameStyle,
    modifier: Modifier = Modifier
) {
    val primary = framePrimary[style.ordinal]
    val secondary = frameSecondary[style.ordinal]
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .background(Color.Transparent, shape)
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(listOf(primary, secondary, primary)),
                shape = shape
            )
            .padding(1.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val glow = primary.copy(alpha = .85f)
            when (style.ordinal % 4) {
                0 -> {
                    drawLine(glow, Offset(w * .16f, 0f), Offset(w * .34f, 0f), 4f, StrokeCap.Round)
                    drawLine(glow, Offset(w * .66f, h), Offset(w * .84f, h), 4f, StrokeCap.Round)
                }
                1 -> {
                    drawCircle(secondary.copy(alpha = .9f), 3.2f, Offset(w * .12f, h * .20f))
                    drawCircle(primary.copy(alpha = .9f), 3.2f, Offset(w * .88f, h * .80f))
                }
                2 -> {
                    drawArc(primary.copy(alpha = .75f), 205f, 105f, false, style = Stroke(3f, cap = StrokeCap.Round))
                    drawArc(secondary.copy(alpha = .75f), 25f, 105f, false, style = Stroke(3f, cap = StrokeCap.Round))
                }
                else -> {
                    val y1 = h * .08f
                    val y2 = h * .92f
                    drawLine(primary, Offset(w * .18f, y1), Offset(w * .42f, y1), 2.5f, StrokeCap.Round)
                    drawLine(secondary, Offset(w * .58f, y2), Offset(w * .82f, y2), 2.5f, StrokeCap.Round)
                }
            }
        }
    }
}
