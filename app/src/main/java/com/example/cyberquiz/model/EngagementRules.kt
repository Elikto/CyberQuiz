package com.example.cyberquiz.model

import kotlin.math.min

data class EngagementMetrics(
    val answered: Int,
    val correct: Int,
    val xp: Int,
    val level: Int,
    val streak: Int,
    val bestStreak: Int,
    val quizCount: Int
)

enum class MissionMetric { ANSWERED, CORRECT, XP }

data class DailyMissionDefinition(
    val id: String,
    val title: String,
    val description: String,
    val metric: MissionMetric,
    val target: Int,
    val rewardCoins: Int
)

data class DailyMissionProgress(
    val definition: DailyMissionDefinition,
    val current: Int,
    val completed: Boolean
)

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val unlocked: (EngagementMetrics) -> Boolean
)

val dailyMissionDefinitions: List<DailyMissionDefinition> = listOf(
    DailyMissionDefinition(
        id = "answer_10",
        title = "Échauffement cyber",
        description = "Réponds à 10 questions aujourd'hui",
        metric = MissionMetric.ANSWERED,
        target = 10,
        rewardCoins = 40
    ),
    DailyMissionDefinition(
        id = "correct_7",
        title = "Précision chirurgicale",
        description = "Donne 7 bonnes réponses aujourd'hui",
        metric = MissionMetric.CORRECT,
        target = 7,
        rewardCoins = 50
    ),
    DailyMissionDefinition(
        id = "xp_75",
        title = "Montée en puissance",
        description = "Gagne 75 XP aujourd'hui",
        metric = MissionMetric.XP,
        target = 75,
        rewardCoins = 60
    )
)

val achievementDefinitions: List<AchievementDefinition> = listOf(
    AchievementDefinition("first_answer", "Premier paquet", "Réponds à ta première question", 40) { it.answered >= 1 },
    AchievementDefinition("questions_10", "Début de carrière", "Réponds à 10 questions", 60) { it.answered >= 10 },
    AchievementDefinition("questions_100", "Centurion", "Réponds à 100 questions", 150) { it.answered >= 100 },
    AchievementDefinition("questions_500", "Archive massive", "Réponds à 500 questions", 320) { it.answered >= 500 },
    AchievementDefinition("streak_10", "Combo x10", "Atteins une série de 10 bonnes réponses", 100) { it.bestStreak >= 10 },
    AchievementDefinition("streak_25", "Pare-feu humain", "Atteins une série de 25 bonnes réponses", 220) { it.bestStreak >= 25 },
    AchievementDefinition("level_5", "Analyste confirmé", "Atteins le niveau 5", 100) { it.level >= 5 },
    AchievementDefinition("level_10", "Hacker éthique", "Atteins le niveau 10", 200) { it.level >= 10 },
    AchievementDefinition("level_20", "Architecte suprême", "Atteins le niveau 20", 400) { it.level >= 20 },
    AchievementDefinition("xp_2500", "Noyau chargé", "Cumule 2 500 XP", 300) { it.xp >= 2500 },
    AchievementDefinition("quiz_25", "Opérateur régulier", "Termine 25 quiz", 240) { it.quizCount >= 25 },
    AchievementDefinition("quiz_100", "Vétéran CyberQuiz", "Termine 100 quiz", 500) { it.quizCount >= 100 },
    AchievementDefinition("accuracy_80", "Haute précision", "Maintiens 80 % de réussite sur au moins 25 réponses", 140) {
        it.answered >= 25 && it.correct * 100 / it.answered >= 80
    }
)

fun loginRewardForStreak(streak: Int): Int =
    20 + (min(streak.coerceAtLeast(1), 7) - 1) * 5

fun missionProgress(
    metrics: EngagementMetrics,
    baseline: EngagementMetrics,
    definition: DailyMissionDefinition
): Int = when (definition.metric) {
    MissionMetric.ANSWERED -> metrics.answered - baseline.answered
    MissionMetric.CORRECT -> metrics.correct - baseline.correct
    MissionMetric.XP -> metrics.xp - baseline.xp
}.coerceAtLeast(0).coerceAtMost(definition.target)

fun dailyMissionProgress(
    metrics: EngagementMetrics,
    baseline: EngagementMetrics
): List<DailyMissionProgress> = dailyMissionDefinitions.map { definition ->
    val current = missionProgress(metrics, baseline, definition)
    DailyMissionProgress(
        definition = definition,
        current = current,
        completed = current >= definition.target
    )
}

fun newlyUnlockedAchievementIds(
    metrics: EngagementMetrics,
    alreadyUnlocked: Set<String>
): Set<String> = achievementDefinitions
    .asSequence()
    .filter { it.id !in alreadyUnlocked && it.unlocked(metrics) }
    .map { it.id }
    .toSet()
