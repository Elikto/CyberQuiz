package com.example.cyberquiz.ui.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UxPreferencesTest {
    @Test
    fun `text scale cycles through every supported size`() {
        assertEquals(TextScaleOption.LARGE, TextScaleOption.STANDARD.next())
        assertEquals(TextScaleOption.EXTRA_LARGE, TextScaleOption.LARGE.next())
        assertEquals(TextScaleOption.STANDARD, TextScaleOption.EXTRA_LARGE.next())
    }

    @Test
    fun `unknown stored text scale falls back to standard`() {
        assertEquals(TextScaleOption.STANDARD, TextScaleOption.fromStorageKey("missing"))
    }

    @Test
    fun `existing install skips new onboarding when no value was stored`() {
        assertTrue(resolveOnboardingCompleted(stored = null, existingInstall = true))
        assertFalse(resolveOnboardingCompleted(stored = null, existingInstall = false))
    }

    @Test
    fun `stored onboarding choice always wins over install fallback`() {
        assertFalse(resolveOnboardingCompleted(stored = false, existingInstall = true))
        assertTrue(resolveOnboardingCompleted(stored = true, existingInstall = false))
    }
}
