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

class ClaudeProvider : AiProvider {
    override val providerType: AiProviderType = AiProviderType.CLAUDE

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun sendPrompt(messages: List<AiMessage>, config: AiProviderConfig): String = withContext(Dispatchers.IO) {
        val endpoint = config.apiEndpoint.ifEmpty { providerType.defaultEndpoint }
        val url = if (endpoint.endsWith("/messages")) endpoint else "$endpoint/messages"

        val jsonBody = JSONObject()
        jsonBody.put("model", config.modelName.ifEmpty { providerType.defaultModel })
        jsonBody.put("max_tokens", 2048)

        val messagesArray = JSONArray()
        var systemText = ""

        for (msg in messages) {
            if (msg.role == "system") {
                systemText += msg.content + "\n"
                continue
            }
            val msgObj = JSONObject()
            msgObj.put("role", msg.role)
            msgObj.put("content", msg.content)
            messagesArray.put(msgObj)
        }

        if (systemText.isNotEmpty()) {
            jsonBody.put("system", systemText.trim())
        }
        jsonBody.put("messages", messagesArray)

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", config.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw RuntimeException("Anthropic Claude Error (${response.code}): $body")
            }
            val json = JSONObject(body)
            val contentArray = json.optJSONArray("content")
            if (contentArray != null && contentArray.length() > 0) {
                val block = contentArray.getJSONObject(0)
                if (block.optString("type") == "text") {
                    return@withContext block.optString("text", "")
                }
            }
            "No content returned from Claude."
        }
    }

    override fun getSupportedModels(): List<String> = listOf("claude-3-5-sonnet-20241022", "claude-3-haiku-20240307", "claude-3-opus-20240229")
}
