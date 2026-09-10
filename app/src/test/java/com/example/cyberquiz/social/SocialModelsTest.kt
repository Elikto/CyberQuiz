package com.example.cyberquiz.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialModelsTest {
    private fun user(id: String) = SocialUser(id, id, "beginner", 1)
    private fun member(id: String, ready: Boolean = true) =
        SocialRoomMember(user(id), ready, answered = 0, correct = 0, finished = false)

    private fun room(mode: String, members: List<SocialRoomMember>) = SocialQuizRoom(
        id = "room", hostUserId = "me", status = "finished",
        questionIds = listOf(1L, 2L), mode = mode, categories = emptyList(),
        startsAt = null, serverNow = null, members = members
    )

    @Test fun `async challenge mode is case insensitive`() {
        assertTrue(room("ASYNC", listOf(member("me"), member("friend"))).isAsyncChallenge())
        assertTrue(room("async", listOf(member("me"), member("friend"))).isAsyncChallenge())
        assertFalse(room("RANDOM", listOf(member("me"), member("friend"))).isAsyncChallenge())
    }

    @Test fun `rematch targets opponents once and excludes current user`() {
        val current = room(
            "RANDOM",
            listOf(member("me"), member("friend-a"), member("friend-b"), member("friend-a"))
        )
        assertEquals(listOf("friend-a", "friend-b"), current.rematchInviteeIds("me"))
    }

    @Test fun `all ready still requires at least two players`() {
        assertFalse(room("RANDOM", listOf(member("me"))).allReady())
        assertTrue(room("RANDOM", listOf(member("me"), member("friend"))).allReady())
    }
}
