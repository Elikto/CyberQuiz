package com.example.cyberquiz.model

import com.example.cyberquiz.data.database.QuestionEntity

internal enum class QuestionBankStatus(val label: String) {
    ALL("Toutes"),
    FAVORITES("Favoris"),
    SEEN("Déjà vues"),
    UNSEEN("Jamais vues"),
    WRONG("Ratées"),
    REPORTED("Signalées")
}

internal data class QuestionBankFilter(
    val query: String = "",
    val category: String? = null,
    val difficulty: String? = null,
    val status: QuestionBankStatus = QuestionBankStatus.ALL
)

internal fun filterQuestionBank(
    questions: List<QuestionEntity>,
    filter: QuestionBankFilter,
    favoriteIds: Set<Long>,
    wrongQuestionIds: Set<Long>,
    reportedIds: Set<Long>
): List<QuestionEntity> {
    val needle = filter.query.trim().lowercase()
    return questions.filter { question ->
        val queryMatches = needle.isBlank() || listOf(
            question.question,
            question.answerA,
            question.answerB,
            question.answerC,
            question.answerD,
            question.category,
            question.difficulty
        ).any { it.lowercase().contains(needle) }
        val categoryMatches = filter.category == null || question.category == filter.category
        val difficultyMatches = filter.difficulty == null ||
            question.difficulty.equals(filter.difficulty, ignoreCase = true)
        val statusMatches = when (filter.status) {
            QuestionBankStatus.ALL -> true
            QuestionBankStatus.FAVORITES -> question.id in favoriteIds
            QuestionBankStatus.SEEN -> question.seen
            QuestionBankStatus.UNSEEN -> !question.seen
            QuestionBankStatus.WRONG -> question.id in wrongQuestionIds
            QuestionBankStatus.REPORTED -> question.id in reportedIds
        }
        queryMatches && categoryMatches && difficultyMatches && statusMatches
    }
}
