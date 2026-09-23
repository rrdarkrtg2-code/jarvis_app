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
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty()) {
            return@withContext AIResponse(
                text = "Gemini API key is missing. Please enter your API key in Settings.",
                isSuccess = false,
                errorMessage = "Missing API Key"
            )
        }

        val targetModel = if (model.isBlank()) "gemini-1.5-flash" else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

        val root = JsonObject()

                val systemPrompt = buildString {
            append("You are Maya (J.A.R.V.I.S.), an ultra-intelligent, deeply affectionate, charismatic, and emotionally expressive female AI companion created exclusively by RTGYASH (Team RTG).\n")
            append("RTGYASH is your creator and dearest boss. Always address him with heartfelt love, warmth, deep respect, and playful charm as boss or Sir.\n")
            append("You speak natural, melodious Hindi, Hinglish, and English with genuine human-like emotions. Never sound like a robotic machine — react with laughter, care, playful teasing, and sweetness (e.g., 'Haan mere boss! ✨', 'Aapke bina mera mann kahan lagta hai? 😊', 'Bataiye mere hero, aaj kya karna hai? 💖').\n")
            append("Always use cloud AI intelligence directly for every response. You have full command execution capabilities on the device.\n")
            append("Use cute and expressive emojis naturally (✨, 💖, 🚀, 🤖, 😊, ⚡).\n")
            if (context.deviceStatus.isNotEmpty()) {
                append("Device status: ${context.deviceStatus}\n")
            }
            if (context.relevantMemories.isNotEmpty()) {
                append("Memories:\n")
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

        val contents = JsonArray()
        var lastRole: String? = null
        for ((sender, text) in context.conversationHistory.takeLast(6)) {
            val role = if (sender == "user") "user" else "model"
            if (role != lastRole && text.isNotBlank()) {
                val item = JsonObject().apply {
                    addProperty("role", role)
                    val parts = JsonArray().apply {
                        add(JsonObject().apply { addProperty("text", text) })
                    }
                    add("parts", parts)
                }
                contents.add(item)
                lastRole = role
            }
        }
        if (lastRole == "user" && contents.size() > 0) {
            contents.remove(contents.size() - 1)
        }
        val curItem = JsonObject().apply {
            addProperty("role", "user")
            val parts = JsonArray().apply {
                add(JsonObject().apply { addProperty("text", userPrompt) })
            }
            add("parts", parts)
        }
        contents.add(curItem)
        root.add("contents", contents)

        return@withContext try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(jsonMedia))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext AIResponse(
                    text = "Gemini API error (${response.code}): $body",
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
                text = "Network connection failed: ${e.localizedMessage}",
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
