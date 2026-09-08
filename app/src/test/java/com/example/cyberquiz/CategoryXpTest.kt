package com.example.cyberquiz

import com.example.cyberquiz.model.categoryLevelProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryXpTest {
    @Test
    fun `category xp rewards every answer and adds a correct bonus`() {
        val progress = categoryLevelProgress(answered = 10, correct = 7)

        assertEquals(85, progress.xp)
        assertEquals(1, progress.level)
        assertEquals(85, progress.xpIntoLevel)
        assertEquals(0.85f, progress.progress, 0.0001f)
    }

    @Test
    fun `category level increases every one hundred xp`() {
        val progress = categoryLevelProgress(answered = 12, correct = 8)

        assertEquals(100, progress.xp)
        assertEquals(2, progress.level)
        assertEquals(0, progress.xpIntoLevel)
        assertEquals(0f, progress.progress, 0.0001f)
    }

    @Test
    fun `invalid correct count is clamped to answered count`() {
        val progress = categoryLevelProgress(answered = 4, correct = 99)

        assertEquals(40, progress.xp)
        assertEquals(1, progress.level)
    }
}
