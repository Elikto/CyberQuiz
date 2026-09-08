package com.example.cyberquiz.contact

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ContactRetryPolicyTest {
    @Test
    fun `network errors are retried`() {
        assertTrue(CyberQuizContactQueue.shouldRetry(IOException("timeout"), runAttemptCount = 0))
    }

    @Test
    fun `retryable server errors are retried`() {
        assertTrue(
            CyberQuizContactQueue.shouldRetry(
                ContactRetryableException("service unavailable"),
                runAttemptCount = 2
            )
        )
    }

    @Test
    fun `permanent errors are not retried`() {
        assertFalse(
            CyberQuizContactQueue.shouldRetry(
                IllegalStateException("bad request"),
                runAttemptCount = 0
            )
        )
    }

    @Test
    fun `retry count is capped`() {
        assertFalse(CyberQuizContactQueue.shouldRetry(IOException("timeout"), runAttemptCount = 5))
    }
}
