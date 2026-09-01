package com.example.engine.math

import kotlin.math.sqrt

/**
 * 2D Circle representation for spatial checks and collisions.
 */
data class Circle2D(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 0f
) {
    val center: Vector2 get() = Vector2(x, y)

    fun set(x: Float, y: Float, radius: Float): Circle2D {
        this.x = x
        this.y = y
        this.radius = radius
        return this
    }

    fun set(center: Vector2, radius: Float): Circle2D = set(center.x, center.y, radius)

    fun contains(px: Float, py: Float): Boolean {
        val dx = x - px
        val dy = y - py
        return (dx * dx + dy * dy) <= (radius * radius)
    }

    fun contains(point: Vector2): Boolean = contains(point.x, point.y)

    fun intersects(other: Circle2D): Boolean {
        val dx = x - other.x
        val dy = y - other.y
        val rSum = radius + other.radius
        return (dx * dx + dy * dy) <= (rSum * rSum)
    }

    fun intersects(rect: Rect2D): Boolean = rect.intersects(this)

    fun copy(): Circle2D = Circle2D(x, y, radius)
}
