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

class GeminiProvider : AiProvider {
    override val providerType: AiProviderType = AiProviderType.GEMINI

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun sendPrompt(messages: List<AiMessage>, config: AiProviderConfig): String = withContext(Dispatchers.IO) {
        val model = config.modelName.ifEmpty { providerType.defaultModel }
        val endpoint = config.apiEndpoint.ifEmpty { providerType.defaultEndpoint }
        val url = "$endpoint/models/$model:generateContent?key=${config.apiKey}"

        val jsonBody = JSONObject()
        val contentsArray = JSONArray()

        for (msg in messages) {
            if (msg.role == "system") continue // System message handled separately or merged
            val contentObj = JSONObject()
            contentObj.put("role", if (msg.role == "assistant") "model" else "user")

            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", msg.content)
            partsArray.put(partObj)

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }
        jsonBody.put("contents", contentsArray)

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw RuntimeException("Gemini API Error (${response.code}): $body")
            }
            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "")
                }
            }
            "No content returned from Gemini."
        }
    }

    override fun getSupportedModels(): List<String> = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-1.0-pro")
}
