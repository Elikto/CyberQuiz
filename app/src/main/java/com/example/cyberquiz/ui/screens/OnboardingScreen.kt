package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.ui.preferences.TextScaleOption
import com.example.cyberquiz.ui.preferences.UxPreferences
import com.example.cyberquiz.ui.theme.CyberBackground

private val OnboardPurple = Color(0xFFD652FF)
private val OnboardBlue = Color(0xFF19BFFF)
private val OnboardCyan = Color(0xFF19F2E5)
private val OnboardGreen = Color(0xFF38E69A)
private val OnboardText = Color(0xFFF5F7FF)
private val OnboardMuted = Color(0xFFA7B4D4)

@Composable
fun OnboardingScreen(
    preferences: UxPreferences,
    onPreferencesChange: (UxPreferences) -> Unit,
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    BackHandler(enabled = step > 0) { step -= 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF081329), CyberBackground)
                )
            )            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OnboardingProgress(step)

        when (step) {
            0 -> WelcomeStep()
            1 -> AccessibilityStep(preferences, onPreferencesChange)
            else -> ReadyStep()
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (step > 0) {
                OnboardingButton(
                    text = "RETOUR",
                    accent = OnboardMuted,
                    modifier = Modifier.weight(1f),
                    onClick = { step -= 1 }
                )
            }
            OnboardingButton(
                text = if (step == 2) "COMMENCER" else "CONTINUER",
                accent = if (step == 2) OnboardGreen else OnboardPurple,
                modifier = Modifier.weight(if (step > 0) 1f else 2f),
                onClick = { if (step == 2) onComplete() else step += 1 }
            )
        }
    }
}
@Composable
private fun OnboardingProgress(step: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(5.dp)
                    .weight(1f)
                    .background(
                        if (index <= step) OnboardPurple else Color(0xFF263653),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bienvenue dans CyberQuiz", color = OnboardText, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(
            "APPRENDS · TESTE-TOI · PROGRESSE",
            color = OnboardCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.7.sp
        )
        OnboardingCard("01", "Quiz adaptatifs", "Travaille les notions utiles et retrouve plus souvent ce qui te pose problème.", OnboardPurple)
        OnboardingCard("02", "Mode examen", "Choisis plusieurs catégories, un nombre de questions et une durée, sans correction avant la fin.", OnboardBlue)
        OnboardingCard("03", "Parcours & collection", "Débloque des étapes, des succès, des titres et des éléments cosmétiques.", OnboardGreen)
    }
}
@Composable
private fun AccessibilityStep(
    preferences: UxPreferences,
    onPreferencesChange: (UxPreferences) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Adapte l'application à toi", color = OnboardText, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Ces réglages restent modifiables à tout moment dans Paramètres.", color = OnboardMuted, fontSize = 12.sp)

        Text("TAILLE DU TEXTE", color = OnboardBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextScaleOption.entries.forEach { option ->
                OnboardingChoice(
                    label = "${option.percent}%",
                    selected = preferences.textScale == option,
                    modifier = Modifier.weight(1f),
                    onClick = { onPreferencesChange(preferences.copy(textScale = option)) }
                )
            }
        }

        OnboardingToggle("Contraste renforcé", "Augmente la lisibilité des couleurs principales.", preferences.highContrast) {
            onPreferencesChange(preferences.copy(highContrast = !preferences.highContrast))
        }
        OnboardingToggle("Vibrations", "Retour tactile discret lors de la validation d'une réponse.", preferences.hapticsEnabled) {
            onPreferencesChange(preferences.copy(hapticsEnabled = !preferences.hapticsEnabled))
        }
        OnboardingToggle("Réduire les animations", "Limite les mouvements décoratifs et les célébrations.", preferences.reducedMotion) {
            onPreferencesChange(preferences.copy(reducedMotion = !preferences.reducedMotion))
        }
    }
}
@Composable
private fun ReadyStep() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tu es prêt", color = OnboardText, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Quelques repères avant de commencer :", color = OnboardMuted, fontSize = 13.sp)
        OnboardingCard("A", "Reprendre un quiz", "Tu peux quitter un quiz configuré et le reprendre plus tard sans perdre ta progression.", OnboardCyan)
        OnboardingCard("B", "Réviser intelligemment", "Les erreurs alimentent les notions à revoir et les révisions quotidiennes.", OnboardPurple)
        OnboardingCard("C", "Explorer", "La banque de questions, les parcours, CyberSquad et la collection restent accessibles depuis l'accueil et le profil.", OnboardBlue)
    }
}

@Composable
private fun OnboardingCard(symbol: String, title: String, text: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .55f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(accent.copy(alpha = .13f), CircleShape)
                .padding(horizontal = 13.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = accent, fontWeight = FontWeight.Black)
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, color = OnboardText, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(text, color = OnboardMuted, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
@Composable
private fun OnboardingChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(if (selected) OnboardPurple.copy(alpha = .18f) else Color(0xFF081329), RoundedCornerShape(13.dp))
            .border(1.dp, if (selected) OnboardPurple else Color(0xFF314C72), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) OnboardText else OnboardMuted, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun OnboardingToggle(title: String, subtitle: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(16.dp))
            .border(1.dp, if (enabled) OnboardCyan.copy(alpha = .65f) else Color(0xFF314C72), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = OnboardText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = OnboardMuted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Text(if (enabled) "ON" else "OFF", color = if (enabled) OnboardCyan else OnboardMuted, fontWeight = FontWeight.Black)
    }
}
@Composable
private fun OnboardingButton(
    text: String,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(accent.copy(alpha = .15f), RoundedCornerShape(16.dp))
            .border(1.2.dp, accent.copy(alpha = .8f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (accent == OnboardMuted) OnboardText else accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}
