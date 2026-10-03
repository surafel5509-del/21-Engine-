package com.example.studio

import com.example.studio.ai.AiConfig
import com.example.studio.ai.AiGameAgent
import com.example.studio.ai.AiProviderType
import com.example.studio.model.GameProject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiGameAgentTest {

    @Test
    fun testLocalAgentActionAddCoin() = runBlocking {
        val agent = AiGameAgent(AiConfig(providerType = AiProviderType.OPENAI))
        val project = GameProject(name = "TestProj")
        val initialSize = project.nodes.size

        val result = agent.processUserPrompt("Please add coin to scene", project)
        assertNotNull(result.modifiedProject)
        assertEquals(initialSize + 1, result.modifiedProject!!.nodes.size)
        assertTrue(result.replyText.contains("GoldCoin"))
    }

    @Test
    fun testLocalAgentActionAddPlatform() = runBlocking {
        val agent = AiGameAgent(AiConfig(providerType = AiProviderType.OPENAI))
        val project = GameProject(name = "TestProj")
        val initialSize = project.nodes.size

        val result = agent.processUserPrompt("Add platform for player", project)
        assertNotNull(result.modifiedProject)
        assertEquals(initialSize + 1, result.modifiedProject!!.nodes.size)
        assertTrue(result.replyText.contains("FloatingPlatform"))
    }
}
