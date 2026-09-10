package com.example.cyberquiz.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamModeRulesTest {
    @Test
    fun `suggested duration scales with question count`() {
        assertEquals(5, suggestedExamTimeLimitMinutes(5))
        assertEquals(10, suggestedExamTimeLimitMinutes(10))
        assertEquals(20, suggestedExamTimeLimitMinutes(20))
        assertEquals(45, suggestedExamTimeLimitMinutes(50))
        assertEquals(90, suggestedExamTimeLimitMinutes(100))
        assertEquals(120, suggestedExamTimeLimitMinutes(200))
    }

    @Test
    fun `deadline is derived from original start time`() {
        assertEquals(1_300_000L, examDeadlineEpochMs(100_000L, 20))
        assertNull(examDeadlineEpochMs(0L, 20))
        assertNull(examDeadlineEpochMs(100_000L, 0))
    }

    @Test
    fun `remaining time never goes negative`() {
        assertEquals(5_000L, examRemainingMillis(10_000L, 5_000L))
        assertEquals(0L, examRemainingMillis(10_000L, 12_000L))
        assertEquals(0L, examRemainingMillis(null, 12_000L))
    }

    @Test
    fun `remaining time formats minutes and hours`() {
        assertEquals("00:00", formatExamRemainingTime(0L))
        assertEquals("00:01", formatExamRemainingTime(1L))
        assertEquals("05:00", formatExamRemainingTime(300_000L))
        assertEquals("1:01:01", formatExamRemainingTime(3_661_000L))
    }

    @Test
    fun `exam config exposes timed state`() {
        val exam = QuizSessionConfig(
            mode = QuizSessionMode.EXAM,
            questionCount = 20,
            timeLimitMinutes = 30
        )
        assertTrue(exam.exam)
        assertTrue(exam.timedExam)
        assertFalse(exam.infinite)
    }
}
