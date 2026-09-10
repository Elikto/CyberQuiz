package com.example.cyberquiz.model

import com.example.cyberquiz.data.database.QuestionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionBankFilterTest {
    @Test
    fun `search matches prompt answers and category`() {
        val questions = listOf(
            question(1, "Réseau", "EASY", "Quel protocole chiffre le Web ?", "HTTP", "HTTPS"),
            question(2, "Malware", "HARD", "Quel logiciel chiffre les fichiers ?", "Ransomware", "Spyware")
        )

        assertEquals(
            listOf(1L),
            filterQuestionBank(
                questions,
                QuestionBankFilter(query = "https"),
                emptySet(),
                emptySet(),
                emptySet()
            ).map { it.id }
        )
        assertEquals(
            listOf(2L),
            filterQuestionBank(
                questions,
                QuestionBankFilter(query = "malware"),
                emptySet(),
                emptySet(),
                emptySet()
            ).map { it.id }
        )
    }

    @Test
    fun `status category and difficulty filters compose`() {
        val questions = listOf(
            question(1, "Réseau", "EASY", "Q1", "A", "B", seen = true),
            question(2, "Réseau", "HARD", "Q2", "A", "B", seen = true),
            question(3, "Web", "HARD", "Q3", "A", "B", seen = false)
        )

        val result = filterQuestionBank(
            questions,
            QuestionBankFilter(
                category = "Réseau",
                difficulty = "hard",
                status = QuestionBankStatus.FAVORITES
            ),
            favoriteIds = setOf(2L, 3L),
            wrongQuestionIds = setOf(1L),
            reportedIds = emptySet()
        )

        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun `wrong reported seen and unseen statuses use their dedicated state`() {
        val questions = listOf(
            question(1, "Réseau", "EASY", "Q1", "A", "B", seen = true),
            question(2, "Web", "MEDIUM", "Q2", "A", "B", seen = false)
        )

        val wrong = filterQuestionBank(
            questions,
            QuestionBankFilter(status = QuestionBankStatus.WRONG),
            emptySet(),
            setOf(2L),
            emptySet()
        )
        val reported = filterQuestionBank(
            questions,
            QuestionBankFilter(status = QuestionBankStatus.REPORTED),
            emptySet(),
            emptySet(),
            setOf(1L)
        )
        val seen = filterQuestionBank(
            questions,
            QuestionBankFilter(status = QuestionBankStatus.SEEN),
            emptySet(),
            emptySet(),
            emptySet()
        )
        val unseen = filterQuestionBank(
            questions,
            QuestionBankFilter(status = QuestionBankStatus.UNSEEN),
            emptySet(),
            emptySet(),
            emptySet()
        )

        assertEquals(listOf(2L), wrong.map { it.id })
        assertEquals(listOf(1L), reported.map { it.id })
        assertTrue(seen.single().seen)
        assertTrue(!unseen.single().seen)
    }

    private fun question(
        id: Long,
        category: String,
        difficulty: String,
        prompt: String,
        answerA: String,
        answerB: String,
        seen: Boolean = false
    ) = QuestionEntity(
        id = id,
        category = category,
        difficulty = difficulty,
        question = prompt,
        answerA = answerA,
        answerB = answerB,
        answerC = "C",
        answerD = "D",
        correctIndex = 1,
        explanation = "Explication",
        seen = seen
    )
}
