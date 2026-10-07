package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import org.json.JSONObject

class AiGameDevAgent(
    var config: AiAgentConfig = AiAgentConfig()
) {
    private val providers = mutableMapOf<AiProviderType, AiAgentProvider>()

    init {
        AiProviderType.entries.forEach { providerType ->
            providers[providerType] = GenericOpenAiCompatibleProvider(providerType)
        }
    }

    fun getActiveProvider(): AiAgentProvider {
        return providers[config.providerType] ?: GenericOpenAiCompatibleProvider(config.providerType)
    }

    suspend fun processUserPrompt(
        prompt: String,
        project: GameProject
    ): AiAgentResult {
        val contextJson = buildProjectContext(project)
        val provider = getActiveProvider()
        val response = provider.generateResponse(prompt, contextJson, config)

        val commandAction = parseAgentCommand(response, project)
        return AiAgentResult(
            responseText = response,
            appliedActionDescription = commandAction?.description
        )
    }

    private fun buildProjectContext(project: GameProject): String {
        val json = JSONObject()
        json.put("projectName", project.name)
        json.put("nodeCount", project.nodes.size)
        json.put("selectedNodeId", project.selectedNodeId)

        val nodesArr = org.json.JSONArray()
        project.nodes.forEach { node ->
            val nodeObj = JSONObject()
            nodeObj.put("id", node.id)
            nodeObj.put("name", node.name)
            nodeObj.put("type", node.nodeType.name)
            nodeObj.put("posX", node.posX)
            nodeObj.put("posY", node.posY)
            nodeObj.put("script", node.scriptCode)
            nodesArr.put(nodeObj)
        }
        json.put("nodes", nodesArr)
        return json.toString()
    }

    private fun parseAgentCommand(response: String, project: GameProject): AiCommandAction? {
        if (!response.contains("```json")) return null

        return try {
            val jsonStart = response.indexOf("```json") + 7
            val jsonEnd = response.indexOf("```", jsonStart)
            if (jsonEnd == -1) return null

            val jsonStr = response.substring(jsonStart, jsonEnd).trim()
            val jsonObj = JSONObject(jsonStr)

            when (jsonObj.optString("action")) {
                "CREATE_NODE" -> {
                    val name = jsonObj.optString("name", "AiNode")
                    val typeStr = jsonObj.optString("nodeType", "SPRITE_2D")
                    val type = try { NodeType.valueOf(typeStr) } catch (e: Exception) { NodeType.SPRITE_2D }
                    val posX = jsonObj.optDouble("posX", 400.0).toFloat()
                    val posY = jsonObj.optDouble("posY", 300.0).toFloat()

                    val newNode = GameNode(
                        name = name,
                        nodeType = type,
                        posX = posX,
                        posY = posY,
                        hasPhysics = (type == NodeType.RIGID_BODY_2D || type == NodeType.STATIC_BODY_2D)
                    )
                    project.addNode(newNode)
                    AiCommandAction("Created new node '$name' (${type.name}) at ($posX, $posY)")
                }
                "UPDATE_SCRIPT" -> {
                    val script = jsonObj.optString("script", "")
                    val selected = project.findNode(project.selectedNodeId)
                    selected?.scriptCode = script
                    AiCommandAction("Updated script for node '${selected?.name}'")
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class AiAgentResult(
    val responseText: String,
    val appliedActionDescription: String? = null
)

data class AiCommandAction(
    val description: String
)
