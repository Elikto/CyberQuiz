package com.example.cyberquiz.social

import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.data.repository.PendingQuestionReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object QuestionReportApiClient {
    private val endpoint = BuildConfig.SOCIAL_API_URL.trimEnd('/') + "/question-reports"

    init {
        require(URL(endpoint).protocol.equals("https", ignoreCase = true)) {
            "CyberQuiz question report API must use HTTPS"
        }
    }

    suspend fun submit(token: String, report: PendingQuestionReport) = withContext(Dispatchers.IO) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 12_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
        }
        try {
            val body = JSONObject()
                .put("questionId", report.questionId)
                .put("question", report.question)
                .put("category", report.category)
                .put("reason", report.reason.wireValue)
                .put("comment", report.comment)
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            if (status !in 200..299) {
                val text = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val message = runCatching {
                    JSONObject(text).optString("detail").takeIf(String::isNotBlank)
                }.getOrNull() ?: "Le signalement n'a pas pu être envoyé"
                throw SocialApiException(status, message)
            }
        } finally {
            connection.disconnect()
        }
    }
}
