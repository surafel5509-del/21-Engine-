package com.example.studio.ai

enum class AiProviderType(val displayName: String, val defaultEndpoint: String, val defaultModel: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"),
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/v1beta", "gemini-1.5-flash"),
    CLAUDE("Anthropic Claude", "https://api.anthropic.com/v1", "claude-3-5-sonnet-20241022"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
    OPENAI_COMPATIBLE("OpenAI Compatible", "https://api.together.xyz/v1", "meta-llama/Llama-3-70b-chat-hf"),
    LOCAL_LLM("Local LLM (Ollama)", "http://10.0.2.2:11434/v1", "llama3")
}

data class AiProviderConfig(
    val providerType: AiProviderType = AiProviderType.OPENAI,
    val apiKey: String = "",
    val apiEndpoint: String = providerType.defaultEndpoint,
    val modelName: String = providerType.defaultModel,
    val temperature: Float = 0.7f
)

data class AiMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActionType {
    CREATE_NODE,
    MODIFY_NODE,
    DELETE_NODE,
    GENERATE_SCRIPT,
    EXPLAIN_ERROR,
    FIX_CODE,
    SEARCH_PROJECT,
    CREATE_ASSET_PRESET,
    UNKNOWN
}

data class AiAgentAction(
    val type: ActionType,
    val targetNodeName: String = "",
    val nodeType: String = "SPRITE_2D",
    val propertyKey: String = "",
    val propertyValue: String = "",
    val scriptContent: String = "",
    val description: String = "",
    val searchPattern: String = ""
)

data class AiAgentResponse(
    val replyText: String,
    val actions: List<AiAgentAction> = emptyList(),
    val rawResponse: String = ""
)
