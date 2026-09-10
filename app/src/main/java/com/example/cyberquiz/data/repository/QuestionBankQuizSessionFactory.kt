package com.example.cyberquiz.data.repository

import android.content.Context
import com.example.cyberquiz.data.database.QuestionEntity

/**
 * Creates a normal resumable QuizViewModel session whose queue contains only the
 * selected question-bank questions. Keeping the persisted session contract means
 * favorites quizzes get the same scoring, history, leave/resume and result UX as
 * every other configured quiz.
 */
internal object QuestionBankQuizSessionFactory {
    const val MAX_QUESTIONS = 50

    private const val PREFS = "cyberquiz_quiz_session"
    private const val KEY_ACTIVE_SESSION_IDS = "active_session_ids"
    private const val CATEGORY_SEPARATOR = "||"
    private const val MAX_ACTIVE_SESSIONS = 6

    fun create(context: Context, questions: List<QuestionEntity>): String? {
        val selected = selectQuestions(questions)
        if (selected.isEmpty()) return null

        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existingIds = prefs.getString(KEY_ACTIVE_SESSION_IDS, "")
            .orEmpty()
            .split(',')
            .filter(String::isNotBlank)
            .distinct()
        if (existingIds.size >= MAX_ACTIVE_SESSIONS) return null

        val id = nextId(existingIds.toSet())
        val categories = selected.map { it.category }.filter(String::isNotBlank).toSet()
        val ids = selected.map { it.id }
        val startedAt = System.currentTimeMillis()

        val editor = prefs.edit()
            .putString(KEY_ACTIVE_SESSION_IDS, (listOf(id) + existingIds).take(MAX_ACTIVE_SESSIONS).joinToString(","))
            .putString(key(id, "mode"), "RANDOM")
            .putString(key(id, "categories"), categories.sorted().joinToString(CATEGORY_SEPARATOR))
            .putInt(key(id, "count"), ids.size)
            .putString(key(id, "queue"), ids.joinToString(","))
            .putInt(key(id, "index"), 0)
            .putInt(key(id, "answered"), 0)
            .putInt(key(id, "correct_count"), 0)
            .putInt(key(id, "xp_gained"), 0)
            .putLong(key(id, "started_at"), startedAt)
            .putString(key(id, "answer_history"), "[]")
            .putLong(key(id, "current_id"), ids.first())
            .putBoolean(key(id, "pending"), false)
            .putBoolean(key(id, "correct"), false)
            .putInt(key(id, "xp"), 0)
            .putInt(key(id, "selected"), -1)
            .remove(key(id, "daily_day"))

        return if (editor.commit()) id else null
    }

    internal fun selectQuestions(questions: List<QuestionEntity>): List<QuestionEntity> = questions
        .asSequence()
        .filter { it.id > 0L }
        .distinctBy { it.id }
        .take(MAX_QUESTIONS)
        .toList()

    private fun key(id: String, field: String): String = "session_${id}_$field"

    private fun nextId(existing: Set<String>): String {
        val now = System.currentTimeMillis()
        var suffix = 0
        var candidate: String
        do {
            candidate = if (suffix == 0) "library_$now" else "library_${now}_$suffix"
            suffix++
        } while (candidate in existing)
        return candidate
    }
}
