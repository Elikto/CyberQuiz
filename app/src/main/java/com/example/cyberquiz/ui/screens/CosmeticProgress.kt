package com.example.cyberquiz.ui.screens

import com.example.cyberquiz.model.EngagementMetrics

internal data class CosmeticProgress(
    val current: Int,
    val target: Int,
    val label: String
) {
    val fraction: Float
        get() = if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)
}

internal fun levelCosmeticProgress(currentLevel: Int, targetLevel: Int): CosmeticProgress =
    CosmeticProgress(
        current = currentLevel.coerceAtMost(targetLevel),
        target = targetLevel.coerceAtLeast(1),
        label = "Niveau $currentLevel / $targetLevel"
    )

internal fun achievementCosmeticProgress(
    achievementId: String?,
    metrics: EngagementMetrics
): CosmeticProgress = when (achievementId) {
    "first_answer" -> metricProgress(metrics.answered, 1, "${metrics.answered.coerceAtMost(1)} / 1 réponse")
    "questions_10" -> metricProgress(metrics.answered, 10, "${metrics.answered.coerceAtMost(10)} / 10 réponses")
    "questions_100" -> metricProgress(metrics.answered, 100, "${metrics.answered.coerceAtMost(100)} / 100 réponses")
    "questions_500" -> metricProgress(metrics.answered, 500, "${metrics.answered.coerceAtMost(500)} / 500 réponses")
    "streak_10" -> metricProgress(metrics.bestStreak, 10, "Série ${metrics.bestStreak.coerceAtMost(10)} / 10")
    "streak_25" -> metricProgress(metrics.bestStreak, 25, "Série ${metrics.bestStreak.coerceAtMost(25)} / 25")
    "level_5" -> levelCosmeticProgress(metrics.level, 5)
    "level_10" -> levelCosmeticProgress(metrics.level, 10)
    "level_20" -> levelCosmeticProgress(metrics.level, 20)
    "xp_2500" -> metricProgress(metrics.xp, 2500, "${metrics.xp.coerceAtMost(2500)} / 2 500 XP")
    "quiz_25" -> metricProgress(metrics.quizCount, 25, "${metrics.quizCount.coerceAtMost(25)} / 25 quiz")
    "quiz_100" -> metricProgress(metrics.quizCount, 100, "${metrics.quizCount.coerceAtMost(100)} / 100 quiz")
    "accuracy_80" -> {
        if (metrics.answered < 25) {
            metricProgress(metrics.answered, 25, "${metrics.answered} / 25 réponses")
        } else {
            val accuracy = if (metrics.answered <= 0) 0 else metrics.correct * 100 / metrics.answered
            metricProgress(accuracy, 80, "${accuracy.coerceAtMost(80)} / 80 %")
        }
    }
    else -> CosmeticProgress(0, 1, "Objectif à découvrir")
}

private fun metricProgress(current: Int, target: Int, label: String): CosmeticProgress =
    CosmeticProgress(current.coerceIn(0, target.coerceAtLeast(1)), target.coerceAtLeast(1), label)
