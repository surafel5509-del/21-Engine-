package com.example.studio.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiProviderTest {

    @Test
    fun testOfflineHeuristicProviderScriptGeneration() = runBlocking {
        val provider = OfflineHeuristicProvider()
        val context = AiContext(selectedNodeName = "PlayerNode")

        val result = provider.generateResponse("Create player script", context)

        assertTrue(result.isSuccess)
        assertNotNull(result.generatedScript)
        assertTrue(result.generatedScript!!.contains("class PlayerScript"))
    }

    @Test
    fun testAiAgentProviderSwitchingAndFallback() = runBlocking {
        val agent = AiAgent(listOf(OfflineHeuristicProvider()))

        assertEquals("offline_heuristics", agent.getActiveProvider().id)

        val result = agent.processUserPrompt("Hello engine", AiContext())
        assertTrue(result.isSuccess)
        assertTrue(result.textResponse.isNotEmpty())
    }
}
