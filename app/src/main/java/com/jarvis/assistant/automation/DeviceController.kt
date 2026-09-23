package com.jarvis.assistant.automation

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.provider.Settings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun getBatteryLevel(): String {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val isCharging: Boolean = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING

        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        return if (pct >= 0) {
            val chargingText = if (isCharging) " and currently charging" else ""
            "Your battery is at $pct percent$chargingText, Sir."
        } else {
            "Unable to read battery status at this moment, Sir."
        }
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            cameraManager.setTorchMode(cameraId, enabled)
            true
        } catch (e: CameraAccessException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun adjustVolume(increase: Boolean): String {
        return try {
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager?.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                AudioManager.FLAG_SHOW_UI
            )
            if (increase) "Volume increased, Sir." else "Volume decreased, Sir."
        } catch (e: Exception) {
            "Could not adjust volume, Sir."
        }
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return "It's ${sdf.format(Date())}, Sir."
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        return "Today is ${sdf.format(Date())}, Sir."
    }

    fun getCurrentDay(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return "Today is ${sdf.format(Date())}, Sir."
    }

    fun openSettings(type: String): Boolean {
        val intent = when (type.lowercase()) {
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "accessibility" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun searchWeb(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    fun searchYouTube(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    fun createFolder(folderName: String): String {
        return try {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val dir = File(root, folderName)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            "Folder '$folderName' created successfully in Documents, Sir! 📁"
        } catch (e: Exception) {
            "Could not create folder: ${e.localizedMessage}"
        }
    }

    fun createWebsite(title: String, htmlContent: String): String {
        return try {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").ifEmpty { "index" }
            val file = File(root, "$cleanTitle.html")
            file.writeText(htmlContent)
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(file), "text/html")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (ignored: Exception) {}
            "Website '$title' generated successfully and saved to Downloads, Sir! 🌐✨"
        } catch (e: Exception) {
            "Website '$title' generated, Sir! 🌐"
        }
    }
}
