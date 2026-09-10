package com.example.cyberquiz.engagement

import android.content.Context
import com.example.cyberquiz.data.database.QuestionEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.random.Random

internal const val DAILY_CHALLENGE_SIZE = 5

internal data class DailyChallengeSnapshot(
    val day: String,
    val completedToday: Boolean,
    val streak: Int,
    val bestStreak: Int,
    val completedCount: Int,
    val badges: Set<String>,
    val lastScore: Int,
    val lastBonusXp: Int
)

internal data class DailyChallengeCompletion(
    val newlyCompleted: Boolean,
    val bonusXp: Int,
    val snapshot: DailyChallengeSnapshot,
    val newBadges: Set<String>
)

internal object DailyChallengeStore {
    private const val PREFS = "cyberquiz_daily_challenge"
    private const val KEY_LAST_DAY = "last_day"
    private const val KEY_STREAK = "streak"
    private const val KEY_BEST_STREAK = "best_streak"
    private const val KEY_COMPLETED_COUNT = "completed_count"
    private const val KEY_BADGES = "badges"
    private const val KEY_LAST_SCORE = "last_score"
    private const val KEY_LAST_BONUS_XP = "last_bonus_xp"

    private const val SESSION_PREFS = "cyberquiz_quiz_session"
    private const val KEY_ACTIVE_SESSION_IDS = "active_session_ids"
    private const val FIELD_DAILY_DAY = "daily_day"

    private val milestones = linkedMapOf(
        2 to "daily_2",
        3 to "daily_3",
        7 to "daily_7",
        30 to "daily_30"
    )
    private val milestoneBonusXp = mapOf(2 to 5, 3 to 5, 7 to 15, 30 to 40)

    fun today(): String = LocalDate.now().toString()

    fun snapshot(context: Context, day: String = today()): DailyChallengeSnapshot {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return DailyChallengeSnapshot(
            day = day,
            completedToday = prefs.getString(KEY_LAST_DAY, null) == day,
            streak = prefs.getInt(KEY_STREAK, 0).coerceAtLeast(0),
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0).coerceAtLeast(0),
            completedCount = prefs.getInt(KEY_COMPLETED_COUNT, 0).coerceAtLeast(0),
            badges = prefs.getStringSet(KEY_BADGES, emptySet())?.toSet().orEmpty(),
            lastScore = prefs.getInt(KEY_LAST_SCORE, 0).coerceAtLeast(0),
            lastBonusXp = prefs.getInt(KEY_LAST_BONUS_XP, 0).coerceAtLeast(0)
        )
    }

    /** Returns today's persisted challenge session, if the player left it unfinished. */
    fun activeSessionId(context: Context, day: String = today()): String? {
        val prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVE_SESSION_IDS, "")
            .orEmpty()
            .split(',')
            .asSequence()
            .filter(String::isNotBlank)
            .firstOrNull { id ->
                prefs.getString("session_${id}_$FIELD_DAILY_DAY", null) == day
            }
    }

    fun complete(
        context: Context,
        day: String,
        score: Int,
        total: Int = DAILY_CHALLENGE_SIZE
    ): DailyChallengeCompletion {
        val existing = snapshot(context, day)
        // A challenge only counts after all five daily questions have been answered.
        // This also protects against a truncated queue after a data/update issue.
        if (total < DAILY_CHALLENGE_SIZE) {
            return DailyChallengeCompletion(false, 0, existing, emptySet())
        }

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (existing.completedToday) {
            return DailyChallengeCompletion(false, 0, existing, emptySet())
        }

        val previousDay = prefs.getString(KEY_LAST_DAY, null)
        val previousStreak = prefs.getInt(KEY_STREAK, 0).coerceAtLeast(0)
        val newStreak = nextStreak(previousStreak, previousDay, day)
        val oldBadges = prefs.getStringSet(KEY_BADGES, emptySet())?.toSet().orEmpty()
        val unlocked = milestones
            .filterKeys { newStreak >= it }
            .values
            .toSet()
        val newBadges = unlocked - oldBadges
        val milestoneBonus = milestones.entries
            .filter { it.value in newBadges }
            .sumOf { milestoneBonusXp[it.key] ?: 0 }
        val scoreBonus = if (score.coerceIn(0, total) == total) 5 else 0
        val bonusXp = 5 + milestoneBonus + scoreBonus
        val best = max(prefs.getInt(KEY_BEST_STREAK, 0), newStreak)
        val completedCount = prefs.getInt(KEY_COMPLETED_COUNT, 0).coerceAtLeast(0) + 1

        prefs.edit()
            .putString(KEY_LAST_DAY, day)
            .putInt(KEY_STREAK, newStreak)
            .putInt(KEY_BEST_STREAK, best)
            .putInt(KEY_COMPLETED_COUNT, completedCount)
            .putStringSet(KEY_BADGES, oldBadges + unlocked)
            .putInt(KEY_LAST_SCORE, score.coerceIn(0, total))
            .putInt(KEY_LAST_BONUS_XP, bonusXp)
            .commit()

        return DailyChallengeCompletion(
            newlyCompleted = true,
            bonusXp = bonusXp,
            snapshot = snapshot(context, day),
            newBadges = newBadges
        )
    }

    fun badgeLabel(id: String): String = when (id) {
        "daily_2" -> "2 jours"
        "daily_3" -> "3 jours"
        "daily_7" -> "7 jours"
        "daily_30" -> "30 jours"
        else -> id
    }

    internal fun nextStreak(previousStreak: Int, previousDay: String?, currentDay: String): Int {
        if (previousDay == null) return 1
        val previous = runCatching { LocalDate.parse(previousDay) }.getOrNull() ?: return 1
        val current = runCatching { LocalDate.parse(currentDay) }.getOrNull() ?: return 1
        val gap = ChronoUnit.DAYS.between(previous, current)
        return when {
            gap <= 0L -> previousStreak.coerceAtLeast(1)
            gap == 1L -> previousStreak.coerceAtLeast(0) + 1
            // One missed calendar day is protected: the series pauses instead of resetting.
            gap == 2L -> previousStreak.coerceAtLeast(1)
            // Longer absences decay progressively rather than destroying the series at once.
            else -> max(1, previousStreak.coerceAtLeast(1) - (gap.toInt() - 2))
        }
    }

    internal fun selectQuestionIds(
        questions: List<QuestionEntity>,
        day: String,
        count: Int = DAILY_CHALLENGE_SIZE
    ): List<Long> {
        if (questions.isEmpty() || count <= 0) return emptyList()
        val random = Random(day.hashCode())
        val groups = questions
            .sortedBy { it.id }
            .groupBy { it.category }
            .mapValues { (_, values) -> values.shuffled(random).toMutableList() }
            .toMutableMap()
        val categoryOrder = groups.keys.sorted().shuffled(random)
        val selected = mutableListOf<Long>()
        var round = 0
        while (selected.size < count && groups.values.any { it.isNotEmpty() }) {
            for (category in categoryOrder) {
                val bucket = groups[category] ?: continue
                if (bucket.isEmpty()) continue
                val index = (round % bucket.size).coerceAtMost(bucket.lastIndex)
                selected += bucket.removeAt(index).id
                if (selected.size >= count) break
            }
            round++
        }
        return selected
    }
}
