package com.example.cyberquiz.model

data class ProfileTitleDefinition(
    val id: String,
    val label: String,
    val achievementId: String,
    val description: String
)

val profileTitleDefinitions: List<ProfileTitleDefinition> = listOf(
    ProfileTitleDefinition(
        id = "packet_rookie",
        label = "Recrue du réseau",
        achievementId = "first_answer",
        description = "Ta première réponse a lancé ta carrière CyberQuiz."
    ),
    ProfileTitleDefinition(
        id = "soc_centurion",
        label = "Centurion du SOC",
        achievementId = "questions_100",
        description = "Cent questions analysées avec méthode."
    ),
    ProfileTitleDefinition(
        id = "human_firewall",
        label = "Pare-feu humain",
        achievementId = "streak_25",
        description = "Une série de 25 bonnes réponses sans rupture."
    ),
    ProfileTitleDefinition(
        id = "ethical_operator",
        label = "Opérateur éthique",
        achievementId = "level_10",
        description = "Niveau 10 atteint dans le parcours CyberQuiz."
    ),
    ProfileTitleDefinition(
        id = "precision_specialist",
        label = "Spécialiste précision",
        achievementId = "accuracy_80",
        description = "Une précision durable d'au moins 80 %."
    ),
    ProfileTitleDefinition(
        id = "black_ice_operator",
        label = "Opérateur Black Ice",
        achievementId = "xp_2500",
        description = "2 500 XP accumulés au fil des entraînements."
    ),
    ProfileTitleDefinition(
        id = "cyberquiz_veteran",
        label = "Vétéran CyberQuiz",
        achievementId = "quiz_100",
        description = "Cent quiz terminés : l'expérience parle."
    )
)

fun unlockedProfileTitles(
    unlockedAchievementIds: Set<String>
): List<ProfileTitleDefinition> = profileTitleDefinitions.filter {
    it.achievementId in unlockedAchievementIds
}

fun effectiveProfileTitle(
    selectedTitleId: String?,
    unlockedAchievementIds: Set<String>
): ProfileTitleDefinition? {
    val unlocked = unlockedProfileTitles(unlockedAchievementIds)
    if (unlocked.isEmpty()) return null
    return unlocked.firstOrNull { it.id == selectedTitleId } ?: unlocked.last()
}

fun collectionCompletionPercent(
    unlockedAchievements: Int,
    totalAchievements: Int,
    unlockedTitles: Int,
    totalTitles: Int,
    unlockedSecretCosmetics: Int,
    totalSecretCosmetics: Int
): Int {
    val unlocked = unlockedAchievements.coerceAtLeast(0) +
        unlockedTitles.coerceAtLeast(0) +
        unlockedSecretCosmetics.coerceAtLeast(0)
    val total = totalAchievements.coerceAtLeast(0) +
        totalTitles.coerceAtLeast(0) +
        totalSecretCosmetics.coerceAtLeast(0)
    if (total == 0) return 0
    return (unlocked.coerceAtMost(total) * 100 / total).coerceIn(0, 100)
}