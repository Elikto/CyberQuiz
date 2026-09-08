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
    fun `every roadmap level has a visible avatar presentation`() {
        val roadmap = levelRoadmap()
        assertEquals(30, roadmap.map { it.avatar }.size)
        assertTrue(roadmap.all { !it.avatar.mystery })
        assertTrue(roadmap.all { !it.banner.mystery })
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
    fun `level five lists all real cosmetic unlocks with preview types`() {
        val rewards = levelRewards(5)
        assertTrue(rewards.any { it.kind == LevelRewardKind.AVATAR && it.name == "Chasseur de malwares" && it.avatar != null })
        assertTrue(rewards.any { it.kind == LevelRewardKind.BANNER && it.name == "Radar SOC" && it.banner != null })
        assertTrue(rewards.any { it.kind == LevelRewardKind.FRAME && it.name == "Hexagone SOC" && it.frame != null })
    }

    @Test
    fun `level one exposes each starter cosmetic instead of summary text`() {
        val rewards = levelRewards(1)
        assertEquals(5, rewards.count { it.kind == LevelRewardKind.AVATAR })
        assertEquals(4, rewards.count { it.kind == LevelRewardKind.BANNER })
        assertEquals(3, rewards.count { it.kind == LevelRewardKind.FRAME })
    }

    @Test
    fun `role lookup clamps to maximum level`() {
        assertEquals("Cyber Légende", playerRoleForLevel(999))
        assertEquals("Recrue cyber", playerRoleForLevel(0))
    }
}
