package com.example.cyberquiz.ui.screens

import com.example.cyberquiz.model.QuizSessionMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizQuestionCountOptionsTest {
    @Test
    fun `exam excludes infinite question count`() {
        val options = quizQuestionCountOptionsForMode(QuizSessionMode.EXAM)
        assertFalse(0 in options)
        assertTrue(options.all { it > 0 })
    }

    @Test
    fun `regular quiz keeps infinite question count`() {
        val options = quizQuestionCountOptionsForMode(QuizSessionMode.RANDOM)
        assertTrue(0 in options)
    }
}
