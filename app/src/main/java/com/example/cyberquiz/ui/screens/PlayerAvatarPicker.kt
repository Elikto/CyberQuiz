package com.example.cyberquiz.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

internal const val PLAYER_COSMETICS_PREFERENCES = "cyberquiz_player_cosmetics"
internal const val PLAYER_SELECTED_AVATAR_KEY = "selected_avatar"
internal const val PLAYER_SELECTED_BANNER_KEY = "selected_banner"

internal enum class PlayerAvatarStyle(
    val storageKey: String,
    val displayName: String,
    val subtitle: String,
    val unlockLevel: Int = 1,
    val mystery: Boolean = false
) {
    BEGINNER("beginner", "Hacker débutant", "Premier accès"),
    SOC("soc", "Analyste SOC", "Veille & défense"),
    PENTEST("pentest", "Pentester", "Mode offensif"),
    GHOST("ghost", "Ghost", "Discret & anonyme"),
    ARCHITECT("architect", "Architecte cyber", "Expert système"),

    MALWARE_HUNTER("malware_hunter", "Chasseur de malwares", "Traque les menaces", unlockLevel = 5),
    BLUE_ELITE("blue_elite", "Blue Team Elite", "Défense renforcée", unlockLevel = 8),
    RED_RUNNER("red_runner", "Red Team Runner", "Attaque contrôlée", unlockLevel = 12),
    CRYPTO_SENTINEL("crypto_sentinel", "Crypto Sentinel", "Gardien du chiffrement", unlockLevel = 16),
    FIREWALL_MASTER("firewall_master", "Maître du Firewall", "Dernière ligne de défense", unlockLevel = 20),

    NETWORK_NINJA("network_ninja", "Ninja réseau", "Récompense secrète", mystery = true),
    CYBER_SAMURAI("cyber_samurai", "Cyber samouraï", "Récompense secrète", mystery = true),
    CIPHER_QUEEN("cipher_queen", "Reine du chiffrement", "Récompense secrète", mystery = true),
    DIGITAL_SPECTER("digital_specter", "Spectre numérique", "Récompense secrète", mystery = true),
    EXPLOIT_HUNTER("exploit_hunter", "Chasseur d'exploits", "Récompense secrète", mystery = true),
    AI_TACTICIAN("ai_tactician", "IA tacticienne", "Récompense secrète", mystery = true),
    BLACK_ICE("black_ice", "Opérateur Black Ice", "Récompense secrète", mystery = true),
    QUANTUM_ARCHIVIST("quantum_archivist", "Archiviste quantique", "Récompense secrète", mystery = true),
    BINARY_DRAGON("binary_dragon", "Dragon binaire", "Récompense secrète", mystery = true),
    OVERMIND("overmind", "Overmind", "Récompense secrète", mystery = true)
}

internal enum class PlayerBannerStyle(
    val storageKey: String,
    val displayName: String,
    val subtitle: String,
    val unlockLevel: Int = 1,
    val mystery: Boolean = false
) {
    CIRCUIT_BLUE("circuit_blue", "Circuit bleu", "Néon classique"),
    MATRIX_GREEN("matrix_green", "Matrix verte", "Flux de données"),
    NETWORK_PURPLE("network_purple", "Réseau violet", "Connexions actives"),
    TERMINAL_DARK("terminal_dark", "Terminal hacker", "Console nocturne"),

    SOC_RADAR("soc_radar", "Radar SOC", "Débloqué au niveau 5", unlockLevel = 5),
    GLITCH_RED("glitch_red", "Glitch rouge", "Débloqué au niveau 10", unlockLevel = 10),
    NEON_HEX("neon_hex", "Hexagones néon", "Débloqué au niveau 15", unlockLevel = 15),
    CYBER_STORM("cyber_storm", "Cyber tempête", "Débloqué au niveau 20", unlockLevel = 20),

    DARK_WEB_MESH("dark_web_mesh", "Dark Web Mesh", "Récompense secrète", mystery = true),
    VAULT_CORE("vault_core", "Coffre fort cyber", "Récompense secrète", mystery = true),
    SYSTEM_RIFT("system_rift", "Faille système", "Récompense secrète", mystery = true),
    QUANTUM_GRID("quantum_grid", "Grille quantique", "Récompense secrète", mystery = true),
    ZERO_DAY("zero_day", "Zero Day", "Récompense secrète", mystery = true),
    AURORA_PACKET("aurora_packet", "Aurora Packet", "Récompense secrète", mystery = true),
    BLACKOUT("blackout", "Blackout", "Récompense secrète", mystery = true),
    LEGENDARY_CORE("legendary_core", "Noyau légendaire", "Récompense secrète", mystery = true)
}

internal val starterPlayerAvatarStyles: List<PlayerAvatarStyle>
    get() = PlayerAvatarStyle.entries.filter { !it.mystery && it.unlockLevel == 1 }

internal val levelPlayerAvatarStyles: List<PlayerAvatarStyle>
    get() = PlayerAvatarStyle.entries.filter { !it.mystery && it.unlockLevel > 1 }

internal val mysteryPlayerAvatarStyles: List<PlayerAvatarStyle>
    get() = PlayerAvatarStyle.entries.filter { it.mystery }

internal val starterPlayerBannerStyles: List<PlayerBannerStyle>
    get() = PlayerBannerStyle.entries.filter { !it.mystery && it.unlockLevel == 1 }

internal val levelPlayerBannerStyles: List<PlayerBannerStyle>
    get() = PlayerBannerStyle.entries.filter { !it.mystery && it.unlockLevel > 1 }

internal val mysteryPlayerBannerStyles: List<PlayerBannerStyle>
    get() = PlayerBannerStyle.entries.filter { it.mystery }

internal fun isAvatarUnlocked(style: PlayerAvatarStyle, playerLevel: Int): Boolean =
    !style.mystery && playerLevel >= style.unlockLevel

internal fun isBannerUnlocked(style: PlayerBannerStyle, playerLevel: Int): Boolean =
    !style.mystery && playerLevel >= style.unlockLevel

internal fun playerAvatarFromStorage(value: String?): PlayerAvatarStyle =
    PlayerAvatarStyle.entries.firstOrNull { it.storageKey == value } ?: PlayerAvatarStyle.BEGINNER

internal fun playerBannerFromStorage(value: String?): PlayerBannerStyle =
    PlayerBannerStyle.entries.firstOrNull { it.storageKey == value } ?: PlayerBannerStyle.CIRCUIT_BLUE

internal fun playerCosmeticsPreferences(context: Context): SharedPreferences =
    context.getSharedPreferences(PLAYER_COSMETICS_PREFERENCES, Context.MODE_PRIVATE)

internal fun storedPlayerAvatar(context: Context): PlayerAvatarStyle =
    playerAvatarFromStorage(
        playerCosmeticsPreferences(context).getString(
            PLAYER_SELECTED_AVATAR_KEY,
            PlayerAvatarStyle.BEGINNER.storageKey
        )
    )

internal fun storedPlayerBanner(context: Context): PlayerBannerStyle =
    playerBannerFromStorage(
        playerCosmeticsPreferences(context).getString(
            PLAYER_SELECTED_BANNER_KEY,
            PlayerBannerStyle.CIRCUIT_BLUE.storageKey
        )
    )

internal fun storePlayerAvatar(context: Context, style: PlayerAvatarStyle) {
    playerCosmeticsPreferences(context)
        .edit()
        .putString(PLAYER_SELECTED_AVATAR_KEY, style.storageKey)
        .apply()
}

internal fun storePlayerBanner(context: Context, style: PlayerBannerStyle) {
    playerCosmeticsPreferences(context)
        .edit()
        .putString(PLAYER_SELECTED_BANNER_KEY, style.storageKey)
        .apply()
}

private val AvatarAccents = listOf(
    0xFF27E9FF, 0xFF4EA1FF, 0xFFE35BFF, 0xFFD8E2FF, 0xFFFFC857,
    0xFF56F39A, 0xFF1DD3F8, 0xFFFF5D8F, 0xFFB388FF, 0xFFFF8A47,
    0xFF58FFE1, 0xFFFF6B6B, 0xFFFF8DE1, 0xFF8EA7FF, 0xFFFFCE5C,
    0xFF73F0FF, 0xFF8CC8FF, 0xFFC5A4FF, 0xFFFF7D54, 0xFFF1F5FF
).map(::Color)

private val AvatarSecondaries = listOf(
    0xFF9D46FF, 0xFF19F2E5, 0xFFFF5D8F, 0xFF7E8FB5, 0xFF21D8FF,
    0xFF00A86B, 0xFF3E68FF, 0xFFFF2E63, 0xFF6A34FF, 0xFFFFC857,
    0xFF1A80FF, 0xFFB62EFF, 0xFFFFD166, 0xFF4C5E98, 0xFFFF5E5B,
    0xFF6A34FF, 0xFF2CE8FF, 0xFF7549FF, 0xFFFFC857, 0xFF8EA7FF
).map(::Color)

private fun avatarAccent(style: PlayerAvatarStyle): Color = AvatarAccents[style.ordinal]
private fun avatarSecondary(style: PlayerAvatarStyle): Color = AvatarSecondaries[style.ordinal]

private val BannerPrimary = listOf(
    0xFF0B6CFF, 0xFF0ACB74, 0xFF7C3CFF, 0xFF1E2C49,
    0xFF1DD3F8, 0xFFFF344F, 0xFFA35BFF, 0xFF346CFF,
    0xFF25375A, 0xFFFFC857, 0xFFFF3C7A, 0xFF54D6FF,
    0xFFFF5D5D, 0xFF64E7FF, 0xFF181D2D, 0xFFFFB43C
).map(::Color)

private val BannerSecondary = listOf(
    0xFF8A2EFF, 0xFF052B19, 0xFF11D8FF, 0xFF080D17,
    0xFF1556FF, 0xFF7A1230, 0xFF19F2E5, 0xFF862EFF,
    0xFF5B2EFF, 0xFF2A1A00, 0xFF4D0A30, 0xFF2F34A7,
    0xFF13131B, 0xFF713BFF, 0xFF08090F, 0xFF792EFF
).map(::Color)

@Composable
internal fun PlayerAvatarButton(
    style: PlayerAvatarStyle,
    onClick: () -> Unit,
    size: Dp = 62.dp,
    syncWithStoredSelection: Boolean = true,
    bannerStyle: PlayerBannerStyle? = null,
    syncBannerWithStoredSelection: Boolean = true,
    showEditBadge: Boolean = true
) {
    val context = LocalContext.current
    val preferences = remember(context) { playerCosmeticsPreferences(context) }
    var storedAvatarKey by remember {
        mutableStateOf(preferences.getString(PLAYER_SELECTED_AVATAR_KEY, style.storageKey) ?: style.storageKey)
    }
    var storedBannerKey by remember {
        mutableStateOf(
            preferences.getString(
                PLAYER_SELECTED_BANNER_KEY,
                PlayerBannerStyle.CIRCUIT_BLUE.storageKey
            ) ?: PlayerBannerStyle.CIRCUIT_BLUE.storageKey
        )
    }

    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                PLAYER_SELECTED_AVATAR_KEY -> {
                    storedAvatarKey = prefs.getString(PLAYER_SELECTED_AVATAR_KEY, style.storageKey) ?: style.storageKey
                }
                PLAYER_SELECTED_BANNER_KEY -> {
                    storedBannerKey = prefs.getString(
                        PLAYER_SELECTED_BANNER_KEY,
                        PlayerBannerStyle.CIRCUIT_BLUE.storageKey
                    ) ?: PlayerBannerStyle.CIRCUIT_BLUE.storageKey
                }
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val effectiveStyle = if (syncWithStoredSelection) playerAvatarFromStorage(storedAvatarKey) else style
    val effectiveBanner = when {
        syncBannerWithStoredSelection -> playerBannerFromStorage(storedBannerKey)
        bannerStyle != null -> bannerStyle
        else -> PlayerBannerStyle.CIRCUIT_BLUE
    }
    val accent = avatarAccent(effectiveStyle)
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFF050A15), shape)
            .border(1.4.dp, accent.copy(alpha = .82f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PlayerBannerBackdrop(
            style = effectiveBanner,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color.Transparent, Color(0x66020812))
                    )
                )
        )
        AvatarArtwork(style = effectiveStyle, modifier = Modifier.size(size - 10.dp))

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(15.dp)
                    .background(Color(0xDD071221), CircleShape)
                    .border(1.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
internal fun PlayerBannerBackdrop(
    style: PlayerBannerStyle,
    modifier: Modifier = Modifier
) {
    val primary = BannerPrimary[style.ordinal]
    val secondary = BannerSecondary[style.ordinal]
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    primary.copy(alpha = .62f),
                    secondary.copy(alpha = .74f),
                    Color(0xFF050914)
                )
            )
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (style.ordinal % 4) {
                0 -> {
                    repeat(4) { i ->
                        val y = h * (.18f + i * .21f)
                        drawLine(primary.copy(alpha = .60f), Offset(0f, y), Offset(w * .34f, y), 1.6f)
                        drawLine(primary.copy(alpha = .60f), Offset(w * .66f, y), Offset(w, y), 1.6f)
                        drawCircle(primary.copy(alpha = .85f), 2.5f, Offset(w * .34f, y))
                        drawCircle(primary.copy(alpha = .85f), 2.5f, Offset(w * .66f, y))
                    }
                    drawLine(secondary.copy(alpha = .65f), Offset(w * .34f, h * .18f), Offset(w * .34f, h * .81f), 1.4f)
                    drawLine(secondary.copy(alpha = .65f), Offset(w * .66f, h * .18f), Offset(w * .66f, h * .81f), 1.4f)
                }
                1 -> {
                    repeat(6) { i ->
                        repeat(4) { j ->
                            drawCircle(
                                primary.copy(alpha = if ((i + j) % 2 == 0) .72f else .30f),
                                1.6f + (j % 2),
                                Offset(w * (.10f + i * .16f), h * (.14f + j * .23f))
                            )
                        }
                    }
                }
                2 -> {
                    repeat(5) { i ->
                        val x = w * (.08f + i * .21f)
                        drawLine(primary.copy(alpha = .45f), Offset(x, 0f), Offset(x + w * .12f, h), 1.2f)
                        drawLine(secondary.copy(alpha = .32f), Offset(x + w * .12f, 0f), Offset(x, h), 1.2f)
                    }
                }
                else -> {
                    repeat(3) { i ->
                        val inset = size.minDimension * (.10f + i * .12f)
                        drawRoundRect(
                            color = if (i % 2 == 0) primary.copy(alpha = .46f) else secondary.copy(alpha = .42f),
                            topLeft = Offset(inset, inset),
                            size = Size(w - inset * 2f, h - inset * 2f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(1.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarArtwork(style: PlayerAvatarStyle, modifier: Modifier = Modifier) {
    val accent = avatarAccent(style)
    val secondary = avatarSecondary(style)

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        drawCircle(
            brush = Brush.radialGradient(listOf(accent.copy(alpha = .24f), Color.Transparent)),
            radius = size.minDimension * .50f,
            center = Offset(cx, h * .50f)
        )

        val hood = Path().apply {
            moveTo(cx, h * .04f)
            cubicTo(w * .18f, h * .14f, w * .08f, h * .48f, w * .15f, h * .90f)
            lineTo(w * .85f, h * .90f)
            cubicTo(w * .92f, h * .48f, w * .82f, h * .14f, cx, h * .04f)
            close()
        }
        drawPath(
            hood,
            brush = Brush.verticalGradient(
                listOf(secondary.copy(alpha = .58f), Color(0xFF1A2440), Color(0xFF070D18))
            )
        )
        drawPath(hood, accent.copy(alpha = .78f), style = Stroke(2.1f))

        drawOval(
            color = Color(0xFF040912),
            topLeft = Offset(w * .26f, h * .29f),
            size = Size(w * .48f, h * .43f)
        )

        when (style.ordinal % 5) {
            0 -> {
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(secondary, accent)),
                    topLeft = Offset(w * .29f, h * .42f),
                    size = Size(w * .42f, h * .11f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawLine(Color.White.copy(alpha = .72f), Offset(w * .36f, h * .47f), Offset(w * .64f, h * .47f), 1.4f, StrokeCap.Round)
            }
            1 -> {
                drawArc(
                    color = accent,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * .22f, h * .29f),
                    size = Size(w * .56f, h * .39f),
                    style = Stroke(2.6f, cap = StrokeCap.Round)
                )
                drawCircle(secondary, 3.0f, Offset(w * .39f, h * .48f))
                drawCircle(secondary, 3.0f, Offset(w * .61f, h * .48f))
            }
            2 -> {
                drawRoundRect(
                    color = secondary.copy(alpha = .84f),
                    topLeft = Offset(w * .29f, h * .39f),
                    size = Size(w * .42f, h * .21f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawLine(Color(0xFF050912), Offset(w * .35f, h * .47f), Offset(w * .46f, h * .45f), 2.8f, StrokeCap.Round)
                drawLine(Color(0xFF050912), Offset(w * .54f, h * .45f), Offset(w * .65f, h * .47f), 2.8f, StrokeCap.Round)
            }
            3 -> {
                drawRoundRect(
                    color = Color(0xFF111827),
                    topLeft = Offset(w * .29f, h * .39f),
                    size = Size(w * .42f, h * .23f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawCircle(accent, 2.8f, Offset(w * .40f, h * .48f))
                drawCircle(accent, 2.8f, Offset(w * .60f, h * .48f))
                repeat(3) { i ->
                    drawLine(
                        secondary.copy(alpha = .70f),
                        Offset(w * .39f, h * (.57f + i * .035f)),
                        Offset(w * .61f, h * (.57f + i * .035f)),
                        1.4f,
                        StrokeCap.Round
                    )
                }
            }
            else -> {
                val badge = Path().apply {
                    moveTo(cx, h * .30f)
                    lineTo(w * .59f, h * .38f)
                    lineTo(cx, h * .45f)
                    lineTo(w * .41f, h * .38f)
                    close()
                }
                drawPath(badge, brush = Brush.linearGradient(listOf(accent, secondary)))
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(accent, secondary)),
                    topLeft = Offset(w * .30f, h * .48f),
                    size = Size(w * .40f, h * .09f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
            }
        }

        when (style.ordinal / 5) {
            1 -> {
                drawCircle(accent.copy(alpha = .70f), 4f, Offset(cx, h * .70f), style = Stroke(1.6f))
                drawLine(secondary, Offset(cx, h * .66f), Offset(cx, h * .74f), 1.5f, StrokeCap.Round)
            }
            2 -> {
                drawLine(accent, Offset(w * .19f, h * .22f), Offset(w * .31f, h * .31f), 2.0f, StrokeCap.Round)
                drawLine(accent, Offset(w * .81f, h * .22f), Offset(w * .69f, h * .31f), 2.0f, StrokeCap.Round)
                drawCircle(secondary.copy(alpha = .70f), 3f, Offset(cx, h * .73f))
            }
            3 -> {
                drawArc(
                    color = accent.copy(alpha = .72f),
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(w * .31f, h * .60f),
                    size = Size(w * .38f, h * .17f),
                    style = Stroke(1.7f, cap = StrokeCap.Round)
                )
                drawCircle(secondary, 2.4f, Offset(w * .28f, h * .33f))
                drawCircle(secondary, 2.4f, Offset(w * .72f, h * .33f))
            }
        }
    }
}
