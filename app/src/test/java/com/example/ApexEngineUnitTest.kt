package com.example

import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import com.example.studio.ai.AiEngineToolExecutor
import com.example.studio.ai.AiToolAction
import com.example.studio.ai.AiToolCommand
import com.example.studio.ai.generateLocalAiSimulation
import com.example.studio.ai.parseToolCommandFromText
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ApexEngineUnitTest {

    @Test
    fun testVector2Math() {
        val v1 = Vector2(10f, 20f)
        val v2 = Vector2(5f, 15f)
        val sum = v1.add(v2)
        assertEquals(15f, sum.x, 0.001f)
        assertEquals(35f, sum.y, 0.001f)
    }

    @Test
    fun testRect2DIntersection() {
        val rectA = Rect2D(0f, 0f, 100f, 100f)
        val rectB = Rect2D(50f, 50f, 100f, 100f)
        val rectC = Rect2D(200f, 200f, 50f, 50f)

        assertTrue(rectA.intersects(rectB))
        assertFalse(rectA.intersects(rectC))
    }

    @Test
    fun testGameProjectNodeOperations() {
        val project = GameProject(name = "Test Engine Project")
        val node1 = GameNode(name = "Player", nodeType = NodeType.CHARACTER_BODY_2D)
        val node2 = GameNode(name = "Coin", nodeType = NodeType.AREA_2D)

        project.addNode(node1)
        project.addNode(node2)

        assertEquals(2, project.nodes.size)
        assertEquals(node2, project.findNode(node2.id))

        project.removeNode(node1.id)
        assertEquals(1, project.nodes.size)
        assertNull(project.findNode(node1.id))
    }

    @Test
    fun testAiEngineToolExecutor() {
        val project = GameProject(name = "AI Test Scene")
        val executor = AiEngineToolExecutor()

        // 1. Create Node Tool
        val createCommand = AiToolCommand(
            action = AiToolAction.CREATE_NODE,
            nodeName = "AI_Enemy",
            nodeType = "RIGID_BODY_2D",
            posX = 500f,
            posY = 300f,
            color = 0xFFEF4444
        )
        val createResult = executor.executeCommand(createCommand, project)
        assertTrue(createResult.success)
        assertEquals(1, project.nodes.size)
        assertEquals("AI_Enemy", project.nodes.first().name)

        // 2. Modify Settings Tool
        val settingsCommand = AiToolCommand(
            action = AiToolAction.MODIFY_PROJECT_SETTINGS,
            gravityX = 0f,
            gravityY = 1500f
        )
        val settingsResult = executor.executeCommand(settingsCommand, project)
        assertTrue(settingsResult.success)
        assertEquals(1500f, project.gravityY, 0.001f)

        // 3. Run Tests Tool
        val testCommand = AiToolCommand(action = AiToolAction.RUN_TESTS)
        val testResult = executor.executeCommand(testCommand, project)
        assertTrue(testResult.success)

        // 4. Delete Node Tool
        val deleteCommand = AiToolCommand(
            action = AiToolAction.DELETE_NODE,
            nodeName = "AI_Enemy"
        )
        val deleteResult = executor.executeCommand(deleteCommand, project)
        assertTrue(deleteResult.success)
        assertEquals(0, project.nodes.size)
    }

    @Test
    fun testLocalAiSimulationAndParsing() {
        val project = GameProject(name = "Simulation Test")
        val (text, toolCmd) = generateLocalAiSimulation("Create a player hero", project)

        assertNotNull(text)
        assertNotNull(toolCmd)
        assertEquals(AiToolAction.CREATE_NODE, toolCmd?.action)
        assertEquals("Player_Hero", toolCmd?.nodeName)

        val jsonBlock = """
            Here is the action:
            ```json_tool
            {
              "action": "CREATE_NODE",
              "nodeName": "Test_Node",
              "nodeType": "SPRITE_2D",
              "posX": 100.0,
              "posY": 200.0
            }
            ```
        """.trimIndent()

        val parsedCmd = parseToolCommandFromText(jsonBlock)
        assertNotNull(parsedCmd)
        assertEquals(AiToolAction.CREATE_NODE, parsedCmd?.action)
        assertEquals("Test_Node", parsedCmd?.nodeName)
        assertEquals(100f, parsedCmd?.posX ?: 0f, 0.001f)
    }
}
