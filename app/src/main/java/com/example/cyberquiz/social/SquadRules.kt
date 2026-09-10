package com.example.cyberquiz.social

internal const val MAX_SQUAD_FRIENDS = 3
internal val SQUAD_QUESTION_COUNTS = listOf(5, 10, 20)

internal fun toggleSquadFriend(
    selected: Set<String>,
    friendId: String,
    maxFriends: Int = MAX_SQUAD_FRIENDS
): Set<String> {
    val cleaned = friendId.trim()
    if (cleaned.isEmpty() || maxFriends <= 0) return selected
    if (cleaned in selected) return selected - cleaned
    if (selected.size >= maxFriends) return selected
    return selected + cleaned
}

internal fun isSupportedSquadQuestionCount(count: Int): Boolean = count in SQUAD_QUESTION_COUNTS
