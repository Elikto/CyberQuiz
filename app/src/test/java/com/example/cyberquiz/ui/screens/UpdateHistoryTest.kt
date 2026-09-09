package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateHistoryTest {
    @Test
    fun `history is sorted newest first and deduplicated`() {
        val entries = listOf(
            UpdateHistoryEntry("1.0.33", "", listOf("a")),
            UpdateHistoryEntry("1.0.57", "", listOf("b")),
            UpdateHistoryEntry("1.0.52", "", listOf("c")),
            UpdateHistoryEntry("1.0.57", "", listOf("duplicate"))
        )

        assertEquals(
            listOf("1.0.57", "1.0.52", "1.0.33"),
            sortUpdateHistory(entries).map { it.version }
        )
    }

    @Test
    fun `version number reads release build suffix`() {
        assertEquals(57, updateVersionNumber("1.0.57"))
        assertEquals(-1, updateVersionNumber("dev"))
    }
}
