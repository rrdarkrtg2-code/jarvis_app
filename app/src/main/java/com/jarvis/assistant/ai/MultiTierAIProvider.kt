package com.jarvis.assistant.ai

class MultiTierAIProvider(
    private val primary: AIProvider,
    private val fallback: AIProvider?
) : AIProvider {

    override val name: String
        get() = if (fallback != null) "${primary.name} (Fallback: ${fallback.name})" else primary.name

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse {
        val primaryResponse = primary.generateResponse(userPrompt, context)
        if (primaryResponse.isSuccess) {
            return primaryResponse
        }

        if (fallback != null) {
            val fallbackResponse = fallback.generateResponse(userPrompt, context)
            if (fallbackResponse.isSuccess) {
                return fallbackResponse.copy(
                    text = "${fallbackResponse.text}\n\n[Switched to fallback provider: ${fallback.name}]"
                )
            }
        }

        return primaryResponse
    }

    override suspend fun testConnection(): Boolean {
        if (primary.testConnection()) return true
        return fallback?.testConnection() ?: false
    }
}
