package com.example

import com.example.engine.components.TilemapComponent
import com.example.engine.scene.PrefabManager
import com.example.engine.scripting.ScriptEngine
import com.example.engine.scripting.ScriptEnvironment
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import org.junit.Assert.*
import org.junit.Test

class EngineCoreUnitTest {

    @Test
    fun testScriptEngineExecution() {
        val project = GameProject()
        val node = GameNode(posX = 100f, posY = 200f)
        var soundPlayed: String? = null
        var messageLogged: String? = null

        val env = ScriptEnvironment(
            node = node,
            project = project,
            onPlaySound = { soundPlayed = it },
            onLogMessage = { messageLogged = it }
        )

        val code = """
            func _ready():
                print("Game Started!")
                play_sound("coin")

            func _process(delta):
                position_x += 100 * delta
        """.trimIndent()

        // Execute _ready
        ScriptEngine.executeHook("_ready", code, env)
        assertEquals("coin", soundPlayed)
        assertNotNull(messageLogged)

        // Execute _process(0.1f)
        val initialX = node.posX
        ScriptEngine.executeHook("_process", code, env, deltaTime = 0.1f)
        assertEquals(initialX + 10f, node.posX, 0.01f)
    }

    @Test
    fun testTilemapComponent() {
        val tilemap = TilemapComponent(cols = 5, rows = 5, tileSize = 32f)
        assertEquals(0, tilemap.getTile(0, 0))

        tilemap.setTile(2, 2, 1) // Set solid ground
        assertEquals(1, tilemap.getTile(2, 2))

        val solidRects = tilemap.getSolidTileRects(0f, 0f)
        assertEquals(1, solidRects.size)
        assertEquals(64f, solidRects[0].x, 0.01f)
        assertEquals(64f, solidRects[0].y, 0.01f)
    }

    @Test
    fun testPrefabManagerInstantiate() {
        val player = PrefabManager.instantiate("player", 100f, 200f)
        assertEquals("Player", player.name)
        assertEquals(100f, player.posX, 0.01f)
        assertEquals(200f, player.posY, 0.01f)
        assertTrue(player.hasPhysics)

        val coin = PrefabManager.instantiate("coin", 300f, 400f)
        assertEquals("Coin", coin.name)
        assertEquals(100, coin.scoreValue)
    }
}
