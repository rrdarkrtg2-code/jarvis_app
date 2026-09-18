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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isCurrentlyRecognizing = false

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
                description = "Maintains wake word and phone screen automation"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val wakeIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_WAKE", true)
        }
        val pendingWake = PendingIntent.getActivity(
            this, 1, wakeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE)
            .setContentTitle("J.A.R.V.I.S. Core Online")
            .setContentText("Boss RTGYASH • Say 'Jarvis' or 'Maya' to wake")
            .setSmallIcon(R.drawable.ic_jarvis_logo)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_jarvis_logo, "Wake Assistant", pendingWake)
            .setOngoing(true)
            .build()

        startForeground(Constants.SERVICE_NOTIFICATION_ID, notification)
    }

    private fun startBackgroundWakeWordDetection() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        isListeningLoopActive = true
        setupRecognizer()
    }

    private fun setupRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isCurrentlyRecognizing = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isCurrentlyRecognizing = false
                    }

                    override fun onError(error: Int) {
                        isCurrentlyRecognizing = false
                        // DO NOT immediately loop-restart! Wait 2.5 seconds to avoid beeping
                        if (isListeningLoopActive) {
                            mainHandler.removeCallbacksAndMessages(null)
                            mainHandler.postDelayed({ restartListening() }, 2500)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isCurrentlyRecognizing = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0].lowercase()
                            if (text.contains("jarvis") || text.contains("maya") || text.contains("wake up")) {
                                val launchIntent = Intent(this@JarvisBackgroundService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("EXTRA_WAKE", true)
                                }
                                startActivity(launchIntent)
                            }
                        }
                        if (isListeningLoopActive) {
                            mainHandler.removeCallbacksAndMessages(null)
                            mainHandler.postDelayed({ restartListening() }, 1500)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0].lowercase()
                            if (text.contains("jarvis") || text.contains("maya") || text.contains("wake up")) {
                                val launchIntent = Intent(this@JarvisBackgroundService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("EXTRA_WAKE", true)
                                }
                                startActivity(launchIntent)
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            restartListening()
        } catch (ignored: Exception) {}
    }

    private fun restartListening() {
        if (!isListeningLoopActive || isCurrentlyRecognizing) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (ignored: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        isListeningLoopActive = false
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
