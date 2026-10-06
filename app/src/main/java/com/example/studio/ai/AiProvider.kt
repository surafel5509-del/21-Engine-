package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject

enum class AiRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL_OUTPUT
}

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: AiRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val pendingAction: AiAction? = null,
    val isActionExecuted: Boolean = false
)

data class AiContext(
    val project: GameProject,
    val selectedNode: GameNode? = null,
    val lastError: String? = null,
    val history: List<AiMessage> = emptyList()
)

sealed class AiAction {
    data class CreateNode(
        val name: String,
        val nodeType: String,
        val posX: Float = 640f,
        val posY: Float = 360f,
        val width: Float = 64f,
        val height: Float = 64f,
        val colorHex: String = "#38BDF8",
        val bodyType: String = "DYNAMIC"
    ) : AiAction()

    data class ModifyNodeProperty(
        val nodeId: String,
        val propertyName: String,
        val value: String
    ) : AiAction()

    data class DeleteNode(
        val nodeId: String
    ) : AiAction()

    data class GenerateScript(
        val nodeId: String,
        val scriptType: String,
        val speedX: Float = 0f,
        val speedY: Float = 0f,
        val targetTag: String = ""
    ) : AiAction()

    data class ExplainError(
        val errorMessage: String,
        val suggestedFix: String
    ) : AiAction()

    data class SearchProject(
        val query: String
    ) : AiAction()
}

data class ProviderConfig(
    val providerId: String,
    val apiKey: String = "",
    val baseUrl: String = "",
    val modelName: String = ""
)

interface AiProvider {
    val id: String
    val name: String
    val isOfflineCapable: Boolean

    suspend fun generateResponse(
        prompt: String,
        context: AiContext,
        config: ProviderConfig
    ): AiResponse
}

data class AiResponse(
    val replyText: String,
    val suggestedAction: AiAction? = null
)
