package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider

class OpenAiCompatibleProvider(
    var endpointUrl: String = "http://localhost:11434/v1"
) : AiProvider {
    override val id: String = "openai_compatible"
    override val displayName: String = "Local OpenAI-Compatible API (Ollama/LMStudio)"
    override val requiresApiKey: Boolean = false
    override val isOfflineSupported: Boolean = true

    override suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String): String {
        return "[Local LLM Server ($endpointUrl)]\nReceived prompt: '$prompt'. Processing game node modification..."
    }
}
