package com.example.cyberquiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColors = darkColorScheme(
    primary = CyberPurple,
    secondary = CyberBlue,
    background = CyberBackground,
    surface = CyberSurface,
    surfaceVariant = CyberSurface2,
    onPrimary = CyberText,
    onBackground = CyberText,
    onSurface = CyberText,
    onSurfaceVariant = CyberMuted,
    error = CyberRed
)

@Composable
fun CyberQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CyberColors, typography = Typography, content = content)
}
