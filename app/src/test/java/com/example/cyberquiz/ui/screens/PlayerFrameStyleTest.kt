package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerFrameStyleTest {
    @Test
    fun `frame catalog keeps unique keys`() {
        assertEquals(12, PlayerFrameStyle.entries.size)
        assertEquals(12, PlayerFrameStyle.entries.map { it.storageKey }.toSet().size)
    }

    @Test
    fun `starter frames are immediately unlocked`() {
        assertTrue(isFrameUnlocked(PlayerFrameStyle.CYAN_PULSE, 1, emptySet(), emptySet()))
        assertTrue(isFrameUnlocked(PlayerFrameStyle.PURPLE_NODE, 1, emptySet(), emptySet()))
    }

    @Test
    fun `level frame unlocks at required level`() {
        assertFalse(isFrameUnlocked(PlayerFrameStyle.SOC_HEX, 4, emptySet(), emptySet()))
        assertTrue(isFrameUnlocked(PlayerFrameStyle.SOC_HEX, 5, emptySet(), emptySet()))
    }

    @Test
    fun `coin frame requires purchase`() {
        assertFalse(isFrameUnlocked(PlayerFrameStyle.CHROME_PACKET, 20, emptySet(), emptySet()))
        assertTrue(
            isFrameUnlocked(
                PlayerFrameStyle.CHROME_PACKET,
                20,
                setOf(PlayerFrameStyle.CHROME_PACKET.storageKey),
                emptySet()
            )
        )
    }

    @Test
    fun `secret frame requires its achievement`() {
        assertFalse(isFrameUnlocked(PlayerFrameStyle.COMBO_TEN, 20, emptySet(), emptySet()))
        assertTrue(isFrameUnlocked(PlayerFrameStyle.COMBO_TEN, 20, emptySet(), setOf("streak_10")))
    }
}
