package com.example.studio.ai

data class AiContext(
    val currentSceneJson: String? = null,
    val selectedNodeName: String? = null,
    val activeScriptContent: String? = null,
    val lastErrorLog: String? = null,
    val projectPath: String? = null
)

data class AiResult(
    val textResponse: String,
    val generatedScript: String? = null,
    val nodeCommand: String? = null,
    val isSuccess: Boolean = true
)

interface AiProvider {
    val id: String
    val displayName: String
    val isOfflineCapable: Boolean get() = false

    suspend fun generateResponse(prompt: String, context: AiContext): AiResult
}
