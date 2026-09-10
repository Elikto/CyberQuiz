package com.example.cyberquiz.ui.screens

internal data class SecretAvatarRule(
    val achievementId: String,
    val condition: String,
    val revealNameWhileLocked: Boolean
)

internal data class SecretBannerRule(
    val achievementId: String,
    val condition: String,
    val revealNameWhileLocked: Boolean
)

internal fun secretAvatarRule(style: PlayerAvatarStyle): SecretAvatarRule? = when (style) {
    PlayerAvatarStyle.NETWORK_NINJA -> SecretAvatarRule(
        achievementId = "streak_10",
        condition = "Atteins une série de 10 bonnes réponses",
        revealNameWhileLocked = true
    )
    PlayerAvatarStyle.CYBER_SAMURAI -> SecretAvatarRule(
        achievementId = "questions_100",
        condition = "Réponds à 100 questions",
        revealNameWhileLocked = true
    )
    PlayerAvatarStyle.CIPHER_QUEEN -> SecretAvatarRule(
        achievementId = "level_10",
        condition = "Atteins le niveau 10",
        revealNameWhileLocked = true
    )
    PlayerAvatarStyle.DIGITAL_SPECTER -> SecretAvatarRule(
        achievementId = "accuracy_80",
        condition = "Maintiens 80 % de réussite sur au moins 25 réponses",
        revealNameWhileLocked = true
    )
    PlayerAvatarStyle.EXPLOIT_HUNTER -> SecretAvatarRule(
        achievementId = "streak_25",
        condition = "Atteins une série de 25 bonnes réponses",
        revealNameWhileLocked = false
    )
    PlayerAvatarStyle.AI_TACTICIAN -> SecretAvatarRule(
        achievementId = "quiz_25",
        condition = "Termine 25 quiz",
        revealNameWhileLocked = false
    )
    PlayerAvatarStyle.BLACK_ICE -> SecretAvatarRule(
        achievementId = "xp_2500",
        condition = "Cumule 2 500 XP",
        revealNameWhileLocked = false
    )
    PlayerAvatarStyle.QUANTUM_ARCHIVIST -> SecretAvatarRule(
        achievementId = "questions_500",
        condition = "Réponds à 500 questions",
        revealNameWhileLocked = false
    )
    PlayerAvatarStyle.BINARY_DRAGON -> SecretAvatarRule(
        achievementId = "level_20",
        condition = "Atteins le niveau 20",
        revealNameWhileLocked = false
    )
    PlayerAvatarStyle.OVERMIND -> SecretAvatarRule(
        achievementId = "quiz_100",
        condition = "Termine 100 quiz",
        revealNameWhileLocked = false
    )
    else -> null
}

internal fun secretBannerRule(style: PlayerBannerStyle): SecretBannerRule? = when (style) {
    PlayerBannerStyle.DARK_WEB_MESH -> SecretBannerRule(
        achievementId = "first_answer",
        condition = "Réponds à ta première question",
        revealNameWhileLocked = true
    )
    PlayerBannerStyle.VAULT_CORE -> SecretBannerRule(
        achievementId = "questions_10",
        condition = "Réponds à 10 questions",
        revealNameWhileLocked = true
    )
    PlayerBannerStyle.SYSTEM_RIFT -> SecretBannerRule(
        achievementId = "streak_10",
        condition = "Atteins une série de 10 bonnes réponses",
        revealNameWhileLocked = true
    )
    PlayerBannerStyle.QUANTUM_GRID -> SecretBannerRule(
        achievementId = "questions_100",
        condition = "Réponds à 100 questions",
        revealNameWhileLocked = true
    )
    PlayerBannerStyle.ZERO_DAY -> SecretBannerRule(
        achievementId = "accuracy_80",
        condition = "Maintiens 80 % de réussite sur au moins 25 réponses",
        revealNameWhileLocked = false
    )
    PlayerBannerStyle.AURORA_PACKET -> SecretBannerRule(
        achievementId = "quiz_25",
        condition = "Termine 25 quiz",
        revealNameWhileLocked = false
    )
    PlayerBannerStyle.BLACKOUT -> SecretBannerRule(
        achievementId = "xp_2500",
        condition = "Cumule 2 500 XP",
        revealNameWhileLocked = false
    )
    PlayerBannerStyle.LEGENDARY_CORE -> SecretBannerRule(
        achievementId = "quiz_100",
        condition = "Termine 100 quiz",
        revealNameWhileLocked = false
    )
    else -> null
}

internal fun isAvatarUnlocked(
    style: PlayerAvatarStyle,
    playerLevel: Int,
    unlockedAchievementIds: Set<String>
): Boolean {
    if (!style.mystery) return playerLevel >= style.unlockLevel
    val rule = secretAvatarRule(style) ?: return false
    return rule.achievementId in unlockedAchievementIds
}

internal fun isBannerUnlocked(
    style: PlayerBannerStyle,
    playerLevel: Int,
    unlockedAchievementIds: Set<String>
): Boolean {
    if (!style.mystery) return playerLevel >= style.unlockLevel
    val rule = secretBannerRule(style) ?: return false
    return rule.achievementId in unlockedAchievementIds
}

internal fun lockedAvatarTitle(style: PlayerAvatarStyle): String {
    val rule = secretAvatarRule(style)
    return if (rule?.revealNameWhileLocked == true) style.displayName else "???"
}

internal fun lockedAvatarCondition(style: PlayerAvatarStyle): String =
    secretAvatarRule(style)?.condition ?: "Condition à découvrir"

internal fun lockedBannerTitle(style: PlayerBannerStyle): String {
    val rule = secretBannerRule(style)
    return if (rule?.revealNameWhileLocked == true) style.displayName else "???"
}

internal fun lockedBannerCondition(style: PlayerBannerStyle): String =
    secretBannerRule(style)?.condition ?: "Condition à découvrir"