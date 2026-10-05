package com.example.studio.ai

interface AiProvider {
    val providerType: AiProviderType
    suspend fun sendPrompt(messages: List<AiMessage>, config: AiProviderConfig): String
    fun getSupportedModels(): List<String>
}
