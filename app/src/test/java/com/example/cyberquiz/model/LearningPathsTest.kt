package com.example.cyberquiz.model

import com.example.cyberquiz.data.database.CategoryProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPathsTest {
    @Test
    fun `category accuracy handles empty and invalid values`() {
        assertEquals(0, categoryAccuracy(0, 0))
        assertEquals(70, categoryAccuracy(10, 7))
        assertEquals(100, categoryAccuracy(5, 8))
        assertEquals(0, categoryAccuracy(5, -2))
    }

    @Test
    fun `step requires enough answers and target accuracy`() {
        assertFalse(isLearningPathStepCompleted(answered = 4, correct = 4))
        assertFalse(isLearningPathStepCompleted(answered = 5, correct = 3))
        assertTrue(isLearningPathStepCompleted(answered = 5, correct = 4))
        assertTrue(isLearningPathStepCompleted(answered = 10, correct = 7))
    }

    @Test
    fun `path unlocks steps in order`() {
        val definition = LearningPathDefinition(
            id = "test",
            title = "Test",
            subtitle = "Test",
            steps = listOf(
                LearningPathStep("one", "One", "Cat A", "A"),
                LearningPathStep("two", "Two", "Cat B", "B"),
                LearningPathStep("three", "Three", "Cat C", "C")
            )
        )
        val progress = buildLearningPathProgress(
            definitions = listOf(definition),
            categoryProgress = listOf(
                CategoryProgressEntity("CYBERSECURITY", "Cat A", answered = 5, correct = 4),
                CategoryProgressEntity("CYBERSECURITY", "Cat B", answered = 2, correct = 2)
            )
        ).single()

        assertTrue(progress.steps[0].completed)
        assertTrue(progress.steps[1].unlocked)
        assertFalse(progress.steps[1].completed)
        assertFalse(progress.steps[2].unlocked)
        assertEquals(1, progress.completedSteps)
        assertEquals(33, progress.progressPercent)
    }

    @Test
    fun `completed historical step remains accessible`() {
        val definition = LearningPathDefinition(
            id = "test",
            title = "Test",
            subtitle = "Test",
            steps = listOf(
                LearningPathStep("one", "One", "Cat A", "A"),
                LearningPathStep("two", "Two", "Cat B", "B")
            )
        )
        val progress = buildLearningPathProgress(
            definitions = listOf(definition),
            categoryProgress = listOf(
                CategoryProgressEntity("CYBERSECURITY", "Cat B", answered = 5, correct = 5)
            )
        ).single()

        assertFalse(progress.steps[0].completed)
        assertTrue(progress.steps[1].completed)
        assertTrue(progress.steps[1].unlocked)
    }
}
