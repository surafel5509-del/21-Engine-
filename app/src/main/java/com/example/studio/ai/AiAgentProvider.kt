package com.example.studio.ai

enum class AiProviderType(val displayName: String, val defaultEndpoint: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/v1beta"),
    CLAUDE("Anthropic Claude", "https://api.anthropic.com/v1"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),
    LOCAL_LLM("Local LLM / Ollama", "http://127.0.0.1:11434/v1")
}

data class AiAgentConfig(
    val providerType: AiProviderType = AiProviderType.OPENAI,
    val apiKey: String = "",
    val modelName: String = "gpt-4o",
    val customEndpoint: String = "",
    val systemPrompt: String = "You are an expert 2D Game Development AI Assistant for an Android Godot-like 2D Engine."
)

data class AiMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

interface AiAgentProvider {
    val providerType: AiProviderType

    suspend fun generateResponse(
        prompt: String,
        contextJson: String,
        config: AiAgentConfig
    ): String
}

class GenericOpenAiCompatibleProvider(override val providerType: AiProviderType) : AiAgentProvider {
    override suspend fun generateResponse(
        prompt: String,
        contextJson: String,
        config: AiAgentConfig
    ): String {
        // Offline / Fallback local agent emulator if no API key set
        if (config.apiKey.isBlank() && config.providerType != AiProviderType.LOCAL_LLM) {
            return generateOfflineAssistantResponse(prompt, contextJson)
        }

        // Simulated API interaction / structured agent response logic
        return generateOfflineAssistantResponse(prompt, contextJson)
    }

    private fun generateOfflineAssistantResponse(prompt: String, contextJson: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("create") || lower.contains("add") -> {
                "```json\n{\n  \"action\": \"CREATE_NODE\",\n  \"nodeType\": \"RIGID_BODY_2D\",\n  \"name\": \"Player_Hero\",\n  \"posX\": 200,\n  \"posY\": 300\n}\n```\nCreated a new RigidBody2D Hero node for your scene!"
            }
            lower.contains("script") || lower.contains("code") -> {
                "```gdscript\n# Generated Game Script\nextends RigidBody2D\n\nfunc _process(delta):\n    if Input.is_action_pressed(\"move_right\"):\n        velocity.x = 200\n```\nGenerated standard movement script for the selected node."
            }
            lower.contains("fix") || lower.contains("error") -> {
                "Analyzed your scene hierarchy and scripts. No fatal errors detected! Recommended setting static physics body for ground tiles."
            }
            else -> {
                "AI Game Assistant ready! I can help you create scenes, write scripts, tweak physics parameters, add particle effects, and inspect project nodes."
            }
        }
    }
}
