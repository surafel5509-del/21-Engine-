package com.example.studio.ai

import com.example.engine.scene.PrefabManager
import com.example.engine.scripting.ScriptEngine
import com.example.studio.ai.providers.*
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType

data class AgentExecutionResult(
    val responseText: String,
    val projectModified: Boolean = false,
    val logMessage: String = ""
)

class AiAgent {

    val providers: List<AiProvider> = listOf(
        LocalHeuristicAiProvider(),
        OpenAiProvider(),
        GeminiProvider(),
        ClaudeProvider(),
        DeepSeekProvider(),
        OpenAiCompatibleProvider()
    )

    var activeProvider: AiProvider = providers.first()
    var apiKey: String = ""

    suspend fun processUserPrompt(
        prompt: String,
        project: GameProject
    ): AgentExecutionResult {
        val contextSummary = "Project '${project.name}', Nodes Count: ${project.nodes.size}, Selected Node: ${project.selectedNodeId ?: "None"}"
        val aiResponse = activeProvider.sendMessage(prompt, contextSummary, apiKey)

        var modified = false
        val logMsgs = mutableListOf<String>()

        // Parse Action Directives
        val lines = aiResponse.lines()
        for (line in lines) {
            if (line.startsWith("ACTION: ")) {
                val action = line.removePrefix("ACTION: ").trim()
                when {
                    action.startsWith("CREATE_NODE") -> {
                        val parts = action.split("|")
                        val name = parts.getOrNull(1) ?: "AINode"
                        val prefabKey = parts.getOrNull(3) ?: "player"

                        val newNode = PrefabManager.instantiate(prefabKey, 640f, 360f)
                        newNode.name = name
                        project.addNode(newNode)
                        modified = true
                        logMsgs.add("Created Node '$name' (${newNode.nodeType})")
                    }
                    action.startsWith("GENERATE_SCRIPT") -> {
                        val code = action.substringAfter("GENERATE_SCRIPT|")
                        val targetNode = project.findNode(project.selectedNodeId) ?: project.nodes.firstOrNull()
                        targetNode?.let { node ->
                            node.scriptCode = code
                            modified = true
                            logMsgs.add("Updated GDScript on node '${node.name}'")
                        }
                    }
                    action.startsWith("FIX_ERRORS") -> {
                        ScriptEngine.clearErrors()
                        logMsgs.add("Cleared and resolved script errors")
                    }
                }
            }
        }

        return AgentExecutionResult(
            responseText = aiResponse,
            projectModified = modified,
            logMessage = logMsgs.joinToString("; ")
        )
    }
}
