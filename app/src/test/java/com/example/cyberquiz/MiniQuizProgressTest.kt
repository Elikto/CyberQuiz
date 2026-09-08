package com.example.cyberquiz

import com.example.cyberquiz.model.QuizHistoryEntry
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.model.categoryMiniQuizAttempts
import org.junit.Assert.assertEquals
import org.junit.Test

class MiniQuizProgressTest {
    @Test
    fun `five question attempts are numbered chronologically per category`() {
        val history = listOf(
            entry(id = "latest", category = "Réseaux", questionCount = 5, endedAt = 300L, correct = 4),
            entry(id = "oldest", category = "Réseaux", questionCount = 5, endedAt = 100L, correct = 2),
            entry(id = "middle", category = "Réseaux", questionCount = 5, endedAt = 200L, correct = 5)
        )

        val attempts = categoryMiniQuizAttempts(history, "Réseaux")

        assertEquals(listOf("oldest", "middle", "latest"), attempts.map { it.historyId })
        assertEquals(listOf(1, 2, 3), attempts.map { it.number })
        assertEquals(listOf(40, 100, 80), attempts.map { it.percent })
    }

    @Test
    fun `only completed five question single category quizzes are included`() {
        val history = listOf(
            entry(id = "valid", category = "Linux", questionCount = 5, endedAt = 100L, correct = 3),
            entry(id = "wrong-count", category = "Linux", questionCount = 10, endedAt = 200L, correct = 7),
            entry(id = "other-category", category = "Windows", questionCount = 5, endedAt = 300L, correct = 4),
            entry(
                id = "multi-category",
                categories = setOf("Linux", "Windows"),
                questionCount = 5,
                endedAt = 400L,
                correct = 4
            ),
            entry(id = "partial", category = "Linux", questionCount = 5, endedAt = 450L, correct = 2, answered = 3),
            entry(id = "empty", category = "Linux", questionCount = 5, endedAt = 500L, correct = 0, answered = 0)
        )

        val attempts = categoryMiniQuizAttempts(history, "Linux")

        assertEquals(1, attempts.size)
        assertEquals("valid", attempts.single().historyId)
        assertEquals(3, attempts.single().correct)
        assertEquals(5, attempts.single().answered)
    }

    private fun entry(
        id: String,
        category: String? = null,
        categories: Set<String> = category?.let(::setOf) ?: emptySet(),
        questionCount: Int,
        endedAt: Long,
        correct: Int,
        answered: Int = questionCount
    ) = QuizHistoryEntry(
        id = id,
        config = QuizSessionConfig(
            mode = QuizSessionMode.RANDOM,
            categories = categories,
            questionCount = questionCount
        ),
        startedAt = endedAt - 10L,
        endedAt = endedAt,
        answered = answered,
        correct = correct,
        xpGained = 0,
        questions = emptyList()
    )
}
