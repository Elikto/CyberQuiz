package com.example.cyberquiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.cyberquiz.ui.preferences.LocalUxPreferences
import com.example.cyberquiz.ui.preferences.UxPreferences

private val CyberColors = darkColorScheme(
    primary = CyberPurple, secondary = CyberBlue, background = CyberBackground,
    surface = CyberSurface, surfaceVariant = CyberSurface2, onPrimary = CyberText,
    onBackground = CyberText, onSurface = CyberText, onSurfaceVariant = CyberMuted, error = CyberRed
)

private val HighContrastCyberColors = darkColorScheme(
    primary = Color(0xFFC9A0FF), secondary = Color(0xFF7CC4FF), background = Color.Black,
    surface = Color(0xFF090912), surfaceVariant = Color(0xFF1B1B2B), onPrimary = Color.White,
    onBackground = Color.White, onSurface = Color.White, onSurfaceVariant = Color(0xFFF2F5FF), error = Color(0xFFFF7891)
)

@Composable
fun CyberQuizTheme(preferences: UxPreferences = UxPreferences(), content: @Composable () -> Unit) {
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity.density, baseDensity.fontScale, preferences.textScale) {
        Density(baseDensity.density, baseDensity.fontScale * preferences.textScale.multiplier)
    }
    CompositionLocalProvider(LocalDensity provides scaledDensity, LocalUxPreferences provides preferences) {
        MaterialTheme(
            colorScheme = if (preferences.highContrast) HighContrastCyberColors else CyberColors,
            typography = Typography,
            content = content
        )
    }
}
