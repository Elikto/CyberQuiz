package com.example.cyberquiz.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.viewmodel.QuizFinishSummary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val FinishPurple = Color(0xFFD652FF)
private val FinishBlue = Color(0xFF19BFFF)
private val FinishCyan = Color(0xFF19F2E5)
private val FinishGreen = Color(0xFF38E69A)
private val FinishOrange = Color(0xFFFFB84A)
private val FinishText = Color(0xFFF5F7FF)
private val FinishMuted = Color(0xFF9FAED3)

private data class FinishTier(
    val level: Int,
    val icon: String,
    val title: String,
    val subtitle: String,
    val reward: String?,
    val accent: Color
)

@Composable
fun QuizFinishCelebrationV8(
    summary: QuizFinishSummary,
    onReplay: () -> Unit,
    onOtherQuiz: () -> Unit,
    onHome: () -> Unit
) {
    val answered = summary.answered.coerceAtLeast(0)
    val correct = summary.correct.coerceIn(0, answered)
    val percent = if (answered == 0) 0 else (correct * 100 / answered)
    val perfect = answered > 0 && correct == answered
    val tier = finishTier(percent, perfect)

    val entrance = remember(percent, answered) { Animatable(0.82f) }
    val burst = remember(percent, answered) { Animatable(0f) }

    LaunchedEffect(percent, answered) {
        entrance.snapTo(0.82f)
        entrance.animateTo(1f, tween(430, easing = FastOutSlowInEasing))
        if (tier.level > 0) {
            burst.snapTo(0f)
            burst.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 650 + tier.level * 110,
                    easing = FastOutSlowInEasing
                )
            )
            if (tier.level >= 4) {
                entrance.animateTo(1.045f, tween(150))
                entrance.animateTo(1f, tween(190))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (tier.level > 0) {
            CelebrationParticlesV8(
                level = tier.level,
                progress = burst.value,
                accent = tier.accent,
                modifier = Modifier.fillMaxWidth().height(190.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = entrance.value
                    scaleY = entrance.value
                }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            tier.accent.copy(alpha = .16f),
                            Color(0xFF0B1430),
                            Color(0xFF061020)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .border(1.5.dp, tier.accent.copy(alpha = .85f), RoundedCornerShape(28.dp))
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(if (tier.level >= 5) 92.dp else 78.dp)
                    .background(tier.accent.copy(alpha = .14f), CircleShape)
                    .border(1.5.dp, tier.accent.copy(alpha = .75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tier.icon,
                    color = tier.accent,
                    fontSize = if (tier.level >= 5) 42.sp else 34.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    tier.title,
                    color = FinishText,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    tier.subtitle,
                    color = if (tier.level == 0) FinishMuted else tier.accent,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = if (tier.level >= 3) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            if (tier.reward != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(tier.accent.copy(alpha = .09f), RoundedCornerShape(16.dp))
                        .border(1.dp, tier.accent.copy(alpha = .38f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tier.reward,
                        color = tier.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                FinishResultStatV8(
                    label = "NOTE",
                    value = "$correct/$answered",
                    accent = tier.accent,
                    modifier = Modifier.weight(1f)
                )
                FinishResultStatV8(
                    label = "RÉUSSITE",
                    value = "$percent%",
                    accent = tier.accent,
                    modifier = Modifier.weight(1f)
                )
                FinishResultStatV8(
                    label = "XP GAGNÉS",
                    value = "+${summary.xpGained}",
                    accent = FinishPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            if (percent == 99 && !perfect) {
                Text(
                    "99 %… Le dernier 1 % s'est caché derrière un pare-feu. 😄",
                    color = Color(0xFFFFE5A8),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FinishOrange.copy(alpha = .08f), RoundedCornerShape(15.dp))
                        .padding(12.dp)
                )
            }

            if (perfect) {
                Text(
                    "Aucune erreur. Le SOC peut dormir tranquille ce soir.",
                    color = Color(0xFFD7FFF0),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            FinishActionButtonV8(
                text = "REJOUER",
                accent = FinishPurple,
                filled = true,
                onClick = onReplay
            )
            FinishActionButtonV8(
                text = "FAIRE UN AUTRE QUIZ",
                accent = FinishCyan,
                filled = false,
                onClick = onOtherQuiz
            )
            FinishActionButtonV8(
                text = "ACCUEIL",
                accent = Color(0xFF7184A9),
                filled = false,
                onClick = onHome
            )
        }
    }
}

@Composable
private fun FinishActionButtonV8(
    text: String,
    accent: Color,
    filled: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (filled) 56.dp else 47.dp)
            .background(
                if (filled) {
                    Brush.horizontalGradient(
                        listOf(FinishPurple.copy(alpha = .95f), Color(0xFF234A91))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = .06f), Color(0xFF071225))
                    )
                },
                RoundedCornerShape(16.dp)
            )
            .border(
                if (filled) 1.2.dp else 1.dp,
                if (filled) FinishPurple else accent.copy(alpha = .70f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (filled) FinishText else accent,
            fontSize = if (filled) 14.sp else 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .8.sp
        )
    }
}

@Composable
private fun FinishResultStatV8(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(Color(0xFF07152C), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(
            label,
            color = FinishMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = .6.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun CelebrationParticlesV8(
    level: Int,
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val count = when (level) {
        1 -> 10
        2 -> 18
        3 -> 28
        4 -> 36
        5 -> 46
        else -> 60
    }
    val travel = 80f + level * 18f
    val alpha = (1f - progress).coerceIn(0f, 1f)

    Canvas(modifier) {
        val center = Offset(size.width / 2f, 92.dp.toPx())
        repeat(count) { index ->
            val angle = (2.0 * PI * index / count) + ((index % 3) * 0.08)
            val distance = (30f + travel * progress) * density
            val x = center.x + cos(angle).toFloat() * distance
            val y = center.y + sin(angle).toFloat() * distance * .72f
            val color = when (index % 4) {
                0 -> accent
                1 -> FinishCyan
                2 -> FinishPurple
                else -> FinishGreen
            }
            drawCircle(
                color = color.copy(alpha = alpha * .88f),
                radius = (2.2f + (index % 3) * .9f) * density,
                center = Offset(x, y)
            )
        }
    }
}

private fun finishTier(percent: Int, perfect: Boolean): FinishTier = when {
    perfect -> FinishTier(
        level = 6,
        icon = "♛",
        title = "100 % · SANS FAUTE",
        subtitle = "Performance parfaite. Tu n'as laissé passer aucune question.",
        reward = "COURONNE CYBER · PERFECT RUN",
        accent = FinishGreen
    )
    percent >= 99 -> FinishTier(
        level = 5,
        icon = "◆",
        title = "99 % · PRESQUE PARFAIT",
        subtitle = "Un résultat exceptionnel. Il ne manquait vraiment rien… ou presque.",
        reward = "DIAMANT CYBER · 99 %",
        accent = FinishCyan
    )
    percent >= 95 -> FinishTier(
        level = 4,
        icon = "🏆",
        title = "95 % + · ÉLITE",
        subtitle = "Tu viens de signer une performance de très haut niveau.",
        reward = "TROPHÉE CYBER · ÉLITE",
        accent = Color(0xFFFFD166)
    )
    percent >= 85 -> FinishTier(
        level = 3,
        icon = "★",
        title = "EXCELLENTE MAÎTRISE",
        subtitle = "85 % ou plus : tes bases deviennent vraiment solides.",
        reward = "DISTINCTION CYBER · EXPERTISE",
        accent = FinishPurple
    )
    percent >= 65 -> FinishTier(
        level = 2,
        icon = "⚡",
        title = "TRÈS BON QUIZ",
        subtitle = "Tu dépasses 65 %. La progression est nette.",
        reward = null,
        accent = FinishBlue
    )
    percent >= 51 -> FinishTier(
        level = 1,
        icon = "✓",
        title = "QUIZ RÉUSSI",
        subtitle = "Tu passes la barre des 51 %. Continue à consolider les notions à revoir.",
        reward = null,
        accent = FinishGreen
    )
    else -> FinishTier(
        level = 0,
        icon = "↻",
        title = "À RENFORCER",
        subtitle = "Ce quiz t'a permis d'identifier les notions à retravailler. Elles restent disponibles dans À revoir.",
        reward = null,
        accent = FinishOrange
    )
}
