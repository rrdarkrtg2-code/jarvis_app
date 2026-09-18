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
                setupMayaVoice()

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

    private fun setupMayaVoice() {
        val engine = tts ?: return

        // 1. Prioritize Indian English (Maya style) or standard English
        val indianLocale = Locale("en", "IN")
        val availableLangs = engine.availableLanguages
        if (availableLangs != null && availableLangs.contains(indianLocale)) {
            engine.language = indianLocale
        } else {
            engine.language = Locale.ENGLISH
        }

        // 2. Set calm, friendly, natural female AI voice pitch & speed
        engine.setPitch(1.10f)       // Natural, clear feminine pitch (Maya / Siri tone)
        engine.setSpeechRate(1.02f)   // Smooth, fluent speech rate

        // 3. Scan system voices for high-quality female profiles
        try {
            val voices = engine.voices
            if (!voices.isNullOrEmpty()) {
                val femaleVoice = voices.firstOrNull { v ->
                    (v.locale.country == "IN" || v.locale.language == "en") &&
                    (v.name.contains("female", ignoreCase = true) ||
                     v.name.contains("cxx", ignoreCase = true) ||
                     v.name.contains("ahp", ignoreCase = true) ||
                     v.name.contains("enc", ignoreCase = true) ||
                     v.name.contains("sfg", ignoreCase = true) ||
                     v.name.contains("zira", ignoreCase = true) ||
                     v.name.contains("woman", ignoreCase = true))
                } ?: voices.firstOrNull {
                    it.locale.country == "IN" && !it.isNetworkConnectionRequired
                }

                if (femaleVoice != null) {
                    engine.voice = femaleVoice
                }
            }
        } catch (ignored: Exception) {}
    }

    fun speak(text: String, speechRate: Float = 1.02f, pitch: Float = 1.10f) {
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

    fun replay(speechRate: Float = 1.02f, pitch: Float = 1.10f) {
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
