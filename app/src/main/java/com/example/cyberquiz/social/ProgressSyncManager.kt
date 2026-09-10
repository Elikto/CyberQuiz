package com.example.cyberquiz.social

import android.content.Context
import android.content.SharedPreferences
import androidx.room.InvalidationTracker
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity
import com.example.cyberquiz.data.database.CyberQuizDatabase
import com.example.cyberquiz.data.database.ProgressEntity
import com.example.cyberquiz.data.database.ReviewItemEntity
import com.example.cyberquiz.data.repository.QuizHistoryStore
import com.example.cyberquiz.model.QuizHistoryEntry
import com.example.cyberquiz.model.QuizHistoryQuestion
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object ProgressSyncManager {
    private const val STATE_PREFS = "cyberquiz_progress_sync"
    private const val STATE_ACCOUNT_ID = "account_id"
    private const val STATE_REVISION = "revision"
    private const val HISTORY_PREFS = "cyberquiz_quiz_history"
    private const val ENGAGEMENT_PREFS = "cyberquiz_engagement"
    private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
    private const val KEY_PURCHASED_FRAMES = "purchased_frames"
    private const val KEY_PURCHASED_AVATARS = "purchased_avatars"
    private const val KEY_PURCHASED_BANNERS = "purchased_banners"
    private const val CYBERSECURITY = "CYBERSECURITY"
    private const val NUTRITION = "NUTRITION"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()
    private val schedulingLock = Any()
    private var pendingJob: Job? = null
    private var rerunRequested = false
    private var initialized = false

    private var roomObserver: InvalidationTracker.Observer? = null
    private var historyListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private var engagementListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        synchronized(schedulingLock) {
            if (initialized) return
            initialized = true

            val database = CyberQuizDatabase.get(appContext)
            roomObserver = object : InvalidationTracker.Observer(
                "progress",
                "review_items",
                "category_progress",
                "concept_progress"
            ) {
                override fun onInvalidated(tables: Set<String>) {
                    request(appContext)
                }
            }.also(database.invalidationTracker::addObserver)

            historyListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                request(appContext)
            }.also { listener ->
                appContext.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
                    .registerOnSharedPreferenceChangeListener(listener)
            }

            engagementListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key in setOf(
                        KEY_UNLOCKED_ACHIEVEMENTS,
                        KEY_PURCHASED_FRAMES,
                        KEY_PURCHASED_AVATARS,
                        KEY_PURCHASED_BANNERS
                    )
                ) {
                    request(appContext)
                }
            }.also { listener ->
                appContext.getSharedPreferences(ENGAGEMENT_PREFS, Context.MODE_PRIVATE)
                    .registerOnSharedPreferenceChangeListener(listener)
            }
        }
        request(appContext, delayMs = 250L)
    }

    fun request(context: Context, delayMs: Long = 900L) {
        val appContext = context.applicationContext
        synchronized(schedulingLock) {
            if (pendingJob?.isActive == true) {
                rerunRequested = true
                return
            }
            rerunRequested = false
            pendingJob = scope.launch {
                delay(delayMs)
                while (true) {
                    runCatching { syncCurrentSession(appContext) }
                    val runAgain = synchronized(schedulingLock) {
                        if (rerunRequested) {
                            rerunRequested = false
                            true
                        } else {
                            pendingJob = null
                            false
                        }
                    }
                    if (!runAgain) break
                    delay(250L)
                }
            }
        }
    }

    suspend fun syncCurrentSession(context: Context) {
        val appContext = context.applicationContext
        val token = SocialTokenStore.load(appContext) ?: return
        syncMutex.withLock {
            val user = SocialApiClient.me(token)
            val state = appContext.getSharedPreferences(STATE_PREFS, Context.MODE_PRIVATE)
            val previousAccountId = state.getString(STATE_ACCOUNT_ID, null)
            val sameAccount = previousAccountId == user.id

            val local = buildLocalSnapshot(appContext)
            val remote = ProgressSyncApiClient.get(token)
            val target = when {
                remote.snapshot == null -> local
                !sameAccount -> remote.snapshot
                else -> ProgressSnapshotMerger.merge(local, remote.snapshot)
            }

            if (target != local) applySnapshot(appContext, target)

            val savedEnvelope = if (remote.snapshot == null || target != remote.snapshot) {
                putWithSingleConflictRetry(token, target, remote.revision)
            } else {
                remote
            }

            state.edit()
                .putString(STATE_ACCOUNT_ID, user.id)
                .putLong(STATE_REVISION, savedEnvelope.revision)
                .apply()
        }
    }

    private suspend fun putWithSingleConflictRetry(
        token: String,
        target: CloudProgressSnapshot,
        baseRevision: Long
    ): CloudProgressEnvelope {
        return try {
            ProgressSyncApiClient.put(token, baseRevision, target)
        } catch (error: SocialApiException) {
            if (error.statusCode != 409) throw error
            val latest = ProgressSyncApiClient.get(token)
            val latestSnapshot = latest.snapshot
            if (latestSnapshot == null) {
                ProgressSyncApiClient.put(token, latest.revision, target)
            } else {
                val merged = ProgressSnapshotMerger.merge(target, latestSnapshot)
                if (merged == latestSnapshot) latest
                else ProgressSyncApiClient.put(token, latest.revision, merged)
            }
        }
    }

    private suspend fun buildLocalSnapshot(context: Context): CloudProgressSnapshot {
        val dao = CyberQuizDatabase.get(context).quizDao()
        val quizTypes = listOf(CYBERSECURITY, NUTRITION)
        val progress = quizTypes.mapNotNull { quizType ->
            dao.progressSnapshot(quizType)?.let { item ->
                CloudQuizProgress(
                    quizType = item.quizType,
                    xp = item.xp,
                    level = item.level,
                    answered = item.answered,
                    correct = item.correct,
                    streak = item.streak,
                    bestStreak = item.bestStreak,
                    totalResponseMs = item.totalResponseMs
                )
            }
        }
        val categories = quizTypes.flatMap { quizType ->
            dao.categoryProgress(quizType).first().map { item ->
                CloudCategoryProgress(
                    quizType = item.quizType,
                    category = item.category,
                    answered = item.answered,
                    correct = item.correct,
                    lastAnsweredAt = item.lastAnsweredAt
                )
            }
        }
        val concepts = quizTypes.flatMap { quizType ->
            dao.conceptProgress(quizType).first().map { item ->
                CloudConceptProgress(
                    quizType = item.quizType,
                    concept = item.concept,
                    category = item.category,
                    attempts = item.attempts,
                    correct = item.correct,
                    reviewMastered = item.reviewMastered,
                    lastResultCorrect = item.lastResultCorrect,
                    lastAnsweredAt = item.lastAnsweredAt
                )
            }
        }
        val reviews = quizTypes.flatMap { quizType ->
            dao.reviewItems(quizType).first().map { item ->
                CloudReviewItem(
                    quizType = item.quizType,
                    concept = item.concept,
                    category = item.category,
                    difficulty = item.difficulty,
                    questionId = item.questionId,
                    question = item.question,
                    correctAnswer = item.correctAnswer,
                    wrongCount = item.wrongCount,
                    correctAfterWrongCount = item.correctAfterWrongCount,
                    mastered = item.mastered,
                    lastWrongAt = item.lastWrongAt,
                    reviewStage = item.reviewStage,
                    nextReviewAt = item.nextReviewAt,
                    lastReviewedAt = item.lastReviewedAt,
                    reviewAttempts = item.reviewAttempts,
                    totalReviewResponseMs = item.totalReviewResponseMs
                )
            }
        }
        val history = QuizHistoryStore(context).load().map(::toCloudHistory)
        val engagementPrefs = context.getSharedPreferences(ENGAGEMENT_PREFS, Context.MODE_PRIVATE)
        val engagement = CloudEngagementOwnership(
            unlockedAchievementIds = engagementPrefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet())?.toSet().orEmpty(),
            purchasedFrameKeys = engagementPrefs.getStringSet(KEY_PURCHASED_FRAMES, emptySet())?.toSet().orEmpty(),
            purchasedAvatarKeys = engagementPrefs.getStringSet(KEY_PURCHASED_AVATARS, emptySet())?.toSet().orEmpty(),
            purchasedBannerKeys = engagementPrefs.getStringSet(KEY_PURCHASED_BANNERS, emptySet())?.toSet().orEmpty()
        )
        return CloudProgressSnapshot(
            progress = progress.sortedBy { it.quizType },
            categories = categories.sortedWith(compareBy({ it.quizType }, { it.category })),
            concepts = concepts.sortedWith(compareBy({ it.quizType }, { it.concept })),
            reviews = reviews.sortedWith(compareBy({ it.quizType }, { it.concept })),
            history = history.sortedByDescending { it.endedAt }.take(CLOUD_HISTORY_LIMIT),
            engagement = engagement
        )
    }

    private suspend fun applySnapshot(context: Context, snapshot: CloudProgressSnapshot) {
        val dao = CyberQuizDatabase.get(context).quizDao()
        snapshot.progress.forEach { item ->
            val existing = dao.progressSnapshot(item.quizType) ?: return@forEach
            val answered = item.answered.coerceAtLeast(0)
            dao.replaceProgressForSync(
                ProgressEntity(
                    id = existing.id,
                    quizType = item.quizType,
                    xp = item.xp.coerceAtLeast(0),
                    level = item.level.coerceAtLeast(1),
                    answered = answered,
                    correct = item.correct.coerceIn(0, answered),
                    streak = item.streak.coerceAtLeast(0),
                    bestStreak = maxOf(item.bestStreak, item.streak).coerceAtLeast(0),
                    totalResponseMs = item.totalResponseMs.coerceAtLeast(0L)
                )
            )
        }
        snapshot.categories.forEach { item ->
            val answered = item.answered.coerceAtLeast(0)
            dao.replaceCategoryProgressForSync(
                CategoryProgressEntity(
                    quizType = item.quizType,
                    category = item.category,
                    answered = answered,
                    correct = item.correct.coerceIn(0, answered),
                    lastAnsweredAt = item.lastAnsweredAt.coerceAtLeast(0L)
                )
            )
        }
        snapshot.concepts.forEach { item ->
            val attempts = item.attempts.coerceAtLeast(0)
            dao.replaceConceptProgressForSync(
                ConceptProgressEntity(
                    quizType = item.quizType,
                    concept = item.concept,
                    category = item.category,
                    attempts = attempts,
                    correct = item.correct.coerceIn(0, attempts),
                    reviewMastered = item.reviewMastered,
                    lastResultCorrect = item.lastResultCorrect,
                    lastAnsweredAt = item.lastAnsweredAt.coerceAtLeast(0L)
                )
            )
        }
        snapshot.reviews.forEach { item ->
            val existing = dao.reviewItemSnapshot(item.quizType, item.concept)
            dao.replaceReviewItemForSync(
                ReviewItemEntity(
                    id = existing?.id ?: 0L,
                    quizType = item.quizType,
                    concept = item.concept,
                    category = item.category,
                    difficulty = item.difficulty,
                    questionId = item.questionId,
                    question = item.question,
                    correctAnswer = item.correctAnswer,
                    wrongCount = item.wrongCount.coerceAtLeast(0),
                    correctAfterWrongCount = item.correctAfterWrongCount.coerceAtLeast(0),
                    mastered = item.mastered,
                    lastWrongAt = item.lastWrongAt.coerceAtLeast(0L),
                    reviewStage = item.reviewStage.coerceIn(0, 6),
                    nextReviewAt = item.nextReviewAt.coerceAtLeast(0L),
                    lastReviewedAt = item.lastReviewedAt.coerceAtLeast(0L),
                    reviewAttempts = item.reviewAttempts.coerceAtLeast(0),
                    totalReviewResponseMs = item.totalReviewResponseMs.coerceAtLeast(0L)
                )
            )
        }

        val historyStore = QuizHistoryStore(context)
        snapshot.history.sortedBy { it.endedAt }.forEach { entry ->
            historyStore.add(toLocalHistory(entry))
        }

        // AccountEconomyManager, not the old progress envelope, owns purchases and coins.
    }

    private fun toCloudHistory(entry: QuizHistoryEntry) = CloudHistoryEntry(
        id = entry.id,
        mode = entry.config.mode.name,
        categories = entry.config.categories.sorted(),
        questionCount = entry.config.questionCount,
        timeLimitMinutes = entry.config.timeLimitMinutes,
        startedAt = entry.startedAt,
        endedAt = entry.endedAt,
        answered = entry.answered,
        correct = entry.correct,
        xpGained = entry.xpGained,
        questions = entry.questions.map { item ->
            CloudHistoryQuestion(
                questionId = item.questionId,
                category = item.category,
                difficulty = item.difficulty,
                question = item.question,
                answers = item.answers,
                correctIndex = item.correctIndex,
                selectedIndex = item.selectedIndex,
                explanation = item.explanation
            )
        }
    )

    private fun toLocalHistory(entry: CloudHistoryEntry) = QuizHistoryEntry(
        id = entry.id,
        config = QuizSessionConfig(
            mode = runCatching { QuizSessionMode.valueOf(entry.mode) }.getOrDefault(QuizSessionMode.RANDOM),
            categories = entry.categories.toSet(),
            questionCount = entry.questionCount,
            timeLimitMinutes = entry.timeLimitMinutes
        ),
        startedAt = entry.startedAt,
        endedAt = entry.endedAt,
        answered = entry.answered,
        correct = entry.correct,
        xpGained = entry.xpGained,
        questions = entry.questions.map { item ->
            QuizHistoryQuestion(
                questionId = item.questionId,
                category = item.category,
                difficulty = item.difficulty,
                question = item.question,
                answers = item.answers,
                correctIndex = item.correctIndex,
                selectedIndex = item.selectedIndex,
                explanation = item.explanation
            )
        }
    )
}
