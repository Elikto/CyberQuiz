package com.example.cyberquiz.data.repository

import android.content.Context
import com.example.cyberquiz.data.database.QuestionEntity
import org.json.JSONObject

internal enum class QuestionReportReason(val wireValue: String, val label: String) {
    INCORRECT("incorrect", "Réponse incorrecte"),
    AMBIGUOUS("ambiguous", "Question ambiguë"),
    OUTDATED("outdated", "Information obsolète"),
    OTHER("other", "Autre problème");

    companion object {
        fun fromWire(value: String): QuestionReportReason = entries.firstOrNull { it.wireValue == value }
            ?: OTHER
    }
}

internal data class PendingQuestionReport(
    val questionId: Long,
    val question: String,
    val category: String,
    val reason: QuestionReportReason,
    val comment: String,
    val createdAt: Long
)

internal class QuestionLibraryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun favoriteIds(): Set<Long> = prefs.getStringSet(KEY_FAVORITES, emptySet())
        .orEmpty()
        .mapNotNull(String::toLongOrNull)
        .filter { it > 0L }
        .toSet()

    fun isFavorite(questionId: Long): Boolean = questionId in favoriteIds()

    /** Returns the new favorite state. */
    fun toggleFavorite(questionId: Long): Boolean {
        if (questionId <= 0L) return false
        val favorites = favoriteIds().toMutableSet()
        val favorite = if (favorites.add(questionId)) true else {
            favorites.remove(questionId)
            false
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites.map(Long::toString).toSet()).commit()
        return favorite
    }

    fun reportedIds(): Set<Long> = prefs.all.keys
        .asSequence()
        .filter { it.startsWith(KEY_REPORT_PREFIX) }
        .mapNotNull { it.removePrefix(KEY_REPORT_PREFIX).toLongOrNull() }
        .toSet()

    fun isReported(questionId: Long): Boolean = prefs.contains(KEY_REPORT_PREFIX + questionId)

    fun savePendingReport(
        question: QuestionEntity,
        reason: QuestionReportReason,
        comment: String,
        createdAt: Long = System.currentTimeMillis()
    ) {
        if (question.id <= 0L) return
        val json = JSONObject()
            .put("questionId", question.id)
            .put("question", question.question.take(MAX_QUESTION_LENGTH))
            .put("category", question.category.take(MAX_CATEGORY_LENGTH))
            .put("reason", reason.wireValue)
            .put("comment", comment.trim().take(MAX_COMMENT_LENGTH))
            .put("createdAt", createdAt.coerceAtLeast(0L))
            .put("sent", false)
        prefs.edit().putString(KEY_REPORT_PREFIX + question.id, json.toString()).commit()
    }

    fun markReportSent(questionId: Long) {
        val key = KEY_REPORT_PREFIX + questionId
        val existing = prefs.getString(key, null) ?: return
        val updated = runCatching { JSONObject(existing).put("sent", true).toString() }.getOrNull() ?: return
        prefs.edit().putString(key, updated).apply()
    }

    fun pendingReports(): List<PendingQuestionReport> = prefs.all
        .asSequence()
        .filter { (key, _) -> key.startsWith(KEY_REPORT_PREFIX) }
        .mapNotNull { (_, raw) -> decodeReport(raw as? String) }
        .filterNot { report -> reportSent(report.questionId) }
        .sortedBy { it.createdAt }
        .toList()

    fun reportSent(questionId: Long): Boolean {
        val raw = prefs.getString(KEY_REPORT_PREFIX + questionId, null) ?: return false
        return runCatching { JSONObject(raw).optBoolean("sent", false) }.getOrDefault(false)
    }

    fun reportReason(questionId: Long): QuestionReportReason? {
        val raw = prefs.getString(KEY_REPORT_PREFIX + questionId, null) ?: return null
        return runCatching {
            QuestionReportReason.fromWire(JSONObject(raw).optString("reason", "other"))
        }.getOrNull()
    }

    private fun decodeReport(raw: String?): PendingQuestionReport? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            PendingQuestionReport(
                questionId = json.getLong("questionId"),
                question = json.optString("question").take(MAX_QUESTION_LENGTH),
                category = json.optString("category").take(MAX_CATEGORY_LENGTH),
                reason = QuestionReportReason.fromWire(json.optString("reason", "other")),
                comment = json.optString("comment").take(MAX_COMMENT_LENGTH),
                createdAt = json.optLong("createdAt", 0L).coerceAtLeast(0L)
            )
        }.getOrNull()?.takeIf { it.questionId > 0L && it.question.isNotBlank() }
    }

    companion object {
        const val PREFS = "cyberquiz_question_library"
        private const val KEY_FAVORITES = "favorite_question_ids"
        private const val KEY_REPORT_PREFIX = "report_"
        const val MAX_QUESTION_LENGTH = 1_500
        const val MAX_CATEGORY_LENGTH = 120
        const val MAX_COMMENT_LENGTH = 800
    }
}
