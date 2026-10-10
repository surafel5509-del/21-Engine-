package com.example.studio.ai

interface AiProvider {
    val id: String
    val displayName: String
    val requiresApiKey: Boolean
    val isOfflineSupported: Boolean

    suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String = ""): String
}
