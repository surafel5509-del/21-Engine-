package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider

class DeepSeekProvider : AiProvider {
    override val id: String = "deepseek"
    override val displayName: String = "DeepSeek Coder"
    override val requiresApiKey: Boolean = true
    override val isOfflineSupported: Boolean = false

    override suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String): String {
        if (apiKey.isBlank()) {
            return "[DeepSeek] API Key required. Please configure your DeepSeek API Key."
        }
        return "[DeepSeek Coder Response]\nGenerated logic for '$prompt'."
    }
}
