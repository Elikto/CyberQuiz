package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            PlayerAvatarStyle.CRYPTO_SENTINEL,
            playerAvatarFromStorage(PlayerAvatarStyle.CRYPTO_SENTINEL.storageKey)
        )
    }

    @Test
    fun `legacy custom or unknown avatar falls back to beginner`() {
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage("custom"))
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage("unknown"))
        assertEquals(PlayerAvatarStyle.BEGINNER, playerAvatarFromStorage(null))
    }

    @Test
    fun `avatar catalog contains five starter five level and ten mystery avatars`() {
        assertEquals(20, PlayerAvatarStyle.entries.size)
        assertEquals(5, starterPlayerAvatarStyles.size)
        assertEquals(5, levelPlayerAvatarStyles.size)
        assertEquals(10, mysteryPlayerAvatarStyles.size)
        assertEquals(20, PlayerAvatarStyle.entries.map { it.storageKey }.toSet().size)
    }

    @Test
    fun `level avatars unlock at their configured levels`() {
        assertFalse(isAvatarUnlocked(PlayerAvatarStyle.MALWARE_HUNTER, 4))
        assertTrue(isAvatarUnlocked(PlayerAvatarStyle.MALWARE_HUNTER, 5))
        assertFalse(isAvatarUnlocked(PlayerAvatarStyle.FIREWALL_MASTER, 19))
        assertTrue(isAvatarUnlocked(PlayerAvatarStyle.FIREWALL_MASTER, 20))
    }

    @Test
    fun `mystery avatars stay locked`() {
        mysteryPlayerAvatarStyles.forEach { style ->
            assertFalse(isAvatarUnlocked(style, 999))
        }
    }

    @Test
    fun `banner catalog uses free level and mystery groups`() {
        assertEquals(16, PlayerBannerStyle.entries.size)
        assertEquals(4, starterPlayerBannerStyles.size)
        assertEquals(4, levelPlayerBannerStyles.size)
        assertEquals(8, mysteryPlayerBannerStyles.size)
        assertEquals(16, PlayerBannerStyle.entries.map { it.storageKey }.toSet().size)
    }

    @Test
    fun `banner level locks and fallback work`() {
        assertEquals(PlayerBannerStyle.CIRCUIT_BLUE, playerBannerFromStorage("unknown"))
        assertFalse(isBannerUnlocked(PlayerBannerStyle.SOC_RADAR, 4))
        assertTrue(isBannerUnlocked(PlayerBannerStyle.SOC_RADAR, 5))
        assertFalse(isBannerUnlocked(PlayerBannerStyle.LEGENDARY_CORE, 999))
    }
}
