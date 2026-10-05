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

class OpenAiProvider : AiProvider {
    override val providerType: AiProviderType = AiProviderType.OPENAI

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
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(body).optJSONObject("error")?.optString("message") ?: body
                } catch (e: Exception) {
                    body
                }
                throw RuntimeException("OpenAI API Error (${response.code}): $errorMsg")
            }
            val json = JSONObject(body)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val message = choice.getJSONObject("message")
                message.getString("content")
            } else {
                "No content returned from OpenAI."
            }
        }
    }

    override fun getSupportedModels(): List<String> = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo")
}
