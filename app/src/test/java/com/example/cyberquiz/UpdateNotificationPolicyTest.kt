package com.example.cyberquiz

import com.example.cyberquiz.update.shouldNotifyUpdate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateNotificationPolicyTest {
    @Test
    fun `newer remote version that was not notified should notify`() {
        assertTrue(
            shouldNotifyUpdate(
                currentVersionCode = 10,
                remoteVersionCode = 11,
                lastNotifiedVersionCode = 0
            )
        )
    }

    @Test
    fun `current or older remote version should not notify`() {
        assertFalse(
            shouldNotifyUpdate(
                currentVersionCode = 10,
                remoteVersionCode = 10,
                lastNotifiedVersionCode = 0
            )
        )
        assertFalse(
            shouldNotifyUpdate(
                currentVersionCode = 10,
                remoteVersionCode = 9,
                lastNotifiedVersionCode = 0
            )
        )
    }

    @Test
    fun `same update should not notify twice`() {
        assertFalse(
            shouldNotifyUpdate(
                currentVersionCode = 10,
                remoteVersionCode = 11,
                lastNotifiedVersionCode = 11
            )
        )
    }

    @Test
    fun `later update should notify after a previous notification`() {
        assertTrue(
            shouldNotifyUpdate(
                currentVersionCode = 10,
                remoteVersionCode = 12,
                lastNotifiedVersionCode = 11
            )
        )
    }
}
