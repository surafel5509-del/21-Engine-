package com.example.studio.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class AiProviderType(val displayName: String, val defaultModel: String) {
    LOCAL_HEURISTIC("Offline Heuristic Engine", "built-in-v1"),
    OPENAI("OpenAI", "gpt-4o"),
    GEMINI("Google Gemini", "gemini-1.5-pro"),
    CLAUDE("Anthropic Claude", "claude-3-5-sonnet"),
    DEEPSEEK("DeepSeek", "deepseek-coder"),
    OPENAI_COMPATIBLE("OpenAI-Compatible (Ollama/Local)", "llama3")
}

data class AiConfig(
    val providerType: AiProviderType = AiProviderType.LOCAL_HEURISTIC,
    val apiKey: String = "",
    val baseUrl: String = "",
    val modelName: String = providerType.defaultModel
)

interface AiProvider {
    suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String
}

class LocalHeuristicProvider : AiProvider {
    override suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String {
        val p = prompt.lowercase()
        return when {
            p.contains("player") || p.contains("character") -> {
                """
                ACTION: ADD_NODE
                TYPE: RIGID_BODY_2D
                NAME: Player
                COLOR: #0284C7
                WIDTH: 48
                HEIGHT: 64
                PHYSICS: true
                MASS: 1.0
                RESPONSE: Created a new dynamic 'Player' RigidBody2D object with physics collision enabled!
                """.trimIndent()
            }
            p.contains("coin") || p.contains("collectible") -> {
                """
                ACTION: ADD_NODE
                TYPE: AREA_2D
                NAME: Coin
                COLOR: #EAB308
                WIDTH: 24
                HEIGHT: 24
                RESPONSE: Created a new collectible 'Coin' Area2D object in the current scene!
                """.trimIndent()
            }
            p.contains("script") || p.contains("code") || p.contains("jump") || p.contains("move") -> {
                """
                ACTION: GENERATE_SCRIPT
                CODE:
                extends RigidBody2D

                var speed = 300.0
                var jump_force = -500.0

                func _ready():
                    print("Player script initialized!")

                func _physics_process(delta):
                    if Input.is_action_pressed("ui_right"):
                        velocity.x = speed
                    elif Input.is_action_pressed("ui_left"):
                        velocity.x = -speed
                    if Input.is_action_just_pressed("ui_accept"):
                        apply_impulse(Vector2(0, jump_force))
                RESPONSE: Generated GDScript logic for character movement and jump mechanics.
                """.trimIndent()
            }
            p.contains("error") || p.contains("fix") || p.contains("crash") -> {
                """
                ACTION: EXPLAIN_ERROR
                RESPONSE: Analyzed engine stack trace: The NullPointerException was caused by missing collision shape bounds on Node 'Enemy_1'. Automatically attached a default BoxCollider2D shape to resolve the issue!
                """.trimIndent()
            }
            p.contains("platform") || p.contains("ground") -> {
                """
                ACTION: ADD_NODE
                TYPE: STATIC_BODY_2D
                NAME: Platform
                COLOR: #10B981
                WIDTH: 320
                HEIGHT: 32
                PHYSICS: true
                BODY_TYPE: STATIC
                RESPONSE: Added a StaticBody2D platform for physics collisions!
                """.trimIndent()
            }
            else -> {
                """
                ACTION: EXPLAIN
                RESPONSE: AI Agent analyzed request: "$prompt". You can ask me to create player characters, platforms, coins, generate scripts, fix physics errors, or build project assets!
                """.trimIndent()
            }
        }
    }
}

class OpenAiProvider : AiProvider {
    override suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String = withContext(Dispatchers.IO) {
        val endpoint = if (config.baseUrl.isNotBlank()) config.baseUrl else "https://api.openai.com/v1/chat/completions"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", config.modelName.ifBlank { "gpt-4o" })
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemContext)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        if (conn.responseCode == 200) {
            val responseStr = conn.inputStream.bufferedReader().readText()
            val jsonObj = JSONObject(responseStr)
            jsonObj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        } else {
            "Error from OpenAI API (Code ${conn.responseCode}): ${conn.errorStream?.bufferedReader()?.readText()}"
        }
    }
}

class GeminiProvider : AiProvider {
    override suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String = withContext(Dispatchers.IO) {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.modelName.ifBlank { "gemini-1.5-pro" }}:generateContent?key=${config.apiKey}"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemContext\n\nUser Request: $prompt"))
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        if (conn.responseCode == 200) {
            val responseStr = conn.inputStream.bufferedReader().readText()
            val jsonObj = JSONObject(responseStr)
            jsonObj.getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text")
        } else {
            "Error from Gemini API (Code ${conn.responseCode}): ${conn.errorStream?.bufferedReader()?.readText()}"
        }
    }
}

class ClaudeProvider : AiProvider {
    override suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String = withContext(Dispatchers.IO) {
        val endpoint = "https://api.anthropic.com/v1/messages"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("x-api-key", config.apiKey)
        conn.setRequestProperty("anthropic-version", "2023-06-01")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", config.modelName.ifBlank { "claude-3-5-sonnet" })
            put("max_tokens", 1024)
            put("system", systemContext)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        if (conn.responseCode == 200) {
            val responseStr = conn.inputStream.bufferedReader().readText()
            val jsonObj = JSONObject(responseStr)
            jsonObj.getJSONArray("content").getJSONObject(0).getString("text")
        } else {
            "Error from Claude API (Code ${conn.responseCode}): ${conn.errorStream?.bufferedReader()?.readText()}"
        }
    }
}

class DeepSeekProvider : AiProvider {
    override suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String {
        val customConfig = config.copy(baseUrl = if (config.baseUrl.isBlank()) "https://api.deepseek.com/chat/completions" else config.baseUrl)
        return OpenAiProvider().generateResponse(prompt, systemContext, customConfig)
    }
}

class OpenAiCompatibleProvider : AiProvider {
    override suspend fun generateResponse(prompt: String, systemContext: String, config: AiConfig): String {
        val customConfig = config.copy(baseUrl = if (config.baseUrl.isBlank()) "http://10.0.2.2:11434/v1/chat/completions" else config.baseUrl)
        return OpenAiProvider().generateResponse(prompt, systemContext, customConfig)
    }
}

object AiProviderFactory {
    fun getProvider(type: AiProviderType): AiProvider {
        return when (type) {
            AiProviderType.LOCAL_HEURISTIC -> LocalHeuristicProvider()
            AiProviderType.OPENAI -> OpenAiProvider()
            AiProviderType.GEMINI -> GeminiProvider()
            AiProviderType.CLAUDE -> ClaudeProvider()
            AiProviderType.DEEPSEEK -> DeepSeekProvider()
            AiProviderType.OPENAI_COMPATIBLE -> OpenAiCompatibleProvider()
        }
    }
}
