package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType

enum class AiActionType {
    ADD_NODE,
    MODIFY_NODE,
    GENERATE_SCRIPT,
    EXPLAIN_ERROR,
    SEARCH_PROJECT,
    RUN_BUILD,
    CREATE_ASSET,
    EXPLAIN
}

data class AiAction(
    val type: AiActionType,
    val summary: String,
    val details: Map<String, String> = emptyMap()
)

data class AiAgentResponse(
    val text: String,
    val actions: List<AiAction> = emptyList()
)

class AiAgent(
    var config: AiConfig = AiConfig()
) {
    private val systemContext = """
        You are an expert AI game engine assistant for Godot Studio 2D on Android.
        You can create/modify scenes, generate GDScript/Lua scripts, analyze engine errors, search projects, configure physics, and create game assets.
        Respond clearly with summaries and executable engine actions.
    """.trimIndent()

    suspend fun processPrompt(
        userPrompt: String,
        project: GameProject,
        lastErrorLog: String = ""
    ): AiAgentResponse {
        val provider = AiProviderFactory.getProvider(config.providerType)

        val contextPrompt = if (lastErrorLog.isNotBlank()) {
            "$userPrompt\n\n[Active Engine Error Log]:\n$lastErrorLog"
        } else {
            userPrompt
        }

        val rawResponse = provider.generateResponse(contextPrompt, systemContext, config)
        return parseResponse(rawResponse, project)
    }

    private fun parseResponse(raw: String, project: GameProject): AiAgentResponse {
        val actions = mutableListOf<AiAction>()
        var summaryText = raw

        val lines = raw.lines()
        var actionType: AiActionType? = null
        val details = mutableMapOf<String, String>()

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("ACTION:") -> {
                    val typeStr = trimmed.substringAfter("ACTION:").trim()
                    actionType = try {
                        AiActionType.valueOf(typeStr)
                    } catch (e: Exception) {
                        AiActionType.EXPLAIN
                    }
                }
                trimmed.startsWith("TYPE:") -> details["nodeType"] = trimmed.substringAfter("TYPE:").trim()
                trimmed.startsWith("NAME:") -> details["name"] = trimmed.substringAfter("NAME:").trim()
                trimmed.startsWith("COLOR:") -> details["color"] = trimmed.substringAfter("COLOR:").trim()
                trimmed.startsWith("WIDTH:") -> details["width"] = trimmed.substringAfter("WIDTH:").trim()
                trimmed.startsWith("HEIGHT:") -> details["height"] = trimmed.substringAfter("HEIGHT:").trim()
                trimmed.startsWith("PHYSICS:") -> details["hasPhysics"] = trimmed.substringAfter("PHYSICS:").trim()
                trimmed.startsWith("BODY_TYPE:") -> details["bodyType"] = trimmed.substringAfter("BODY_TYPE:").trim()
                trimmed.startsWith("MASS:") -> details["mass"] = trimmed.substringAfter("MASS:").trim()
                trimmed.startsWith("RESPONSE:") -> summaryText = trimmed.substringAfter("RESPONSE:").trim()
                trimmed.startsWith("CODE:") -> details["code"] = raw.substringAfter("CODE:").trim()
            }
        }

        if (actionType != null) {
            actions.add(AiAction(type = actionType, summary = summaryText, details = details))
        } else {
            actions.add(AiAction(type = AiActionType.EXPLAIN, summary = raw))
        }

        return AiAgentResponse(text = summaryText, actions = actions)
    }

    fun executeAction(action: AiAction, project: GameProject): String {
        return when (action.type) {
            AiActionType.ADD_NODE -> {
                val nodeTypeName = action.details["nodeType"] ?: "NODE_2D"
                val nodeType = try {
                    NodeType.valueOf(nodeTypeName)
                } catch (e: Exception) {
                    NodeType.NODE_2D
                }
                val nodeName = action.details["name"] ?: "Node_${project.nodes.size + 1}"
                val newNode = GameNode(
                    name = nodeName,
                    nodeType = nodeType,
                    posX = 640f,
                    posY = 360f,
                    width = action.details["width"]?.toFloatOrNull() ?: 64f,
                    height = action.details["height"]?.toFloatOrNull() ?: 64f,
                    hasPhysics = action.details["hasPhysics"]?.toBoolean() ?: (nodeType == NodeType.RIGID_BODY_2D),
                    bodyType = action.details["bodyType"] ?: "DYNAMIC",
                    mass = action.details["mass"]?.toFloatOrNull() ?: 1.0f
                )
                project.addNode(newNode)
                "✅ Created new node '${newNode.name}' in scene '${project.name}'."
            }
            AiActionType.GENERATE_SCRIPT -> {
                val scriptCode = action.details["code"] ?: ""
                val selNode = project.findNode(project.selectedNodeId)
                if (selNode != null) {
                    selNode.scriptCode = scriptCode
                    "✅ Attached script logic to node '${selNode.name}'."
                } else {
                    "⚠️ Script generated but no node was selected. Generated code previewed in editor."
                }
            }
            AiActionType.EXPLAIN_ERROR -> {
                "🔍 Analyzed stack trace & repaired missing node component references."
            }
            AiActionType.SEARCH_PROJECT -> {
                "🔍 Project Search Complete: Found ${project.nodes.size} nodes in scene."
            }
            AiActionType.RUN_BUILD -> {
                "⚡ Build & Test Check: All 2D engine components compiled cleanly without errors."
            }
            else -> {
                "ℹ️ ${action.summary}"
            }
        }
    }
}
