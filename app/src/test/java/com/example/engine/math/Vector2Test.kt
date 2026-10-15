package com.example.engine.math

import org.junit.Assert.*
import org.junit.Test

class Vector2Test {

    @Test
    fun testVector2AdditionAndSubtraction() {
        val v1 = Vector2(3f, 4f)
        val v2 = Vector2(1f, 2f)

        v1.add(v2)
        assertEquals(4f, v1.x, 0.001f)
        assertEquals(6f, v1.y, 0.001f)

        v1.subtract(Vector2(2f, 1f))
        assertEquals(2f, v1.x, 0.001f)
        assertEquals(5f, v1.y, 0.001f)
    }

    @Test
    fun testVector2LengthAndNormalize() {
        val v = Vector2(3f, 4f)
        assertEquals(5f, v.length(), 0.001f)

        v.normalize()
        assertEquals(1f, v.length(), 0.001f)
        assertEquals(0.6f, v.x, 0.001f)
        assertEquals(0.8f, v.y, 0.001f)
    }

    @Test
    fun testVector2DotAndCross() {
        val a = Vector2(1f, 0f)
        val b = Vector2(0f, 1f)

        assertEquals(0f, a.dot(b), 0.001f)
        assertEquals(1f, a.cross(b), 0.001f)
    }
}
