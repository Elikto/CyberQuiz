package com.example.cyberquiz.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyMissionClaimRulesTest {
    @Test
    fun `completed unclaimed mission becomes claimable`() {
        val definition = dailyMissionDefinitions.first()
        val progress = listOf(
            DailyMissionProgress(
                definition = definition,
                current = definition.target,
                completed = true
            )
        )

        assertEquals(setOf(definition.id), claimableMissionIds(progress, emptySet()))
    }

    @Test
    fun `claimed or incomplete missions do not stay claimable`() {
        val first = dailyMissionDefinitions[0]
        val second = dailyMissionDefinitions[1]
        val progress = listOf(
            DailyMissionProgress(first, first.target, completed = true),
            DailyMissionProgress(second, second.target - 1, completed = false)
        )

        assertTrue(claimableMissionIds(progress, setOf(first.id)).isEmpty())
    }
}
