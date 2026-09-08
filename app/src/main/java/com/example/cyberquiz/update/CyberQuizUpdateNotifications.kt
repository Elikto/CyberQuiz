package com.example.cyberquiz.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.MainActivity
import com.example.cyberquiz.R
import java.util.concurrent.TimeUnit

internal fun shouldNotifyUpdate(
    currentVersionCode: Int,
    remoteVersionCode: Int,
    lastNotifiedVersionCode: Int
): Boolean = remoteVersionCode > currentVersionCode && remoteVersionCode > lastNotifiedVersionCode

internal object CyberQuizUpdateNotificationManager {
    private const val CHANNEL_ID = "cyberquiz_updates"
    private const val CHANNEL_NAME = "Mises à jour CyberQuiz"
    private const val PREFERENCES_NAME = "cyberquiz_update_notifications"
    private const val KEY_LAST_NOTIFIED_VERSION_CODE = "last_notified_version_code"
    private const val NOTIFICATION_ID = 240901

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerte lorsqu'une nouvelle version de CyberQuiz est disponible."
            setShowBadge(true)
        }

        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun notifyIfAllowed(context: Context, update: CyberQuizUpdateInfo) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val lastNotifiedVersionCode = preferences.getInt(KEY_LAST_NOTIFIED_VERSION_CODE, 0)

        if (
            !shouldNotifyUpdate(
                currentVersionCode = BuildConfig.VERSION_CODE,
                remoteVersionCode = update.versionCode,
                lastNotifiedVersionCode = lastNotifiedVersionCode
            )
        ) {
            return
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            update.versionCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = "Version ${update.versionName} disponible. Appuie pour ouvrir CyberQuiz et l'installer."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_update_notification)
            .setContentTitle("Mise à jour CyberQuiz disponible")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val posted = runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }.isSuccess

        if (posted) {
            preferences.edit()
                .putInt(KEY_LAST_NOTIFIED_VERSION_CODE, update.versionCode)
                .apply()
        }
    }
}

class CyberQuizUpdateCheckWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val update = CyberQuizUpdateManager.checkForUpdate() ?: return Result.success()
        CyberQuizUpdateNotificationManager.notifyIfAllowed(applicationContext, update)
        return Result.success()
    }
}

internal object CyberQuizUpdateNotificationScheduler {
    private const val PERIODIC_WORK_NAME = "cyberquiz-update-notification-periodic"
    private const val IMMEDIATE_WORK_NAME = "cyberquiz-update-notification-now"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateCheck = OneTimeWorkRequestBuilder<CyberQuizUpdateCheckWorker>()
            .setConstraints(constraints)
            .build()

        val periodicCheck = PeriodicWorkRequestBuilder<CyberQuizUpdateCheckWorker>(
            1,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            immediateCheck
        )
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicCheck
        )
    }
}
