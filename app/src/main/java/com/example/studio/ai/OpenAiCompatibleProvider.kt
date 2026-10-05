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

class OpenAiCompatibleProvider : AiProvider {
    override val providerType: AiProviderType = AiProviderType.OPENAI_COMPATIBLE

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun sendPrompt(messages: List<AiMessage>, config: AiProviderConfig): String = withContext(Dispatchers.IO) {
        val endpoint = config.apiEndpoint.ifEmpty { providerType.defaultEndpoint }
        val url = if (endpoint.endsWith("/chat/completions")) endpoint else "$endpoint/chat/completions"

        val jsonBody = JSONObject()
        jsonBody.put("model", config.modelName.ifEmpty { providerType.defaultModel })
        jsonBody.put("temperature", config.temperature)

        val messagesArray = JSONArray()
        for (msg in messages) {
            val msgObj = JSONObject()
            msgObj.put("role", msg.role)
            msgObj.put("content", msg.content)
            messagesArray.put(msgObj)
        }
        jsonBody.put("messages", messagesArray)

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder().url(url).post(requestBody)

        if (config.apiKey.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw RuntimeException("OpenAI Compatible API Error (${response.code}): $body")
            }
            val json = JSONObject(body)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                choices.getJSONObject(0).getJSONObject("message").getString("content")
            } else {
                "No content returned."
            }
        }
    }

    override fun getSupportedModels(): List<String> = listOf("meta-llama/Llama-3-70b-chat-hf", "mistralai/Mixtral-8x7B-Instruct-v0.1", "custom")
}
