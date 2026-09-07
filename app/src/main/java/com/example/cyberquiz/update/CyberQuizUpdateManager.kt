package com.example.cyberquiz.update

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

internal data class CyberQuizUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String
)

internal object CyberQuizUpdateManager {
    private const val METADATA_URL =
        "https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/update.json"
    private const val APK_MIME = "application/vnd.android.package-archive"

    suspend fun checkForUpdate(): CyberQuizUpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("$METADATA_URL?ts=${System.currentTimeMillis()}")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 6_000
                readTimeout = 6_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "CyberQuiz-Android")
                setRequestProperty("Cache-Control", "no-cache")
            }

            try {
                if (connection.responseCode !in 200..299) return@runCatching null

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                if (!json.optBoolean("updateReady", false)) return@runCatching null

                val remoteCode = json.optInt("versionCode", 0)
                val remoteName = json.optString("versionName", "Nouvelle version")
                val apkUrl = json.optString("apkUrl", "")

                if (remoteCode <= BuildConfig.VERSION_CODE || apkUrl.isBlank()) {
                    null
                } else {
                    CyberQuizUpdateInfo(
                        versionCode = remoteCode,
                        versionName = remoteName,
                        apkUrl = apkUrl
                    )
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    suspend fun downloadAndLaunchInstaller(
        activity: Activity,
        update: CyberQuizUpdateInfo
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileName = "CyberQuiz-update-${update.versionCode}.apk"

            activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let { directory ->
                File(directory, fileName).delete()
            }

            val request = DownloadManager.Request(Uri.parse(update.apkUrl))
                .setTitle("CyberQuiz ${update.versionName}")
                .setDescription("Téléchargement de la mise à jour")
                .setMimeType(APK_MIME)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(
                    activity,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

            val downloadId = manager.enqueue(request)
            var attempts = 0

            while (attempts < 1_200) {
                delay(500)
                attempts++

                manager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
                    if (!cursor.moveToFirst()) return@use

                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val uri = manager.getUriForDownloadedFile(downloadId)
                                ?: error("Le fichier téléchargé est introuvable.")

                            withContext(Dispatchers.Main) {
                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, APK_MIME)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                activity.startActivity(installIntent)
                            }
                            return@runCatching
                        }

                        DownloadManager.STATUS_FAILED -> {
                            val reason = cursor.getInt(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
                            )
                            error("Échec du téléchargement Android (code $reason).")
                        }
                    }
                }
            }

            error("Le téléchargement de la mise à jour a expiré.")
        }
    }
}