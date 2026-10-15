package com.example.studio.ai

class AiAgent(
    initialProviders: List<AiProvider> = listOf(OfflineHeuristicProvider())
) {
    private val providers = mutableMapOf<String, AiProvider>()
    var activeProviderId: String = "offline_heuristics"

    init {
        for (p in initialProviders) {
            registerProvider(p)
        }
    }

    fun registerProvider(provider: AiProvider) {
        providers[provider.id] = provider
    }

    fun getActiveProvider(): AiProvider {
        return providers[activeProviderId] ?: providers.values.first()
    }

    fun getAvailableProviders(): List<AiProvider> {
        return providers.values.toList()
    }

    suspend fun processUserPrompt(prompt: String, context: AiContext): AiResult {
        val provider = getActiveProvider()
        return try {
            provider.generateResponse(prompt, context)
        } catch (e: Exception) {
            // Fallback to offline heuristic if active network provider fails
            OfflineHeuristicProvider().generateResponse(
                prompt = prompt,
                context = context.copy(lastErrorLog = "Provider ${provider.displayName} failed: ${e.message}")
            )
        }
    }
}
