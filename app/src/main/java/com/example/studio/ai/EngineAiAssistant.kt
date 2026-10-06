package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType

class AiAgentRegistry {
    private val providers = mutableMapOf<String, AiProvider>()

    init {
        registerProvider(RuleBasedLocalAiProvider())
        registerProvider(OpenAiCompatibleProvider())
        registerProvider(FirebaseGeminiProvider())
    }

    fun registerProvider(provider: AiProvider) {
        providers[provider.id] = provider
    }

    fun getProvider(id: String): AiProvider? {
        return providers[id]
    }

    fun getAllProviders(): List<AiProvider> {
        return providers.values.toList()
    }
}

class EngineAiAssistant(
    val registry: AiAgentRegistry = AiAgentRegistry()
) {
    private var activeProviderId: String = "local_rule_based"
    private var providerConfigs = mutableMapOf<String, ProviderConfig>(
        "local_rule_based" to ProviderConfig("local_rule_based"),
        "openai_compatible" to ProviderConfig("openai_compatible", baseUrl = "https://api.openai.com/v1", modelName = "gpt-4o-mini"),
        "firebase_gemini" to ProviderConfig("firebase_gemini", modelName = "gemini-1.5-flash")
    )

    fun setActiveProvider(providerId: String) {
        if (registry.getProvider(providerId) != null) {
            activeProviderId = providerId
        }
    }

    fun getActiveProvider(): AiProvider {
        return registry.getProvider(activeProviderId) ?: RuleBasedLocalAiProvider()
    }

    fun updateProviderConfig(config: ProviderConfig) {
        providerConfigs[config.providerId] = config
    }

    fun getProviderConfig(providerId: String): ProviderConfig {
        return providerConfigs[providerId] ?: ProviderConfig(providerId)
    }

    suspend fun processUserPrompt(
        prompt: String,
        context: AiContext
    ): AiResponse {
        val provider = getActiveProvider()
        val config = getProviderConfig(provider.id)
        return provider.generateResponse(prompt, context, config)
    }

    fun executeAction(
        action: AiAction,
        project: GameProject,
        selectedNode: GameNode? = null
    ): Pair<GameProject, GameNode?> {
        when (action) {
            is AiAction.CreateNode -> {
                val nodeTypeEnum = try {
                    NodeType.valueOf(action.nodeType)
                } catch (e: Exception) {
                    NodeType.RIGID_BODY_2D
                }

                val newNode = GameNode(
                    name = action.name,
                    nodeType = nodeTypeEnum,
                    posX = action.posX,
                    posY = action.posY,
                    scaleX = action.width,
                    scaleY = action.height,
                    colorHex = action.colorHex,
                    hasPhysics = (nodeTypeEnum == NodeType.RIGID_BODY_2D || nodeTypeEnum == NodeType.STATIC_BODY_2D),
                    bodyType = action.bodyType,
                    isParticleEmitter = (nodeTypeEnum == NodeType.CPU_PARTICLES_2D)
                )
                project.addNode(newNode)
                return Pair(project, newNode)
            }

            is AiAction.ModifyNodeProperty -> {
                val targetNode = project.findNode(action.nodeId) ?: selectedNode
                if (targetNode != null) {
                    when (action.propertyName) {
                        "posX" -> targetNode.posX = action.value.toFloatOrNull() ?: targetNode.posX
                        "posY" -> targetNode.posY = action.value.toFloatOrNull() ?: targetNode.posY
                        "scaleX" -> targetNode.scaleX = action.value.toFloatOrNull() ?: targetNode.scaleX
                        "scaleY" -> targetNode.scaleY = action.value.toFloatOrNull() ?: targetNode.scaleY
                        "colorHex" -> targetNode.colorHex = action.value
                        "bodyType" -> targetNode.bodyType = action.value
                    }
                }
                return Pair(project, targetNode)
            }

            is AiAction.DeleteNode -> {
                project.removeNode(action.nodeId)
                val remainingNode = project.findNode(project.selectedNodeId)
                return Pair(project, remainingNode)
            }

            is AiAction.GenerateScript -> {
                val targetNode = project.findNode(action.nodeId) ?: selectedNode
                if (targetNode != null) {
                    targetNode.scriptType = action.scriptType
                    targetNode.speedX = action.speedX
                    targetNode.speedY = action.speedY
                    targetNode.targetTag = action.targetTag
                }
                return Pair(project, targetNode)
            }

            is AiAction.ExplainError, is AiAction.SearchProject -> {
                // Read-only diagnostic tools
                return Pair(project, selectedNode)
            }
        }
    }
}
