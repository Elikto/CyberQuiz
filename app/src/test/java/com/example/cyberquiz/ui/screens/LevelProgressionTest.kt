package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelProgressionTest {
    @Test
    fun `roadmap contains thirty levels from highest to lowest`() {
        val roadmap = levelRoadmap()
        assertEquals(30, roadmap.size)
        assertEquals(30, roadmap.first().level)
        assertEquals(1, roadmap.last().level)
    }

    @Test
    fun `level roles cover key progression milestones`() {
        assertEquals("Recrue cyber", playerRoleForLevel(1))
        assertEquals("Analyste SOC", playerRoleForLevel(5))
        assertEquals("Hacker éthique", playerRoleForLevel(10))
        assertEquals("Architecte cyber", playerRoleForLevel(20))
        assertEquals("Cyber Légende", playerRoleForLevel(30))
    }

    @Test
    fun `level five lists all real cosmetic unlocks`() {
        val rewards = levelRewards(5)
        assertTrue(rewards.contains("Avatar · Chasseur de malwares"))
        assertTrue(rewards.contains("Bannière · Radar SOC"))
        assertTrue(rewards.contains("Contour · Hexagone SOC"))
    }

    @Test
    fun `level one explains the starter cosmetic pack`() {
        val rewards = levelRewards(1)
        assertTrue(rewards.contains("Pack de départ · 5 avatars"))
        assertTrue(rewards.contains("4 bannières"))
        assertTrue(rewards.contains("3 contours"))
    }

    @Test
    fun `role lookup clamps to maximum level`() {
        assertEquals("Cyber Légende", playerRoleForLevel(999))
        assertEquals("Recrue cyber", playerRoleForLevel(0))
    }
}
