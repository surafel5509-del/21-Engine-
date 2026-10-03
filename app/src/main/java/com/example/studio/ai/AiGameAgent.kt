package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import com.example.studio.model.PrefabManager

data class AgentActionResult(
    val replyText: String,
    val modifiedProject: GameProject? = null
)

/**
 * High-level AI Game Development Agent that analyzes project state, generates GDScript, creates scene objects, and fixes issues.
 */
class AiGameAgent(var config: AiConfig = AiConfig()) {

    var provider: AiProvider = AiProviderFactory.createProvider(config)

    fun updateConfig(newConfig: AiConfig) {
        config = newConfig
        provider = AiProviderFactory.createProvider(newConfig)
    }

    suspend fun processUserPrompt(prompt: String, project: GameProject): AgentActionResult {
        val lower = prompt.lowercase()

        // 1. Direct Local Action Parsing for Instant Offline Response
        if (lower.contains("add coin") || lower.contains("spawn coin")) {
            val coinNode = PrefabManager.instantiatePrefab("GoldCoin", 640f, 300f)
            if (coinNode != null) {
                val updatedProj = project.copy()
                updatedProj.addNode(coinNode)
                return AgentActionResult("Added a new GoldCoin prefab node to the scene!", updatedProj)
            }
        }

        if (lower.contains("add platform") || lower.contains("create platform")) {
            val platformNode = PrefabManager.instantiatePrefab("FloatingPlatform", 640f, 450f)
            if (platformNode != null) {
                val updatedProj = project.copy()
                updatedProj.addNode(platformNode)
                return AgentActionResult("Added a new FloatingPlatform static body node to the scene!", updatedProj)
            }
        }

        if (lower.contains("add enemy") || lower.contains("create slime")) {
            val enemyNode = PrefabManager.instantiatePrefab("EnemySlime", 750f, 400f)
            if (enemyNode != null) {
                val updatedProj = project.copy()
                updatedProj.addNode(enemyNode)
                return AgentActionResult("Added a new EnemySlime character node to the scene!", updatedProj)
            }
        }

        if (lower.contains("create player") || lower.contains("add player")) {
            val playerNode = PrefabManager.instantiatePrefab("PlayerCharacter", 300f, 300f)
            if (playerNode != null) {
                val updatedProj = project.copy()
                updatedProj.addNode(playerNode)
                return AgentActionResult("Added a new PlayerCharacter node to the scene!", updatedProj)
            }
        }

        // 2. LLM Completion via Provider
        val projectSummary = buildProjectContext(project)
        val llmResponse = provider.generateCompletion(prompt, projectSummary)

        return AgentActionResult(llmResponse, null)
    }

    private fun buildProjectContext(project: GameProject): String {
        val sb = StringBuilder()
        sb.append("Project Name: ").append(project.name).append("\n")
        sb.append("Node Count: ").append(project.nodes.size).append("\n")
        sb.append("Nodes List:\n")
        project.nodes.forEach { node ->
            sb.append("- Name: ").append(node.name)
                .append(", Type: ").append(node.nodeType.name)
                .append(", Pos: (").append(node.posX).append(", ").append(node.posY).append(")")
                .append(", Behavior: ").append(node.behavior.name).append("\n")
        }
        return sb.toString()
    }
}
