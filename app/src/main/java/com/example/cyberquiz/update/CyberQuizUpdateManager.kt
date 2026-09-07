package com.example.cyberquiz.update

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

internal data class CyberQuizUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String
)

internal object CyberQuizUpdateManager {
    private const val METADATA_URL =
        "https://github.com/Elikto/CyberQuiz/releases/download/apk-latest/update.json"
    private const val APK_MIME = "application/vnd.android.package-archive"
    private const val MAX_METADATA_CHARS = 32_768
    private const val MAX_VERSION_NAME_CHARS = 80
    private const val MAX_APK_BYTES = 150L * 1024L * 1024L
    private val SHA256_PATTERN = Regex("^[a-f0-9]{64}$")

    suspend fun checkForUpdate(): CyberQuizUpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("$METADATA_URL?ts=${System.currentTimeMillis()}")
            val connection = (url.openConnection() as HttpsURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 6_000
                readTimeout = 6_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "CyberQuiz-Android")
                setRequestProperty("Cache-Control", "no-cache")
            }

            try {
                if (connection.responseCode !in 200..299) return@runCatching null

                val body = readBoundedMetadata(connection)
                val json = JSONObject(body)
                if (!json.optBoolean("updateReady", false)) return@runCatching null
                if (json.optString("packageName") != BuildConfig.APPLICATION_ID) return@runCatching null

                val remoteCode = json.optInt("versionCode", 0)
                val remoteName = json.optString("versionName", "Nouvelle version")
                    .trim()
                    .take(MAX_VERSION_NAME_CHARS)
                    .ifBlank { "Nouvelle version" }
                val apkUrl = json.optString("apkUrl", "")
                val expectedSha256 = json.optString("sha256", "")
                    .lowercase(Locale.US)

                if (
                    remoteCode <= BuildConfig.VERSION_CODE ||
                    !isTrustedDownloadUrl(apkUrl) ||
                    !SHA256_PATTERN.matches(expectedSha256)
                ) {
                    null
                } else {
                    CyberQuizUpdateInfo(
                        versionCode = remoteCode,
                        versionName = remoteName,
                        apkUrl = apkUrl,
                        sha256 = expectedSha256
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
            if (!isTrustedDownloadUrl(update.apkUrl)) {
                error("Adresse de mise à jour non autorisée.")
            }

            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileName = "CyberQuiz-update-${update.versionCode}.apk"
            val directory = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: error("Le dossier de téléchargement sécurisé est indisponible.")
            val targetFile = File(directory, fileName)
            targetFile.delete()

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

                    val totalSize = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    if (totalSize > MAX_APK_BYTES) {
                        manager.remove(downloadId)
                        targetFile.delete()
                        error("La mise à jour annoncée est anormalement volumineuse.")
                    }

                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            verifyDownloadedApk(activity, targetFile, update)

                            val installUri = FileProvider.getUriForFile(
                                activity,
                                "${activity.packageName}.fileprovider",
                                targetFile
                            )

                            withContext(Dispatchers.Main) {
                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(installUri, APK_MIME)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                if (installIntent.resolveActivity(activity.packageManager) == null) {
                                    error("Aucun installateur Android disponible.")
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

            manager.remove(downloadId)
            targetFile.delete()
            error("Le téléchargement de la mise à jour a expiré.")
        }
    }

    private fun readBoundedMetadata(connection: HttpsURLConnection): String =
        connection.inputStream.bufferedReader().use { reader ->
            val output = StringBuilder()
            val buffer = CharArray(4_096)
            while (true) {
                val read = reader.read(buffer)
                if (read < 0) break
                if (output.length + read > MAX_METADATA_CHARS) {
                    error("Métadonnées de mise à jour trop volumineuses.")
                }
                output.append(buffer, 0, read)
            }
            output.toString()
        }

    private fun isTrustedDownloadUrl(rawUrl: String): Boolean {
        val uri = runCatching { Uri.parse(rawUrl) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("github.com", ignoreCase = true) &&
            uri.path?.startsWith("/Elikto/CyberQuiz/releases/download/") == true
    }

    private fun verifyDownloadedApk(
        activity: Activity,
        file: File,
        update: CyberQuizUpdateInfo
    ) {
        if (!file.isFile || file.length() <= 0L || file.length() > MAX_APK_BYTES) {
            file.delete()
            error("APK téléchargé invalide.")
        }

        val actualSha256 = sha256(file)
        if (!actualSha256.equals(update.sha256, ignoreCase = true)) {
            file.delete()
            error("La signature SHA-256 de la mise à jour ne correspond pas.")
        }

        val packageManager = activity.packageManager
        val archiveInfo = getArchivePackageInfo(packageManager, file)
            ?: error("Impossible de vérifier le paquet téléchargé.")
        val currentInfo = getInstalledPackageInfo(packageManager, activity.packageName)

        if (archiveInfo.packageName != activity.packageName) {
            file.delete()
            error("Le paquet téléchargé n'est pas CyberQuiz.")
        }

        val archiveVersionCode = packageVersionCode(archiveInfo)
        if (
            archiveVersionCode != update.versionCode.toLong() ||
            archiveVersionCode <= BuildConfig.VERSION_CODE.toLong()
        ) {
            file.delete()
            error("Version de mise à jour incohérente.")
        }

        val currentSigners = signerDigests(currentInfo)
        val archiveSigners = signerDigests(archiveInfo)
        if (
            currentSigners.isEmpty() ||
            archiveSigners.isEmpty() ||
            currentSigners != archiveSigners
        ) {
            file.delete()
            error("La signature Android de la mise à jour est différente de l'application installée.")
        }
    }

    @Suppress("DEPRECATION")
    private fun getArchivePackageInfo(
        packageManager: PackageManager,
        file: File
    ): PackageInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageArchiveInfo(
            file.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
    } else {
        packageManager.getPackageArchiveInfo(
            file.absolutePath,
            PackageManager.GET_SIGNATURES
        )
    }

    @Suppress("DEPRECATION")
    private fun getInstalledPackageInfo(
        packageManager: PackageManager,
        packageName: String
    ): PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
    } else {
        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
    }

    @Suppress("DEPRECATION")
    private fun signerDigests(packageInfo: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo ?: return emptySet()
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            packageInfo.signatures
        }

        return signatures
            ?.map { signature -> sha256(signature.toByteArray()) }
            ?.toSet()
            .orEmpty()
    }

    @Suppress("DEPRECATION")
    private fun packageVersionCode(packageInfo: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(16 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().toHex()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    private fun ByteArray.toHex(): String = joinToString(separator = "") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}
