package com.example.cyberquiz.model

const val CATEGORY_XP_PER_ANSWER = 5
const val CATEGORY_XP_CORRECT_BONUS = 5
const val CATEGORY_XP_PER_LEVEL = 100

data class CategoryLevelProgress(
    val xp: Int,
    val level: Int,
    val xpIntoLevel: Int,
    val progress: Float
)

fun categoryLevelProgress(answered: Int, correct: Int): CategoryLevelProgress {
    val safeAnswered = answered.coerceAtLeast(0)
    val safeCorrect = correct.coerceIn(0, safeAnswered)
    val xp = safeAnswered * CATEGORY_XP_PER_ANSWER + safeCorrect * CATEGORY_XP_CORRECT_BONUS
    val level = 1 + xp / CATEGORY_XP_PER_LEVEL
    val xpIntoLevel = xp % CATEGORY_XP_PER_LEVEL

    return CategoryLevelProgress(
        xp = xp,
        level = level,
        xpIntoLevel = xpIntoLevel,
        progress = xpIntoLevel / CATEGORY_XP_PER_LEVEL.toFloat()
    )
}
