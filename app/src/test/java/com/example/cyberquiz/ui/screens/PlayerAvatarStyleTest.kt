package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerAvatarStyleTest {
    @Test
    fun `stored avatar key resolves to the matching avatar`() {
        assertEquals(
            PlayerAvatarStyle.PENTEST,
            playerAvatarFromStorage(PlayerAvatarStyle.PENTEST.storageKey)
        )
    }

    @Test
    fun `unknown avatar key falls back to beginner`() {
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage("unknown"))
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage(null))
    }

    @Test
    fun `five avatar choices expose unique storage keys`() {
        assertEquals(5, PlayerAvatarStyle.entries.size)
        assertTrue(PlayerAvatarStyle.entries.map { it.storageKey }.toSet().size == 5)
    }
}
