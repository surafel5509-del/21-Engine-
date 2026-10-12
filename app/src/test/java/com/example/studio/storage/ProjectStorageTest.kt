package com.example.studio.storage

import com.example.engine.scene.Prefab
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import org.junit.Assert.*
import org.junit.Test

class ProjectStorageTest {

    @Test
    fun testProjectNodeOperations() {
        val project = GameProject("TestGame")
        val playerNode = GameNode("Player", NodeType.RIGID_BODY_2D, posX = 100f, posY = 200f)
        project.addNode(playerNode)

        assertEquals(1, project.nodes.size)
        val found = project.findNode(playerNode.id)
        assertNotNull(found)
        assertEquals("Player", found?.name)

        project.removeNode(playerNode.id)
        assertEquals(0, project.nodes.size)
    }

    @Test
    fun testPrefabInstantiationIntoProject() {
        val project = GameProject("PrefabTest")
        val coinPrefab = Prefab.createCoinPrefab()

        val instance = coinPrefab.instantiate(300f, 400f)
        project.addNode(instance)

        assertEquals(1, project.nodes.size)
        assertEquals(300f, project.nodes[0].posX)
        assertEquals(400f, project.nodes[0].posY)
    }
}
