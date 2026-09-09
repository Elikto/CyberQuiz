package com.example.cyberquiz

import android.app.Application
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.social.ProgressSyncManager
import com.example.cyberquiz.update.CyberQuizUpdateNotificationManager
import com.example.cyberquiz.update.CyberQuizUpdateNotificationScheduler

/**
 * Application process entry point.
 *
 * Update checks stay passive: they never download or install an APK automatically.
 * A background worker only posts a notification when a newer trusted CyberQuiz release exists.
 */
class CyberQuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EngagementStore.recordDailyLogin(this)
        ProgressSyncManager.initialize(this)
        CyberQuizUpdateNotificationManager.createNotificationChannel(this)
        CyberQuizUpdateNotificationScheduler.schedule(this)
    }
}
