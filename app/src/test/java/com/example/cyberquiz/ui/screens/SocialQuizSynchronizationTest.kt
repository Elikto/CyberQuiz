package com.example.cyberquiz.ui.screens

import com.example.cyberquiz.social.SocialQuizRoom
import org.junit.Assert.assertEquals
import org.junit.Test

class SocialQuizSynchronizationTest {
    @Test
    fun countdown_uses_server_time_instead_of_device_clock() {
        val room = SocialQuizRoom(
            id = "room-1",
            hostUserId = "host",
            status = "countdown",
            questionIds = listOf(1L, 2L),
            mode = "RANDOM",
            categories = emptyList(),
            startsAt = "2026-09-09T18:00:05Z",
            serverNow = "2026-09-09T18:00:00Z",
            members = emptyList()
        )

        assertEquals(5_000L, synchronizedStartDelayMs(room))
    }

    @Test
    fun countdown_never_returns_a_negative_delay() {
        val room = SocialQuizRoom(
            id = "room-1",
            hostUserId = "host",
            status = "active",
            questionIds = listOf(1L),
            mode = "RANDOM",
            categories = emptyList(),
            startsAt = "2026-09-09T18:00:00Z",
            serverNow = "2026-09-09T18:00:04Z",
            members = emptyList()
        )

        assertEquals(0L, synchronizedStartDelayMs(room))
    }
}
