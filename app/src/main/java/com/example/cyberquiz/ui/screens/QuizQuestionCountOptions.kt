package com.example.cyberquiz.ui.screens

internal val QUIZ_QUESTION_COUNT_OPTIONS = listOf(5, 10, 20, 50, 100, 200, 0)

internal fun quizQuestionCountLabel(count: Int): String =
    if (count == 0) "INFINI" else count.toString()
