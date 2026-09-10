package com.example.cyberquiz.model

import com.example.cyberquiz.data.database.ReviewItemEntity
import kotlin.math.max

internal const val ADAPTIVE_REVIEW_DAILY_TARGET = 5
private const val MINUTE_MS = 60_000L
private const val DAY_MS = 24L * 60L * MINUTE_MS
private const val MAX_REVIEW_STAGE = 6

internal data class AdaptiveReviewDecision(
    val stage: Int,
    val nextReviewAt: Long
)

internal object AdaptiveReviewEngine {
    private val stageIntervalsDays = listOf(0, 1, 2, 4, 7, 14, 30)

    /**
     * Correct answers increase the spacing interval. Fast, confident answers may
     * advance one extra stage, while very slow answers stay one stage closer.
     * Repeated previous errors reduce the effective stage so fragile knowledge
     * comes back sooner. A wrong answer returns the item to the first stage.
     */
    fun schedule(
        previousStage: Int,
        correct: Boolean,
        wrongCount: Int,
        responseMs: Long,
        now: Long
    ): AdaptiveReviewDecision {
        val safeResponse = responseMs.coerceIn(0L, 120_000L)
        if (!correct) {
            return AdaptiveReviewDecision(stage = 0, nextReviewAt = now)
        }

        val speedAdjustment = when {
            safeResponse in 1..7_500L -> 1
            safeResponse > 25_000L -> -1
            else -> 0
        }
        val lapsePenalty = (wrongCount.coerceAtLeast(0) / 3).coerceAtMost(2)
        val stage = (previousStage.coerceIn(0, MAX_REVIEW_STAGE) + 1 + speedAdjustment - lapsePenalty)
            .coerceIn(1, MAX_REVIEW_STAGE)
        val intervalDays = stageIntervalsDays[stage]
        return AdaptiveReviewDecision(
            stage = stage,
            nextReviewAt = now + intervalDays * DAY_MS
        )
    }

    fun effectiveDueAt(item: ReviewItemEntity): Long {
        if (!item.mastered) return 0L
        if (item.nextReviewAt > 0L) return item.nextReviewAt
        // Legacy mastered items did not have a spaced-repetition date. Give them
        // a conservative one-week interval from the last known error.
        return if (item.lastWrongAt > 0L) item.lastWrongAt + 7L * DAY_MS else 0L
    }

    fun isDue(item: ReviewItemEntity, now: Long = System.currentTimeMillis()): Boolean =
        !item.mastered || effectiveDueAt(item) <= now

    /** Higher is more urgent. */
    fun priority(item: ReviewItemEntity, now: Long = System.currentTimeMillis()): Double {
        val dueAt = effectiveDueAt(item)
        val overdueHours = if (dueAt <= 0L || now <= dueAt) 0.0 else {
            ((now - dueAt).toDouble() / (60.0 * MINUTE_MS)).coerceAtMost(240.0)
        }
        val attempts = item.reviewAttempts.coerceAtLeast(1)
        val averageResponseMs = item.totalReviewResponseMs.toDouble() / attempts
        val slowPenalty = when {
            averageResponseMs > 30_000.0 -> 18.0
            averageResponseMs > 20_000.0 -> 10.0
            averageResponseMs > 12_000.0 -> 4.0
            else -> 0.0
        }
        val recoveryAttempts = item.correctAfterWrongCount.coerceAtLeast(0)
        val fragility = item.wrongCount.toDouble() / max(1, item.wrongCount + recoveryAttempts)
        return item.wrongCount.coerceAtLeast(0) * 12.0 +
            fragility * 30.0 +
            slowPenalty +
            overdueHours * 0.25 -
            item.reviewStage.coerceIn(0, MAX_REVIEW_STAGE) * 3.0
    }

    fun rankDue(
        items: List<ReviewItemEntity>,
        now: Long = System.currentTimeMillis(),
        excludedQuestionIds: Set<Long> = emptySet()
    ): List<ReviewItemEntity> = items
        .asSequence()
        .filter { it.questionId !in excludedQuestionIds }
        .filter { isDue(it, now) }
        .sortedByDescending { priority(it, now) }
        .toList()

    fun dueCount(items: List<ReviewItemEntity>, now: Long = System.currentTimeMillis()): Int =
        items.count { isDue(it, now) }
}
