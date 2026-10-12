package com.example.studio.ai

import com.example.studio.ai.providers.*
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject

/**
 * Main AI Assistant Agent managing provider switching and executing actions on projects & scripts.
 */
class StudioAiAgent {
    private val providers = mutableMapOf<ProviderType, AiProvider>(
        ProviderType.LOCAL_HEURISTIC to LocalHeuristicProvider(),
        ProviderType.OPEN_AI to OpenAiProvider(),
        ProviderType.GEMINI to GeminiProvider(),
        ProviderType.CLAUDE to ClaudeProvider(),
        ProviderType.DEEPSEEK to DeepSeekProvider(),
        ProviderType.OPENAI_COMPATIBLE to OpenAiCompatibleProvider()
    )

    var currentProviderType: ProviderType = ProviderType.LOCAL_HEURISTIC

    fun getActiveProvider(): AiProvider {
        return providers[currentProviderType] ?: providers[ProviderType.LOCAL_HEURISTIC]!!
    }

    fun configureProvider(type: ProviderType, apiKey: String, baseUrl: String? = null) {
        val provider = providers[type]
        if (provider != null) {
            provider.apiKey = apiKey
            if (!baseUrl.isNullOrBlank()) {
                provider.baseUrl = baseUrl
            }
        }
    }

    suspend fun generateScript(prompt: String, contextNode: GameNode? = null): AiResponse {
        return getActiveProvider().generateScript(prompt, contextNode)
    }

    suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse {
        return getActiveProvider().explainError(errorMessage, scriptContent)
    }

    suspend fun modifyScene(prompt: String, project: GameProject): AiResponse {
        return getActiveProvider().modifyScene(prompt, project)
    }

    suspend fun createObject(prompt: String, project: GameProject): AiResponse {
        return getActiveProvider().createObject(prompt, project)
    }
}
