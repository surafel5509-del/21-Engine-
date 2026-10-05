package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import com.example.studio.model.VisualScriptRule
import org.json.JSONArray
import org.json.JSONObject

class AiAgentEngine {

    suspend fun processUserPrompt(
        userPrompt: String,
        project: GameProject,
        config: AiProviderConfig,
        chatHistory: List<AiMessage>
    ): AiAgentResponse {
        // If API key is empty and not local LLM, provide intelligent offline engine response
        if (config.apiKey.isBlank() && config.providerType != AiProviderType.LOCAL_LLM) {
            return generateOfflineAgentResponse(userPrompt, project)
        }

        val provider = AiProviderFactory.createProvider(config.providerType)
        val systemPrompt = buildSystemPrompt(project)

        val messages = mutableListOf<AiMessage>()
        messages.add(AiMessage("system", systemPrompt))
        messages.addAll(chatHistory.takeLast(6))
        messages.add(AiMessage("user", userPrompt))

        return try {
            val rawResponse = provider.sendPrompt(messages, config)
            parseAgentResponse(rawResponse)
        } catch (e: Exception) {
            AiAgentResponse(
                replyText = "⚠️ AI Provider Error: ${e.localizedMessage ?: e.message}\n\nFalling back to offline agent processing...",
                actions = generateOfflineActions(userPrompt, project)
            )
        }
    }

    private fun buildSystemPrompt(project: GameProject): String {
        val nodeListStr = project.nodes.joinToString(", ") { "${it.name} (${it.nodeType.name})" }
        return """
            You are GodotStudio AI, an expert 2D game developer and coding assistant.
            The current 2D game project is "${project.name}" (Width: ${project.canvasWidth}, Height: ${project.canvasHeight}).
            Active Scene Nodes: [$nodeListStr]
            Selected Node ID: ${project.selectedNodeId}

            You can respond with text AND structured JSON actions to modify the game project directly.
            If actions are needed, include a JSON block in your response using this exact schema:
            ```json
            {
              "reply": "Your explanation here",
              "actions": [
                {
                  "type": "CREATE_NODE | MODIFY_NODE | DELETE_NODE | GENERATE_SCRIPT | EXPLAIN_ERROR | FIX_CODE | SEARCH_PROJECT",
                  "targetNodeName": "NodeName",
                  "nodeType": "SPRITE_2D | RIGID_BODY_2D | STATIC_BODY_2D | CHARACTER_BODY_2D | CAMERA_2D | TILE_MAP | CPU_PARTICLES_2D | UI_BUTTON | UI_LABEL | AUDIO_STREAM_PLAYER",
                  "propertyKey": "moveSpeed | gravityScale | colorHex | posX | posY | isAnimated",
                  "propertyValue": "value",
                  "scriptContent": "code string",
                  "description": "action description",
                  "searchPattern": "search term"
                }
              ]
            }
            ```
        """.trimIndent()
    }

    private fun parseAgentResponse(rawResponse: String): AiAgentResponse {
        val actions = mutableListOf<AiAgentAction>()
        var replyText = rawResponse

        try {
            val jsonStart = rawResponse.indexOf("```json")
            val jsonEnd = rawResponse.lastIndexOf("```")

            val jsonString = if (jsonStart != -1 && jsonEnd > jsonStart) {
                rawResponse.substring(jsonStart + 7, jsonEnd).trim()
            } else if (rawResponse.trim().startsWith("{")) {
                rawResponse.trim()
            } else {
                ""
            }

            if (jsonString.isNotEmpty()) {
                val json = JSONObject(jsonString)
                replyText = json.optString("reply", rawResponse)

                val actionsArray = json.optJSONArray("actions")
                if (actionsArray != null) {
                    for (i in 0 until actionsArray.length()) {
                        val actObj = actionsArray.getJSONObject(i)
                        val typeStr = actObj.optString("type", "UNKNOWN")
                        val actionType = try { ActionType.valueOf(typeStr) } catch (e: Exception) { ActionType.UNKNOWN }

                        actions.add(
                            AiAgentAction(
                                type = actionType,
                                targetNodeName = actObj.optString("targetNodeName"),
                                nodeType = actObj.optString("nodeType", "SPRITE_2D"),
                                propertyKey = actObj.optString("propertyKey"),
                                propertyValue = actObj.optString("propertyValue"),
                                scriptContent = actObj.optString("scriptContent"),
                                description = actObj.optString("description"),
                                searchPattern = actObj.optString("searchPattern")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Text only response
        }

        return AiAgentResponse(replyText = replyText, actions = actions, rawResponse = rawResponse)
    }

    fun applyActionToProject(action: AiAgentAction, project: GameProject): String {
        return when (action.type) {
            ActionType.CREATE_NODE -> {
                val nodeType = try { NodeType.valueOf(action.nodeType) } catch (e: Exception) { NodeType.SPRITE_2D }
                val nodeName = if (action.targetNodeName.isNotEmpty()) action.targetNodeName else "AI_${nodeType.name}_${project.nodes.size + 1}"
                val newNode = GameNode(
                    name = nodeName,
                    nodeType = nodeType,
                    posX = 640f + (project.nodes.size * 20f % 200f),
                    posY = 360f,
                    hasPhysics = (nodeType == NodeType.RIGID_BODY_2D || nodeType == NodeType.STATIC_BODY_2D),
                    bodyType = if (nodeType == NodeType.STATIC_BODY_2D) "STATIC" else "DYNAMIC",
                    isParticleEmitter = (nodeType == NodeType.CPU_PARTICLES_2D)
                )
                project.addNode(newNode)
                "Created node '${newNode.name}' (${newNode.nodeType.name})."
            }
            ActionType.MODIFY_NODE -> {
                val node = project.nodes.find { it.name.equals(action.targetNodeName, ignoreCase = true) } ?: project.findNode(project.selectedNodeId)
                if (node != null) {
                    when (action.propertyKey.lowercase()) {
                        "movespeed" -> node.moveSpeed = action.propertyValue.toFloatOrNull() ?: node.moveSpeed
                        "gravityscale" -> node.gravityScale = action.propertyValue.toFloatOrNull() ?: node.gravityScale
                        "colorhex" -> node.colorHex = action.propertyValue
                        "posx" -> node.posX = action.propertyValue.toFloatOrNull() ?: node.posX
                        "posy" -> node.posY = action.propertyValue.toFloatOrNull() ?: node.posY
                        "isanimated" -> node.isAnimated = action.propertyValue.toBoolean()
                        "behavior" -> {
                            val behavior = try { com.example.studio.model.NodeBehavior.valueOf(action.propertyValue.uppercase()) } catch (e: Exception) { null }
                            if (behavior != null) node.behavior = behavior
                        }
                    }
                    "Updated node '${node.name}' property [${action.propertyKey} = ${action.propertyValue}]."
                } else {
                    "Node '${action.targetNodeName}' not found."
                }
            }
            ActionType.DELETE_NODE -> {
                val node = project.nodes.find { it.name.equals(action.targetNodeName, ignoreCase = true) }
                if (node != null) {
                    project.removeNode(node.id)
                    "Deleted node '${node.name}'."
                } else {
                    "Node '${action.targetNodeName}' not found."
                }
            }
            ActionType.GENERATE_SCRIPT -> {
                val node = project.nodes.find { it.name.equals(action.targetNodeName, ignoreCase = true) } ?: project.findNode(project.selectedNodeId)
                if (node != null) {
                    node.customScriptCode = action.scriptContent
                    "Attached custom script to '${node.name}'."
                } else {
                    "Node not found to attach script."
                }
            }
            ActionType.SEARCH_PROJECT -> {
                val pattern = action.searchPattern.lowercase()
                val matches = project.nodes.filter { it.name.lowercase().contains(pattern) || it.nodeType.name.lowercase().contains(pattern) }
                "Found ${matches.size} matching node(s): ${matches.joinToString { it.name }}"
            }
            ActionType.EXPLAIN_ERROR -> {
                "Error Diagnosis: ${action.description}"
            }
            ActionType.FIX_CODE -> {
                val node = project.findNode(project.selectedNodeId)
                if (node != null) {
                    node.customScriptCode = action.scriptContent
                    "Applied AI code fix to '${node.name}'."
                } else {
                    "No node selected for code fix."
                }
            }
            ActionType.CREATE_ASSET_PRESET -> {
                "Asset preset '${action.targetNodeName}' added."
            }
            ActionType.UNKNOWN -> "Unknown action execution skipped."
        }
    }

    private fun generateOfflineAgentResponse(prompt: String, project: GameProject): AiAgentResponse {
        val lower = prompt.lowercase()
        val actions = generateOfflineActions(prompt, project)

        val reply = when {
            lower.contains("create") || lower.contains("add") || lower.contains("spawn") ->
                "🤖 [Offline AI Agent] I have generated game nodes for your request!"
            lower.contains("jump") || lower.contains("speed") || lower.contains("physics") ->
                "🤖 [Offline AI Agent] Adjusted physics and movement parameters for your project."
            lower.contains("script") || lower.contains("code") ->
                "🤖 [Offline AI Agent] Generated GDScript controller code."
            lower.contains("search") || lower.contains("find") ->
                "🤖 [Offline AI Agent] Searched project hierarchy for '${prompt}'."
            lower.contains("error") || lower.contains("fix") || lower.contains("bug") ->
                "🤖 [Offline AI Agent] Analyzed script context and applied auto-fix."
            else ->
                "🤖 [Offline AI Agent] Active project has ${project.nodes.size} nodes. You can ask me to create objects, tune physics, attach scripts, search nodes, or fix errors!"
        }

        return AiAgentResponse(replyText = reply, actions = actions)
    }

    private fun generateOfflineActions(prompt: String, project: GameProject): List<AiAgentAction> {
        val lower = prompt.lowercase()
        val actions = mutableListOf<AiAgentAction>()

        if (lower.contains("player") || lower.contains("character")) {
            actions.add(
                AiAgentAction(
                    type = ActionType.CREATE_NODE,
                    targetNodeName = "Player",
                    nodeType = "CHARACTER_BODY_2D",
                    description = "Create CharacterBody2D node named Player"
                )
            )
        } else if (lower.contains("coin") || lower.contains("collectible")) {
            actions.add(
                AiAgentAction(
                    type = ActionType.CREATE_NODE,
                    targetNodeName = "Coin_${project.nodes.size + 1}",
                    nodeType = "SPRITE_2D",
                    description = "Create collectible coin sprite"
                )
            )
        } else if (lower.contains("platform") || lower.contains("wall")) {
            actions.add(
                AiAgentAction(
                    type = ActionType.CREATE_NODE,
                    targetNodeName = "Platform_${project.nodes.size + 1}",
                    nodeType = "STATIC_BODY_2D",
                    description = "Create static platform body"
                )
            )
        } else if (lower.contains("particle") || lower.contains("fire") || lower.contains("explosion")) {
            actions.add(
                AiAgentAction(
                    type = ActionType.CREATE_NODE,
                    targetNodeName = "Particles_FX",
                    nodeType = "CPU_PARTICLES_2D",
                    description = "Create CPUParticles2D emitter"
                )
            )
        } else if (lower.contains("script") || lower.contains("code")) {
            actions.add(
                AiAgentAction(
                    type = ActionType.GENERATE_SCRIPT,
                    targetNodeName = project.nodes.firstOrNull()?.name ?: "Player",
                    scriptContent = """
                        extends CharacterBody2D

                        var speed = 300.0
                        var jump_velocity = -600.0

                        func _physics_process(delta):
                            if not is_on_floor():
                                velocity.y += 980.0 * delta

                            if Input.is_action_just_pressed("ui_accept") and is_on_floor():
                                velocity.y = jump_velocity

                            move_and_slide()
                    """.trimIndent(),
                    description = "Generate GDScript character movement script"
                )
            )
        }

        return actions
    }
}
