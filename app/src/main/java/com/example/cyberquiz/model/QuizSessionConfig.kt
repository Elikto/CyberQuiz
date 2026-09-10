package com.example.cyberquiz.model

enum class QuizSessionMode {
    EASY,
    MEDIUM,
    HARD,
    RANDOM,
    DIFFICULTIES,
    EXAM
}

data class QuizSessionConfig(
    val mode: QuizSessionMode = QuizSessionMode.RANDOM,
    val categories: Set<String> = Category.entries.map { it.label }.toSet(),
    val questionCount: Int = 10,
    val timeLimitMinutes: Int = 0
) {
    val infinite: Boolean
        get() = questionCount == 0

    val exam: Boolean
        get() = mode == QuizSessionMode.EXAM

    val timedExam: Boolean
        get() = exam && timeLimitMinutes > 0
}

data class ActiveQuizSessionSummary(
    val id: String,
    val config: QuizSessionConfig,
    val answered: Int = 0,
    val pendingAnswer: Boolean = false
)
