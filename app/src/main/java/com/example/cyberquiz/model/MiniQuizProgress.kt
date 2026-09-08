package com.example.cyberquiz.model

const val CATEGORY_MINI_QUIZ_SIZE = 5

data class CategoryMiniQuizAttempt(
    val number: Int,
    val historyId: String,
    val answered: Int,
    val correct: Int,
    val percent: Int,
    val endedAt: Long
)

fun categoryMiniQuizAttempts(
    history: List<QuizHistoryEntry>,
    category: String
): List<CategoryMiniQuizAttempt> = history
    .asSequence()
    .filter { entry ->
        entry.config.questionCount == CATEGORY_MINI_QUIZ_SIZE &&
            entry.config.categories.size == 1 &&
            category in entry.config.categories &&
            entry.answered > 0
    }
    .sortedWith(compareBy<QuizHistoryEntry> { it.endedAt }.thenBy { it.id })
    .mapIndexed { index, entry ->
        CategoryMiniQuizAttempt(
            number = index + 1,
            historyId = entry.id,
            answered = entry.answered,
            correct = entry.correct,
            percent = entry.percent,
            endedAt = entry.endedAt
        )
    }
    .toList()
