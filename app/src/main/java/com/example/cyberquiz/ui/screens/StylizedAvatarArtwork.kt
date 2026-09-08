package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
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

@Composable
internal fun StylizedAvatarArtwork(
    style: PlayerAvatarStyle,
    accent: Color,
    secondary: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val dark = Color(0xFF050912)
        val mid = Color(0xFF111B2F)

        drawCircle(
            brush = Brush.radialGradient(listOf(accent.copy(alpha = .28f), Color.Transparent)),
            radius = size.minDimension * .50f,
            center = Offset(cx, h * .50f)
        )

        if (style == PlayerAvatarStyle.BEGINNER) {
            drawCircle(Color(0xFF202A3D), size.minDimension * .34f, Offset(cx, h * .48f))
            drawCircle(Color(0xFFCBD7F5), 3.8f, Offset(w * .39f, h * .44f))
            drawCircle(Color(0xFFCBD7F5), 3.8f, Offset(w * .61f, h * .44f))
            drawLine(accent, Offset(w * .39f, h * .62f), Offset(w * .61f, h * .62f), 2.8f, StrokeCap.Round)
            drawRoundRect(
                color = secondary.copy(alpha = .72f),
                topLeft = Offset(w * .30f, h * .15f),
                size = Size(w * .40f, h * .12f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawLine(secondary, Offset(cx, h * .15f), Offset(cx, h * .08f), 2.2f, StrokeCap.Round)
            drawCircle(accent, 2.8f, Offset(cx, h * .07f))
            return@Canvas
        }

        if (style == PlayerAvatarStyle.SOC) {
            drawRoundRect(
                color = Color(0xFF202C43),
                topLeft = Offset(w * .23f, h * .22f),
                size = Size(w * .54f, h * .54f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = dark,
                topLeft = Offset(w * .30f, h * .37f),
                size = Size(w * .40f, h * .18f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawLine(accent, Offset(w * .36f, h * .46f), Offset(w * .44f, h * .46f), 3.2f, StrokeCap.Round)
            drawLine(accent, Offset(w * .56f, h * .46f), Offset(w * .64f, h * .46f), 3.2f, StrokeCap.Round)
            drawArc(secondary, 205f, 130f, false, Offset(w * .16f, h * .27f), Size(w * .68f, h * .42f), style = Stroke(2.3f))
            drawCircle(secondary, 3.2f, Offset(w * .79f, h * .57f))
            return@Canvas
        }

        val hood = Path().apply {
            moveTo(cx, h * .04f)
            cubicTo(w * .18f, h * .12f, w * .10f, h * .43f, w * .16f, h * .90f)
            lineTo(w * .84f, h * .90f)
            cubicTo(w * .90f, h * .43f, w * .82f, h * .12f, cx, h * .04f)
            close()
        }
        drawPath(
            hood,
            brush = Brush.verticalGradient(listOf(secondary.copy(alpha = .66f), mid, dark))
        )
        drawPath(hood, accent.copy(alpha = .78f), style = Stroke(2.1f))

        when (style) {
            PlayerAvatarStyle.PENTEST -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFFF4E75), accent)),
                    topLeft = Offset(w * .28f, h * .39f), size = Size(w * .44f, h * .14f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawLine(dark, Offset(w * .35f, h * .46f), Offset(w * .44f, h * .43f), 3f, StrokeCap.Round)
                drawLine(dark, Offset(w * .56f, h * .43f), Offset(w * .65f, h * .46f), 3f, StrokeCap.Round)
                drawLine(accent, Offset(cx, h * .57f), Offset(cx, h * .69f), 2f)
            }
            PlayerAvatarStyle.GHOST -> {
                val mask = Path().apply {
                    moveTo(cx, h * .24f)
                    cubicTo(w * .27f, h * .30f, w * .26f, h * .62f, cx, h * .73f)
                    cubicTo(w * .74f, h * .62f, w * .73f, h * .30f, cx, h * .24f)
                    close()
                }
                drawPath(mask, Color(0xFFE6ECFF).copy(alpha = .88f))
                drawOval(dark, Offset(w * .34f, h * .41f), Size(w * .10f, h * .12f))
                drawOval(dark, Offset(w * .56f, h * .41f), Size(w * .10f, h * .12f))
                drawArc(dark, 15f, 150f, false, Offset(w * .39f, h * .54f), Size(w * .22f, h * .10f), style = Stroke(2.8f))
            }
            PlayerAvatarStyle.ARCHITECT -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                val crown = Path().apply {
                    moveTo(w * .30f, h * .31f)
                    lineTo(w * .39f, h * .18f)
                    lineTo(cx, h * .30f)
                    lineTo(w * .61f, h * .18f)
                    lineTo(w * .70f, h * .31f)
                    close()
                }
                drawPath(crown, Brush.linearGradient(listOf(Color(0xFFFFD166), accent)))
                drawRoundRect(accent, Offset(w * .31f, h * .43f), Size(w * .38f, h * .09f), CornerRadius(5f, 5f))
            }
            PlayerAvatarStyle.MALWARE_HUNTER -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                drawCircle(accent, 5f, Offset(w * .39f, h * .46f))
                drawCircle(accent, 5f, Offset(w * .61f, h * .46f))
                drawRoundRect(secondary, Offset(w * .36f, h * .56f), Size(w * .28f, h * .13f), CornerRadius(7f, 7f))
                repeat(3) { i -> drawLine(dark, Offset(w * (.42f + i * .08f), h * .58f), Offset(w * (.42f + i * .08f), h * .66f), 1.6f) }
            }
            PlayerAvatarStyle.BLUE_ELITE -> {
                drawRoundRect(Color(0xFF08111F), Offset(w * .25f, h * .28f), Size(w * .50f, h * .42f), CornerRadius(11f, 11f))
                drawArc(accent, 195f, 150f, false, Offset(w * .28f, h * .32f), Size(w * .44f, h * .25f), style = Stroke(3f, cap = StrokeCap.Round))
                drawCircle(secondary, 4f, Offset(cx, h * .62f))
                drawCircle(accent, 7f, Offset(cx, h * .62f), style = Stroke(1.8f))
            }
            PlayerAvatarStyle.RED_RUNNER -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                drawLine(Color(0xFFFF4F6D), Offset(w * .29f, h * .44f), Offset(w * .71f, h * .37f), 5f, StrokeCap.Round)
                drawLine(accent, Offset(w * .34f, h * .55f), Offset(w * .66f, h * .55f), 2f, StrokeCap.Round)
                drawLine(secondary, Offset(w * .22f, h * .24f), Offset(w * .34f, h * .32f), 2.4f)
            }
            PlayerAvatarStyle.CRYPTO_SENTINEL -> {
                val gem = Path().apply {
                    moveTo(cx, h * .26f); lineTo(w * .69f, h * .42f); lineTo(cx, h * .70f); lineTo(w * .31f, h * .42f); close()
                }
                drawPath(gem, Brush.linearGradient(listOf(accent, secondary.copy(alpha = .55f))))
                drawPath(gem, Color.White.copy(alpha = .55f), style = Stroke(1.4f))
                drawLine(dark, Offset(cx, h * .30f), Offset(cx, h * .64f), 2f)
                drawLine(dark, Offset(w * .35f, h * .43f), Offset(w * .65f, h * .43f), 2f)
            }
            PlayerAvatarStyle.FIREWALL_MASTER -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                repeat(3) { i ->
                    drawRoundRect(
                        color = if (i % 2 == 0) Color(0xFFFF7A4F) else accent,
                        topLeft = Offset(w * (.29f + i * .14f), h * .40f),
                        size = Size(w * .12f, h * .22f), cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
            PlayerAvatarStyle.NETWORK_NINJA -> {
                drawOval(Color(0xFF090C16), Offset(w * .25f, h * .29f), Size(w * .50f, h * .42f))
                drawRoundRect(accent, Offset(w * .30f, h * .43f), Size(w * .40f, h * .08f), CornerRadius(4f, 4f))
                drawLine(Color.White, Offset(w * .37f, h * .47f), Offset(w * .45f, h * .47f), 2f)
                drawLine(Color.White, Offset(w * .55f, h * .47f), Offset(w * .63f, h * .47f), 2f)
                drawLine(secondary, Offset(w * .78f, h * .22f), Offset(w * .62f, h * .37f), 3f, StrokeCap.Round)
            }
            PlayerAvatarStyle.CYBER_SAMURAI -> {
                drawRoundRect(dark, Offset(w * .27f, h * .29f), Size(w * .46f, h * .40f), CornerRadius(8f, 8f))
                drawArc(accent, 200f, 140f, false, Offset(w * .20f, h * .20f), Size(w * .60f, h * .32f), style = Stroke(4f))
                drawLine(secondary, Offset(w * .34f, h * .49f), Offset(w * .66f, h * .49f), 3f)
                drawLine(secondary, Offset(cx, h * .49f), Offset(cx, h * .67f), 2f)
            }
            PlayerAvatarStyle.CIPHER_QUEEN -> {
                drawOval(dark, Offset(w * .26f, h * .28f), Size(w * .48f, h * .43f))
                val tiara = Path().apply {
                    moveTo(w * .29f, h * .35f); lineTo(w * .40f, h * .19f); lineTo(cx, h * .33f); lineTo(w * .60f, h * .19f); lineTo(w * .71f, h * .35f); close()
                }
                drawPath(tiara, Brush.linearGradient(listOf(accent, Color(0xFFFFD166), secondary)))
                drawCircle(accent, 3.4f, Offset(w * .40f, h * .48f)); drawCircle(accent, 3.4f, Offset(w * .60f, h * .48f))
            }
            PlayerAvatarStyle.DIGITAL_SPECTER -> {
                val face = Path().apply {
                    moveTo(cx, h * .22f); cubicTo(w * .25f, h * .32f, w * .28f, h * .65f, cx, h * .76f); cubicTo(w * .72f, h * .65f, w * .75f, h * .32f, cx, h * .22f); close()
                }
                drawPath(face, Brush.verticalGradient(listOf(accent.copy(alpha = .70f), Color.Transparent)))
                repeat(4) { i -> drawLine(secondary.copy(alpha = .65f), Offset(w * .30f, h * (.38f + i * .08f)), Offset(w * .70f, h * (.34f + i * .08f)), 1.6f) }
            }
            PlayerAvatarStyle.EXPLOIT_HUNTER -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                drawLine(accent, Offset(w * .30f, h * .40f), Offset(w * .45f, h * .52f), 3f)
                drawLine(accent, Offset(w * .45f, h * .40f), Offset(w * .30f, h * .52f), 3f)
                drawCircle(secondary, 4f, Offset(w * .62f, h * .46f))
                drawArc(secondary, 205f, 130f, false, Offset(w * .37f, h * .52f), Size(w * .26f, h * .12f), style = Stroke(2f))
            }
            PlayerAvatarStyle.AI_TACTICIAN -> {
                drawRoundRect(Color(0xFF0B1220), Offset(w * .25f, h * .27f), Size(w * .50f, h * .44f), CornerRadius(12f, 12f))
                drawCircle(accent, 9f, Offset(cx, h * .47f), style = Stroke(2.2f))
                drawCircle(secondary, 3f, Offset(cx, h * .47f))
                repeat(6) { i ->
                    val y = h * (.35f + i * .055f)
                    drawLine(accent.copy(alpha = .35f), Offset(w * .31f, y), Offset(w * .69f, y), 1f)
                }
            }
            PlayerAvatarStyle.BLACK_ICE -> {
                drawOval(Color(0xFF03060B), Offset(w * .24f, h * .27f), Size(w * .52f, h * .45f))
                val shard = Path().apply { moveTo(cx, h * .27f); lineTo(w * .66f, h * .51f); lineTo(cx, h * .70f); lineTo(w * .34f, h * .51f); close() }
                drawPath(shard, Brush.linearGradient(listOf(Color(0xFFB8E8FF), accent.copy(alpha = .55f))))
                drawPath(shard, Color.White.copy(alpha = .45f), style = Stroke(1.4f))
            }
            PlayerAvatarStyle.QUANTUM_ARCHIVIST -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                repeat(3) { i ->
                    drawCircle(
                        color = if (i % 2 == 0) accent.copy(alpha = .75f) else secondary.copy(alpha = .75f),
                        radius = size.minDimension * (.11f + i * .045f),
                        center = Offset(cx, h * .49f),
                        style = Stroke(1.5f)
                    )
                }
                drawCircle(Color.White, 2.8f, Offset(cx, h * .49f))
            }
            PlayerAvatarStyle.BINARY_DRAGON -> {
                drawOval(dark, Offset(w * .25f, h * .28f), Size(w * .50f, h * .43f))
                val leftHorn = Path().apply { moveTo(w * .34f, h * .33f); lineTo(w * .20f, h * .16f); lineTo(w * .43f, h * .30f); close() }
                val rightHorn = Path().apply { moveTo(w * .66f, h * .33f); lineTo(w * .80f, h * .16f); lineTo(w * .57f, h * .30f); close() }
                drawPath(leftHorn, accent); drawPath(rightHorn, secondary)
                drawLine(Color(0xFFFF6B6B), Offset(w * .34f, h * .46f), Offset(w * .44f, h * .43f), 3f)
                drawLine(Color(0xFFFF6B6B), Offset(w * .56f, h * .43f), Offset(w * .66f, h * .46f), 3f)
                drawArc(accent, 30f, 120f, false, Offset(w * .37f, h * .56f), Size(w * .26f, h * .11f), style = Stroke(2.2f))
            }
            PlayerAvatarStyle.OVERMIND -> {
                drawOval(Color(0xFF03050B), Offset(w * .23f, h * .25f), Size(w * .54f, h * .47f))
                drawCircle(accent.copy(alpha = .85f), 12f, Offset(cx, h * .47f), style = Stroke(2.6f))
                drawCircle(secondary, 5f, Offset(cx, h * .47f))
                repeat(8) { i ->
                    val angle = Math.toRadians(i * 45.0)
                    val dx = kotlin.math.cos(angle).toFloat() * w * .20f
                    val dy = kotlin.math.sin(angle).toFloat() * h * .20f
                    drawLine(accent.copy(alpha = .55f), Offset(cx, h * .47f), Offset(cx + dx, h * .47f + dy), 1.4f)
                }
            }
            PlayerAvatarStyle.BEGINNER,
            PlayerAvatarStyle.SOC -> Unit
        }
    }
}
