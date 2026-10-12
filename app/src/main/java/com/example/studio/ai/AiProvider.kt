package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject

enum class ProviderType(val displayName: String) {
    LOCAL_HEURISTIC("Offline Local Agent"),
    OPEN_AI("OpenAI (GPT-4o/3.5)"),
    GEMINI("Google Gemini"),
    CLAUDE("Anthropic Claude"),
    DEEPSEEK("DeepSeek V3/R1"),
    OPENAI_COMPATIBLE("Custom Local / OpenAI Endpoint")
}

data class AiResponse(
    val text: String,
    val suggestedScript: String? = null,
    val modifiedProject: GameProject? = null,
    val isError: Boolean = false
)

/**
 * Interface for pluggable AI Providers in the game studio.
 */
interface AiProvider {
    val type: ProviderType
    var apiKey: String
    var baseUrl: String

    suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse
    suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse
    suspend fun modifyScene(prompt: String, project: GameProject): AiResponse
    suspend fun createObject(prompt: String, project: GameProject): AiResponse
}
