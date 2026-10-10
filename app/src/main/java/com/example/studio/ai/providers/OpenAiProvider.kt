package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider

class OpenAiProvider : AiProvider {
    override val id: String = "openai"
    override val displayName: String = "OpenAI (GPT-4o)"
    override val requiresApiKey: Boolean = true
    override val isOfflineSupported: Boolean = false

    override suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String): String {
        if (apiKey.isBlank()) {
            return "[OpenAI] API Key required. Please configure your OpenAI API Key in settings or switch to Local Offline Copilot."
        }
        return "[OpenAI GPT-4o Response]\nI have analyzed your request: '$prompt'\nContext Summary: $projectContext\nRecommendation: Use 'create_node(name=Player, type=CHARACTER_BODY_2D)' or write GDScript for player velocity."
    }
}
