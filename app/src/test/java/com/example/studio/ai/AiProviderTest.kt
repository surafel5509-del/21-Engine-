package com.example.studio.ai

import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AiProviderTest {

    private lateinit var project: GameProject
    private lateinit var agentEngine: AiAgentEngine

    @Before
    fun setUp() {
        project = GameProject(name = "Test 2D Game")
        agentEngine = AiAgentEngine()
    }

    @Test
    fun testFactoryCreatesCorrectProviderInstances() {
        for (providerType in AiProviderType.values()) {
            val provider = AiProviderFactory.createProvider(providerType)
            assertEquals(providerType, provider.providerType)
            assertTrue(provider.getSupportedModels().isNotEmpty())
        }
    }

    @Test
    fun testOfflineAiAgentSpawnsPlayerNode() = runBlocking {
        val config = AiProviderConfig(providerType = AiProviderType.OPENAI, apiKey = "")
        val response = agentEngine.processUserPrompt("Create a CharacterBody2D player node", project, config, emptyList())

        assertTrue(response.replyText.contains("Offline AI Agent"))
        assertTrue(response.actions.isNotEmpty())

        val action = response.actions.first()
        assertEquals(ActionType.CREATE_NODE, action.type)

        val initialSize = project.nodes.size
        val result = agentEngine.applyActionToProject(action, project)

        assertTrue(result.contains("Created node"))
        assertEquals(initialSize + 1, project.nodes.size)
    }

    @Test
    fun testAiAgentModifiesNodeProperty() {
        val node = GameNode(name = "Enemy", nodeType = NodeType.RIGID_BODY_2D, moveSpeed = 100f)
        project.addNode(node)

        val action = AiAgentAction(
            type = ActionType.MODIFY_NODE,
            targetNodeName = "Enemy",
            propertyKey = "moveSpeed",
            propertyValue = "250"
        )

        val result = agentEngine.applyActionToProject(action, project)
        assertTrue(result.contains("Updated node"))
        assertEquals(250f, node.moveSpeed, 0.01f)
    }

    @Test
    fun testAiAgentAttachesScriptToNode() {
        val node = GameNode(name = "Coin", nodeType = NodeType.SPRITE_2D)
        project.addNode(node)

        val scriptCode = "extends Area2D\nfunc _ready(): print('Coin ready')"
        val action = AiAgentAction(
            type = ActionType.GENERATE_SCRIPT,
            targetNodeName = "Coin",
            scriptContent = scriptCode
        )

        val result = agentEngine.applyActionToProject(action, project)
        assertTrue(result.contains("Attached custom script"))
        assertEquals(scriptCode, node.customScriptCode)
    }
}
