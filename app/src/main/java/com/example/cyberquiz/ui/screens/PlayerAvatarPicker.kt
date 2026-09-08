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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class PlayerAvatarStyle(
    val storageKey: String,
    val displayName: String,
    val subtitle: String
) {
    BEGINNER("beginner", "Hacker débutant", "Premier accès"),
    SOC("soc", "Analyste SOC", "Veille & défense"),
    PENTEST("pentest", "Pentester", "Mode offensif"),
    GHOST("ghost", "Ghost", "Discret & anonyme"),
    ARCHITECT("architect", "Architecte cyber", "Expert système")
}

internal fun playerAvatarFromStorage(value: String?): PlayerAvatarStyle =
    PlayerAvatarStyle.entries.firstOrNull { it.storageKey == value } ?: PlayerAvatarStyle.BEGINNER

private fun avatarAccent(style: PlayerAvatarStyle): Color = when (style) {
    PlayerAvatarStyle.BEGINNER -> Color(0xFF27E9FF)
    PlayerAvatarStyle.SOC -> Color(0xFF4EA1FF)
    PlayerAvatarStyle.PENTEST -> Color(0xFFE35BFF)
    PlayerAvatarStyle.GHOST -> Color(0xFFD8E2FF)
    PlayerAvatarStyle.ARCHITECT -> Color(0xFFFFC857)
}

private fun avatarSecondary(style: PlayerAvatarStyle): Color = when (style) {
    PlayerAvatarStyle.BEGINNER -> Color(0xFF9D46FF)
    PlayerAvatarStyle.SOC -> Color(0xFF19F2E5)
    PlayerAvatarStyle.PENTEST -> Color(0xFFFF5D8F)
    PlayerAvatarStyle.GHOST -> Color(0xFF7E8FB5)
    PlayerAvatarStyle.ARCHITECT -> Color(0xFF21D8FF)
}

@Composable
internal fun PlayerAvatarButton(
    style: PlayerAvatarStyle,
    onClick: () -> Unit,
    size: Dp = 62.dp
) {
    val accent = avatarAccent(style)
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = .20f), Color(0xFF0A1730), Color(0xFF050A15))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.4.dp, accent.copy(alpha = .78f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AvatarArtwork(style = style, modifier = Modifier.size(size - 10.dp))
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(15.dp)
                .background(Color(0xFF071221), CircleShape)
                .border(1.dp, accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AvatarArtwork(style: PlayerAvatarStyle, modifier: Modifier = Modifier) {
    val accent = avatarAccent(style)
    val secondary = avatarSecondary(style)

    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        drawCircle(
            brush = Brush.radialGradient(listOf(accent.copy(alpha = .18f), Color.Transparent)),
            radius = size.minDimension * .50f,
            center = Offset(cx, h * .50f)
        )

        val hood = Path().apply {
            moveTo(cx, h * .05f)
            cubicTo(w * .20f, h * .14f, w * .10f, h * .48f, w * .16f, h * .89f)
            lineTo(w * .84f, h * .89f)
            cubicTo(w * .90f, h * .48f, w * .80f, h * .14f, cx, h * .05f)
            close()
        }
        drawPath(
            hood,
            brush = Brush.verticalGradient(
                listOf(
                    secondary.copy(alpha = .48f),
                    Color(0xFF1C2743),
                    Color(0xFF090F1E)
                )
            )
        )
        drawPath(hood, accent.copy(alpha = .72f), style = Stroke(2.1f))

        drawOval(
            color = Color(0xFF050B14),
            topLeft = Offset(w * .27f, h * .30f),
            size = Size(w * .46f, h * .42f)
        )

        when (style) {
            PlayerAvatarStyle.BEGINNER -> {
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(secondary, accent)),
                    topLeft = Offset(w * .30f, h * .43f),
                    size = Size(w * .40f, h * .105f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawLine(
                    Color.White.copy(alpha = .75f),
                    Offset(w * .38f, h * .47f),
                    Offset(w * .62f, h * .47f),
                    1.4f,
                    StrokeCap.Round
                )
            }

            PlayerAvatarStyle.SOC -> {
                drawRoundRect(
                    color = accent.copy(alpha = .88f),
                    topLeft = Offset(w * .31f, h * .43f),
                    size = Size(w * .38f, h * .095f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawArc(
                    color = secondary,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(w * .18f, h * .25f),
                    size = Size(w * .64f, h * .48f),
                    style = Stroke(2.5f, cap = StrokeCap.Round)
                )
                drawLine(
                    secondary,
                    Offset(w * .73f, h * .57f),
                    Offset(w * .80f, h * .64f),
                    2.2f,
                    StrokeCap.Round
                )
                drawCircle(secondary, 2.6f, Offset(w * .80f, h * .64f))
            }

            PlayerAvatarStyle.PENTEST -> {
                drawRoundRect(
                    color = Color(0xFF15101F),
                    topLeft = Offset(w * .29f, h * .39f),
                    size = Size(w * .42f, h * .22f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawLine(secondary, Offset(w * .35f, h * .47f), Offset(w * .46f, h * .45f), 2.8f, StrokeCap.Round)
                drawLine(accent, Offset(w * .54f, h * .45f), Offset(w * .65f, h * .47f), 2.8f, StrokeCap.Round)
                drawLine(accent.copy(alpha = .65f), Offset(w * .42f, h * .60f), Offset(w * .58f, h * .60f), 1.7f, StrokeCap.Round)
            }

            PlayerAvatarStyle.GHOST -> {
                drawRoundRect(
                    color = Color(0xFF111827),
                    topLeft = Offset(w * .30f, h * .40f),
                    size = Size(w * .40f, h * .22f),
                    cornerRadius = CornerRadius(9f, 9f)
                )
                drawCircle(accent, 2.5f, Offset(w * .40f, h * .48f))
                drawCircle(accent, 2.5f, Offset(w * .60f, h * .48f))
                listOf(.37f, .46f, .55f).forEachIndexed { i, start ->
                    drawLine(
                        secondary.copy(alpha = .70f),
                        Offset(w * start, h * (.57f + i * .035f)),
                        Offset(w * (start + .11f), h * (.57f + i * .035f)),
                        1.4f,
                        StrokeCap.Round
                    )
                }
            }

            PlayerAvatarStyle.ARCHITECT -> {
                val badge = Path().apply {
                    moveTo(cx, h * .31f)
                    lineTo(w * .58f, h * .38f)
                    lineTo(cx, h * .44f)
                    lineTo(w * .42f, h * .38f)
                    close()
                }
                drawPath(badge, brush = Brush.linearGradient(listOf(accent, secondary)))
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(accent, secondary)),
                    topLeft = Offset(w * .31f, h * .48f),
                    size = Size(w * .38f, h * .085f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawCircle(accent.copy(alpha = .45f), 4f, Offset(cx, h * .67f), style = Stroke(1.5f))
            }
        }

        drawArc(
            color = secondary.copy(alpha = .72f),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * .33f, h * .57f),
            size = Size(w * .34f, h * .17f),
            style = Stroke(1.5f, cap = StrokeCap.Round)
        )
    }
}

@Composable
internal fun PlayerAvatarPickerDialog(
    selected: PlayerAvatarStyle,
    onSelect: (PlayerAvatarStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF081225),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Choisis ton avatar",
                    color = Color(0xFFF5F7FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Les skins et déblocages arriveront ensuite.",
                    color = Color(0xFF9FAED3),
                    fontSize = 10.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                PlayerAvatarStyle.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        rowItems.forEach { style ->
                            AvatarChoiceCard(
                                style = style,
                                selected = style == selected,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelect(style) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("FERMER", color = Color(0xFF8FA9D8), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun AvatarChoiceCard(
    style: PlayerAvatarStyle,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val accent = avatarAccent(style)
    Column(
        modifier = modifier
            .aspectRatio(.92f)
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = if (selected) .16f else .07f), Color(0xFF0A152A))
                ),
                RoundedCornerShape(18.dp)
            )
            .border(
                if (selected) 1.7.dp else 1.dp,
                accent.copy(alpha = if (selected) .95f else .36f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PlayerAvatarButton(style = style, onClick = onClick, size = 54.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                style.displayName,
                color = Color(0xFFF5F7FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                if (selected) "SÉLECTIONNÉ" else style.subtitle,
                color = if (selected) accent else Color(0xFF9FAED3),
                fontSize = 7.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
