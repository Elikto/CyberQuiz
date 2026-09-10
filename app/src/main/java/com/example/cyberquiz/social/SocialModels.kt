package com.example.cyberquiz.social

data class SocialUser(
    val id: String,
    val nickname: String,
    val avatarKey: String,
    val level: Int,
    val friendCode: String? = null,
    val email: String? = null
)

data class SocialSession(
    val token: String,
    val user: SocialUser
)

data class SocialConfig(
    val googleClientId: String?
)

data class SocialFriendRequest(
    val id: String,
    val from: SocialUser,
    val createdAt: String? = null
)

data class SocialQuizInvite(
    val id: String,
    val roomId: String,
    val from: SocialUser,
    val mode: String = "RANDOM",
    val createdAt: String? = null
)

data class SocialRoomMember(
    val user: SocialUser,
    val ready: Boolean,
    val answered: Int,
    val correct: Int,
    val finished: Boolean
)

data class SocialQuizRoom(
    val id: String,
    val hostUserId: String,
    val status: String,
    val questionIds: List<Long>,
    val mode: String,
    val categories: List<String>,
    val startsAt: String?,
    val serverNow: String?,
    val members: List<SocialRoomMember>
) {
    fun currentMember(userId: String): SocialRoomMember? = members.firstOrNull { it.user.id == userId }
    fun allReady(): Boolean = members.size >= 2 && members.all { it.ready }
    fun isAsyncChallenge(): Boolean = mode.equals("ASYNC", ignoreCase = true)
    fun rematchInviteeIds(currentUserId: String): List<String> = members.map { it.user.id }.filter { it != currentUserId }.distinct()
}
