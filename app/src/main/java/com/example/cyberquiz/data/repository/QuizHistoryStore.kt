package com.example.cyberquiz.data.repository

import android.content.Context
import com.example.cyberquiz.model.QuizHistoryEntry
import com.example.cyberquiz.model.QuizHistoryQuestion
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import org.json.JSONArray
import org.json.JSONObject

class QuizHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<QuizHistoryEntry> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    decodeEntry(array.getJSONObject(index))?.let(::add)
                }
            }.take(MAX_HISTORY)
        }.getOrDefault(emptyList())
    }

    fun add(entry: QuizHistoryEntry): List<QuizHistoryEntry> {
        val updated = (listOf(entry) + load())
            .distinctBy { it.id }
            .take(MAX_HISTORY)
        prefs.edit().putString(KEY_HISTORY, encodeEntries(updated).toString()).apply()
        return updated
    }

    fun encodeQuestions(questions: List<QuizHistoryQuestion>): String =
        encodeQuestionArray(questions).toString()

    fun decodeQuestions(raw: String?): List<QuizHistoryQuestion> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { decodeQuestionArray(JSONArray(raw)) }.getOrDefault(emptyList())
    }

    private fun encodeEntries(entries: List<QuizHistoryEntry>): JSONArray = JSONArray().apply {
        entries.forEach { entry -> put(encodeEntry(entry)) }
    }

    private fun encodeEntry(entry: QuizHistoryEntry): JSONObject = JSONObject().apply {
        put("id", entry.id)
        put("mode", entry.config.mode.name)
        put("categories", JSONArray(entry.config.categories.sorted()))
        put("questionCount", entry.config.questionCount)
        put("startedAt", entry.startedAt)
        put("endedAt", entry.endedAt)
        put("answered", entry.answered)
        put("correct", entry.correct)
        put("xpGained", entry.xpGained)
        put("questions", encodeQuestionArray(entry.questions))
    }

    private fun decodeEntry(json: JSONObject): QuizHistoryEntry? = runCatching {
        val categoriesJson = json.optJSONArray("categories") ?: JSONArray()
        val categories = buildSet {
            for (index in 0 until categoriesJson.length()) {
                val value = categoriesJson.optString(index)
                if (value.isNotBlank()) add(value)
            }
        }
        val mode = runCatching {
            QuizSessionMode.valueOf(json.optString("mode", QuizSessionMode.RANDOM.name))
        }.getOrDefault(QuizSessionMode.RANDOM)

        QuizHistoryEntry(
            id = json.getString("id"),
            config = QuizSessionConfig(
                mode = mode,
                categories = categories,
                questionCount = json.optInt("questionCount", 10)
            ),
            startedAt = json.optLong("startedAt", 0L),
            endedAt = json.optLong("endedAt", 0L),
            answered = json.optInt("answered", 0),
            correct = json.optInt("correct", 0),
            xpGained = json.optInt("xpGained", 0),
            questions = decodeQuestionArray(json.optJSONArray("questions") ?: JSONArray())
        )
    }.getOrNull()

    private fun encodeQuestionArray(questions: List<QuizHistoryQuestion>): JSONArray = JSONArray().apply {
        questions.forEach { item ->
            put(JSONObject().apply {
                put("questionId", item.questionId)
                put("category", item.category)
                put("difficulty", item.difficulty)
                put("question", item.question)
                put("answers", JSONArray(item.answers))
                put("correctIndex", item.correctIndex)
                put("selectedIndex", item.selectedIndex)
                put("explanation", item.explanation)
            })
        }
    }

    private fun decodeQuestionArray(array: JSONArray): List<QuizHistoryQuestion> = buildList {
        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index) ?: continue
            val answersJson = json.optJSONArray("answers") ?: JSONArray()
            val answers = buildList {
                for (answerIndex in 0 until answersJson.length()) {
                    add(answersJson.optString(answerIndex))
                }
            }
            add(
                QuizHistoryQuestion(
                    questionId = json.optLong("questionId", -1L),
                    category = json.optString("category"),
                    difficulty = json.optString("difficulty"),
                    question = json.optString("question"),
                    answers = answers,
                    correctIndex = json.optInt("correctIndex", -1),
                    selectedIndex = json.optInt("selectedIndex", -1),
                    explanation = json.optString("explanation")
                )
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "cyberquiz_quiz_history"
        private const val KEY_HISTORY = "completed_quizzes"
        private const val MAX_HISTORY = 25
    }
}
