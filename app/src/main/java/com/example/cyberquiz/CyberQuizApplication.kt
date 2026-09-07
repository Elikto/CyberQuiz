package com.example.cyberquiz

import android.app.Activity
import android.app.Application
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.cyberquiz.update.CyberQuizUpdateInfo
import com.example.cyberquiz.update.CyberQuizUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CyberQuizApplication : Application(), Application.ActivityLifecycleCallbacks {
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var updateCheckedThisProcess = false
    private var promptVisible = false
    private var resumedActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        resumedActivity = activity
        if (updateCheckedThisProcess || promptVisible) return

        updateCheckedThisProcess = true
        updateScope.launch {
            delay(1_200)
            val update = CyberQuizUpdateManager.checkForUpdate() ?: return@launch
            if (resumedActivity === activity && !activity.isFinishing && !activity.isDestroyed) {
                showUpdateDialog(activity, update)
            }
        }
    }

    private fun showUpdateDialog(activity: Activity, update: CyberQuizUpdateInfo) {
        if (promptVisible) return
        promptVisible = true

        AlertDialog.Builder(activity)
            .setTitle("Mise à jour disponible")
            .setMessage(
                "CyberQuiz ${update.versionName} est disponible. " +
                    "Tu peux la télécharger et l'installer directement depuis l'application."
            )
            .setPositiveButton("Mettre à jour") { _, _ ->
                if (!activity.packageManager.canRequestPackageInstalls()) {
                    updateCheckedThisProcess = false
                    Toast.makeText(
                        activity,
                        "Autorise CyberQuiz à installer des applications, puis reviens dans l'app.",
                        Toast.LENGTH_LONG
                    ).show()
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivity(settingsIntent)
                } else {
                    Toast.makeText(
                        activity,
                        "Téléchargement de la mise à jour…",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateScope.launch {
                        val result = CyberQuizUpdateManager.downloadAndLaunchInstaller(activity, update)
                        result.exceptionOrNull()?.let {
                            Toast.makeText(
                                activity,
                                "Impossible d'installer la mise à jour : ${it.message ?: "erreur inconnue"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Plus tard", null)
            .setOnDismissListener { promptVisible = false }
            .show()
    }

    override fun onActivityPaused(activity: Activity) {
        if (resumedActivity === activity) resumedActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        if (resumedActivity === activity) resumedActivity = null
    }

    override fun onTerminate() {
        updateScope.cancel()
        super.onTerminate()
    }
}