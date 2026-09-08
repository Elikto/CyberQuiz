package com.example.cyberquiz.contact

import android.os.Build
import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal object CyberQuizContactClient {
    private const val MAX_RESPONSE_CHARS = 16_384

    suspend fun send(reason: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = BuildConfig.CONTACT_API_URL.trim()
            if (endpoint.isBlank()) {
                error("Le service de contact n'est pas encore configuré.")
            }

            val url = URL(endpoint)
            if (!url.protocol.equals("https", ignoreCase = true) || url.host.isBlank()) {
                error("Adresse du service de contact invalide.")
            }

            val payload = JSONObject()
                .put("reason", reason)
                .put("message", message.trim())
                .put("appVersion", BuildConfig.VERSION_NAME)
                .put("platform", "Android ${Build.VERSION.RELEASE}")
                .toString()
                .toByteArray(Charsets.UTF_8)

            val connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 12_000
                doOutput = true
                instanceFollowRedirects = false
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
                setFixedLengthStreamingMode(payload.size)
            }

            try {
                connection.outputStream.use { it.write(payload) }
                val status = connection.responseCode
                if (status !in 200..299) {
                    val detail = readErrorDetail(connection)
                    when (status) {
                        429 -> error("Trop de messages envoyés. Réessaie plus tard.")
                        503 -> error("Le service de contact est temporairement indisponible.")
                        else -> error(detail ?: "Le message n'a pas pu être envoyé.")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun readErrorDetail(connection: HttpsURLConnection): String? {
        val stream = connection.errorStream ?: return null
        val body = stream.bufferedReader().use { reader ->
            val output = StringBuilder()
            val buffer = CharArray(1024)
            while (true) {
                val read = reader.read(buffer)
                if (read < 0) break
                if (output.length + read > MAX_RESPONSE_CHARS) break
                output.append(buffer, 0, read)
            }
            output.toString()
        }

        return runCatching {
            JSONObject(body).optString("detail").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
