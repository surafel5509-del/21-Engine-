package com.example

import com.example.studio.ai.AiActionType
import com.example.studio.ai.AiAgent
import com.example.studio.ai.AiConfig
import com.example.studio.ai.AiProviderType
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAgentTest {

    @Test
    fun testOfflineHeuristicAiAgentCreatesPlayerNode() = runBlocking {
        val agent = AiAgent(config = AiConfig(providerType = AiProviderType.LOCAL_HEURISTIC))
        val project = GameProject(name = "Test 2D Game")

        val response = agent.processPrompt("Create a new player character with physics", project)
        assertNotNull(response)
        assertTrue(response.actions.isNotEmpty())

        val action = response.actions.first()
        assertEquals(AiActionType.ADD_NODE, action.type)

        val resultMsg = agent.executeAction(action, project)
        assertTrue(resultMsg.contains("Created new node"))
        assertEquals(1, project.nodes.size)
        assertEquals("Player", project.nodes.first().name)
        assertEquals(NodeType.RIGID_BODY_2D, project.nodes.first().nodeType)
        assertTrue(project.nodes.first().hasPhysics)
    }

    @Test
    fun testOfflineHeuristicAiAgentGeneratesScript() = runBlocking {
        val agent = AiAgent(config = AiConfig(providerType = AiProviderType.LOCAL_HEURISTIC))
        val project = GameProject(name = "Script Test")

        val response = agent.processPrompt("Generate jump script", project)
        assertNotNull(response)

        val action = response.actions.first()
        assertEquals(AiActionType.GENERATE_SCRIPT, action.type)
        assertTrue(action.details.containsKey("code"))
    }
}
