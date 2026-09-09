package com.example.cyberquiz.engagement

import android.content.Context

private const val MAX_REWARD_LEVEL = 30

data class LevelRewardState(
    val pendingLevels: Set<Int>,
    val claimedLevels: Set<Int>,
    val unseenPopupLevels: Set<Int>
) {
    val hasPendingReward: Boolean
        get() = pendingLevels.isNotEmpty()

    val nextPopupLevel: Int?
        get() = unseenPopupLevels.minOrNull()
}

object LevelRewardStore {
    private const val PREFS = "cyberquiz_level_rewards"
    private const val KEY_INITIALIZED = "initialized"
    private const val KEY_LAST_LEVEL = "last_level"
    private const val KEY_PENDING_LEVELS = "pending_levels"
    private const val KEY_CLAIMED_LEVELS = "claimed_levels"
    private const val KEY_UNSEEN_POPUPS = "unseen_popups"

    fun sync(context: Context, currentLevel: Int): LevelRewardState {
        val prefs = prefs(context)
        val safeLevel = currentLevel.coerceIn(1, MAX_REWARD_LEVEL)

        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            // Existing players keep all lower-level rewards without being flooded by legacy pop-ups.
            // Their current level becomes the first chest to discover with the new system.
            val claimed = (1 until safeLevel).toSet()
            prefs.edit()
                .putBoolean(KEY_INITIALIZED, true)
                .putInt(KEY_LAST_LEVEL, safeLevel)
                .putStringSet(KEY_CLAIMED_LEVELS, encode(claimed))
                .putStringSet(KEY_PENDING_LEVELS, setOf(safeLevel.toString()))
                .putStringSet(KEY_UNSEEN_POPUPS, setOf(safeLevel.toString()))
                .commit()
            return snapshot(context)
        }

        val lastLevel = prefs.getInt(KEY_LAST_LEVEL, safeLevel).coerceIn(1, MAX_REWARD_LEVEL)
        if (safeLevel > lastLevel) {
            val newlyReached = ((lastLevel + 1)..safeLevel).toSet()
            val pending = decode(prefs.getStringSet(KEY_PENDING_LEVELS, emptySet())) + newlyReached
            val unseen = decode(prefs.getStringSet(KEY_UNSEEN_POPUPS, emptySet())) + newlyReached
            prefs.edit()
                .putInt(KEY_LAST_LEVEL, safeLevel)
                .putStringSet(KEY_PENDING_LEVELS, encode(pending))
                .putStringSet(KEY_UNSEEN_POPUPS, encode(unseen))
                .commit()
        }
        return snapshot(context)
    }

    fun snapshot(context: Context): LevelRewardState {
        val prefs = prefs(context)
        return LevelRewardState(
            pendingLevels = decode(prefs.getStringSet(KEY_PENDING_LEVELS, emptySet())),
            claimedLevels = decode(prefs.getStringSet(KEY_CLAIMED_LEVELS, emptySet())),
            unseenPopupLevels = decode(prefs.getStringSet(KEY_UNSEEN_POPUPS, emptySet()))
        )
    }

    fun acknowledgePopup(context: Context, level: Int): LevelRewardState {
        val prefs = prefs(context)
        val unseen = decode(prefs.getStringSet(KEY_UNSEEN_POPUPS, emptySet())) - level
        prefs.edit().putStringSet(KEY_UNSEEN_POPUPS, encode(unseen)).commit()
        return snapshot(context)
    }

    fun claimLevel(context: Context, level: Int, currentLevel: Int): Int {
        val safeLevel = level.coerceIn(1, MAX_REWARD_LEVEL)
        if (safeLevel > currentLevel.coerceIn(1, MAX_REWARD_LEVEL)) return 0

        val prefs = prefs(context)
        val pending = decode(prefs.getStringSet(KEY_PENDING_LEVELS, emptySet()))
        if (safeLevel !in pending) return 0

        val claimed = decode(prefs.getStringSet(KEY_CLAIMED_LEVELS, emptySet())) + safeLevel
        val remainingPending = pending - safeLevel
        val unseen = decode(prefs.getStringSet(KEY_UNSEEN_POPUPS, emptySet())) - safeLevel

        // Mark the chest claimed first so repeated taps cannot duplicate rewards.
        val saved = prefs.edit()
            .putStringSet(KEY_CLAIMED_LEVELS, encode(claimed))
            .putStringSet(KEY_PENDING_LEVELS, encode(remainingPending))
            .putStringSet(KEY_UNSEEN_POPUPS, encode(unseen))
            .commit()
        if (!saved) return 0

        val coins = levelCoinReward(safeLevel)
        EngagementStore.grantCoins(context, coins)
        return coins
    }

    fun isClaimed(context: Context, level: Int): Boolean =
        level.coerceIn(1, MAX_REWARD_LEVEL) in snapshot(context).claimedLevels

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun encode(levels: Set<Int>): Set<String> = levels.map(Int::toString).toSet()

    private fun decode(values: Set<String>?): Set<Int> =
        values.orEmpty().mapNotNull(String::toIntOrNull).filter { it in 1..MAX_REWARD_LEVEL }.toSet()
}

internal fun levelCoinReward(level: Int): Int = when (level.coerceIn(1, MAX_REWARD_LEVEL)) {
    in 1..4 -> 10
    in 5..9 -> 15
    in 10..14 -> 20
    in 15..19 -> 25
    in 20..24 -> 30
    in 25..29 -> 40
    else -> 60
}

internal fun newlyReachedLevels(previousLevel: Int, currentLevel: Int): List<Int> {
    val previous = previousLevel.coerceIn(1, MAX_REWARD_LEVEL)
    val current = currentLevel.coerceIn(1, MAX_REWARD_LEVEL)
    if (current <= previous) return emptyList()
    return ((previous + 1)..current).toList()
}
