package com.example.cyberquiz.ui.screens

import com.example.cyberquiz.model.QuizSessionMode

internal val QUIZ_QUESTION_COUNT_OPTIONS = listOf(5, 10, 20, 50, 100, 200, 0)

internal fun quizQuestionCountOptionsForMode(mode: QuizSessionMode): List<Int> =
    if (mode == QuizSessionMode.EXAM) {
        QUIZ_QUESTION_COUNT_OPTIONS.filter { it > 0 }
    } else {
        QUIZ_QUESTION_COUNT_OPTIONS
    }

internal fun quizQuestionCountLabel(count: Int): String =
    if (count == 0) "INFINI" else count.toString()
