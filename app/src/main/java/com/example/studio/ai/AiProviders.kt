package com.example.studio.ai

import com.example.studio.model.NodeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RuleBasedLocalAiProvider : AiProvider {
    override val id: String = "local_rule_based"
    override val name: String = "Offline Local AI (Rule-Based Engine)"
    override val isOfflineCapable: Boolean = true

    override suspend fun generateResponse(
        prompt: String,
        context: AiContext,
        config: ProviderConfig
    ): AiResponse {
        val lowerPrompt = prompt.lowercase()

        return when {
            lowerPrompt.contains("create") || lowerPrompt.contains("add") -> {
                handleCreateCommand(lowerPrompt, context)
            }
            lowerPrompt.contains("move") || lowerPrompt.contains("set position") -> {
                handleMoveCommand(lowerPrompt, context)
            }
            lowerPrompt.contains("script") || lowerPrompt.contains("logic") || lowerPrompt.contains("jump") -> {
                handleScriptCommand(lowerPrompt, context)
            }
            lowerPrompt.contains("error") || lowerPrompt.contains("fix") || lowerPrompt.contains("bug") -> {
                handleErrorCommand(prompt, context)
            }
            lowerPrompt.contains("search") || lowerPrompt.contains("find") || lowerPrompt.contains("list") -> {
                handleSearchCommand(prompt, context)
            }
            lowerPrompt.contains("delete") || lowerPrompt.contains("remove") -> {
                handleDeleteCommand(context)
            }
            else -> {
                AiResponse(
                    replyText = "I am your offline studio AI assistant. You can ask me to create objects (e.g. 'create a player', 'add a platform'), modify selected nodes, attach script logic, search the project, or explain errors.",
                    suggestedAction = null
                )
            }
        }
    }

    private fun handleCreateCommand(prompt: String, context: AiContext): AiResponse {
        return when {
            prompt.contains("platform") -> {
                AiResponse(
                    replyText = "I can create a Static Platform node for your scene.",
                    suggestedAction = AiAction.CreateNode(
                        name = "Platform_Node",
                        nodeType = NodeType.STATIC_BODY_2D.name,
                        posX = 640f,
                        posY = 550f,
                        width = 240f,
                        height = 32f,
                        colorHex = "#10B981",
                        bodyType = "STATIC"
                    )
                )
            }
            prompt.contains("particle") -> {
                AiResponse(
                    replyText = "I can create a CPU Particle Emitter for visual effects.",
                    suggestedAction = AiAction.CreateNode(
                        name = "Particle_Emitter",
                        nodeType = NodeType.CPU_PARTICLES_2D.name,
                        posX = 640f,
                        posY = 360f,
                        width = 16f,
                        height = 16f,
                        colorHex = "#F59E0B",
                        bodyType = "STATIC"
                    )
                )
            }
            prompt.contains("coin") -> {
                AiResponse(
                    replyText = "I can create a collectible Coin item.",
                    suggestedAction = AiAction.CreateNode(
                        name = "Gold_Coin",
                        nodeType = NodeType.AREA_2D.name,
                        posX = 640f,
                        posY = 420f,
                        width = 28f,
                        height = 28f,
                        colorHex = "#FBBF24",
                        bodyType = "STATIC"
                    )
                )
            }
            prompt.contains("enemy") || prompt.contains("slime") -> {
                AiResponse(
                    replyText = "I can create an Enemy Slime object with physics.",
                    suggestedAction = AiAction.CreateNode(
                        name = "Enemy_Slime",
                        nodeType = NodeType.RIGID_BODY_2D.name,
                        posX = 700f,
                        posY = 300f,
                        width = 44f,
                        height = 36f,
                        colorHex = "#EF4444",
                        bodyType = "DYNAMIC"
                    )
                )
            }
            else -> {
                AiResponse(
                    replyText = "I can create a RigidBody2D Player object in your scene.",
                    suggestedAction = AiAction.CreateNode(
                        name = "Player_Node",
                        nodeType = NodeType.RIGID_BODY_2D.name,
                        posX = 640f,
                        posY = 300f,
                        width = 48f,
                        height = 48f,
                        colorHex = "#38BDF8",
                        bodyType = "DYNAMIC"
                    )
                )
            }
        }
    }

    private fun handleMoveCommand(prompt: String, context: AiContext): AiResponse {
        val selected = context.selectedNode
            ?: return AiResponse(replyText = "Please select a node in the Hierarchy to modify its position.")

        return AiResponse(
            replyText = "I can adjust the position of '${selected.name}'.",
            suggestedAction = AiAction.ModifyNodeProperty(
                nodeId = selected.id,
                propertyName = "posX",
                value = "640.0"
            )
        )
    }

    private fun handleScriptCommand(prompt: String, context: AiContext): AiResponse {
        val selected = context.selectedNode
            ?: return AiResponse(replyText = "Please select a node in the scene tree to attach a visual script.")

        val scriptType = when {
            prompt.contains("touch") -> "TOUCH_IMPULSE"
            prompt.contains("bounce") -> "BOUNCE"
            prompt.contains("spawn") -> "SPAWNER"
            else -> "AUTO_PATROL"
        }

        return AiResponse(
            replyText = "I generated a $scriptType script logic for '${selected.name}'.",
            suggestedAction = AiAction.GenerateScript(
                nodeId = selected.id,
                scriptType = scriptType,
                speedX = 120f,
                speedY = -250f
            )
        )
    }

    private fun handleErrorCommand(prompt: String, context: AiContext): AiResponse {
        val errorMsg = context.lastError ?: "No runtime error recorded."
        return AiResponse(
            replyText = "Error Diagnostics: '$errorMsg'. I recommend checking collision bounds and body types.",
            suggestedAction = AiAction.ExplainError(
                errorMessage = errorMsg,
                suggestedFix = "Ensure RigidBody2D components have collision shapes and static platforms are set to STATIC body type."
            )
        )
    }

    private fun handleSearchCommand(prompt: String, context: AiContext): AiResponse {
        val query = prompt.replace("search", "").replace("find", "").trim()
        val matchCount = context.project.nodes.count { it.name.lowercase().contains(query.lowercase()) }
        return AiResponse(
            replyText = "Found $matchCount matching node(s) for query '$query' in project '${context.project.name}'.",
            suggestedAction = AiAction.SearchProject(query = query)
        )
    }

    private fun handleDeleteCommand(context: AiContext): AiResponse {
        val selected = context.selectedNode
            ?: return AiResponse(replyText = "Please select a node in the scene tree to delete.")

        return AiResponse(
            replyText = "I can remove node '${selected.name}' from your project.",
            suggestedAction = AiAction.DeleteNode(nodeId = selected.id)
        )
    }
}

class OpenAiCompatibleProvider : AiProvider {
    override val id: String = "openai_compatible"
    override val name: String = "OpenAI / Claude / DeepSeek / Local LLM"
    override val isOfflineCapable: Boolean = false

    override suspend fun generateResponse(
        prompt: String,
        context: AiContext,
        config: ProviderConfig
    ): AiResponse = withContext(Dispatchers.IO) {
        if (config.baseUrl.isBlank()) {
            return@withContext AiResponse(
                replyText = "Provider endpoint URL is required (e.g. https://api.openai.com/v1 or http://localhost:11434/v1)."
            )
        }

        try {
            val endpoint = if (config.baseUrl.endsWith("/")) {
                "${config.baseUrl}chat/completions"
            } else {
                "${config.baseUrl}/chat/completions"
            }

            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            if (config.apiKey.isNotBlank()) {
                conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            }
            conn.connectTimeout = 8000
            conn.readTimeout = 12000
            conn.doOutput = true

            val systemMessage = "You are an AI assistant for an Android 2D Game Engine. Selected node: ${context.selectedNode?.name ?: "none"}. Project nodes count: ${context.project.nodes.size}."

            val jsonBody = JSONObject().apply {
                put("model", if (config.modelName.isNotBlank()) config.modelName else "gpt-3.5-turbo")
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemMessage)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messagesArray)
                put("max_tokens", 500)
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val messageObj = choices.getJSONObject(0).getJSONObject("message")
                    val content = messageObj.getString("content")
                    return@withContext AiResponse(replyText = content)
                }
            }
            return@withContext AiResponse(replyText = "API Error ${conn.responseCode}: ${conn.responseMessage}")
        } catch (e: Exception) {
            return@withContext AiResponse(replyText = "Network / Provider Error: ${e.message}")
        }
    }
}
