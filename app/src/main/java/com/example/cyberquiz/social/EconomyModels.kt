package com.example.cyberquiz.social

import com.example.cyberquiz.model.EngagementMetrics

internal data class EconomySeed(
    val coins: Int,
    val loginDay: String?,
    val loginStreak: Int,
    val loginRewardToday: Int,
    val baselineDay: String?,
    val baselineAnswered: Int,
    val baselineCorrect: Int,
    val baselineXp: Int,
    val claimedMissionIds: Set<String>,
    val unlockedAchievementIds: Set<String>,
    val claimedLevels: Set<Int>,
    val purchasedFrameKeys: Set<String>,
    val purchasedAvatarKeys: Set<String>,
    val purchasedBannerKeys: Set<String>
)

internal data class EconomyState(
    val coins: Int,
    val loginDay: String?,
    val loginStreak: Int,
    val loginRewardToday: Int,
    val baselineDay: String?,
    val baselineAnswered: Int,
    val baselineCorrect: Int,
    val baselineXp: Int,
    val claimedMissionIds: Set<String>,
    val unlockedAchievementIds: Set<String>,
    val claimedLevels: Set<Int>,
    val purchasedFrameKeys: Set<String>,
    val purchasedAvatarKeys: Set<String>,
    val purchasedBannerKeys: Set<String>,
    val updatedAt: String?
)

internal data class EconomyMutationContext(
    val metrics: EngagementMetrics,
    val seed: EconomySeed
)
