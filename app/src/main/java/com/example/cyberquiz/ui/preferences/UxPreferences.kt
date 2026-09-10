package com.example.cyberquiz.ui.preferences

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf

enum class TextScaleOption(
    val storageKey: String,
    val multiplier: Float,
    val label: String,
    val percent: Int
) {
    STANDARD("standard", 1.0f, "Standard", 100),
    LARGE("large", 1.15f, "Grand", 115),
    EXTRA_LARGE("extra_large", 1.30f, "Très grand", 130);

    fun next(): TextScaleOption = when (this) {
        STANDARD -> LARGE
        LARGE -> EXTRA_LARGE
        EXTRA_LARGE -> STANDARD
    }

    companion object {
        fun fromStorageKey(value: String?): TextScaleOption =
            entries.firstOrNull { it.storageKey == value } ?: STANDARD
    }
}

data class UxPreferences(
    val textScale: TextScaleOption = TextScaleOption.STANDARD,
    val highContrast: Boolean = false,
    val hapticsEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val onboardingCompleted: Boolean = false
)

internal fun resolveOnboardingCompleted(stored: Boolean?, existingInstall: Boolean): Boolean =
    stored ?: existingInstall

val LocalUxPreferences = staticCompositionLocalOf { UxPreferences() }

class UxPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(existingInstall: Boolean = false): UxPreferences = UxPreferences(
        textScale = TextScaleOption.fromStorageKey(prefs.getString(KEY_TEXT_SCALE, null)),
        highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false),
        hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, false),
        reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false),
        onboardingCompleted = resolveOnboardingCompleted(
            stored = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false).takeIf { prefs.contains(KEY_ONBOARDING_COMPLETED) },
            existingInstall = existingInstall
        )
    )

    fun save(value: UxPreferences) {
        prefs.edit()
            .putString(KEY_TEXT_SCALE, value.textScale.storageKey)
            .putBoolean(KEY_HIGH_CONTRAST, value.highContrast)
            .putBoolean(KEY_HAPTICS, value.hapticsEnabled)
            .putBoolean(KEY_REDUCED_MOTION, value.reducedMotion)
            .putBoolean(KEY_ONBOARDING_COMPLETED, value.onboardingCompleted)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "cyberquiz_ux_preferences"
        const val KEY_TEXT_SCALE = "text_scale"
        const val KEY_HIGH_CONTRAST = "high_contrast"
        const val KEY_HAPTICS = "haptics"
        const val KEY_REDUCED_MOTION = "reduced_motion"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed_v1"
    }
}
