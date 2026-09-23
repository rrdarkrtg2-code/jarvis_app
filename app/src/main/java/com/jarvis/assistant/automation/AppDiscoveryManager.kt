package com.jarvis.assistant.automation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class InstalledApp(
    val label: String,
    val packageName: String
)

class AppDiscoveryManager(private val context: Context) {

    private val aliases = mapOf(
        "yt" to "youtube",
        "insta" to "instagram",
        "ig" to "instagram",
        "wa" to "whatsapp",
        "watsapp" to "whatsapp",
        "fb" to "facebook",
        "calc" to "calculator",
        "msg" to "messages",
        "sms" to "messages",
        "chrome" to "chrome",
        "gpay" to "google pay"
    )

    fun findAndLaunchApp(appName: String): AppLaunchResult {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val normalized = appName.lowercase().trim()
        val query = aliases[normalized] ?: normalized

        val exactMatches = mutableListOf<InstalledApp>()
        val partialMatches = mutableListOf<InstalledApp>()

        for (app in apps) {
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName) ?: continue
            val label = pm.getApplicationLabel(app).toString().lowercase()

            if (label == query) {
                exactMatches.add(InstalledApp(pm.getApplicationLabel(app).toString(), app.packageName))
            } else if (label.startsWith(query) || label.split(" ", "-", "_").contains(query)) {
                exactMatches.add(InstalledApp(pm.getApplicationLabel(app).toString(), app.packageName))
            } else if (label.contains(query)) {
                partialMatches.add(InstalledApp(pm.getApplicationLabel(app).toString(), app.packageName))
            }
        }

        val candidates = if (exactMatches.isNotEmpty()) exactMatches else partialMatches

        return when {
            candidates.size == 1 -> {
                launchAppByPackage(candidates.first().packageName)
                AppLaunchResult.Launched(candidates.first().label)
            }
            candidates.size > 1 -> {
                val best = candidates.firstOrNull { it.label.lowercase() == query || it.label.lowercase().startsWith(query) }
                if (best != null && (query.length <= 4 || candidates.size <= 2)) {
                    launchAppByPackage(best.packageName)
                    AppLaunchResult.Launched(best.label)
                } else {
                    AppLaunchResult.DisambiguationRequired(candidates.map { it.label })
                }
            }
            else -> {
                AppLaunchResult.NotFound(appName)
            }
        }
    }

    private fun launchAppByPackage(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }
}

sealed class AppLaunchResult {
    data class Launched(val appName: String) : AppLaunchResult()
    data class DisambiguationRequired(val candidates: List<String>) : AppLaunchResult()
    data class NotFound(val requestedName: String) : AppLaunchResult()
}
