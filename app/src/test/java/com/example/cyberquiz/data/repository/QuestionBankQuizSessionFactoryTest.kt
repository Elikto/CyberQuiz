package com.example.cyberquiz.data.repository

import com.example.cyberquiz.data.database.QuestionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class QuestionBankQuizSessionFactoryTest {
    @Test
    fun `selection drops invalid ids and duplicates while preserving order`() {
        val selected = QuestionBankQuizSessionFactory.selectQuestions(
            listOf(
                question(0),
                question(3),
                question(3),
                question(2),
                question(1)
            )
        )

        assertEquals(listOf(3L, 2L, 1L), selected.map { it.id })
    }

    @Test
    fun `selection is capped to maximum quiz size`() {
        val selected = QuestionBankQuizSessionFactory.selectQuestions(
            (1L..80L).map(::question)
        )

        assertEquals(QuestionBankQuizSessionFactory.MAX_QUESTIONS, selected.size)
        assertEquals(1L, selected.first().id)
        assertEquals(50L, selected.last().id)
        assertFalse(selected.any { it.id > 50L })
    }

    private fun question(id: Long) = QuestionEntity(
        id = id,
        category = "Réseau",
        difficulty = "EASY",
        question = "Question $id",
        answerA = "A",
        answerB = "B",
        answerC = "C",
        answerD = "D",
        correctIndex = 0,
        explanation = "Explication"
    )
}
