package com.example.cyberquiz.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngagementRulesTest {
    @Test
    fun `daily login reward increases until day seven`() {
        assertEquals(20, loginRewardForStreak(1))
        assertEquals(30, loginRewardForStreak(3))
        assertEquals(50, loginRewardForStreak(7))
        assertEquals(50, loginRewardForStreak(20))
    }

    @Test
    fun `daily missions use progress since baseline`() {
        val baseline = EngagementMetrics(100, 60, 900, 10, 0, 12, 8)
        val current = EngagementMetrics(112, 68, 985, 10, 4, 12, 9)
        val progress = dailyMissionProgress(current, baseline)

        assertEquals(3, progress.size)
        assertTrue(progress.all { it.completed })
        assertEquals(10, progress.first { it.definition.id == "answer_10" }.current)
        assertEquals(7, progress.first { it.definition.id == "correct_7" }.current)
        assertEquals(75, progress.first { it.definition.id == "xp_75" }.current)
    }

    @Test
    fun `achievement unlocks are only returned once`() {
        val metrics = EngagementMetrics(120, 100, 1050, 11, 3, 26, 20)
        val already = setOf("first_answer", "questions_10")
        val unlocked = newlyUnlockedAchievementIds(metrics, already)

        assertTrue("questions_100" in unlocked)
        assertTrue("streak_10" in unlocked)
        assertTrue("streak_25" in unlocked)
        assertTrue("level_5" in unlocked)
        assertTrue("level_10" in unlocked)
        assertTrue("accuracy_80" in unlocked)
        assertTrue("first_answer" !in unlocked)
    }
}
