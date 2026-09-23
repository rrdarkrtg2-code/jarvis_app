package com.jarvis.assistant.voice

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

class TtsManager(
    private val context: Context,
    private val onInitSuccess: () -> Unit = {},
    private val onSpeechDone: () -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    var isSpeaking: Boolean = false
        private set

    private var lastSpokenText: String = ""

    // ElevenLabs API Configuration (Sweet & Expressive Female AI Voice from the reels)
    var elevenLabsApiKey: String = "sk_7f555da89acb8adcec2270889528b94086d1a09510138e4d"
    var elevenLabsVoiceId: String = "pFZP5JQG7iQjIQuC4Bku" // Sarah - warm, expressive, gentle female voice

    // Fish Audio API Configuration (Backup AI voice)
    var fishAudioApiKey: String = "sk-fish-g5_HthiFvRnFfVPLBsGho9UWh87Hbignt6gU-87mGMc"
    var fishAudioVoiceId: String = "439895c3270543439da5da1532a5d21b"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

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
        val indianLocale = Locale("en", "IN")
        val availableLangs = engine.availableLanguages
        if (availableLangs != null && availableLangs.contains(indianLocale)) {
            engine.language = indianLocale
        } else {
            engine.language = Locale.ENGLISH
        }

        engine.setPitch(1.25f)
        engine.setSpeechRate(1.02f)

        try {
            val voices = engine.voices
            if (!voices.isNullOrEmpty()) {
                val femaleVoice = voices.firstOrNull { v ->
                    (v.locale.country == "IN" || v.locale.language == "en" || v.locale.language == "hi") &&
                    (v.name.contains("female", ignoreCase = true) ||
                     v.name.contains("cxx", ignoreCase = true) ||
                     v.name.contains("hie", ignoreCase = true) ||
                     v.name.contains("cfc", ignoreCase = true) ||
                     v.name.contains("ahp", ignoreCase = true) ||
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

    fun speak(text: String, speechRate: Float = 1.0f, pitch: Float = 1.25f) {
        lastSpokenText = text
        stop()

        scope.launch {
            // 1. Try ElevenLabs (Expressive Multilingual Indian/Hindi Female Voice)
            if (elevenLabsApiKey.isNotBlank()) {
                val ok = speakWithElevenLabs(text)
                if (ok) return@launch
            }

            // 2. Try Fish Audio as backup
            if (fishAudioApiKey.isNotBlank()) {
                val ok = speakWithFishAudio(text)
                if (ok) return@launch
            }

            // 3. Fallback to on-device high quality female TTS
            speakWithLocalTts(text, speechRate, pitch)
        }
    }

    private suspend fun speakWithElevenLabs(text: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", 0.35)
                    put("similarity_boost", 0.90)
                }
                put("voice_settings", voiceSettings)
            }.toString()

            val request = Request.Builder()
                .url("https://api.elevenlabs.io/v1/text-to-speech/$elevenLabsVoiceId")
                .header("xi-api-key", elevenLabsApiKey)
                .header("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                return@withContext false
            }

            val tempAudioFile = File(context.cacheDir, "jarvis_elevenlabs_output.mp3")
            response.body!!.byteStream().use { input ->
                FileOutputStream(tempAudioFile).use { output ->
                    input.copyTo(output)
                }
            }

            withContext(Dispatchers.Main) {
                playAudioFile(tempAudioFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun speakWithFishAudio(text: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("reference_id", fishAudioVoiceId)
                put("format", "mp3")
            }.toString()

            val request = Request.Builder()
                .url("https://api.fish.audio/v1/tts")
                .header("Authorization", "Bearer $fishAudioApiKey")
                .header("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                return@withContext false
            }

            val tempAudioFile = File(context.cacheDir, "jarvis_tts_output.mp3")
            response.body!!.byteStream().use { input ->
                FileOutputStream(tempAudioFile).use { output ->
                    input.copyTo(output)
                }
            }

            withContext(Dispatchers.Main) {
                playAudioFile(tempAudioFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun playAudioFile(file: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    isSpeaking = true
                    start()
                }
                setOnCompletionListener {
                    isSpeaking = false
                    try { file.delete() } catch (ignored: Exception) {}
                    onSpeechDone()
                }
                setOnErrorListener { _, _, _ ->
                    isSpeaking = false
                    try { file.delete() } catch (ignored: Exception) {}
                    onSpeechDone()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            isSpeaking = false
            onSpeechDone()
        }
    }

    private fun speakWithLocalTts(text: String, speechRate: Float, pitch: Float) {
        val engine = tts ?: return
        val hasHindi = text.any { it in '\u0900'..'\u097F' } ||
                listOf("kaise", "kya", "hai", "karo", "bolo", "achha", "haan", "nahi", "tum", "mera", "meri", "hum", "aap", "theek", "batao", "boss", "yaar", "namaste", "shukriya").any { text.contains(it, ignoreCase = true) }

        if (hasHindi) {
            val hindiLocale = Locale("hi", "IN")
            if (engine.availableLanguages?.contains(hindiLocale) == true) {
                engine.language = hindiLocale
            }
        } else {
            engine.language = Locale("en", "IN")
        }

        engine.setSpeechRate(speechRate)
        engine.setPitch(1.25f)
        val utteranceId = System.currentTimeMillis().toString()
        isSpeaking = true
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
        } catch (ignored: Exception) {}

        tts?.stop()
        isSpeaking = false
    }

    fun replay(speechRate: Float = 1.0f, pitch: Float = 1.25f) {
        if (lastSpokenText.isNotEmpty()) {
            speak(lastSpokenText, speechRate, pitch)
        }
    }

    fun shutdown() {
        stop()
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (ignored: Exception) {}
        tts?.shutdown()
        tts = null
        isSpeaking = false
    }
}
