package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CosmeticUnlockRulesTest {
    @Test
    fun `secret banner requires its mapped achievement`() {
        val style = PlayerBannerStyle.DARK_WEB_MESH

        assertFalse(isBannerUnlocked(style, playerLevel = 30, unlockedAchievementIds = emptySet()))
        assertTrue(isBannerUnlocked(style, playerLevel = 1, unlockedAchievementIds = setOf("first_answer")))
    }

    @Test
    fun `locked secret banner exposes configured condition`() {
        val style = PlayerBannerStyle.VAULT_CORE

        assertEquals("questions_10", secretBannerRule(style)?.achievementId)
        assertTrue(lockedBannerCondition(style).contains("10"))
    }

    @Test
    fun `regular banner still follows level requirement`() {
        val style = PlayerBannerStyle.GLITCH_RED

        assertFalse(isBannerUnlocked(style, playerLevel = 9, unlockedAchievementIds = setOf("quiz_100")))
        assertTrue(isBannerUnlocked(style, playerLevel = 10, unlockedAchievementIds = emptySet()))
    }
}
