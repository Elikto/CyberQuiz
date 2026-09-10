package com.example.cyberquiz.social

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Persistent safety net for account synchronization.
 *
 * Normal progress changes are synchronized quickly by [ProgressSyncManager]. This
 * worker guarantees that both learning progress and the transactional economy are
 * retried later after a temporary network/process failure.
 */
internal class ProgressSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (SocialTokenStore.load(applicationContext).isNullOrBlank()) return Result.success()
        return try {
            ProgressSyncManager.syncCurrentSession(applicationContext)
            AccountEconomyManager.syncCurrentSession(applicationContext)
            Result.success()
        } catch (error: SocialApiException) {
            if (error.statusCode == 401) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK = "cyberquiz-account-progress-periodic-sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<ProgressSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
