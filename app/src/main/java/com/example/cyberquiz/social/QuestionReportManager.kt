package com.example.cyberquiz.social

import android.content.Context
import com.example.cyberquiz.data.database.QuestionEntity
import com.example.cyberquiz.data.repository.QuestionLibraryStore
import com.example.cyberquiz.data.repository.QuestionReportReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal enum class QuestionReportDelivery {
    SENT,
    QUEUED
}

internal object QuestionReportManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun submit(
        context: Context,
        question: QuestionEntity,
        reason: QuestionReportReason,
        comment: String
    ): QuestionReportDelivery {
        val appContext = context.applicationContext
        val store = QuestionLibraryStore(appContext)
        store.savePendingReport(question, reason, comment)
        val token = SocialTokenStore.load(appContext) ?: return QuestionReportDelivery.QUEUED
        val report = store.pendingReports().firstOrNull { it.questionId == question.id }
            ?: return QuestionReportDelivery.QUEUED
        return try {
            QuestionReportApiClient.submit(token, report)
            store.markReportSent(report.questionId)
            QuestionReportDelivery.SENT
        } catch (error: SocialApiException) {
            if (error.statusCode == 401) QuestionReportDelivery.QUEUED else throw error
        }
    }

    fun requestFlush(context: Context) {
        val appContext = context.applicationContext
        scope.launch { runCatching { flushPending(appContext) } }
    }

    suspend fun flushPending(context: Context) {
        val appContext = context.applicationContext
        val token = SocialTokenStore.load(appContext) ?: return
        val store = QuestionLibraryStore(appContext)
        for (report in store.pendingReports()) {
            try {
                QuestionReportApiClient.submit(token, report)
                store.markReportSent(report.questionId)
            } catch (error: SocialApiException) {
                if (error.statusCode == 401) return
                // Keep this report queued and continue later rather than losing it.
                return
            } catch (_: Exception) {
                return
            }
        }
    }
}
