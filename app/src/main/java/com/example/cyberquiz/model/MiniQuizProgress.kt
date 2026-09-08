package com.example.cyberquiz.model

const val CATEGORY_MINI_QUIZ_SIZE = 5

data class CategoryQuizAttempt(
    val number: Int,
    val historyId: String,
    val answered: Int,
    val correct: Int,
    val percent: Int,
    val questionCount: Int,
    val endedAt: Long
)

data class CategoryMiniQuizAttempt(
    val number: Int,
    val historyId: String,
    val answered: Int,
    val correct: Int,
    val percent: Int,
    val endedAt: Long
)

fun categoryQuizAttempts(
    history: List<QuizHistoryEntry>,
    category: String
): List<CategoryQuizAttempt> = history
    .asSequence()
    .filter { entry ->
        entry.config.questionCount > 0 &&
            entry.config.categories.size == 1 &&
            category in entry.config.categories &&
            entry.answered == entry.config.questionCount
    }
    .sortedWith(compareBy<QuizHistoryEntry> { it.endedAt }.thenBy { it.id })
    .mapIndexed { index, entry ->
        CategoryQuizAttempt(
            number = index + 1,
            historyId = entry.id,
            answered = entry.answered,
            correct = entry.correct,
            percent = entry.percent,
            questionCount = entry.config.questionCount,
            endedAt = entry.endedAt
        )
    }
    .toList()

fun categoryMiniQuizAttempts(
    history: List<QuizHistoryEntry>,
    category: String
): List<CategoryMiniQuizAttempt> = categoryQuizAttempts(history, category)
    .asSequence()
    .filter { it.questionCount == CATEGORY_MINI_QUIZ_SIZE }
    .mapIndexed { index, attempt ->
        CategoryMiniQuizAttempt(
            number = index + 1,
            historyId = attempt.historyId,
            answered = attempt.answered,
            correct = attempt.correct,
            percent = attempt.percent,
            endedAt = attempt.endedAt
        )
    }
    .toList()
