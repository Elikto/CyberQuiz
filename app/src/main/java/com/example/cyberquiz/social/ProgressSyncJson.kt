package com.example.cyberquiz.social

import org.json.JSONArray
import org.json.JSONObject

internal object ProgressSyncJson {
    fun encode(snapshot: CloudProgressSnapshot): JSONObject = JSONObject().apply {
        put("version", snapshot.version)
        put("progress", JSONArray().apply { snapshot.progress.forEach { put(encodeProgress(it)) } })
        put("categories", JSONArray().apply { snapshot.categories.forEach { put(encodeCategory(it)) } })
        put("concepts", JSONArray().apply { snapshot.concepts.forEach { put(encodeConcept(it)) } })
        put("reviews", JSONArray().apply { snapshot.reviews.forEach { put(encodeReview(it)) } })
        put("history", JSONArray().apply { snapshot.history.forEach { put(encodeHistory(it)) } })
        put("engagement", JSONObject().apply {
            put("unlockedAchievementIds", snapshot.engagement.unlockedAchievementIds.toJsonArray())
            put("purchasedFrameKeys", snapshot.engagement.purchasedFrameKeys.toJsonArray())
            put("purchasedAvatarKeys", snapshot.engagement.purchasedAvatarKeys.toJsonArray())
            put("purchasedBannerKeys", snapshot.engagement.purchasedBannerKeys.toJsonArray())
        })
    }

    fun decode(json: JSONObject): CloudProgressSnapshot {
        require(json.optInt("version", -1) == CLOUD_PROGRESS_VERSION) { "Version de sauvegarde non prise en charge" }
        return CloudProgressSnapshot(
            progress = json.objects("progress").map(::decodeProgress),
            categories = json.objects("categories").map(::decodeCategory),
            concepts = json.objects("concepts").map(::decodeConcept),
            reviews = json.objects("reviews").map(::decodeReview),
            history = json.objects("history").map(::decodeHistory),
            engagement = decodeEngagement(json.optJSONObject("engagement") ?: JSONObject())
        )
    }

    private fun encodeProgress(item: CloudQuizProgress) = JSONObject().apply {
        put("quizType", item.quizType)
        put("xp", item.xp)
        put("level", item.level)
        put("answered", item.answered)
        put("correct", item.correct)
        put("streak", item.streak)
        put("bestStreak", item.bestStreak)
        put("totalResponseMs", item.totalResponseMs)
    }

    private fun decodeProgress(json: JSONObject) = CloudQuizProgress(
        quizType = json.getString("quizType"),
        xp = json.optInt("xp", 0).coerceAtLeast(0),
        level = json.optInt("level", 1).coerceAtLeast(1),
        answered = json.optInt("answered", 0).coerceAtLeast(0),
        correct = json.optInt("correct", 0).coerceAtLeast(0),
        streak = json.optInt("streak", 0).coerceAtLeast(0),
        bestStreak = json.optInt("bestStreak", 0).coerceAtLeast(0),
        totalResponseMs = json.optLong("totalResponseMs", 0L).coerceAtLeast(0L)
    )

    private fun encodeCategory(item: CloudCategoryProgress) = JSONObject().apply {
        put("quizType", item.quizType)
        put("category", item.category)
        put("answered", item.answered)
        put("correct", item.correct)
        put("lastAnsweredAt", item.lastAnsweredAt)
    }

    private fun decodeCategory(json: JSONObject) = CloudCategoryProgress(
        quizType = json.getString("quizType"),
        category = json.getString("category"),
        answered = json.optInt("answered", 0).coerceAtLeast(0),
        correct = json.optInt("correct", 0).coerceAtLeast(0),
        lastAnsweredAt = json.optLong("lastAnsweredAt", 0L).coerceAtLeast(0L)
    )

    private fun encodeConcept(item: CloudConceptProgress) = JSONObject().apply {
        put("quizType", item.quizType)
        put("concept", item.concept)
        put("category", item.category)
        put("attempts", item.attempts)
        put("correct", item.correct)
        put("reviewMastered", item.reviewMastered)
        put("lastResultCorrect", item.lastResultCorrect)
        put("lastAnsweredAt", item.lastAnsweredAt)
    }

    private fun decodeConcept(json: JSONObject) = CloudConceptProgress(
        quizType = json.getString("quizType"),
        concept = json.getString("concept"),
        category = json.optString("category"),
        attempts = json.optInt("attempts", 0).coerceAtLeast(0),
        correct = json.optInt("correct", 0).coerceAtLeast(0),
        reviewMastered = json.optBoolean("reviewMastered", false),
        lastResultCorrect = json.optBoolean("lastResultCorrect", false),
        lastAnsweredAt = json.optLong("lastAnsweredAt", 0L).coerceAtLeast(0L)
    )

    private fun encodeReview(item: CloudReviewItem) = JSONObject().apply {
        put("quizType", item.quizType)
        put("concept", item.concept)
        put("category", item.category)
        put("difficulty", item.difficulty)
        put("questionId", item.questionId)
        put("question", item.question)
        put("correctAnswer", item.correctAnswer)
        put("wrongCount", item.wrongCount)
        put("correctAfterWrongCount", item.correctAfterWrongCount)
        put("mastered", item.mastered)
        put("lastWrongAt", item.lastWrongAt)
        put("reviewStage", item.reviewStage)
        put("nextReviewAt", item.nextReviewAt)
        put("lastReviewedAt", item.lastReviewedAt)
        put("reviewAttempts", item.reviewAttempts)
        put("totalReviewResponseMs", item.totalReviewResponseMs)
    }

    private fun decodeReview(json: JSONObject) = CloudReviewItem(
        quizType = json.getString("quizType"),
        concept = json.getString("concept"),
        category = json.optString("category"),
        difficulty = json.optString("difficulty"),
        questionId = json.optLong("questionId", -1L),
        question = json.optString("question"),
        correctAnswer = json.optString("correctAnswer"),
        wrongCount = json.optInt("wrongCount", 0).coerceAtLeast(0),
        correctAfterWrongCount = json.optInt("correctAfterWrongCount", 0).coerceAtLeast(0),
        mastered = json.optBoolean("mastered", false),
        lastWrongAt = json.optLong("lastWrongAt", 0L).coerceAtLeast(0L),
        reviewStage = json.optInt("reviewStage", 0).coerceAtLeast(0),
        nextReviewAt = json.optLong("nextReviewAt", 0L).coerceAtLeast(0L),
        lastReviewedAt = json.optLong("lastReviewedAt", 0L).coerceAtLeast(0L),
        reviewAttempts = json.optInt("reviewAttempts", 0).coerceAtLeast(0),
        totalReviewResponseMs = json.optLong("totalReviewResponseMs", 0L).coerceAtLeast(0L)
    )

    private fun encodeHistory(entry: CloudHistoryEntry) = JSONObject().apply {
        put("id", entry.id)
        put("mode", entry.mode)
        put("categories", JSONArray(entry.categories))
        put("questionCount", entry.questionCount)
        put("startedAt", entry.startedAt)
        put("endedAt", entry.endedAt)
        put("answered", entry.answered)
        put("correct", entry.correct)
        put("xpGained", entry.xpGained)
        put("questions", JSONArray().apply { entry.questions.forEach { put(encodeHistoryQuestion(it)) } })
    }

    private fun decodeHistory(json: JSONObject) = CloudHistoryEntry(
        id = json.getString("id"),
        mode = json.optString("mode", "RANDOM"),
        categories = json.strings("categories"),
        questionCount = json.optInt("questionCount", 10).coerceAtLeast(0),
        startedAt = json.optLong("startedAt", 0L).coerceAtLeast(0L),
        endedAt = json.optLong("endedAt", 0L).coerceAtLeast(0L),
        answered = json.optInt("answered", 0).coerceAtLeast(0),
        correct = json.optInt("correct", 0).coerceAtLeast(0),
        xpGained = json.optInt("xpGained", 0).coerceAtLeast(0),
        questions = json.objects("questions").map(::decodeHistoryQuestion)
    )

    private fun encodeHistoryQuestion(item: CloudHistoryQuestion) = JSONObject().apply {
        put("questionId", item.questionId)
        put("category", item.category)
        put("difficulty", item.difficulty)
        put("question", item.question)
        put("answers", JSONArray(item.answers))
        put("correctIndex", item.correctIndex)
        put("selectedIndex", item.selectedIndex)
        put("explanation", item.explanation)
    }

    private fun decodeHistoryQuestion(json: JSONObject) = CloudHistoryQuestion(
        questionId = json.optLong("questionId", -1L),
        category = json.optString("category"),
        difficulty = json.optString("difficulty"),
        question = json.optString("question"),
        answers = json.strings("answers"),
        correctIndex = json.optInt("correctIndex", -1),
        selectedIndex = json.optInt("selectedIndex", -1),
        explanation = json.optString("explanation")
    )

    private fun decodeEngagement(json: JSONObject) = CloudEngagementOwnership(
        unlockedAchievementIds = json.strings("unlockedAchievementIds").toSet(),
        purchasedFrameKeys = json.strings("purchasedFrameKeys").toSet(),
        purchasedAvatarKeys = json.strings("purchasedAvatarKeys").toSet(),
        purchasedBannerKeys = json.strings("purchasedBannerKeys").toSet()
    )

    private fun Set<String>.toJsonArray(): JSONArray = JSONArray().apply {
        this@toJsonArray.sorted().forEach(::put)
    }

    private fun JSONObject.objects(key: String): List<JSONObject> {
        val array = optJSONArray(key) ?: return emptyList()
        return buildList {
            repeat(array.length()) { index -> array.optJSONObject(index)?.let(::add) }
        }
    }

    private fun JSONObject.strings(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return buildList {
            repeat(array.length()) { index ->
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }
}
