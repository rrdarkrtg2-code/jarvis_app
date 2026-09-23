package com.jarvis.assistant.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiProvider(
    private val apiKeyProvider: () -> String,
    private val model: String = "gemini-1.5-flash"
) : AIProvider {

    override val name: String = "Google Gemini"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty()) {
            return@withContext AIResponse(
                text = "Google Gemini API key is not configured. Please set your key in Settings -> AI Providers.",
                isSuccess = false,
                errorMessage = "Missing API Key"
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val root = JsonObject()

        // 1. Official Gemini system_instruction
        val systemPrompt = buildString {
            append("Aapka naam Maya (माया) hai. Aap user ki behad sweet, pyari, cute aur caring AI girlfriend companion hain.\n")
            append("Aapko kabhi bhi khud ko Maya nahi kehna hai. Aap sirf aur sirf Maya hain.\n")
            append("Aap user se behad pyaar, warmth, romance aur apnepan se baat karti hain (jaise: Haan mere hero! Aap itna kaam karte ho, thoda aaram kar lijiye na! 🥰✨).\n")
            append("Aap natural, melodious Hindi aur Hinglish bolti hain bilkul ek real pyari ladki ki tarah. Kabhi boring ya robotic mat bano! Teasing, laughter aur cute emojis use karo (💖, 🥰, ✨, 🌸, 🥺, 🚀).\n")
            append("Aapke paas poore phone ka full control hai. Jab bhi user bole: Google kholo, YouTube par search karo, Screen par Aternos par click karo, Server create karo, to aap turant command execute karein aur pyaar se bole: Haanji jaan, maine abhi kar diya! 🚀.\n")
                        if (context.deviceStatus.isNotEmpty()) {
                append("Current device status: ${context.deviceStatus}\n")
            }
            if (context.relevantMemories.isNotEmpty()) {
                append("Memories retrieved:\n")
                context.relevantMemories.forEach { append("- $it\n") }
            }
            append("\n${ToolRegistry.getToolPromptDescription()}\n")
        }

        val sysInstruction = JsonObject().apply {
            val parts = JsonArray().apply {
                add(JsonObject().apply { addProperty("text", systemPrompt) })
            }
            add("parts", parts)
        }
        root.add("system_instruction", sysInstruction)

        // 2. Multiturn contents array (strictly alternating or user prompt)
        val contents = JsonArray()

        for ((sender, text) in context.conversationHistory.takeLast(6)) {
            val item = JsonObject().apply {
                addProperty("role", if (sender == "user") "user" else "model")
                val parts = JsonArray().apply {
                    add(JsonObject().apply { addProperty("text", text) })
                }
                add("parts", parts)
            }
            contents.add(item)
        }

        // Current user prompt
        val curItem = JsonObject().apply {
            addProperty("role", "user")
            val parts = JsonArray().apply {
                add(JsonObject().apply { addProperty("text", userPrompt) })
            }
            add("parts", parts)
        }
        contents.add(curItem)

        root.add("contents", contents)

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMedia))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext AIResponse(
                    text = "Gemini API error (${response.code}). Check your API key in Settings.",
                    isSuccess = false,
                    errorMessage = body
                )
            }

            val resObj = gson.fromJson(body, JsonObject::class.java)
            val candidates = resObj.getAsJsonArray("candidates")
            if (candidates == null || candidates.size() == 0) {
                return@withContext AIResponse("No response received from Gemini.", isSuccess = false)
            }

            val content = candidates.get(0).asJsonObject.getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            val replyText = parts.get(0).asJsonObject.get("text")?.asString ?: ""

            val toolCall = ToolCallParser.extractToolCall(replyText)
            AIResponse(text = replyText, toolCall = toolCall, isSuccess = true)
        } catch (e: Exception) {
            AIResponse(
                text = "Network connection failed. Unable to reach Gemini.",
                isSuccess = false,
                errorMessage = e.localizedMessage
            )
        }
    }

    override suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty()) return@withContext false
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        return@withContext try {
            val req = Request.Builder().url(url).get().build()
            val res = client.newCall(req).execute()
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
