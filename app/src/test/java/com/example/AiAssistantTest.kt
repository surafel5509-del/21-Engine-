package com.example

import com.example.studio.ai.*
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiAssistantTest {

    @Test
    fun testAiAgentRegistryProviders() {
        val registry = AiAgentRegistry()
        val providers = registry.getAllProviders()

        assertTrue(providers.isNotEmpty())
        assertNotNull(registry.getProvider("local_rule_based"))
        assertNotNull(registry.getProvider("openai_compatible"))
        assertNotNull(registry.getProvider("firebase_gemini"))
    }

    @Test
    fun testRuleBasedLocalAiProviderCreateAction() = runBlocking {
        val provider = RuleBasedLocalAiProvider()
        val project = GameProject(name = "TestProject")
        val context = AiContext(project = project)

        val response = provider.generateResponse("create a platform", context, ProviderConfig("local_rule_based"))

        assertNotNull(response.suggestedAction)
        assertTrue(response.suggestedAction is AiAction.CreateNode)

        val createAction = response.suggestedAction as AiAction.CreateNode
        assertEquals("Platform_Node", createAction.name)
        assertEquals("STATIC_BODY_2D", createAction.nodeType)
        assertEquals("STATIC", createAction.bodyType)
    }

    @Test
    fun testEngineAiAssistantActionExecution() {
        val assistant = EngineAiAssistant()
        val project = GameProject(name = "TestProject")

        val action = AiAction.CreateNode(
            name = "Test_Hero",
            nodeType = "RIGID_BODY_2D",
            posX = 500f,
            posY = 300f,
            colorHex = "#38BDF8"
        )

        val (updatedProject, newNode) = assistant.executeAction(action, project)

        assertNotNull(newNode)
        assertEquals("Test_Hero", newNode?.name)
        assertEquals(NodeType.RIGID_BODY_2D, newNode?.nodeType)
        assertEquals(1, updatedProject.nodes.size)
        assertEquals("Test_Hero", updatedProject.nodes.first().name)
    }

    @Test
    fun testModifyNodePropertyActionExecution() {
        val assistant = EngineAiAssistant()
        val node = GameNode(id = "node_1", name = "TestNode", posX = 100f, posY = 100f)
        val project = GameProject(name = "TestProject", nodes = mutableListOf(node))

        val action = AiAction.ModifyNodeProperty(
            nodeId = "node_1",
            propertyName = "posX",
            value = "640.0"
        )

        val (updatedProject, targetNode) = assistant.executeAction(action, project, node)

        assertNotNull(targetNode)
        assertEquals(640f, targetNode?.posX)
    }
}
