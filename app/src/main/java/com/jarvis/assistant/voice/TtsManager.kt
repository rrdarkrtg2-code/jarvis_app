package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onInitSuccess: () -> Unit = {},
    private val onSpeechDone: () -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    var isSpeaking: Boolean = false
        private set

    private var lastSpokenText: String = ""

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configure default British J.A.R.V.I.S. voice
                setupJarvisVoice()

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        onSpeechDone()
                    }

                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        onSpeechDone()
                    }
                })
                onInitSuccess()
            }
        }
    }

    private fun setupJarvisVoice() {
        val engine = tts ?: return

        // 1. Set locale to Great Britain for authentic British accent
        engine.language = Locale.UK

        // 2. Set calm, deep, steady J.A.R.V.I.S. pitch and rate
        engine.setPitch(0.88f)      // Slightly lower pitch for Paul Bettany tone
        engine.setSpeechRate(0.96f)  // Composed, articulate pacing

        // 3. Scan available system voices for British male profiles
        try {
            val voices = engine.voices
            if (!voices.isNullOrEmpty()) {
                val preferredJarvisVoice = voices.firstOrNull { v ->
                    v.locale.language == "en" &&
                    (v.locale.country == "GB" || v.locale.country == "UK") &&
                    (v.name.contains("male", ignoreCase = true) ||
                     v.name.contains("rjs", ignoreCase = true) ||
                     v.name.contains("gbd", ignoreCase = true) ||
                     v.name.contains("fis", ignoreCase = true))
                } ?: voices.firstOrNull {
                    it.locale.language == "en" &&
                    (it.locale.country == "GB" || it.locale.country == "UK") &&
                    !it.isNetworkConnectionRequired
                }

                if (preferredJarvisVoice != null) {
                    engine.voice = preferredJarvisVoice
                }
            }
        } catch (ignored: Exception) {}
    }

    fun speak(text: String, speechRate: Float = 0.96f, pitch: Float = 0.88f) {
        lastSpokenText = text
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(pitch)
        val utteranceId = System.currentTimeMillis().toString()
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    fun replay(speechRate: Float = 0.96f, pitch: Float = 0.88f) {
        if (lastSpokenText.isNotEmpty()) {
            speak(lastSpokenText, speechRate, pitch)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isSpeaking = false
    }
}
