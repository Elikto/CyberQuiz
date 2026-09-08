package com.example.cyberquiz.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopCosmeticsTest {
    @Test
    fun `shop exposes five avatars five banners at ten coins`() {
        assertEquals(5, ShopAvatarStyle.entries.size)
        assertEquals(5, ShopBannerStyle.entries.size)
        assertEquals(10, SHOP_COSMETIC_COST)
        assertEquals(5, ShopAvatarStyle.entries.map { it.storageKey }.toSet().size)
        assertEquals(5, ShopBannerStyle.entries.map { it.storageKey }.toSet().size)
    }

    @Test
    fun `unlock progress is clamped`() {
        assertEquals(0f, CosmeticProgress(0, 10, "").fraction, 0f)
        assertEquals(.5f, CosmeticProgress(5, 10, "").fraction, 0f)
        assertEquals(1f, CosmeticProgress(50, 10, "").fraction, 0f)
        assertTrue(levelCosmeticProgress(2, 5).label.contains("2 / 5"))
    }
}