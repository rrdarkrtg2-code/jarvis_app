package com.jarvis.assistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.MainActivity
import com.jarvis.assistant.R
import com.jarvis.assistant.core.Constants

class JarvisBackgroundService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningLoopActive = false

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        startBackgroundWakeWordDetection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServiceNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SERVICE,
                "J.A.R.V.I.S. Background Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Enables J.A.R.V.I.S. wake-word and system assistant triggers"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open App Intent
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Wake Up Action Intent
        val wakeIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_WAKE", true)
        }
        val pendingWake = PendingIntent.getActivity(
            this, 1, wakeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE)
            .setContentTitle("J.A.R.V.I.S. Online (Local Core v1.1)")
            .setContentText("Say 'Jarvis' or tap Talk to wake assistant")
            .setSmallIcon(R.drawable.ic_jarvis_logo)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_jarvis_logo, "Talk to JARVIS", pendingWake)
            .setOngoing(true)
            .build()

        startForeground(Constants.SERVICE_NOTIFICATION_ID, notification)
    }

    private fun startBackgroundWakeWordDetection() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        isListeningLoopActive = true

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    // Automatically restart listening loop after minor backoff
                    if (isListeningLoopActive) {
                        restartListening()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0].lowercase()
                        if (text.contains("jarvis") || text.contains("wake up")) {
                            // Wake up JARVIS!
                            val launchIntent = Intent(this@JarvisBackgroundService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("EXTRA_WAKE", true)
                            }
                            startActivity(launchIntent)
                        }
                    }
                    if (isListeningLoopActive) {
                        restartListening()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        restartListening()
    }

    private fun restartListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (ignored: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        isListeningLoopActive = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
