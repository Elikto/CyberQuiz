package com.example.cyberquiz.model

enum class QuizSessionMode {
    EASY,
    MEDIUM,
    HARD,
    RANDOM,
    DIFFICULTIES
}

data class QuizSessionConfig(
    val mode: QuizSessionMode = QuizSessionMode.RANDOM,
    val categories: Set<String> = Category.entries.map { it.label }.toSet(),
    val questionCount: Int = 10
) {
    val infinite: Boolean
        get() = questionCount == 0
}

data class ActiveQuizSessionSummary(
    val config: QuizSessionConfig,
    val answered: Int = 0,
    val pendingAnswer: Boolean = false
)
