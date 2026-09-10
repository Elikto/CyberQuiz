package com.example.cyberquiz.social

import android.content.Context
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.repository.QuizHistoryStore
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.engagement.LevelRewardStore
import com.example.cyberquiz.model.EngagementMetrics

/**
 * Bridges the legacy offline economy with the authenticated server economy.
 *
 * Guests keep the current fully-local behavior. Signed-in players use the server
 * as the authority for every spend/claim operation, which prevents two devices
 * from claiming or spending the same balance twice. Local SharedPreferences are
 * then refreshed from the committed server state so existing UI keeps working.
 */
internal object AccountEconomyManager {
    private const val ENGAGEMENT_PREFS = "cyberquiz_engagement"
    private const val LEVEL_PREFS = "cyberquiz_level_rewards"

    private const val KEY_COINS = "coins"
    private const val KEY_LOGIN_DAY = "login_day"
    private const val KEY_LOGIN_STREAK = "login_streak"
    private const val KEY_LOGIN_REWARD = "login_reward"
    private const val KEY_BASELINE_DAY = "baseline_day"
    private const val KEY_BASELINE_ANSWERED = "baseline_answered"
    private const val KEY_BASELINE_CORRECT = "baseline_correct"
    private const val KEY_BASELINE_XP = "baseline_xp"
    private const val KEY_CLAIMED_MISSIONS = "claimed_missions"
    private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
    private const val KEY_PURCHASED_FRAMES = "purchased_frames"
    private const val KEY_PURCHASED_AVATARS = "purchased_avatars"
    private const val KEY_PURCHASED_BANNERS = "purchased_banners"

    private const val KEY_PENDING_LEVELS = "pending_levels"
    private const val KEY_CLAIMED_LEVELS = "claimed_levels"
    private const val KEY_UNSEEN_POPUPS = "unseen_popups"

    suspend fun syncCurrentSession(context: Context): EconomyState? {
        val appContext = context.applicationContext
        val token = SocialTokenStore.load(appContext) ?: return null
        val metrics = metrics(appContext)
        val state = EconomyApiClient.sync(token, metrics, seed(appContext))
        apply(appContext, state)
        return state
    }

    suspend fun claimMission(context: Context, missionId: String): Int {
        val appContext = context.applicationContext
        val metrics = metrics(appContext)
        val token = SocialTokenStore.load(appContext)
        if (token.isNullOrBlank()) {
            return EngagementStore.claimMission(appContext, missionId, metrics)
        }
        val before = EngagementStore.currentCoins(appContext)
        val state = EconomyApiClient.claimMission(token, missionId, metrics, seed(appContext))
        apply(appContext, state)
        return (state.coins - before).coerceAtLeast(0)
    }

    suspend fun claimLevel(context: Context, level: Int): Int {
        val appContext = context.applicationContext
        val metrics = metrics(appContext)
        val token = SocialTokenStore.load(appContext)
        if (token.isNullOrBlank()) {
            return LevelRewardStore.claimLevel(appContext, level, metrics.level)
        }
        val before = EngagementStore.currentCoins(appContext)
        val state = EconomyApiClient.claimLevel(token, level, metrics, seed(appContext))
        apply(appContext, state)
        return (state.coins - before).coerceAtLeast(0)
    }

    suspend fun purchase(
        context: Context,
        kind: String,
        storageKey: String,
        cost: Int
    ): Boolean {
        val appContext = context.applicationContext
        val token = SocialTokenStore.load(appContext)
        if (token.isNullOrBlank()) {
            return when (kind) {
                "frame" -> EngagementStore.purchaseFrame(appContext, storageKey, cost)
                "avatar" -> EngagementStore.purchaseAvatar(appContext, storageKey, cost)
                "banner" -> EngagementStore.purchaseBanner(appContext, storageKey, cost)
                else -> false
            }
        }
        val metrics = metrics(appContext)
        val state = EconomyApiClient.purchase(token, kind, storageKey, metrics, seed(appContext))
        apply(appContext, state)
        return when (kind) {
            "frame" -> storageKey in state.purchasedFrameKeys
            "avatar" -> storageKey in state.purchasedAvatarKeys
            "banner" -> storageKey in state.purchasedBannerKeys
            else -> false
        }
    }

    suspend fun metrics(context: Context): EngagementMetrics {
        val appContext = context.applicationContext
        val progress = CyberQuizDatabase.get(appContext).quizDao().progressSnapshot("CYBERSECURITY")
        val historyCount = QuizHistoryStore(appContext).load().size
        return EngagementMetrics(
            answered = progress?.answered ?: 0,
            correct = progress?.correct ?: 0,
            xp = progress?.xp ?: 0,
            level = (progress?.level ?: 1).coerceAtLeast(1),
            streak = progress?.streak ?: 0,
            bestStreak = progress?.bestStreak ?: 0,
            quizCount = historyCount
        )
    }

    fun seed(context: Context): EconomySeed {
        val appContext = context.applicationContext
        val engagement = appContext.getSharedPreferences(ENGAGEMENT_PREFS, Context.MODE_PRIVATE)
        val levels = appContext.getSharedPreferences(LEVEL_PREFS, Context.MODE_PRIVATE)
        return EconomySeed(
            coins = engagement.getInt(KEY_COINS, 0).coerceAtLeast(0),
            loginDay = engagement.getString(KEY_LOGIN_DAY, null),
            loginStreak = engagement.getInt(KEY_LOGIN_STREAK, 0).coerceAtLeast(0),
            loginRewardToday = engagement.getInt(KEY_LOGIN_REWARD, 0).coerceAtLeast(0),
            baselineDay = engagement.getString(KEY_BASELINE_DAY, null),
            baselineAnswered = engagement.getInt(KEY_BASELINE_ANSWERED, 0).coerceAtLeast(0),
            baselineCorrect = engagement.getInt(KEY_BASELINE_CORRECT, 0).coerceAtLeast(0),
            baselineXp = engagement.getInt(KEY_BASELINE_XP, 0).coerceAtLeast(0),
            claimedMissionIds = engagement.getStringSet(KEY_CLAIMED_MISSIONS, emptySet())?.toSet().orEmpty(),
            unlockedAchievementIds = engagement.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet())?.toSet().orEmpty(),
            claimedLevels = decodeLevels(levels.getStringSet(KEY_CLAIMED_LEVELS, emptySet())),
            purchasedFrameKeys = engagement.getStringSet(KEY_PURCHASED_FRAMES, emptySet())?.toSet().orEmpty(),
            purchasedAvatarKeys = engagement.getStringSet(KEY_PURCHASED_AVATARS, emptySet())?.toSet().orEmpty(),
            purchasedBannerKeys = engagement.getStringSet(KEY_PURCHASED_BANNERS, emptySet())?.toSet().orEmpty()
        )
    }

    fun apply(context: Context, state: EconomyState) {
        val appContext = context.applicationContext
        val engagement = appContext.getSharedPreferences(ENGAGEMENT_PREFS, Context.MODE_PRIVATE)
        engagement.edit()
            .putInt(KEY_COINS, state.coins.coerceAtLeast(0))
            .putNullableString(KEY_LOGIN_DAY, state.loginDay)
            .putInt(KEY_LOGIN_STREAK, state.loginStreak.coerceAtLeast(0))
            .putInt(KEY_LOGIN_REWARD, state.loginRewardToday.coerceAtLeast(0))
            .putNullableString(KEY_BASELINE_DAY, state.baselineDay)
            .putInt(KEY_BASELINE_ANSWERED, state.baselineAnswered.coerceAtLeast(0))
            .putInt(KEY_BASELINE_CORRECT, state.baselineCorrect.coerceAtLeast(0))
            .putInt(KEY_BASELINE_XP, state.baselineXp.coerceAtLeast(0))
            .putStringSet(KEY_CLAIMED_MISSIONS, state.claimedMissionIds)
            .putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, state.unlockedAchievementIds)
            .putStringSet(KEY_PURCHASED_FRAMES, state.purchasedFrameKeys)
            .putStringSet(KEY_PURCHASED_AVATARS, state.purchasedAvatarKeys)
            .putStringSet(KEY_PURCHASED_BANNERS, state.purchasedBannerKeys)
            .commit()

        val levelPrefs = appContext.getSharedPreferences(LEVEL_PREFS, Context.MODE_PRIVATE)
        val claimed = state.claimedLevels
        val pending = decodeLevels(levelPrefs.getStringSet(KEY_PENDING_LEVELS, emptySet())) - claimed
        val unseen = decodeLevels(levelPrefs.getStringSet(KEY_UNSEEN_POPUPS, emptySet())) - claimed
        levelPrefs.edit()
            .putStringSet(KEY_CLAIMED_LEVELS, encodeLevels(claimed))
            .putStringSet(KEY_PENDING_LEVELS, encodeLevels(pending))
            .putStringSet(KEY_UNSEEN_POPUPS, encodeLevels(unseen))
            .commit()
    }

    private fun android.content.SharedPreferences.Editor.putNullableString(
        key: String,
        value: String?
    ): android.content.SharedPreferences.Editor = if (value == null) remove(key) else putString(key, value)

    private fun encodeLevels(levels: Set<Int>): Set<String> = levels
        .asSequence()
        .filter { it in 1..30 }
        .map(Int::toString)
        .toSet()

    private fun decodeLevels(values: Set<String>?): Set<Int> = values.orEmpty()
        .mapNotNull(String::toIntOrNull)
        .filter { it in 1..30 }
        .toSet()
}
