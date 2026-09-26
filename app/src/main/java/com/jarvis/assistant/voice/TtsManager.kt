package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(context.applicationContext, this)

    @Volatile
    var isSpeaking: Boolean = false
        private set

    private var initialized = false
    private var lastSpokenText: String = ""
    private var lastRate: Float = 1.0f
    private var lastPitch: Float = 1.0f

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
        if (initialized) {
            textToSpeech.language = Locale.getDefault()
        }
    }

    fun speak(text: String, speechRate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!initialized || text.isBlank()) return
        lastSpokenText = text
        lastRate = speechRate
        lastPitch = pitch
        textToSpeech.setSpeechRate(speechRate.coerceIn(0.1f, 3.0f))
        textToSpeech.setPitch(pitch.coerceIn(0.1f, 3.0f))
        isSpeaking = true
        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis-${System.currentTimeMillis()}"
        )
    }

    fun replay(speechRate: Float = lastRate, pitch: Float = lastPitch) {
        if (lastSpokenText.isNotBlank()) {
            speak(lastSpokenText, speechRate, pitch)
        }
    }

    fun stop() {
        textToSpeech.stop()
        isSpeaking = false
    }

    fun shutdown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        isSpeaking = false
    }
}
