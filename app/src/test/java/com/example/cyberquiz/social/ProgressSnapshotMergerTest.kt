package com.example.cyberquiz.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressSnapshotMergerTest {
    @Test
    fun mergeNeverRegressesCumulativeProgress() {
        val local = CloudProgressSnapshot(
            progress = listOf(
                CloudQuizProgress("CYBERSECURITY", 420, 5, 40, 30, 4, 9, 8000)
            )
        )
        val remote = CloudProgressSnapshot(
            progress = listOf(
                CloudQuizProgress("CYBERSECURITY", 350, 4, 45, 28, 2, 10, 9000)
            )
        )

        val merged = ProgressSnapshotMerger.merge(local, remote).progress.single()

        assertEquals(420, merged.xp)
        assertEquals(45, merged.answered)
        assertEquals(30, merged.correct)
        assertEquals(10, merged.bestStreak)
        assertEquals(9000L, merged.totalResponseMs)
    }

    @Test
    fun mergeKeepsLatestConceptStateWhilePreservingCounters() {
        val local = CloudProgressSnapshot(
            concepts = listOf(
                CloudConceptProgress("CYBERSECURITY", "DNS", "Réseaux", 3, 2, true, true, 100)
            )
        )
        val remote = CloudProgressSnapshot(
            concepts = listOf(
                CloudConceptProgress("CYBERSECURITY", "DNS", "Réseaux", 4, 2, false, false, 200)
            )
        )

        val merged = ProgressSnapshotMerger.merge(local, remote).concepts.single()

        assertEquals(4, merged.attempts)
        assertEquals(2, merged.correct)
        assertFalse(merged.reviewMastered)
        assertFalse(merged.lastResultCorrect)
        assertEquals(200L, merged.lastAnsweredAt)
    }

    @Test
    fun mergeUnionsHistoryAndCloudOwnedRewards() {
        val local = CloudProgressSnapshot(
            history = listOf(history("same", 100), history("local", 80)),
            engagement = CloudEngagementOwnership(
                unlockedAchievementIds = setOf("first_win"),
                purchasedAvatarKeys = setOf("ghost")
            )
        )
        val remote = CloudProgressSnapshot(
            history = listOf(history("same", 120), history("remote", 90)),
            engagement = CloudEngagementOwnership(
                unlockedAchievementIds = setOf("level_5"),
                purchasedBannerKeys = setOf("matrix_green")
            )
        )

        val merged = ProgressSnapshotMerger.merge(local, remote)

        assertEquals(listOf("same", "remote", "local"), merged.history.map { it.id })
        assertEquals(120L, merged.history.first().endedAt)
        assertTrue("first_win" in merged.engagement.unlockedAchievementIds)
        assertTrue("level_5" in merged.engagement.unlockedAchievementIds)
        assertTrue("ghost" in merged.engagement.purchasedAvatarKeys)
        assertTrue("matrix_green" in merged.engagement.purchasedBannerKeys)
    }

    private fun history(id: String, endedAt: Long) = CloudHistoryEntry(
        id = id,
        mode = "RANDOM",
        categories = listOf("Réseaux"),
        questionCount = 5,
        startedAt = endedAt - 10,
        endedAt = endedAt,
        answered = 5,
        correct = 4,
        xpGained = 50,
        questions = emptyList()
    )
}
