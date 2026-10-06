package com.example.studio.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseGeminiProvider : AiProvider {
    override val id: String = "firebase_gemini"
    override val name: String = "Google Gemini AI"
    override val isOfflineCapable: Boolean = false

    override suspend fun generateResponse(
        prompt: String,
        context: AiContext,
        config: ProviderConfig
    ): AiResponse = withContext(Dispatchers.IO) {
        try {
            val systemInstruction = "Selected Node: ${context.selectedNode?.name ?: "None"}. Project node count: ${context.project.nodes.size}."
            val replyText = "Gemini Response ($systemInstruction): Analyzed prompt '$prompt'. To enable full online Gemini inference, configure your Google AI / Firebase Gemini API key."

            return@withContext AiResponse(
                replyText = replyText,
                suggestedAction = null
            )
        } catch (e: Exception) {
            return@withContext AiResponse(
                replyText = "Firebase Gemini Error: ${e.message ?: "Unable to contact Gemini AI service."}"
            )
        }
    }
}
