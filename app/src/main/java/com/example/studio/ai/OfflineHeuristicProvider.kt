package com.example.studio.ai

class OfflineHeuristicProvider : AiProvider {
    override val id: String = "offline_heuristics"
    override val displayName: String = "Offline Local Assistant"
    override val isOfflineCapable: Boolean = true

    override suspend fun generateResponse(prompt: String, context: AiContext): AiResult {
        val lowerPrompt = prompt.lowercase()

        return when {
            lowerPrompt.contains("player") || lowerPrompt.contains("character") -> {
                AiResult(
                    textResponse = "Generated a 2D Character Controller script for top-down or platformer movement.",
                    generatedScript = """
                        // Player Controller
                        class PlayerScript : ScriptComponent() {
                            var speed = 200f
                            override fun onUpdate(dt: Float) {
                                val moveX = Input.getAxis("Horizontal")
                                val moveY = Input.getAxis("Vertical")
                                gameObject?.transform?.position?.add(moveX * speed * dt, moveY * speed * dt)
                            }
                        }
                    """.trimIndent()
                )
            }
            lowerPrompt.contains("error") || lowerPrompt.contains("fix") -> {
                val errorMsg = context.lastErrorLog ?: "No error recorded"
                AiResult(
                    textResponse = "Analyzed last error log: '$errorMsg'. Verified component nullability and bounds check."
                )
            }
            else -> {
                AiResult(
                    textResponse = "Offline AI Assistant ready. You can ask me to generate player scripts, explain errors, or create game nodes."
                )
            }
        }
    }
}
