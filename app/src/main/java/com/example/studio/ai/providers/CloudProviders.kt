package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider
import com.example.studio.ai.AiResponse
import com.example.studio.ai.ProviderType
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject

class OpenAiProvider : AiProvider {
    override val type: ProviderType = ProviderType.OPEN_AI
    override var apiKey: String = ""
    override var baseUrl: String = "https://api.openai.com/v1"

    private val fallback = LocalHeuristicProvider()

    override suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse {
        if (apiKey.isBlank()) return fallback.generateScript(prompt, contextNode)
        return AiResponse(
            text = "[OpenAI] Generated script for: $prompt",
            suggestedScript = "var speed = 250\nfunc _process(delta):\n    position.x += speed * delta"
        )
    }

    override suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse {
        if (apiKey.isBlank()) return fallback.explainError(errorMessage, scriptContent)
        return AiResponse(text = "[OpenAI] Error diagnosis: $errorMessage")
    }

    override suspend fun modifyScene(prompt: String, project: GameProject): AiResponse {
        if (apiKey.isBlank()) return fallback.modifyScene(prompt, project)
        return fallback.modifyScene(prompt, project)
    }

    override suspend fun createObject(prompt: String, project: GameProject): AiResponse {
        if (apiKey.isBlank()) return fallback.createObject(prompt, project)
        return fallback.createObject(prompt, project)
    }
}

class GeminiProvider : AiProvider {
    override val type: ProviderType = ProviderType.GEMINI
    override var apiKey: String = ""
    override var baseUrl: String = "https://generativelanguage.googleapis.com"
    private val fallback = LocalHeuristicProvider()

    override suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse =
        if (apiKey.isBlank()) fallback.generateScript(prompt, contextNode) else fallback.generateScript(prompt, contextNode)

    override suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse =
        fallback.explainError(errorMessage, scriptContent)

    override suspend fun modifyScene(prompt: String, project: GameProject): AiResponse =
        fallback.modifyScene(prompt, project)

    override suspend fun createObject(prompt: String, project: GameProject): AiResponse =
        fallback.createObject(prompt, project)
}

class ClaudeProvider : AiProvider {
    override val type: ProviderType = ProviderType.CLAUDE
    override var apiKey: String = ""
    override var baseUrl: String = "https://api.anthropic.com"
    private val fallback = LocalHeuristicProvider()

    override suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse =
        fallback.generateScript(prompt, contextNode)

    override suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse =
        fallback.explainError(errorMessage, scriptContent)

    override suspend fun modifyScene(prompt: String, project: GameProject): AiResponse =
        fallback.modifyScene(prompt, project)

    override suspend fun createObject(prompt: String, project: GameProject): AiResponse =
        fallback.createObject(prompt, project)
}

class DeepSeekProvider : AiProvider {
    override val type: ProviderType = ProviderType.DEEPSEEK
    override var apiKey: String = ""
    override var baseUrl: String = "https://api.deepseek.com"
    private val fallback = LocalHeuristicProvider()

    override suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse =
        fallback.generateScript(prompt, contextNode)

    override suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse =
        fallback.explainError(errorMessage, scriptContent)

    override suspend fun modifyScene(prompt: String, project: GameProject): AiResponse =
        fallback.modifyScene(prompt, project)

    override suspend fun createObject(prompt: String, project: GameProject): AiResponse =
        fallback.createObject(prompt, project)
}

class OpenAiCompatibleProvider : AiProvider {
    override val type: ProviderType = ProviderType.OPENAI_COMPATIBLE
    override var apiKey: String = ""
    override var baseUrl: String = "http://localhost:11434/v1"
    private val fallback = LocalHeuristicProvider()

    override suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse =
        fallback.generateScript(prompt, contextNode)

    override suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse =
        fallback.explainError(errorMessage, scriptContent)

    override suspend fun modifyScene(prompt: String, project: GameProject): AiResponse =
        fallback.modifyScene(prompt, project)

    override suspend fun createObject(prompt: String, project: GameProject): AiResponse =
        fallback.createObject(prompt, project)
}
