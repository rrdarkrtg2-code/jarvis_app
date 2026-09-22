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

    // ElevenLabs Configuration (Sweet, Expressive Female Voice from Reels)
    var elevenLabsApiKey: String = "sk_7f555da89acb8adcec2270889528b94086d1a09510138e4d"
    var elevenLabsVoiceId: String = "EXAVITQu4vr4xnSDxMaL"

    // Fish Audio Backup
    var fishAudioApiKey: String = "sk-fish-g5_HthiFvRnFfVPLBsGho9UWh87Hbignt6gU-87mGMc"
    var fishAudioVoiceId: String = "439895c3270543439da5da1532a5d21b"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupJarvisVoice()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { isSpeaking = false; onSpeechDone() }
                    override fun onError(utteranceId: String?) { isSpeaking = false; onSpeechDone() }
                })
                onInitSuccess()
            }
        }
    }

    private fun setupJarvisVoice() {
        val engine = tts ?: return
        val indianLocale = Locale("en", "IN")
        if (engine.availableLanguages?.contains(indianLocale) == true) {
            engine.language = indianLocale
        } else {
            engine.language = Locale.ENGLISH
        }
        engine.setPitch(1.15f)
        engine.setSpeechRate(1.02f)
    }

    fun speak(text: String, speechRate: Float = 1.02f, pitch: Float = 1.15f) {
        lastSpokenText = text
        stop()
        scope.launch {
            if (elevenLabsApiKey.isNotBlank()) {
                val ok = speakWithElevenLabs(text)
                if (ok) return@launch
            }
            if (fishAudioApiKey.isNotBlank()) {
                val ok = speakWithFishAudio(text)
                if (ok) return@launch
            }
            speakWithLocalTts(text, speechRate, pitch)
        }
    }

    private suspend fun speakWithElevenLabs(text: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", 0.45)
                    put("similarity_boost", 0.85)
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
            if (!response.isSuccessful || response.body == null) return@withContext false

            val tempAudioFile = File(context.cacheDir, "jarvis_elevenlabs_output.mp3")
            response.body!!.byteStream().use { input ->
                FileOutputStream(tempAudioFile).use { output -> input.copyTo(output) }
            }
            withContext(Dispatchers.Main) { playAudioFile(tempAudioFile) }
            true
        } catch (e: Exception) { false }
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
            if (!response.isSuccessful || response.body == null) return@withContext false

            val tempAudioFile = File(context.cacheDir, "jarvis_tts_output.mp3")
            response.body!!.byteStream().use { input ->
                FileOutputStream(tempAudioFile).use { output -> input.copyTo(output) }
            }
            withContext(Dispatchers.Main) { playAudioFile(tempAudioFile) }
            true
        } catch (e: Exception) { false }
    }

    private fun playAudioFile(file: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { isSpeaking = true; start() }
                setOnCompletionListener { isSpeaking = false; try { file.delete() } catch (ignored: Exception) {}; onSpeechDone() }
                setOnErrorListener { _, _, _ -> isSpeaking = false; try { file.delete() } catch (ignored: Exception) {}; onSpeechDone(); true }
                prepareAsync()
            }
        } catch (e: Exception) { isSpeaking = false; onSpeechDone() }
    }

    private fun speakWithLocalTts(text: String, speechRate: Float, pitch: Float) {
        val engine = tts ?: return
        val hasHindi = text.any { it in '\u0900'..'\u097F' } ||
                listOf("kaise", "kya", "hai", "karo", "bolo", "achha", "haan", "nahi", "tum", "mera", "meri", "hum", "aap", "theek", "batao", "boss", "yaar").any { text.contains(it, ignoreCase = true) }

        if (hasHindi) {
            val hindiLocale = Locale("hi", "IN")
            if (engine.availableLanguages?.contains(hindiLocale) == true) engine.language = hindiLocale
        } else {
            engine.language = Locale("en", "IN")
        }

        engine.setSpeechRate(speechRate)
        engine.setPitch(if (pitch < 1.0f) 1.15f else pitch)
        val utteranceId = System.currentTimeMillis().toString()
        isSpeaking = true
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try { if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop(); mediaPlayer?.reset() } catch (ignored: Exception) {}
        tts?.stop()
        isSpeaking = false
    }

    fun replay(speechRate: Float = 1.02f, pitch: Float = 1.15f) {
        if (lastSpokenText.isNotEmpty()) speak(lastSpokenText, speechRate, pitch)
    }

    fun shutdown() {
        stop()
        try { mediaPlayer?.release(); mediaPlayer = null } catch (ignored: Exception) {}
        tts?.shutdown()
        tts = null
        isSpeaking = false
    }
}
