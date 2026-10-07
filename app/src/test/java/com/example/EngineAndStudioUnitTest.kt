package com.example

import com.example.engine.math.Vector2
import com.example.engine.math.Rect2D
import com.example.engine.physics.PhysicsWorld2D
import com.example.studio.ai.AiAgentConfig
import com.example.studio.ai.AiGameDevAgent
import com.example.studio.ai.AiProviderType
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EngineAndStudioUnitTest {

    @Test
    fun testVector2Math() {
        val v1 = Vector2(10f, 20f)
        val v2 = Vector2(5f, 5f)
        v1.add(v2)
        assertEquals(15f, v1.x, 0.001f)
        assertEquals(25f, v1.y, 0.001f)
    }

    @Test
    fun testRect2DIntersection() {
        val r1 = Rect2D(0f, 0f, 100f, 100f)
        val r2 = Rect2D(50f, 50f, 100f, 100f)
        val r3 = Rect2D(200f, 200f, 50f, 50f)

        assertTrue(r1.intersects(r2))
        assertFalse(r1.intersects(r3))
    }

    @Test
    fun testGameProjectNodeOperations() {
        val project = GameProject(name = "Test Game")
        val node1 = GameNode(name = "Player", nodeType = NodeType.RIGID_BODY_2D, posX = 100f, posY = 200f)

        project.addNode(node1)
        assertEquals(1, project.nodes.size)

        val found = project.findNode(node1.id)
        assertNotNull(found)
        assertEquals("Player", found?.name)

        project.removeNode(node1.id)
        assertEquals(0, project.nodes.size)
    }

    @Test
    fun testAiAgentCommands() = runBlocking {
        val project = GameProject(name = "AI Test Project")
        val agent = AiGameDevAgent(
            config = AiAgentConfig(
                providerType = AiProviderType.OPENAI,
                apiKey = ""
            )
        )

        val result = agent.processUserPrompt("Create a rigid body node named Hero", project)
        assertNotNull(result.responseText)
        assertTrue(project.nodes.isNotEmpty())
        val created = project.nodes.first()
        assertEquals("Player_Hero", created.name)
    }
}
