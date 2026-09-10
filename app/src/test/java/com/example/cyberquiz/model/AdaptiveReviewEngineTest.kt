package com.example.cyberquiz.model

import com.example.cyberquiz.data.database.ReviewItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveReviewEngineTest {
    private val now = 1_800_000_000_000L

    @Test
    fun `wrong answer becomes immediately due`() {
        val decision = AdaptiveReviewEngine.schedule(
            previousStage = 4,
            correct = false,
            wrongCount = 5,
            responseMs = 18_000L,
            now = now
        )

        assertEquals(0, decision.stage)
        assertEquals(now, decision.nextReviewAt)
    }

    @Test
    fun `mastered answers progressively increase spacing`() {
        val first = AdaptiveReviewEngine.schedule(0, true, 1, 12_000L, now)
        val second = AdaptiveReviewEngine.schedule(first.stage, true, 1, 12_000L, now)

        assertTrue(second.stage > first.stage)
        assertTrue(second.nextReviewAt > first.nextReviewAt)
    }

    @Test
    fun `fast confident answer advances faster than slow answer`() {
        val fast = AdaptiveReviewEngine.schedule(2, true, 1, 5_000L, now)
        val slow = AdaptiveReviewEngine.schedule(2, true, 1, 35_000L, now)

        assertTrue(fast.stage > slow.stage)
        assertTrue(fast.nextReviewAt > slow.nextReviewAt)
    }

    @Test
    fun `frequent previous errors keep review closer`() {
        val fragile = AdaptiveReviewEngine.schedule(3, true, 8, 12_000L, now)
        val stable = AdaptiveReviewEngine.schedule(3, true, 1, 12_000L, now)

        assertTrue(fragile.stage < stable.stage)
        assertTrue(fragile.nextReviewAt < stable.nextReviewAt)
    }

    @Test
    fun `mastered item returns only when due`() {
        val future = review(mastered = true, nextReviewAt = now + 86_400_000L)
        val overdue = review(mastered = true, nextReviewAt = now - 1L)

        assertFalse(AdaptiveReviewEngine.isDue(future, now))
        assertTrue(AdaptiveReviewEngine.isDue(overdue, now))
    }

    @Test
    fun `fragile slow item ranks before stable item`() {
        val fragile = review(
            mastered = false,
            wrongCount = 6,
            reviewAttempts = 3,
            totalReviewResponseMs = 90_000L
        )
        val stable = review(
            mastered = false,
            wrongCount = 1,
            reviewStage = 3,
            reviewAttempts = 3,
            totalReviewResponseMs = 15_000L
        )

        assertTrue(AdaptiveReviewEngine.priority(fragile, now) > AdaptiveReviewEngine.priority(stable, now))
    }

    private fun review(
        mastered: Boolean,
        wrongCount: Int = 1,
        reviewStage: Int = 0,
        nextReviewAt: Long = 0L,
        reviewAttempts: Int = 0,
        totalReviewResponseMs: Long = 0L
    ) = ReviewItemEntity(
        quizType = "CYBERSECURITY",
        concept = "TLS",
        category = "Web",
        difficulty = "MEDIUM",
        questionId = 1L,
        question = "Test ?",
        correctAnswer = "TLS",
        wrongCount = wrongCount,
        mastered = mastered,
        reviewStage = reviewStage,
        nextReviewAt = nextReviewAt,
        reviewAttempts = reviewAttempts,
        totalReviewResponseMs = totalReviewResponseMs
    )
}
