package com.example.studio.ai

object AiProviderFactory {
    fun createProvider(type: AiProviderType): AiProvider {
        return when (type) {
            AiProviderType.OPENAI -> OpenAiProvider()
            AiProviderType.GEMINI -> GeminiProvider()
            AiProviderType.CLAUDE -> ClaudeProvider()
            AiProviderType.DEEPSEEK -> DeepSeekProvider()
            AiProviderType.OPENAI_COMPATIBLE -> OpenAiCompatibleProvider()
            AiProviderType.LOCAL_LLM -> LocalLlmProvider()
        }
    }
}
