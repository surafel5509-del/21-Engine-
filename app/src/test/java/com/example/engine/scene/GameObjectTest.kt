package com.example.engine.scene

import org.junit.Assert.*
import org.junit.Test

class GameObjectTest {

    @Test
    fun testGameObjectHierarchy() {
        val parent = GameObject("Parent")
        val child = GameObject("Child")

        parent.addChild(child)

        assertEquals(1, parent.childCount)
        assertEquals(parent, child.parent)
        assertEquals(child, parent.getChildAt(0))
    }

    @Test
    fun testTransformPropagation() {
        val parent = GameObject("Parent")
        val child = GameObject("Child")

        parent.setPosition(100f, 50f)
        child.setPosition(10f, 20f)
        parent.addChild(child)

        val worldPos = child.transform.worldPosition
        assertEquals(110f, worldPos.x, 0.001f)
        assertEquals(70f, worldPos.y, 0.001f)
    }
}
