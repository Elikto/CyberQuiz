package com.example.cyberquiz.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionReportReasonTest {
    @Test
    fun `wire values round trip and unknown values fall back safely`() {
        QuestionReportReason.entries.forEach { reason ->
            assertEquals(reason, QuestionReportReason.fromWire(reason.wireValue))
        }
        assertEquals(QuestionReportReason.OTHER, QuestionReportReason.fromWire("unexpected"))
    }
}
