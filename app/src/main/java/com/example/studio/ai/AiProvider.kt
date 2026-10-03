package com.example.studio.ai

import com.example.studio.model.GameProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class AiProviderType(val displayName: String, val defaultModel: String, val defaultBaseUrl: String) {
    GOOGLE_GEMINI("Google Gemini", "gemini-1.5-flash", "https://generativelanguage.googleapis.com/v1beta/models/"),
    OPENAI("OpenAI", "gpt-4o-mini", "https://api.openai.com/v1/chat/completions"),
    ANTHROPIC_CLAUDE("Anthropic Claude", "claude-3-5-haiku-20241022", "https://api.anthropic.com/v1/messages"),
    DEEPSEEK("DeepSeek", "deepseek-chat", "https://api.deepseek.com/v1/chat/completions"),
    OPENAI_COMPATIBLE("OpenAI-Compatible REST API", "default", "http://localhost:11434/v1/chat/completions"),
    LOCAL_LLM("Local LLM (Ollama / LM Studio)", "llama3", "http://10.0.2.2:11434/api/generate")
}

data class AiProviderConfig(
    var providerType: AiProviderType = AiProviderType.GOOGLE_GEMINI,
    var apiKey: String = "",
    var modelName: String = providerType.defaultModel,
    var baseUrl: String = providerType.defaultBaseUrl,
    var temperature: Float = 0.7f
)

data class AiAgentResponse(
    val textMessage: String,
    val toolCommand: AiToolCommand? = null,
    val rawResponse: String = ""
)

interface AiProvider {
    val config: AiProviderConfig
    suspend fun generateResponse(userPrompt: String, project: GameProject): AiAgentResponse
}

/**
 * Base HTTP helper for AI REST APIs
 */
abstract class BaseHttpAiProvider(override val config: AiProviderConfig) : AiProvider {

    protected fun buildSystemPrompt(project: GameProject): String {
        return """
            You are the AI Game Development Assistant for Apex2D Game Studio.
            Current Project: "${project.name}"
            Gravity: X=${project.gravityX}, Y=${project.gravityY}
            Node Count: ${project.nodes.size}
            Nodes in Project: ${project.nodes.joinToString(", ") { "${it.name} (${it.nodeType.name}, pos=[${it.posX},${it.posY}], tag=${it.tag})" }}

            You can modify the game project directly by responding with a JSON tool command at the end of your response inside a ```json_tool ... ``` block if needed.

            Tool JSON schema format:
            ```json_tool
            {
              "action": "CREATE_NODE|MODIFY_NODE|DELETE_NODE|GENERATE_SCRIPT|FIX_CODE|EXPLAIN_ERROR|CREATE_ASSET|SEARCH_PROJECT|MODIFY_PROJECT_SETTINGS",
              "nodeName": "Player",
              "nodeType": "CHARACTER_BODY_2D",
              "posX": 300.0,
              "posY": 400.0,
              "color": 4278222847,
              "scriptCode": "// GDScript code",
              "paramString": "parameter details"
            }
            ```

            Always be helpful, concise, and provide game development advice.
        """.trimIndent()
    }
}

class OpenAiCompatibleProvider(config: AiProviderConfig) : BaseHttpAiProvider(config) {
    override suspend fun generateResponse(userPrompt: String, project: GameProject): AiAgentResponse = withContext(Dispatchers.IO) {
        if (config.apiKey.isEmpty() && config.providerType != AiProviderType.LOCAL_LLM) {
            // Simulated response when offline or key not provided
            val mockResponse = generateLocalAiSimulation(userPrompt, project)
            return@withContext AiAgentResponse(textMessage = mockResponse.first, toolCommand = mockResponse.second)
        }

        try {
            val url = URL(if (config.baseUrl.isNotEmpty()) config.baseUrl else config.providerType.defaultBaseUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            if (config.apiKey.isNotEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            }
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val jsonBody = JSONObject().apply {
                put("model", if (config.modelName.isNotEmpty()) config.modelName else config.providerType.defaultModel)
                val messagesArr = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", buildSystemPrompt(project))
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
                put("messages", messagesArr)
                put("temperature", config.temperature)
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody.toString())
            }

            if (conn.responseCode in 200..299) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResp = JSONObject(responseStr)
                val choices = jsonResp.optJSONArray("choices")
                val content = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content") ?: "No response from AI."
                val toolCmd = parseToolCommandFromText(content)
                AiAgentResponse(textMessage = cleanText(content), toolCommand = toolCmd, rawResponse = responseStr)
            } else {
                val mock = generateLocalAiSimulation(userPrompt, project)
                AiAgentResponse(textMessage = "API Returned ${conn.responseCode}. (Fallback Assistant):\n${mock.first}", toolCommand = mock.second)
            }
        } catch (e: Exception) {
            val mock = generateLocalAiSimulation(userPrompt, project)
            AiAgentResponse(textMessage = "AI Agent (Offline Engine Mode):\n${mock.first}", toolCommand = mock.second)
        }
    }
}

class GeminiProvider(config: AiProviderConfig) : BaseHttpAiProvider(config) {
    override suspend fun generateResponse(userPrompt: String, project: GameProject): AiAgentResponse = withContext(Dispatchers.IO) {
        if (config.apiKey.isEmpty()) {
            val mock = generateLocalAiSimulation(userPrompt, project)
            return@withContext AiAgentResponse(textMessage = "Gemini API key is missing. Enabled Local Engine AI:\n${mock.first}", toolCommand = mock.second)
        }
        try {
            val fullUrl = "${config.providerType.defaultBaseUrl}${config.modelName}:generateContent?key=${config.apiKey}"
            val url = URL(fullUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "${buildSystemPrompt(project)}\n\nUser Question: $userPrompt"))
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

            if (conn.responseCode in 200..299) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResp = JSONObject(responseStr)
                val candidates = jsonResp.optJSONArray("candidates")
                val firstPart = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                val content = firstPart?.optString("text") ?: "No text generated."
                val toolCmd = parseToolCommandFromText(content)
                AiAgentResponse(textMessage = cleanText(content), toolCommand = toolCmd, rawResponse = responseStr)
            } else {
                val mock = generateLocalAiSimulation(userPrompt, project)
                AiAgentResponse(textMessage = "Gemini Response Code ${conn.responseCode}:\n${mock.first}", toolCommand = mock.second)
            }
        } catch (e: Exception) {
            val mock = generateLocalAiSimulation(userPrompt, project)
            AiAgentResponse(textMessage = "Gemini Request Exception (${e.message}). Local Assistant:\n${mock.first}", toolCommand = mock.second)
        }
    }
}

/**
 * Fallback local engine intelligence when API key is not configured or offline.
 */
fun generateLocalAiSimulation(userPrompt: String, project: GameProject): Pair<String, AiToolCommand?> {
    val promptLower = userPrompt.lowercase()
    return when {
        promptLower.contains("player") || promptLower.contains("character") || promptLower.contains("hero") -> {
            val tool = AiToolCommand(
                action = AiToolAction.CREATE_NODE,
                nodeName = "Player_Hero",
                nodeType = "CHARACTER_BODY_2D",
                posX = 400f,
                posY = 300f,
                color = 0xFF0284C7,
                scriptCode = """
                    extends CharacterBody2D

                    var speed = 300
                    var jump_force = -600

                    func _physics_process(delta):
                        if Input.is_action_pressed("ui_right"):
                            velocity.x = speed
                        elif Input.is_action_pressed("ui_left"):
                            velocity.x = -speed
                        if Input.is_action_just_pressed("ui_jump") and is_on_floor():
                            velocity.y = jump_force
                """.trimIndent()
            )
            Pair("Created a new 2D Player Character Node with platformer physics script attached!", tool)
        }
        promptLower.contains("coin") || promptLower.contains("collectable") || promptLower.contains("gem") -> {
            val tool = AiToolCommand(
                action = AiToolAction.CREATE_NODE,
                nodeName = "Gold_Coin",
                nodeType = "AREA_2D",
                posX = 600f,
                posY = 250f,
                color = 0xFFFFD600,
                paramString = "coin"
            )
            Pair("Created a collectable Gold Coin Area2D node with pickup sound and collision logic!", tool)
        }
        promptLower.contains("enemy") || promptLower.contains("slime") || promptLower.contains("patrol") -> {
            val tool = AiToolCommand(
                action = AiToolAction.CREATE_NODE,
                nodeName = "Slime_Enemy",
                nodeType = "RIGID_BODY_2D",
                posX = 700f,
                posY = 320f,
                color = 0xFF10B981,
                paramString = "ENEMY_PATROL"
            )
            Pair("Created Slime Enemy RigidBody2D with auto-patrol behavior!", tool)
        }
        promptLower.contains("script") || promptLower.contains("code") || promptLower.contains("logic") -> {
            val tool = AiToolCommand(
                action = AiToolAction.GENERATE_SCRIPT,
                nodeName = project.nodes.firstOrNull()?.name ?: "Node2D",
                scriptCode = """
                    extends Node2D

                    func _ready():
                        print("Node initialized successfully in Apex2D engine!")

                    func _process(delta):
                        rotation += 1.0 * delta
                """.trimIndent()
            )
            Pair("Generated GDScript code for node script component!", tool)
        }
        promptLower.contains("gravity") || promptLower.contains("physics") || promptLower.contains("setting") -> {
            val tool = AiToolCommand(
                action = AiToolAction.MODIFY_PROJECT_SETTINGS,
                gravityX = 0f,
                gravityY = 1200f
            )
            Pair("Updated project physics settings: Set gravityY to 1200.", tool)
        }
        else -> {
            Pair("Apex2D AI Assistant analyzed project '${project.name}'. Nodes in scene: ${project.nodes.size}. You can ask me to create player characters, enemies, coins, scripts, or adjust gravity!", null)
        }
    }
}

fun parseToolCommandFromText(text: String): AiToolCommand? {
    val startIndex = text.indexOf("```json_tool")
    if (startIndex == -1) return null
    val block = text.substring(startIndex)
    val firstBrace = block.indexOf('{')
    val lastBrace = block.lastIndexOf('}')
    if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) return null
    val jsonStr = block.substring(firstBrace, lastBrace + 1)
    return try {
        val json = JSONObject(jsonStr)
        AiToolCommand(
            action = AiToolAction.valueOf(json.optString("action", "CREATE_NODE")),
            nodeName = json.optString("nodeName", "New_Node"),
            nodeType = json.optString("nodeType", "SPRITE_2D"),
            posX = json.optDouble("posX", 640.0).toFloat(),
            posY = json.optDouble("posY", 360.0).toFloat(),
            color = json.optLong("color", 0xFF0284C7),
            scriptCode = json.optString("scriptCode", ""),
            paramString = json.optString("paramString", ""),
            gravityX = json.optDouble("gravityX", 0.0).toFloat(),
            gravityY = json.optDouble("gravityY", 980.0).toFloat()
        )
    } catch (e: Exception) {
        null
    }
}

fun cleanText(text: String): String {
    val startIndex = text.indexOf("```json_tool")
    if (startIndex == -1) return text.trim()
    val endIndex = text.indexOf("```", startIndex + 12)
    return if (endIndex != -1) {
        (text.substring(0, startIndex) + text.substring(endIndex + 3)).trim()
    } else {
        text.substring(0, startIndex).trim()
    }
}

object AiProviderFactory {
    fun createProvider(config: AiProviderConfig): AiProvider {
        return when (config.providerType) {
            AiProviderType.GOOGLE_GEMINI -> GeminiProvider(config)
            AiProviderType.OPENAI,
            AiProviderType.ANTHROPIC_CLAUDE,
            AiProviderType.DEEPSEEK,
            AiProviderType.OPENAI_COMPATIBLE,
            AiProviderType.LOCAL_LLM -> OpenAiCompatibleProvider(config)
        }
    }
}
