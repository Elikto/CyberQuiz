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
        assertEquals(
            PlayerAvatarStyle.CUSTOM,
            playerAvatarFromStorage(PlayerAvatarStyle.CUSTOM.storageKey)
        )
    }

    @Test
    fun `unknown avatar key falls back to beginner`() {
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage("unknown"))
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage(null))
    }

    @Test
    fun `five built in avatar choices keep unique storage keys`() {
        assertEquals(5, builtInPlayerAvatarStyles.size)
        assertTrue(builtInPlayerAvatarStyles.none { it == PlayerAvatarStyle.CUSTOM })
        assertTrue(builtInPlayerAvatarStyles.map { it.storageKey }.toSet().size == 5)
    }

    @Test
    fun `custom avatar uses its own storage key`() {
        assertEquals("custom", PlayerAvatarStyle.CUSTOM.storageKey)
        assertEquals(6, PlayerAvatarStyle.entries.size)
    }
}
