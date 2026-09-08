package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CyberAvatarAccents = listOf(
    0xFF27E9FF, 0xFF4EA1FF, 0xFFE35BFF, 0xFFD8E2FF, 0xFFFFC857,
    0xFF56F39A, 0xFF1DD3F8, 0xFFFF5D8F, 0xFFB388FF, 0xFFFF8A47,
    0xFF58FFE1, 0xFFFF6B6B, 0xFFFF8DE1, 0xFF8EA7FF, 0xFFFFCE5C,
    0xFF73F0FF, 0xFF8CC8FF, 0xFFC5A4FF, 0xFFFF7D54, 0xFFF1F5FF
).map(::Color)

private val CyberAvatarSecondaries = listOf(
    0xFF9D46FF, 0xFF19F2E5, 0xFFFF5D8F, 0xFF7E8FB5, 0xFF21D8FF,
    0xFF00A86B, 0xFF3E68FF, 0xFFFF2E63, 0xFF6A34FF, 0xFFFFC857,
    0xFF1A80FF, 0xFFB62EFF, 0xFFFFD166, 0xFF4C5E98, 0xFFFF5E5B,
    0xFF6A34FF, 0xFF2CE8FF, 0xFF7549FF, 0xFFFFC857, 0xFF8EA7FF
).map(::Color)

@Composable
internal fun CyberAvatarView(
    style: PlayerAvatarStyle,
    banner: PlayerBannerStyle,
    frame: PlayerFrameStyle = PlayerFrameStyle.CYAN_PULSE,
    onClick: () -> Unit,
    size: Dp = 64.dp,
    showEditBadge: Boolean = true
) {
    val accent = CyberAvatarAccents[style.ordinal]
    val secondary = CyberAvatarSecondaries[style.ordinal]
    val outerShape = RoundedCornerShape(24.dp)
    val innerShape = RoundedCornerShape(19.dp)

    Box(
        modifier = Modifier
            .size(size + 8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PlayerFrameDecoration(
            style = frame,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .size(size)
                .clip(innerShape)
                .background(Color(0xFF050A15), innerShape)
                .border(1.2.dp, accent.copy(alpha = .80f), innerShape),
            contentAlignment = Alignment.Center
        ) {
            PlayerBannerBackdrop(
                style = banner,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(innerShape)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(innerShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.Transparent, Color(0x66020812))
                        )
                    )
            )
            StylizedAvatarArtwork(
                style = style,
                accent = accent,
                secondary = secondary,
                modifier = Modifier.size(size - 10.dp)
            )
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(1.dp)
                    .size(18.dp)
                    .background(Color(0xEE071221), CircleShape)
                    .border(1.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
