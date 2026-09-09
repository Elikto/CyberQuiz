package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `roadmap index matches descending level order`() {
        assertEquals(0, roadmapIndexForLevel(30))
        assertEquals(10, roadmapIndexForLevel(20))
        assertEquals(15, roadmapIndexForLevel(15))
        assertEquals(29, roadmapIndexForLevel(1))
        assertEquals(0, roadmapIndexForLevel(999))
        assertEquals(29, roadmapIndexForLevel(0))
    }

    @Test
    fun `every roadmap level has an avatar and coin chest reward`() {
        val roadmap = levelRoadmap()
        assertTrue(roadmap.all { entry -> entry.rewards.any { it.kind == LevelRewardKind.AVATAR } })
        assertTrue(roadmap.all { entry -> entry.rewards.any { it.kind == LevelRewardKind.COINS && it.coinAmount > 0 } })
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
    fun `level five chest contains avatar coins and real cosmetic unlocks`() {
        val rewards = levelRewards(5)
        assertTrue(rewards.any { it.kind == LevelRewardKind.AVATAR && it.name == "Chasseur de malwares" && it.avatar != null })
        assertTrue(rewards.any { it.kind == LevelRewardKind.COINS && it.coinAmount == 15 })
        assertTrue(rewards.any { it.kind == LevelRewardKind.BANNER && it.name == "Radar SOC" && it.banner != null })
        assertTrue(rewards.any { it.kind == LevelRewardKind.FRAME && it.name == "Hexagone SOC" && it.frame != null })
    }

    @Test
    fun `ordinary level chest only needs avatar and coins`() {
        val rewards = levelRewards(3)
        assertEquals(1, rewards.count { it.kind == LevelRewardKind.AVATAR })
        assertEquals(1, rewards.count { it.kind == LevelRewardKind.COINS })
        assertEquals(0, rewards.count { it.kind == LevelRewardKind.BANNER })
        assertEquals(0, rewards.count { it.kind == LevelRewardKind.FRAME })
    }

    @Test
    fun `level avatar stays visible before level twenty`() {
        assertFalse(shouldBlurLevelAvatar(level = 1, claimed = false))
        assertFalse(shouldBlurLevelAvatar(level = 19, claimed = false))
    }

    @Test
    fun `level avatar is hidden from level twenty until claimed`() {
        assertTrue(shouldBlurLevelAvatar(level = 20, claimed = false))
        assertTrue(shouldBlurLevelAvatar(level = 30, claimed = false))
        assertFalse(shouldBlurLevelAvatar(level = 20, claimed = true))
    }

    @Test
    fun `role lookup clamps to maximum level`() {
        assertEquals("Cyber Légende", playerRoleForLevel(999))
        assertEquals("Recrue cyber", playerRoleForLevel(0))
    }
}
