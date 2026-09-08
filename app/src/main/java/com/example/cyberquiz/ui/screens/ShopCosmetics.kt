package com.example.cyberquiz.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

internal const val PLAYER_SELECTED_SHOP_AVATAR_KEY = "selected_shop_avatar"
internal const val PLAYER_SELECTED_SHOP_BANNER_KEY = "selected_shop_banner"
internal const val SHOP_COSMETIC_COST = 10

internal enum class ShopAvatarStyle(
    val storageKey: String,
    val displayName: String,
    val subtitle: String
) {
    PIXEL_BUDDY("shop_pixel_buddy", "Pixel Buddy", "Compagnon 8-bit"),
    BUG_BOT("shop_bug_bot", "Bug Bot", "Petit robot débogueur"),
    FOX_ZERO("shop_fox_zero", "Fox Zero", "Renard cyber"),
    DRONE_EYE("shop_drone_eye", "Drone Eye", "Œil de surveillance"),
    ROOT_MECHA("shop_root_mecha", "Root Mecha", "Armure root")
}

internal enum class ShopBannerStyle(
    val storageKey: String,
    val displayName: String,
    val subtitle: String
) {
    AURORA_GRID("shop_aurora_grid", "Grille Aurora", "Lueurs polaires"),
    PACKET_RAIN("shop_packet_rain", "Pluie de paquets", "Flux réseau"),
    BLACK_NEON("shop_black_neon", "Black Neon", "Néon sombre"),
    BINARY_SUNSET("shop_binary_sunset", "Binary Sunset", "Coucher numérique"),
    ZERO_TRACE("shop_zero_trace", "Zero Trace", "Signal fantôme")
}

internal fun shopAvatarFromStorage(value: String?): ShopAvatarStyle? =
    ShopAvatarStyle.entries.firstOrNull { it.storageKey == value }

internal fun shopBannerFromStorage(value: String?): ShopBannerStyle? =
    ShopBannerStyle.entries.firstOrNull { it.storageKey == value }

internal fun storedShopAvatar(context: Context): ShopAvatarStyle? =
    shopAvatarFromStorage(playerCosmeticsPreferences(context).getString(PLAYER_SELECTED_SHOP_AVATAR_KEY, null))

internal fun storedShopBanner(context: Context): ShopBannerStyle? =
    shopBannerFromStorage(playerCosmeticsPreferences(context).getString(PLAYER_SELECTED_SHOP_BANNER_KEY, null))

internal fun storeShopAvatar(context: Context, style: ShopAvatarStyle) {
    playerCosmeticsPreferences(context).edit().putString(PLAYER_SELECTED_SHOP_AVATAR_KEY, style.storageKey).apply()
}

internal fun storeShopBanner(context: Context, style: ShopBannerStyle) {
    playerCosmeticsPreferences(context).edit().putString(PLAYER_SELECTED_SHOP_BANNER_KEY, style.storageKey).apply()
}

internal fun clearShopAvatar(context: Context) {
    playerCosmeticsPreferences(context).edit().remove(PLAYER_SELECTED_SHOP_AVATAR_KEY).apply()
}

internal fun clearShopBanner(context: Context) {
    playerCosmeticsPreferences(context).edit().remove(PLAYER_SELECTED_SHOP_BANNER_KEY).apply()
}

internal fun shopAvatarAccent(style: ShopAvatarStyle): Color = when (style) {
    ShopAvatarStyle.PIXEL_BUDDY -> Color(0xFF43E8FF)
    ShopAvatarStyle.BUG_BOT -> Color(0xFF76F59C)
    ShopAvatarStyle.FOX_ZERO -> Color(0xFFFF8A58)
    ShopAvatarStyle.DRONE_EYE -> Color(0xFFFF5D91)
    ShopAvatarStyle.ROOT_MECHA -> Color(0xFFB98CFF)
}

@Composable
internal fun ShopAvatarArtwork(
    style: ShopAvatarStyle,
    modifier: Modifier = Modifier
) {
    val accent = shopAvatarAccent(style)
    val dark = Color(0xFF050A13)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        drawCircle(
            brush = Brush.radialGradient(listOf(accent.copy(alpha = .22f), Color.Transparent)),
            radius = size.minDimension * .48f,
            center = c
        )
        when (style) {
            ShopAvatarStyle.PIXEL_BUDDY -> {
                drawRoundRect(Color(0xFF24314B), Offset(w*.24f,h*.24f), Size(w*.52f,h*.52f), CornerRadius(5f,5f))
                drawRect(accent, Offset(w*.33f,h*.39f), Size(w*.10f,h*.10f))
                drawRect(accent, Offset(w*.57f,h*.39f), Size(w*.10f,h*.10f))
                drawRect(Color(0xFFDCE8FF), Offset(w*.39f,h*.61f), Size(w*.22f,h*.05f))
                drawLine(accent, Offset(w*.50f,h*.24f), Offset(w*.50f,h*.13f), 2.4f, StrokeCap.Round)
                drawCircle(accent, 3f, Offset(w*.50f,h*.11f))
            }
            ShopAvatarStyle.BUG_BOT -> {
                drawRoundRect(Color(0xFF172B27), Offset(w*.22f,h*.25f), Size(w*.56f,h*.50f), CornerRadius(14f,14f))
                drawCircle(accent, 5f, Offset(w*.38f,h*.45f))
                drawCircle(accent, 5f, Offset(w*.62f,h*.45f))
                repeat(3) { i -> drawCircle(Color(0xFFBFFFD4), 2f, Offset(w*(.39f+i*.11f),h*.62f)) }
                drawLine(accent, Offset(w*.29f,h*.24f), Offset(w*.20f,h*.14f), 2f)
                drawLine(accent, Offset(w*.71f,h*.24f), Offset(w*.80f,h*.14f), 2f)
            }
            ShopAvatarStyle.FOX_ZERO -> {
                val head = Path().apply {
                    moveTo(w*.25f,h*.33f); lineTo(w*.18f,h*.12f); lineTo(w*.40f,h*.27f)
                    quadraticBezierTo(w*.50f,h*.20f,w*.60f,h*.27f); lineTo(w*.82f,h*.12f); lineTo(w*.75f,h*.33f)
                    quadraticBezierTo(w*.78f,h*.66f,w*.50f,h*.78f); quadraticBezierTo(w*.22f,h*.66f,w*.25f,h*.33f); close()
                }
                drawPath(head, Brush.verticalGradient(listOf(accent, Color(0xFF783A2C))))
                drawPath(head, Color.White.copy(alpha=.35f), style=Stroke(1.5f))
                drawLine(dark, Offset(w*.34f,h*.45f), Offset(w*.44f,h*.48f), 3f, StrokeCap.Round)
                drawLine(dark, Offset(w*.56f,h*.48f), Offset(w*.66f,h*.45f), 3f, StrokeCap.Round)
                drawCircle(dark, 3.2f, Offset(w*.50f,h*.60f))
            }
            ShopAvatarStyle.DRONE_EYE -> {
                drawCircle(Color(0xFF171E31), size.minDimension*.33f, c)
                drawCircle(accent.copy(alpha=.25f), size.minDimension*.25f, c)
                drawCircle(accent, size.minDimension*.14f, c, style=Stroke(4f))
                drawCircle(Color.White, 3f, c)
                repeat(4) { i ->
                    val x = if (i % 2 == 0) w*(if(i==0) .12f else .88f) else w*.50f
                    val y = if (i % 2 == 1) h*(if(i==1) .12f else .88f) else h*.50f
                    drawLine(accent.copy(alpha=.65f), c, Offset(x,y), 2f, StrokeCap.Round)
                }
            }
            ShopAvatarStyle.ROOT_MECHA -> {
                val mask = Path().apply {
                    moveTo(w*.50f,h*.14f); lineTo(w*.73f,h*.28f); lineTo(w*.69f,h*.69f); lineTo(w*.50f,h*.82f); lineTo(w*.31f,h*.69f); lineTo(w*.27f,h*.28f); close()
                }
                drawPath(mask, Brush.linearGradient(listOf(Color(0xFF273353), accent.copy(alpha=.70f))))
                drawPath(mask, accent, style=Stroke(2.2f))
                drawLine(Color(0xFFE4D7FF), Offset(w*.34f,h*.44f), Offset(w*.46f,h*.47f), 3f, StrokeCap.Round)
                drawLine(Color(0xFFE4D7FF), Offset(w*.54f,h*.47f), Offset(w*.66f,h*.44f), 3f, StrokeCap.Round)
                drawRoundRect(dark, Offset(w*.39f,h*.60f), Size(w*.22f,h*.07f), CornerRadius(4f,4f))
            }
        }
    }
}

@Composable
internal fun ShopBannerBackdrop(
    style: ShopBannerStyle,
    modifier: Modifier = Modifier
) {
    val colors = when (style) {
        ShopBannerStyle.AURORA_GRID -> listOf(Color(0xFF0C2C52), Color(0xFF3354A5), Color(0xFF12A88E))
        ShopBannerStyle.PACKET_RAIN -> listOf(Color(0xFF03251D), Color(0xFF064A38), Color(0xFF07121A))
        ShopBannerStyle.BLACK_NEON -> listOf(Color(0xFF08080F), Color(0xFF3F116A), Color(0xFF061A27))
        ShopBannerStyle.BINARY_SUNSET -> listOf(Color(0xFF521A40), Color(0xFF9A3B50), Color(0xFF10254B))
        ShopBannerStyle.ZERO_TRACE -> listOf(Color(0xFF091019), Color(0xFF15283E), Color(0xFF05070D))
    }
    Box(modifier.background(Brush.linearGradient(colors))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (style) {
                ShopBannerStyle.AURORA_GRID -> {
                    repeat(5) { i ->
                        val y = h * (.15f + i*.18f)
                        drawLine(Color(0xFF5CF5DB).copy(alpha=.25f), Offset(0f,y), Offset(w,y), 1f)
                    }
                    repeat(5) { i ->
                        val x = w * (.12f + i*.19f)
                        drawLine(Color(0xFF8CB7FF).copy(alpha=.20f), Offset(x,0f), Offset(x,h), 1f)
                    }
                }
                ShopBannerStyle.PACKET_RAIN -> {
                    repeat(8) { i ->
                        val x = w * (.08f + i*.13f)
                        val start = h * ((i%3)*.12f)
                        drawLine(Color(0xFF72FFA8).copy(alpha=.45f), Offset(x,start), Offset(x,start+h*.35f), 1.8f, StrokeCap.Round)
                    }
                }
                ShopBannerStyle.BLACK_NEON -> {
                    drawLine(Color(0xFFD652FF).copy(alpha=.52f), Offset(0f,h*.25f), Offset(w,h*.65f), 2f)
                    drawLine(Color(0xFF27DFFF).copy(alpha=.38f), Offset(0f,h*.75f), Offset(w,h*.35f), 2f)
                }
                ShopBannerStyle.BINARY_SUNSET -> {
                    drawCircle(Color(0xFFFFC16A).copy(alpha=.34f), h*.28f, Offset(w*.73f,h*.37f))
                    repeat(4) { i -> drawLine(Color.White.copy(alpha=.13f), Offset(0f,h*(.58f+i*.10f)), Offset(w,h*(.58f+i*.10f)), 1f) }
                }
                ShopBannerStyle.ZERO_TRACE -> {
                    repeat(4) { i ->
                        val inset = size.minDimension * (.08f + i*.09f)
                        drawRoundRect(Color(0xFF8EC8FF).copy(alpha=.16f), Offset(inset,inset), Size(w-inset*2,h-inset*2), CornerRadius(8f,8f), style=Stroke(1.1f))
                    }
                }
            }
        }
    }
}