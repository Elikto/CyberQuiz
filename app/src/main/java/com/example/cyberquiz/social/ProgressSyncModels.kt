package com.example.cyberquiz.social

internal const val CLOUD_PROGRESS_VERSION = 1
internal const val CLOUD_HISTORY_LIMIT = 25

internal data class CloudQuizProgress(
    val quizType: String,
    val xp: Int,
    val level: Int,
    val answered: Int,
    val correct: Int,
    val streak: Int,
    val bestStreak: Int,
    val totalResponseMs: Long
)

internal data class CloudCategoryProgress(
    val quizType: String,
    val category: String,
    val answered: Int,
    val correct: Int,
    val lastAnsweredAt: Long
)

internal data class CloudConceptProgress(
    val quizType: String,
    val concept: String,
    val category: String,
    val attempts: Int,
    val correct: Int,
    val reviewMastered: Boolean,
    val lastResultCorrect: Boolean,
    val lastAnsweredAt: Long
)

internal data class CloudReviewItem(
    val quizType: String,
    val concept: String,
    val category: String,
    val difficulty: String,
    val questionId: Long,
    val question: String,
    val correctAnswer: String,
    val wrongCount: Int,
    val correctAfterWrongCount: Int,
    val mastered: Boolean,
    val lastWrongAt: Long,
    val reviewStage: Int = 0,
    val nextReviewAt: Long = 0L,
    val lastReviewedAt: Long = 0L,
    val reviewAttempts: Int = 0,
    val totalReviewResponseMs: Long = 0L
)

internal data class CloudHistoryQuestion(
    val questionId: Long,
    val category: String,
    val difficulty: String,
    val question: String,
    val answers: List<String>,
    val correctIndex: Int,
    val selectedIndex: Int,
    val explanation: String
)

internal data class CloudHistoryEntry(
    val id: String,
    val mode: String,
    val categories: List<String>,
    val questionCount: Int,
    val startedAt: Long,
    val endedAt: Long,
    val answered: Int,
    val correct: Int,
    val xpGained: Int,
    val questions: List<CloudHistoryQuestion>
)

internal data class CloudEngagementOwnership(
    val unlockedAchievementIds: Set<String> = emptySet(),
    val purchasedFrameKeys: Set<String> = emptySet(),
    val purchasedAvatarKeys: Set<String> = emptySet(),
    val purchasedBannerKeys: Set<String> = emptySet()
)

internal data class CloudProgressSnapshot(
    val version: Int = CLOUD_PROGRESS_VERSION,
    val progress: List<CloudQuizProgress> = emptyList(),
    val categories: List<CloudCategoryProgress> = emptyList(),
    val concepts: List<CloudConceptProgress> = emptyList(),
    val reviews: List<CloudReviewItem> = emptyList(),
    val history: List<CloudHistoryEntry> = emptyList(),
    val engagement: CloudEngagementOwnership = CloudEngagementOwnership()
)

internal data class CloudProgressEnvelope(
    val revision: Long,
    val updatedAt: String?,
    val snapshot: CloudProgressSnapshot?
)

internal object ProgressSnapshotMerger {
    fun merge(local: CloudProgressSnapshot, remote: CloudProgressSnapshot): CloudProgressSnapshot =
        CloudProgressSnapshot(
            progress = mergeByKey(local.progress, remote.progress, { it.quizType }, ::mergeQuizProgress),
            categories = mergeByKey(
                local.categories,
                remote.categories,
                { "${it.quizType}\u0000${it.category}" },
                ::mergeCategoryProgress
            ),
            concepts = mergeByKey(
                local.concepts,
                remote.concepts,
                { "${it.quizType}\u0000${it.concept}" },
                ::mergeConceptProgress
            ),
            reviews = mergeByKey(
                local.reviews,
                remote.reviews,
                { "${it.quizType}\u0000${it.concept}" },
                ::mergeReviewItem
            ),
            history = (local.history + remote.history)
                .groupBy { it.id }
                .map { (_, entries) -> entries.maxByOrNull { it.endedAt }!! }
                .sortedByDescending { it.endedAt }
                .take(CLOUD_HISTORY_LIMIT),
            engagement = CloudEngagementOwnership(
                unlockedAchievementIds = local.engagement.unlockedAchievementIds + remote.engagement.unlockedAchievementIds,
                purchasedFrameKeys = local.engagement.purchasedFrameKeys + remote.engagement.purchasedFrameKeys,
                purchasedAvatarKeys = local.engagement.purchasedAvatarKeys + remote.engagement.purchasedAvatarKeys,
                purchasedBannerKeys = local.engagement.purchasedBannerKeys + remote.engagement.purchasedBannerKeys
            )
        )

    private fun mergeQuizProgress(a: CloudQuizProgress, b: CloudQuizProgress): CloudQuizProgress {
        val answered = maxOf(a.answered, b.answered).coerceAtLeast(0)
        val correct = maxOf(a.correct, b.correct).coerceIn(0, answered)
        return a.copy(
            xp = maxOf(a.xp, b.xp).coerceAtLeast(0),
            level = maxOf(a.level, b.level).coerceAtLeast(1),
            answered = answered,
            correct = correct,
            streak = maxOf(a.streak, b.streak).coerceAtLeast(0),
            bestStreak = maxOf(a.bestStreak, b.bestStreak, a.streak, b.streak).coerceAtLeast(0),
            totalResponseMs = maxOf(a.totalResponseMs, b.totalResponseMs).coerceAtLeast(0L)
        )
    }

    private fun mergeCategoryProgress(a: CloudCategoryProgress, b: CloudCategoryProgress): CloudCategoryProgress {
        val answered = maxOf(a.answered, b.answered).coerceAtLeast(0)
        return a.copy(
            answered = answered,
            correct = maxOf(a.correct, b.correct).coerceIn(0, answered),
            lastAnsweredAt = maxOf(a.lastAnsweredAt, b.lastAnsweredAt)
        )
    }

    private fun mergeConceptProgress(a: CloudConceptProgress, b: CloudConceptProgress): CloudConceptProgress {
        val latest = if (b.lastAnsweredAt > a.lastAnsweredAt) b else a
        val attempts = maxOf(a.attempts, b.attempts).coerceAtLeast(0)
        return latest.copy(
            attempts = attempts,
            correct = maxOf(a.correct, b.correct).coerceIn(0, attempts),
            lastAnsweredAt = maxOf(a.lastAnsweredAt, b.lastAnsweredAt)
        )
    }

    private fun mergeReviewItem(a: CloudReviewItem, b: CloudReviewItem): CloudReviewItem {
        val latestWrong = if (b.lastWrongAt > a.lastWrongAt) b else a
        val bestRecovery = when {
            b.correctAfterWrongCount > a.correctAfterWrongCount -> b
            a.correctAfterWrongCount > b.correctAfterWrongCount -> a
            else -> latestWrong
        }
        val latestReview = when {
            b.lastReviewedAt > a.lastReviewedAt -> b
            a.lastReviewedAt > b.lastReviewedAt -> a
            else -> bestRecovery
        }
        return latestWrong.copy(
            wrongCount = maxOf(a.wrongCount, b.wrongCount).coerceAtLeast(0),
            correctAfterWrongCount = maxOf(a.correctAfterWrongCount, b.correctAfterWrongCount).coerceAtLeast(0),
            mastered = latestReview.mastered,
            lastWrongAt = maxOf(a.lastWrongAt, b.lastWrongAt),
            reviewStage = latestReview.reviewStage.coerceAtLeast(0),
            nextReviewAt = latestReview.nextReviewAt.coerceAtLeast(0L),
            lastReviewedAt = maxOf(a.lastReviewedAt, b.lastReviewedAt).coerceAtLeast(0L),
            reviewAttempts = maxOf(a.reviewAttempts, b.reviewAttempts).coerceAtLeast(0),
            totalReviewResponseMs = maxOf(a.totalReviewResponseMs, b.totalReviewResponseMs).coerceAtLeast(0L)
        )
    }

    private fun <T, K> mergeByKey(
        local: List<T>,
        remote: List<T>,
        key: (T) -> K,
        merge: (T, T) -> T
    ): List<T> {
        val result = linkedMapOf<K, T>()
        local.forEach { item -> result[key(item)] = item }
        remote.forEach { item ->
            val itemKey = key(item)
            result[itemKey] = result[itemKey]?.let { previous -> merge(previous, item) } ?: item
        }
        return result.values.toList()
    }
}
