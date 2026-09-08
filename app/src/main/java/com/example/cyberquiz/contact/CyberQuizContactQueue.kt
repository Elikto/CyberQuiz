package com.example.cyberquiz.contact

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

internal object CyberQuizContactQueue {
    private const val PREFS_NAME = "cyberquiz_contact_queue"
    private const val WORK_SUBMISSION_ID = "submission_id"
    private const val MAX_ATTEMPTS = 6

    fun enqueue(context: Context, reason: String, message: String, urgent: Boolean): Result<String> {
        val appContext = context.applicationContext
        val submissionId = UUID.randomUUID().toString()
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return runCatching {
            val payload = JSONObject()
                .put("reason", reason)
                .put("message", message.trim())
                .put("urgent", urgent)
                .toString()

            check(prefs.edit().putString(submissionId, payload).commit()) {
                "Le message n'a pas pu être préparé pour l'envoi."
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CyberQuizContactWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(WORK_SUBMISSION_ID to submissionId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(appContext).enqueue(request)
            submissionId
        }.onFailure {
            prefs.edit().remove(submissionId).apply()
        }
    }

    internal data class QueuedContact(
        val reason: String,
        val message: String,
        val urgent: Boolean,
    )

    internal fun load(context: Context, submissionId: String): QueuedContact? {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(submissionId, null)
            ?: return null

        return runCatching {
            val json = JSONObject(raw)
            val reason = json.getString("reason")
            val message = json.getString("message")
            val urgent = json.optBoolean("urgent", false)
            QueuedContact(reason = reason, message = message, urgent = urgent)
        }.getOrNull()
    }

    internal fun remove(context: Context, submissionId: String) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(submissionId)
            .apply()
    }

    internal fun shouldRetry(error: Throwable, runAttemptCount: Int): Boolean {
        return error is IOException && runAttemptCount < MAX_ATTEMPTS - 1
    }

    internal fun submissionIdFrom(inputData: Data): String? =
        inputData.getString(WORK_SUBMISSION_ID)?.takeIf { it.isNotBlank() }
}

class CyberQuizContactWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val submissionId = CyberQuizContactQueue.submissionIdFrom(inputData)
            ?: return Result.failure()
        val queued = CyberQuizContactQueue.load(applicationContext, submissionId)
            ?: return Result.failure()
        val sendResult = CyberQuizContactClient.send(
            reason = queued.reason,
            message = queued.message,
            urgent = queued.urgent,
            submissionId = submissionId
        )

        return sendResult.fold(
            onSuccess = {
                CyberQuizContactQueue.remove(applicationContext, submissionId)
                Result.success()
            },
            onFailure = { error ->
                if (CyberQuizContactQueue.shouldRetry(error, runAttemptCount)) {
                    Result.retry()
                } else {
                    CyberQuizContactQueue.remove(applicationContext, submissionId)
                    Result.failure()
                }
            }
        )
    }
}
