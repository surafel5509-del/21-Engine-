package com.example.engine

import com.example.engine.scripting.GdScriptEngine
import com.example.engine.tilemap.TilemapLayer
import com.example.engine.tilemap.TileType
import com.example.studio.model.GameNode
import com.example.studio.model.PrefabManager
import org.junit.Assert.*
import org.junit.Test

class ScriptingAndTilemapTest {

    @Test
    fun testGdScriptExecution() {
        val node = GameNode(name = "TestNode", posX = 100f, posY = 100f)
        val script = """
            var speed = 200.0

            func _ready():
                print("Node Ready")

            func _process(delta):
                position.x += speed * delta
        """.trimIndent()

        val engine = GdScriptEngine(node, script)
        assertEquals(200.0f, engine.variables["speed"])

        engine.executeReady()
        engine.executeProcess(0.5f) // Should move pos_x by 200 * 0.5 = 100
        assertEquals(200f, node.posX, 0.01f)
    }

    @Test
    fun testTilemapLayerSetAndGet() {
        val tilemap = TilemapLayer(cols = 10, rows = 10, tileSize = 32f)
        tilemap.setTile(2, 3, TileType.BRICK.id)
        assertEquals(TileType.BRICK, tilemap.getTile(2, 3))
        assertTrue(tilemap.getTile(2, 3).isSolid)
    }

    @Test
    fun testPrefabManagerInstantiate() {
        val player = PrefabManager.instantiatePrefab("PlayerCharacter", 300f, 400f)
        assertNotNull(player)
        assertEquals("PlayerCharacter_Copy", player!!.name)
        assertEquals(300f, player.posX, 0.01f)
        assertEquals(400f, player.posY, 0.01f)
    }
}
