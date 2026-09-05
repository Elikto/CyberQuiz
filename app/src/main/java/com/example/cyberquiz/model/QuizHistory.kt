package com.example.cyberquiz.model

data class QuizHistoryQuestion(
    val questionId: Long,
    val category: String,
    val difficulty: String,
    val question: String,
    val answers: List<String>,
    val correctIndex: Int,
    val selectedIndex: Int,
    val explanation: String
) {
    val correct: Boolean
        get() = selectedIndex == correctIndex
}

data class QuizHistoryEntry(
    val id: String,
    val config: QuizSessionConfig,
    val startedAt: Long,
    val endedAt: Long,
    val answered: Int,
    val correct: Int,
    val xpGained: Int,
    val questions: List<QuizHistoryQuestion>
) {
    val percent: Int
        get() = if (answered == 0) 0 else correct * 100 / answered
}
