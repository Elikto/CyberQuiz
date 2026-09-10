package com.example.cyberquiz.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionRulesTest {
    @Test
    fun `titles unlock only when their achievement is owned`() {
        val unlocked = unlockedProfileTitles(setOf("first_answer", "level_10"))

        assertEquals(listOf("packet_rookie", "ethical_operator"), unlocked.map { it.id })
    }

    @Test
    fun `selected unlocked title remains effective`() {
        val title = effectiveProfileTitle(
            selectedTitleId = "packet_rookie",
            unlockedAchievementIds = setOf("first_answer", "level_10")
        )

        assertEquals("packet_rookie", title?.id)
    }

    @Test
    fun `invalid selection falls back to latest unlocked title`() {
        val title = effectiveProfileTitle(
            selectedTitleId = "cyberquiz_veteran",
            unlockedAchievementIds = setOf("first_answer", "level_10")
        )

        assertEquals("ethical_operator", title?.id)
    }

    @Test
    fun `no achievement means no title`() {
        assertNull(effectiveProfileTitle(null, emptySet()))
    }

    @Test
    fun `collection completion is bounded and handles empty catalogs`() {
        assertEquals(0, collectionCompletionPercent(0, 0, 0, 0, 0, 0))
        assertEquals(50, collectionCompletionPercent(5, 10, 2, 4, 3, 6))
        assertEquals(100, collectionCompletionPercent(99, 1, 99, 1, 99, 1))
        assertTrue(profileTitleDefinitions.map { it.id }.toSet().size == profileTitleDefinitions.size)
    }
}
