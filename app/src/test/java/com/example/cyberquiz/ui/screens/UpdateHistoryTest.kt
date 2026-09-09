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

    @Test
    fun `existing French update note stays unchanged`() {
        val note = "Ajout des récompenses de quêtes et des coffres de niveau."
        assertEquals(note, frenchUpdateChange(note))
    }

    @Test
    fun `English update history title is converted to French`() {
        assertEquals(
            "Amélioration de l’historique des mises à jour et de sa génération automatique.",
            frenchUpdateChange("Keep update history complete on every release (#56)")
        )
    }

    @Test
    fun `English quest reward title is converted to French`() {
        assertEquals(
            "Ajout et amélioration des quêtes, récompenses et coffres de niveau.",
            frenchUpdateChange("Add quest rewards and animated level chests (#57)")
        )
    }

    @Test
    fun `unknown English release note never leaks into the French history`() {
        assertEquals(
            "Améliorations et corrections diverses de CyberQuiz.",
            frenchUpdateChange("Refactor internal architecture for cleaner components")
        )
    }

    @Test
    fun `English note containing the shared word version is still translated`() {
        assertEquals(
            "Améliorations et corrections diverses de CyberQuiz.",
            frenchUpdateChange("New application version with cleaner components")
        )
    }
}
