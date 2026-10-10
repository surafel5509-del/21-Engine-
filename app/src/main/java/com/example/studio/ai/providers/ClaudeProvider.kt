package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider

class ClaudeProvider : AiProvider {
    override val id: String = "claude"
    override val displayName: String = "Anthropic Claude 3.5 Sonnet"
    override val requiresApiKey: Boolean = true
    override val isOfflineSupported: Boolean = false

    override suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String): String {
        if (apiKey.isBlank()) {
            return "[Anthropic Claude] API Key required. Please configure your Anthropic API Key."
        }
        return "[Claude 3.5 Sonnet Response]\nAnalyzing game structure. Proposed GDScript: func _process(delta): position_x += 200 * delta"
    }
}
