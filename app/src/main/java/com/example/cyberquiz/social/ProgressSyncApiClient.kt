package com.example.cyberquiz.social

import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object ProgressSyncApiClient {
    private val baseUrl = BuildConfig.SOCIAL_API_URL.trimEnd('/').also { value ->
        require(URL(value).protocol.equals("https", ignoreCase = true)) {
            "CyberQuiz progress sync API must use HTTPS"
        }
    }

    suspend fun get(token: String): CloudProgressEnvelope = withContext(Dispatchers.IO) {
        parseEnvelope(request("GET", "/progress", token))
    }

    suspend fun put(
        token: String,
        baseRevision: Long,
        snapshot: CloudProgressSnapshot
    ): CloudProgressEnvelope = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("baseRevision", baseRevision)
            .put("snapshot", ProgressSyncJson.encode(snapshot))
        parseEnvelope(request("PUT", "/progress", token, body))
    }

    private fun request(
        method: String,
        path: String,
        token: String,
        body: JSONObject? = null
    ): JSONObject {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) {
                connection.outputStream.use { stream ->
                    stream.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    if (text.isBlank()) null else JSONObject(text).optString("detail").takeIf { it.isNotBlank() }
                }.getOrNull() ?: "La synchronisation CyberQuiz a échoué"
                throw SocialApiException(status, message)
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseEnvelope(json: JSONObject): CloudProgressEnvelope {
        val snapshot = if (!json.has("snapshot") || json.isNull("snapshot")) {
            null
        } else {
            ProgressSyncJson.decode(json.getJSONObject("snapshot"))
        }
        return CloudProgressEnvelope(
            revision = json.optLong("revision", 0L).coerceAtLeast(0L),
            updatedAt = if (json.has("updatedAt") && !json.isNull("updatedAt")) {
                json.optString("updatedAt").takeIf { it.isNotBlank() }
            } else {
                null
            },
            snapshot = snapshot
        )
    }
}
