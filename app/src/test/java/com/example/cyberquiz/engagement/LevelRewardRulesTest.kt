package com.example.cyberquiz.engagement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelRewardRulesTest {
    @Test
    fun `coin reward grows across level milestones`() {
        assertEquals(10, levelCoinReward(1))
        assertEquals(15, levelCoinReward(5))
        assertEquals(20, levelCoinReward(10))
        assertEquals(25, levelCoinReward(15))
        assertEquals(30, levelCoinReward(20))
        assertEquals(40, levelCoinReward(25))
        assertEquals(60, levelCoinReward(30))
    }

    @Test
    fun `all crossed levels are queued in order`() {
        assertEquals(listOf(6, 7, 8), newlyReachedLevels(5, 8))
        assertTrue(newlyReachedLevels(8, 8).isEmpty())
        assertTrue(newlyReachedLevels(9, 4).isEmpty())
    }

    @Test
    fun `level reward rules clamp to supported range`() {
        assertEquals(10, levelCoinReward(-5))
        assertEquals(60, levelCoinReward(999))
    }
}
