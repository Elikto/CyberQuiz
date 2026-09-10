package com.example.cyberquiz.engagement

import com.example.cyberquiz.data.database.QuestionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChallengeRulesTest {
    @Test
    fun `consecutive day increases streak`() {
        assertEquals(5, DailyChallengeStore.nextStreak(4, "2026-09-08", "2026-09-09"))
    }

    @Test
    fun `one missed day protects streak`() {
        assertEquals(7, DailyChallengeStore.nextStreak(7, "2026-09-07", "2026-09-09"))
    }

    @Test
    fun `longer absence decays progressively instead of resetting`() {
        assertEquals(5, DailyChallengeStore.nextStreak(8, "2026-09-04", "2026-09-09"))
        assertEquals(1, DailyChallengeStore.nextStreak(2, "2026-08-01", "2026-09-09"))
    }

    @Test
    fun `daily selection is deterministic and contains five unique questions`() {
        val questions = (1L..12L).map { id ->
            QuestionEntity(
                id = id,
                category = "Cat${id % 4}",
                difficulty = "MEDIUM",
                question = "Question $id",
                answerA = "A",
                answerB = "B",
                answerC = "C",
                answerD = "D",
                correctIndex = 0,
                explanation = "Explication"
            )
        }

        val first = DailyChallengeStore.selectQuestionIds(questions, "2026-09-10")
        val second = DailyChallengeStore.selectQuestionIds(questions, "2026-09-10")

        assertEquals(DAILY_CHALLENGE_SIZE, first.size)
        assertEquals(first, second)
        assertEquals(first.size, first.toSet().size)
        val selectedCategories = questions.filter { it.id in first }.map { it.category }.toSet()
        assertTrue(selectedCategories.size >= 3)
    }
}
