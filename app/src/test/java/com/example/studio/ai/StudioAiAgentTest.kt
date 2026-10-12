package com.example.studio.ai

import com.example.studio.ai.providers.LocalHeuristicProvider
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class StudioAiAgentTest {

    @Test
    fun testLocalHeuristicScriptGeneration() = runBlocking {
        val provider = LocalHeuristicProvider()
        val response = provider.generateScript("make player move", null)

        assertFalse(response.isError)
        assertNotNull(response.suggestedScript)
        assertTrue(response.suggestedScript!!.contains("speed"))
    }

    @Test
    fun testObjectCreationByAiAgent() = runBlocking {
        val agent = StudioAiAgent()
        val project = GameProject("TestProj")

        val response = agent.createObject("create a platform block", project)
        assertNotNull(response.modifiedProject)

        val addedNode = response.modifiedProject!!.nodes.lastOrNull()
        assertNotNull(addedNode)
        assertEquals(NodeType.STATIC_BODY_2D, addedNode!!.nodeType)
    }

    @Test
    fun testProviderSwitching() {
        val agent = StudioAiAgent()
        assertEquals(ProviderType.LOCAL_HEURISTIC, agent.currentProviderType)

        agent.currentProviderType = ProviderType.OPEN_AI
        assertEquals(ProviderType.OPEN_AI, agent.getActiveProvider().type)
    }
}
