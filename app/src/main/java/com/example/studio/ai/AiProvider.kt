package com.example.studio.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiProviderType {
    OPENAI,
    GEMINI,
    CLAUDE,
    DEEPSEEK,
    LOCAL_LLM
}

data class AiConfig(
    val providerType: AiProviderType = AiProviderType.OPENAI,
    val apiKey: String = "",
    val baseUrl: String = "",
    val modelName: String = ""
)

/**
 * Provider-agnostic AI LLM Interface for game generation, code creation, and scene editing.
 */
interface AiProvider {
    suspend fun generateCompletion(prompt: String, projectContext: String): String
}

class OpenAiProvider(private val config: AiConfig) : AiProvider {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()

    override suspend fun generateCompletion(prompt: String, projectContext: String): String = withContext(Dispatchers.IO) {
        if (config.apiKey.isEmpty()) {
            return@withContext "[AI Assistant] Please configure OpenAI API Key in settings."
        }
        val url = if (config.baseUrl.isNotEmpty()) config.baseUrl else "https://api.openai.com/v1/chat/completions"
        val model = if (config.modelName.isNotEmpty()) config.modelName else "gpt-4o-mini"

        val json = JSONObject().apply {
            put("model", model)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an expert Godot 2D Game Engine AI Agent. Project Context:\n$projectContext")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val resJson = JSONObject(body)
                    return@withContext resJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } else {
                    return@withContext "OpenAI Error (${response.code}): $body"
                }
            }
        } catch (e: Exception) {
            return@withContext "Network Error: ${e.message}"
        }
    }
}

class GeminiProvider(private val config: AiConfig) : AiProvider {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()

    override suspend fun generateCompletion(prompt: String, projectContext: String): String = withContext(Dispatchers.IO) {
        if (config.apiKey.isEmpty()) {
            return@withContext "[AI Assistant] Please configure Google Gemini API Key in settings."
        }
        val model = if (config.modelName.isNotEmpty()) config.modelName else "gemini-1.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${config.apiKey}"

        val json = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", "System: You are an expert 2D Game Dev AI. Context:\n$projectContext\n\nUser: $prompt"))
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val resJson = JSONObject(body)
                    val candidates = resJson.getJSONArray("candidates")
                    val text = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    return@withContext text
                } else {
                    return@withContext "Gemini Error (${response.code}): $body"
                }
            }
        } catch (e: Exception) {
            return@withContext "Network Error: ${e.message}"
        }
    }
}

class ClaudeProvider(private val config: AiConfig) : AiProvider {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()

    override suspend fun generateCompletion(prompt: String, projectContext: String): String = withContext(Dispatchers.IO) {
        if (config.apiKey.isEmpty()) {
            return@withContext "[AI Assistant] Please configure Anthropic API Key in settings."
        }
        val url = "https://api.anthropic.com/v1/messages"
        val model = if (config.modelName.isNotEmpty()) config.modelName else "claude-3-5-sonnet-20241022"

        val json = JSONObject().apply {
            put("model", model)
            put("max_tokens", 1024)
            put("system", "You are an expert Anthropic Claude 2D Game Engine AI Agent. Context:\n$projectContext")
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", config.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val resJson = JSONObject(body)
                    val content = resJson.getJSONArray("content")
                    return@withContext content.getJSONObject(0).getString("text")
                } else {
                    return@withContext "Claude Error (${response.code}): $body"
                }
            }
        } catch (e: Exception) {
            return@withContext "Network Error: ${e.message}"
        }
    }
}

class DeepSeekProvider(private val config: AiConfig) : AiProvider {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()

    override suspend fun generateCompletion(prompt: String, projectContext: String): String = withContext(Dispatchers.IO) {
        val url = if (config.baseUrl.isNotEmpty()) config.baseUrl else "https://api.deepseek.com/chat/completions"
        val model = if (config.modelName.isNotEmpty()) config.modelName else "deepseek-coder"

        val openAiDelegate = OpenAiProvider(config.copy(baseUrl = url, modelName = model))
        return@withContext openAiDelegate.generateCompletion(prompt, projectContext)
    }
}

class LocalLlmProvider(private val config: AiConfig) : AiProvider {
    override suspend fun generateCompletion(prompt: String, projectContext: String): String = withContext(Dispatchers.IO) {
        val url = if (config.baseUrl.isNotEmpty()) config.baseUrl else "http://localhost:11434/v1/chat/completions"
        val model = if (config.modelName.isNotEmpty()) config.modelName else "llama3"

        val openAiDelegate = OpenAiProvider(config.copy(baseUrl = url, modelName = model))
        return@withContext openAiDelegate.generateCompletion(prompt, projectContext)
    }
}

object AiProviderFactory {
    fun createProvider(config: AiConfig): AiProvider {
        return when (config.providerType) {
            AiProviderType.OPENAI -> OpenAiProvider(config)
            AiProviderType.GEMINI -> GeminiProvider(config)
            AiProviderType.CLAUDE -> ClaudeProvider(config)
            AiProviderType.DEEPSEEK -> DeepSeekProvider(config)
            AiProviderType.LOCAL_LLM -> LocalLlmProvider(config)
        }
    }
}
