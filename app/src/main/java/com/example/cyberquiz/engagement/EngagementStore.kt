package com.example.cyberquiz.engagement

import android.content.Context
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.model.achievementDefinitions
import com.example.cyberquiz.model.dailyMissionDefinitions
import com.example.cyberquiz.model.dailyMissionProgress
import com.example.cyberquiz.model.loginRewardForStreak
import com.example.cyberquiz.model.newlyUnlockedAchievementIds
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class EngagementSnapshot(
    val coins: Int,
    val loginStreak: Int,
    val loginRewardToday: Int,
    val missions: List<com.example.cyberquiz.model.DailyMissionProgress>,
    val claimedMissionIds: Set<String>,
    val unlockedAchievementIds: Set<String>,
    val purchasedFrameKeys: Set<String>,
    val purchasedAvatarKeys: Set<String>,
    val purchasedBannerKeys: Set<String>
)

object EngagementStore {
    private const val PREFS = "cyberquiz_engagement"
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

    fun recordDailyLogin(context: Context, nowMillis: Long = System.currentTimeMillis()): Int {
        val prefs = prefs(context)
        val today = dayKey(nowMillis)
        if (prefs.getString(KEY_LOGIN_DAY, null) == today) {
            return prefs.getInt(KEY_LOGIN_REWARD, 0)
        }

        val previous = previousDayKey(nowMillis)
        val lastDay = prefs.getString(KEY_LOGIN_DAY, null)
        val streak = if (lastDay == previous) prefs.getInt(KEY_LOGIN_STREAK, 0) + 1 else 1
        val reward = loginRewardForStreak(streak)
        val coins = prefs.getInt(KEY_COINS, 0) + reward

        prefs.edit()
            .putString(KEY_LOGIN_DAY, today)
            .putInt(KEY_LOGIN_STREAK, streak)
            .putInt(KEY_LOGIN_REWARD, reward)
            .putInt(KEY_COINS, coins)
            .commit()
        return reward
    }

    fun sync(
        context: Context,
        metrics: EngagementMetrics,
        nowMillis: Long = System.currentTimeMillis()
    ): EngagementSnapshot {
        recordDailyLogin(context, nowMillis)
        val prefs = prefs(context)
        val today = dayKey(nowMillis)

        if (prefs.getString(KEY_BASELINE_DAY, null) != today) {
            prefs.edit()
                .putString(KEY_BASELINE_DAY, today)
                .putInt(KEY_BASELINE_ANSWERED, metrics.answered)
                .putInt(KEY_BASELINE_CORRECT, metrics.correct)
                .putInt(KEY_BASELINE_XP, metrics.xp)
                .putStringSet(KEY_CLAIMED_MISSIONS, emptySet())
                .commit()
        }

        val baseline = EngagementMetrics(
            answered = prefs.getInt(KEY_BASELINE_ANSWERED, metrics.answered),
            correct = prefs.getInt(KEY_BASELINE_CORRECT, metrics.correct),
            xp = prefs.getInt(KEY_BASELINE_XP, metrics.xp),
            level = metrics.level,
            streak = 0,
            bestStreak = 0,
            quizCount = 0
        )

        var coins = prefs.getInt(KEY_COINS, 0)
        val unlocked = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet())?.toSet().orEmpty()
        val newlyUnlocked = newlyUnlockedAchievementIds(metrics, unlocked)
        if (newlyUnlocked.isNotEmpty()) {
            coins += achievementDefinitions
                .filter { it.id in newlyUnlocked }
                .sumOf { it.rewardCoins }
        }
        val updatedUnlocked = unlocked + newlyUnlocked

        val claimed = prefs.getStringSet(KEY_CLAIMED_MISSIONS, emptySet())?.toSet().orEmpty()
        val missionProgress = dailyMissionProgress(metrics, baseline)
        val newlyClaimed = missionProgress
            .filter { it.completed && it.definition.id !in claimed }
            .map { it.definition.id }
            .toSet()
        if (newlyClaimed.isNotEmpty()) {
            coins += dailyMissionDefinitions
                .filter { it.id in newlyClaimed }
                .sumOf { it.rewardCoins }
        }
        val updatedClaimed = claimed + newlyClaimed

        prefs.edit()
            .putInt(KEY_COINS, coins)
            .putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, updatedUnlocked)
            .putStringSet(KEY_CLAIMED_MISSIONS, updatedClaimed)
            .commit()

        return snapshotFromPrefs(context, metrics, baseline)
    }

    fun snapshot(
        context: Context,
        metrics: EngagementMetrics,
        nowMillis: Long = System.currentTimeMillis()
    ): EngagementSnapshot {
        recordDailyLogin(context, nowMillis)
        val prefs = prefs(context)
        val today = dayKey(nowMillis)
        val baseline = if (prefs.getString(KEY_BASELINE_DAY, null) == today) {
            EngagementMetrics(
                answered = prefs.getInt(KEY_BASELINE_ANSWERED, metrics.answered),
                correct = prefs.getInt(KEY_BASELINE_CORRECT, metrics.correct),
                xp = prefs.getInt(KEY_BASELINE_XP, metrics.xp),
                level = metrics.level,
                streak = 0,
                bestStreak = 0,
                quizCount = 0
            )
        } else {
            metrics
        }
        return snapshotFromPrefs(context, metrics, baseline)
    }

    fun currentCoins(context: Context): Int = prefs(context).getInt(KEY_COINS, 0)

    fun grantCoins(context: Context, amount: Int): Int {
        if (amount <= 0) return currentCoins(context)
        val prefs = prefs(context)
        val updated = prefs.getInt(KEY_COINS, 0) + amount
        prefs.edit().putInt(KEY_COINS, updated).commit()
        return updated
    }

    fun unlockedAchievementIds(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet())?.toSet().orEmpty()

    fun spendCoins(context: Context, amount: Int): Boolean {
        if (amount <= 0) return true
        val prefs = prefs(context)
        val current = prefs.getInt(KEY_COINS, 0)
        if (current < amount) return false
        return prefs.edit().putInt(KEY_COINS, current - amount).commit()
    }

    private fun purchaseKey(context: Context, storageKey: String, cost: Int, setKey: String): Boolean {
        val prefs = prefs(context)
        val purchased = prefs.getStringSet(setKey, emptySet())?.toSet().orEmpty()
        if (storageKey in purchased) return true
        if (!spendCoins(context, cost)) return false
        return prefs.edit().putStringSet(setKey, purchased + storageKey).commit()
    }

    fun purchaseFrame(context: Context, frameKey: String, cost: Int): Boolean =
        purchaseKey(context, frameKey, cost, KEY_PURCHASED_FRAMES)

    fun purchaseAvatar(context: Context, avatarKey: String, cost: Int): Boolean =
        purchaseKey(context, avatarKey, cost, KEY_PURCHASED_AVATARS)

    fun purchaseBanner(context: Context, bannerKey: String, cost: Int): Boolean =
        purchaseKey(context, bannerKey, cost, KEY_PURCHASED_BANNERS)

    fun purchasedFrameKeys(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PURCHASED_FRAMES, emptySet())?.toSet().orEmpty()

    fun purchasedAvatarKeys(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PURCHASED_AVATARS, emptySet())?.toSet().orEmpty()

    fun purchasedBannerKeys(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PURCHASED_BANNERS, emptySet())?.toSet().orEmpty()

    private fun snapshotFromPrefs(
        context: Context,
        metrics: EngagementMetrics,
        baseline: EngagementMetrics
    ): EngagementSnapshot {
        val prefs = prefs(context)
        return EngagementSnapshot(
            coins = prefs.getInt(KEY_COINS, 0),
            loginStreak = prefs.getInt(KEY_LOGIN_STREAK, 0),
            loginRewardToday = prefs.getInt(KEY_LOGIN_REWARD, 0),
            missions = dailyMissionProgress(metrics, baseline),
            claimedMissionIds = prefs.getStringSet(KEY_CLAIMED_MISSIONS, emptySet())?.toSet().orEmpty(),
            unlockedAchievementIds = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet())?.toSet().orEmpty(),
            purchasedFrameKeys = prefs.getStringSet(KEY_PURCHASED_FRAMES, emptySet())?.toSet().orEmpty(),
            purchasedAvatarKeys = prefs.getStringSet(KEY_PURCHASED_AVATARS, emptySet())?.toSet().orEmpty(),
            purchasedBannerKeys = prefs.getStringSet(KEY_PURCHASED_BANNERS, emptySet())?.toSet().orEmpty()
        )
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun dayKey(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    private fun previousDayKey(millis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.DATE, -1)
        }
        return dayKey(calendar.timeInMillis)
    }
}