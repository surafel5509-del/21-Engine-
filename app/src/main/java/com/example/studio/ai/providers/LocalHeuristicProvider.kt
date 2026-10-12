package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider
import com.example.studio.ai.AiResponse
import com.example.studio.ai.ProviderType
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType

/**
 * Offline rule-based AI agent that generates scripts, creates objects, and modifies scenes without needing internet access or API keys.
 */
class LocalHeuristicProvider : AiProvider {
    override val type: ProviderType = ProviderType.LOCAL_HEURISTIC
    override var apiKey: String = ""
    override var baseUrl: String = ""

    override suspend fun generateScript(prompt: String, contextNode: GameNode?): AiResponse {
        val lower = prompt.lowercase()
        val script = when {
            lower.contains("move") || lower.contains("player") || lower.contains("walk") -> """
                var speed = 200

                func _ready():
                    print("Player script initialized")

                func _process(delta):
                    position.x += speed * delta
            """.trimIndent()

            lower.contains("jump") || lower.contains("gravity") -> """
                var gravity = 980
                var jump_velocity = -400

                func _process(delta):
                    position.y += gravity * delta
            """.trimIndent()

            lower.contains("rotate") || lower.contains("spin") -> """
                var rotation_speed = 3.14

                func _process(delta):
                    rotation += rotation_speed * delta
            """.trimIndent()

            else -> """
                var health = 100

                func _ready():
                    print("Custom object created")

                func _process(delta):
                    pass
            """.trimIndent()
        }

        return AiResponse(
            text = "Generated local GDScript based on prompt: \"$prompt\"",
            suggestedScript = script
        )
    }

    override suspend fun explainError(errorMessage: String, scriptContent: String): AiResponse {
        val explanation = when {
            errorMessage.contains("position") -> "Variable or property 'position' requires a valid Transform or GameObject bound to the script."
            errorMessage.contains("delta") -> "Ensure '_process(delta)' receives float delta parameter during loop tick."
            else -> "Syntax or runtime error found: $errorMessage. Check variable declarations ('var name = value') and function definitions."
        }
        val fixedScript = scriptContent.ifEmpty {
            "var speed = 100\nfunc _process(delta):\n    position.x += speed * delta"
        }
        return AiResponse(
            text = "Analysis: $explanation",
            suggestedScript = fixedScript
        )
    }

    override suspend fun modifyScene(prompt: String, project: GameProject): AiResponse {
        val updated = project.copy()
        if (prompt.lowercase().contains("grid")) {
            updated.gridSnap = !updated.gridSnap
        }
        return AiResponse(
            text = "Local Agent modified project settings based on: $prompt",
            modifiedProject = updated
        )
    }

    override suspend fun createObject(prompt: String, project: GameProject): AiResponse {
        val lower = prompt.lowercase()
        val nodeType = when {
            lower.contains("player") || lower.contains("character") -> NodeType.RIGID_BODY_2D
            lower.contains("platform") || lower.contains("wall") || lower.contains("block") -> NodeType.STATIC_BODY_2D
            lower.contains("coin") || lower.contains("collectible") -> NodeType.AREA_2D
            lower.contains("particle") || lower.contains("fire") || lower.contains("smoke") -> NodeType.CPU_PARTICLES_2D
            lower.contains("label") || lower.contains("text") || lower.contains("ui") -> NodeType.LABEL_UI
            else -> NodeType.SPRITE_2D
        }

        val newNode = GameNode(
            name = "AI_${nodeType.name.lowercase().replace("_", "")}_${project.nodes.size + 1}",
            nodeType = nodeType,
            posX = 640f + (project.nodes.size * 20f),
            posY = 360f,
            hasPhysics = (nodeType == NodeType.RIGID_BODY_2D || nodeType == NodeType.STATIC_BODY_2D),
            bodyType = if (nodeType == NodeType.STATIC_BODY_2D) "STATIC" else "DYNAMIC"
        )

        project.addNode(newNode)
        return AiResponse(
            text = "Created ${newNode.name} (${newNode.nodeType}) in the scene.",
            modifiedProject = project
        )
    }
}
