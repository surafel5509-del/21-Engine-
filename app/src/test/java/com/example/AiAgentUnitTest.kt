package com.example

import com.example.studio.ai.AiAgent
import com.example.studio.model.GameProject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiAgentUnitTest {

    @Test
    fun testAiAgentProvidersRegistered() {
        val agent = AiAgent()
        assertTrue(agent.providers.isNotEmpty())
        assertEquals("local_heuristic", agent.providers.first().id)
        assertTrue(agent.providers.any { it.id == "openai" })
        assertTrue(agent.providers.any { it.id == "gemini" })
        assertTrue(agent.providers.any { it.id == "claude" })
        assertTrue(agent.providers.any { it.id == "deepseek" })
    }

    @Test
    fun testOfflineHeuristicAiCreateNode() = runBlocking {
        val project = GameProject()
        val agent = AiAgent()
        val initialCount = project.nodes.size

        val result = agent.processUserPrompt("Create a player character", project)

        assertTrue(result.projectModified)
        assertEquals(initialCount + 1, project.nodes.size)
        val createdNode = project.nodes.last()
        assertEquals("Player", createdNode.name)
    }

    @Test
    fun testOfflineHeuristicAiGenerateScript() = runBlocking {
        val project = GameProject()
        val agent = AiAgent()

        val result = agent.processUserPrompt("Write GDScript for movement", project)

        assertTrue(result.projectModified)
        val node = project.nodes.first()
        assertTrue(node.scriptCode.contains("func _process"))
    }
}
