package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSetupScreenUxTest {
    private val categories = listOf("Réseau", "Web", "Crypto")

    @Test
    fun `new quiz starts with every category selected`() {
        assertEquals(categories.toSet(), defaultNewQuizCategories(categories))
    }

    @Test
    fun `unchecking all categories expands selector`() {
        val result = toggleAllQuizCategories(
            categories = categories,
            selected = categories.toSet(),
            expanded = false
        )

        assertTrue(result.selected.isEmpty())
        assertTrue(result.expanded)
    }

    @Test
    fun `checking all categories preserves manual collapsed state`() {
        val result = toggleAllQuizCategories(
            categories = categories,
            selected = setOf("Réseau"),
            expanded = false
        )

        assertEquals(categories.toSet(), result.selected)
        assertFalse(result.expanded)
    }

    @Test
    fun `checking all categories preserves manual expanded state`() {
        val result = toggleAllQuizCategories(
            categories = categories,
            selected = setOf("Réseau"),
            expanded = true
        )

        assertEquals(categories.toSet(), result.selected)
        assertTrue(result.expanded)
    }
}
