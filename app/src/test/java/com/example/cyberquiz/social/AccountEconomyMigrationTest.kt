package com.example.cyberquiz.social

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountEconomyMigrationTest {
    @Test
    fun legacyOwnershipIsMergedIntoFirstEconomySeed() {
        val local = EconomySeed(
            coins = 120,
            loginDay = "2026-09-10",
            loginStreak = 4,
            loginRewardToday = 35,
            baselineDay = "2026-09-10",
            baselineAnswered = 40,
            baselineCorrect = 32,
            baselineXp = 500,
            claimedMissionIds = setOf("answer_10"),
            unlockedAchievementIds = setOf("first_answer"),
            claimedLevels = setOf(1, 2),
            purchasedFrameKeys = setOf("shop_neon_orbit"),
            purchasedAvatarKeys = emptySet(),
            purchasedBannerKeys = emptySet()
        )
        val legacy = CloudEngagementOwnership(
            unlockedAchievementIds = setOf("questions_10"),
            purchasedFrameKeys = setOf("shop_pixel_gate"),
            purchasedAvatarKeys = setOf("shop_bug_bot"),
            purchasedBannerKeys = setOf("shop_zero_trace")
        )

        val merged = AccountEconomyManager.mergeLegacyOwnership(local, legacy)

        assertEquals(120, merged.coins)
        assertEquals(setOf("first_answer", "questions_10"), merged.unlockedAchievementIds)
        assertEquals(setOf("shop_neon_orbit", "shop_pixel_gate"), merged.purchasedFrameKeys)
        assertEquals(setOf("shop_bug_bot"), merged.purchasedAvatarKeys)
        assertEquals(setOf("shop_zero_trace"), merged.purchasedBannerKeys)
        assertEquals(setOf("answer_10"), merged.claimedMissionIds)
        assertEquals(setOf(1, 2), merged.claimedLevels)
    }

    @Test
    fun missingLegacySnapshotLeavesLocalSeedUntouched() {
        val local = EconomySeed(
            coins = 15,
            loginDay = null,
            loginStreak = 0,
            loginRewardToday = 0,
            baselineDay = null,
            baselineAnswered = 0,
            baselineCorrect = 0,
            baselineXp = 0,
            claimedMissionIds = emptySet(),
            unlockedAchievementIds = emptySet(),
            claimedLevels = emptySet(),
            purchasedFrameKeys = emptySet(),
            purchasedAvatarKeys = emptySet(),
            purchasedBannerKeys = emptySet()
        )

        assertEquals(local, AccountEconomyManager.mergeLegacyOwnership(local, null))
    }
}
