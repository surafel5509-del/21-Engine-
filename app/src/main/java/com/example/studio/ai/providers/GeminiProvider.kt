package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider

class GeminiProvider : AiProvider {
    override val id: String = "gemini"
    override val displayName: String = "Google Gemini Pro"
    override val requiresApiKey: Boolean = true
    override val isOfflineSupported: Boolean = false

    override suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String): String {
        if (apiKey.isBlank()) {
            return "[Google Gemini] API Key required. Please configure your Gemini API Key in settings."
        }
        return "[Google Gemini Pro Response]\nAnalyzed request: '$prompt'. Suggested action: Modify gravity or add character controller script."
    }
}
