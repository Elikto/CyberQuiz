package com.example.cyberquiz.model

import com.example.cyberquiz.data.database.CategoryProgressEntity

internal const val LEARNING_PATH_STEP_QUESTIONS = 5
internal const val LEARNING_PATH_MIN_ANSWERS = 5
internal const val LEARNING_PATH_REQUIRED_ACCURACY = 70

internal data class LearningPathStep(
    val id: String,
    val title: String,
    val category: String,
    val objective: String
)

internal data class LearningPathDefinition(
    val id: String,
    val title: String,
    val subtitle: String,
    val steps: List<LearningPathStep>
)

internal data class LearningPathStepProgress(
    val step: LearningPathStep,
    val answered: Int,
    val correct: Int,
    val accuracy: Int,
    val completed: Boolean,
    val unlocked: Boolean
)

internal data class LearningPathProgress(
    val definition: LearningPathDefinition,
    val steps: List<LearningPathStepProgress>
) {
    val completedSteps: Int = steps.count { it.completed }
    val progressPercent: Int = if (steps.isEmpty()) 0 else completedSteps * 100 / steps.size
    val completed: Boolean = steps.isNotEmpty() && completedSteps == steps.size
}

internal val CYBER_LEARNING_PATHS: List<LearningPathDefinition> = listOf(
    LearningPathDefinition(
        id = "fundamentals",
        title = "Fondamentaux cyber",
        subtitle = "Comprendre les systèmes et les réseaux avant de les défendre.",
        steps = listOf(
            LearningPathStep("networks", "1 · Réseaux", Category.RESEAUX.label, "Comprendre adressage, protocoles et circulation des données."),
            LearningPathStep("linux", "2 · Linux", Category.LINUX.label, "Maîtriser les bases d'un système très présent en cybersécurité."),
            LearningPathStep("windows", "3 · Windows", Category.WINDOWS.label, "Identifier les mécanismes essentiels d'un poste Windows."),
            LearningPathStep("system", "4 · Sécurité système", Category.SYSTEM.label, "Relier permissions, durcissement et protection du système.")
        )
    ),
    LearningPathDefinition(
        id = "defense",
        title = "Défense & investigation",
        subtitle = "Reconnaître les attaques, protéger les données et comprendre les traces.",
        steps = listOf(
            LearningPathStep("crypto", "1 · Cryptographie", Category.CRYPTO.label, "Comprendre chiffrement, hachage, clés et certificats."),
            LearningPathStep("malware", "2 · Malware", Category.MALWARE.label, "Reconnaître les familles de logiciels malveillants et leurs effets."),
            LearningPathStep("social", "3 · Ingénierie sociale", Category.SOCIAL.label, "Repérer les techniques qui ciblent le facteur humain."),
            LearningPathStep("forensics", "4 · Forensics", Category.FORENSICS.label, "Analyser des traces sans altérer les éléments de preuve.")
        )
    ),
    LearningPathDefinition(
        id = "offensive",
        title = "Web, identité & offensive",
        subtitle = "Comprendre comment les systèmes sont testés pour mieux les sécuriser.",
        steps = listOf(
            LearningPathStep("web", "1 · Sécurité Web", Category.WEB.label, "Identifier les vulnérabilités courantes des applications Web."),
            LearningPathStep("osint", "2 · OSINT", Category.OSINT.label, "Collecter et recouper des informations disponibles publiquement."),
            LearningPathStep("pentest", "3 · Pentest", Category.PENTEST.label, "Comprendre une démarche de test d'intrusion autorisé et méthodique."),
            LearningPathStep("ad", "4 · Active Directory", Category.AD.label, "Comprendre identités, domaines, permissions et risques associés.")
        )
    ),
    LearningPathDefinition(
        id = "modern",
        title = "Environnements modernes",
        subtitle = "Étendre tes réflexes de sécurité au cloud et au mobile.",
        steps = listOf(
            LearningPathStep("cloud", "1 · Cloud Security", Category.CLOUD.label, "Comprendre responsabilités, identités et exposition des services cloud."),
            LearningPathStep("mobile", "2 · Mobile Security", Category.MOBILE.label, "Reconnaître les risques propres aux applications et appareils mobiles.")
        )
    )
)

internal fun categoryAccuracy(answered: Int, correct: Int): Int {
    if (answered <= 0) return 0
    return (correct.coerceIn(0, answered) * 100 / answered).coerceIn(0, 100)
}

internal fun isLearningPathStepCompleted(
    answered: Int,
    correct: Int,
    minAnswers: Int = LEARNING_PATH_MIN_ANSWERS,
    requiredAccuracy: Int = LEARNING_PATH_REQUIRED_ACCURACY
): Boolean {
    if (answered < minAnswers.coerceAtLeast(1)) return false
    return categoryAccuracy(answered, correct) >= requiredAccuracy.coerceIn(0, 100)
}

internal fun buildLearningPathProgress(
    definitions: List<LearningPathDefinition> = CYBER_LEARNING_PATHS,
    categoryProgress: List<CategoryProgressEntity>
): List<LearningPathProgress> {
    val progressByCategory = categoryProgress.associateBy { it.category }
    return definitions.map { definition ->
        var previousCompleted = true
        val steps = definition.steps.mapIndexed { index, step ->
            val stored = progressByCategory[step.category]
            val answered = stored?.answered?.coerceAtLeast(0) ?: 0
            val correct = stored?.correct?.coerceIn(0, answered) ?: 0
            val completed = isLearningPathStepCompleted(answered, correct)
            val unlocked = index == 0 || previousCompleted
            previousCompleted = previousCompleted && completed
            LearningPathStepProgress(
                step = step,
                answered = answered,
                correct = correct,
                accuracy = categoryAccuracy(answered, correct),
                completed = completed,
                unlocked = unlocked || completed
            )
        }
        LearningPathProgress(definition, steps)
    }
}
