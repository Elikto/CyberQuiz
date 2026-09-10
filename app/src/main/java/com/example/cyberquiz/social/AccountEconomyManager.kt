package com.example.cyberquiz.social

import android.content.Context
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.repository.QuizHistoryStore
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.engagement.LevelRewardStore
import com.example.cyberquiz.model.EngagementMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Bridges the legacy offline economy with the authenticated server economy.
 *
 * Guests keep the current fully-local behavior. Signed-in players use the server
 * as the authority for every spend/claim operation. Local taps may be reflected
 * immediately by the legacy stores, but the matching operation is persisted and
 * replayed idempotently on the server so two devices cannot double claim/spend.
 */
internal object AccountEconomyManager {
    private const val ENGAGEMENT_PREFS = "cyberquiz_engagement"
    private const val LEVEL_PREFS = "cyberquiz_level_rewards"
    private const val PENDING_PREFS = "cyberquiz_economy_pending"
    private const val KEY_PENDING_OPERATIONS = "operations"

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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    fun request(context: Context) {
        val appContext = context.applicationContext
        scope.launch { runCatching { syncCurrentSession(appContext) } }
    }

    fun queueMissionClaim(context: Context, missionId: String) {
        if (SocialTokenStore.load(context).isNullOrBlank()) return
        enqueue(context, "mission:$missionId")
    }

    fun queueLevelClaim(context: Context, level: Int) {
        if (SocialTokenStore.load(context).isNullOrBlank()) return
        enqueue(context, "level:${level.coerceIn(1, 30)}")
    }

    fun queuePurchase(context: Context, kind: String, storageKey: String) {
        if (SocialTokenStore.load(context).isNullOrBlank()) return
        if (kind !in setOf("frame", "avatar", "banner")) return
        enqueue(context, "purchase:$kind:$storageKey")
    }

    suspend fun syncCurrentSession(context: Context): EconomyState? {
        val appContext = context.applicationContext
        val token = SocialTokenStore.load(appContext) ?: return null
        return mutex.withLock {
            val metrics = metrics(appContext)
            var state = EconomyApiClient.sync(token, metrics, seed(appContext))
            apply(appContext, state)
            state = drainPending(appContext, token, state)
            state
        }
    }

    suspend fun claimMission(context: Context, missionId: String): Int {
        val appContext = context.applicationContext
        val metrics = metrics(appContext)
        val token = SocialTokenStore.load(appContext)
        if (token.isNullOrBlank()) {
            return EngagementStore.claimMission(appContext, missionId, metrics)
        }
        val before = EngagementStore.currentCoins(appContext)
        val state = mutex.withLock {
            val synced = EconomyApiClient.sync(token, metrics, seed(appContext))
            apply(appContext, synced)
            val claimed = EconomyApiClient.claimMission(token, missionId, metrics, seed(appContext))
            apply(appContext, claimed)
            removePending(appContext, "mission:$missionId")
            claimed
        }
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
        val state = mutex.withLock {
            val synced = EconomyApiClient.sync(token, metrics, seed(appContext))
            apply(appContext, synced)
            val claimed = EconomyApiClient.claimLevel(token, level, metrics, seed(appContext))
            apply(appContext, claimed)
            removePending(appContext, "level:$level")
            claimed
        }
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
        val state = mutex.withLock {
            val synced = EconomyApiClient.sync(token, metrics, seed(appContext))
            apply(appContext, synced)
            val purchased = EconomyApiClient.purchase(token, kind, storageKey, metrics, seed(appContext))
            apply(appContext, purchased)
            removePending(appContext, "purchase:$kind:$storageKey")
            purchased
        }
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

    private suspend fun drainPending(
        context: Context,
        token: String,
        initial: EconomyState
    ): EconomyState {
        var state = initial
        val operations = pending(context).sorted()
        for (operation in operations) {
            val metrics = metrics(context)
            try {
                state = when {
                    operation.startsWith("mission:") -> EconomyApiClient.claimMission(
                        token,
                        operation.removePrefix("mission:"),
                        metrics,
                        seed(context)
                    )
                    operation.startsWith("level:") -> EconomyApiClient.claimLevel(
                        token,
                        operation.removePrefix("level:").toIntOrNull() ?: continue,
                        metrics,
                        seed(context)
                    )
                    operation.startsWith("purchase:") -> {
                        val parts = operation.split(':', limit = 3)
                        if (parts.size != 3) continue
                        EconomyApiClient.purchase(token, parts[1], parts[2], metrics, seed(context))
                    }
                    else -> {
                        removePending(context, operation)
                        continue
                    }
                }
                apply(context, state)
                removePending(context, operation)
            } catch (error: SocialApiException) {
                if (error.statusCode == 401) throw error
                if (operation.startsWith("purchase:") && error.statusCode in setOf(404, 409)) {
                    removePending(context, operation)
                    state = EconomyApiClient.sync(token, metrics, seed(context))
                    apply(context, state)
                }
            }
        }
        return state
    }

    private fun enqueue(context: Context, operation: String) {
        val prefs = context.applicationContext.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PENDING_OPERATIONS, emptySet())?.toSet().orEmpty()
        prefs.edit().putStringSet(KEY_PENDING_OPERATIONS, current + operation).commit()
        request(context)
    }

    private fun pending(context: Context): Set<String> =
        context.applicationContext.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_PENDING_OPERATIONS, emptySet())?.toSet().orEmpty()

    private fun removePending(context: Context, operation: String) {
        val prefs = context.applicationContext.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PENDING_OPERATIONS, emptySet())?.toSet().orEmpty()
        if (operation in current) {
            prefs.edit().putStringSet(KEY_PENDING_OPERATIONS, current - operation).commit()
        }
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
