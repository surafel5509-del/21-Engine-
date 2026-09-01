package com.example.engine.math

import kotlin.math.max
import kotlin.math.min

/**
 * 2D Axis-Aligned Bounding Box (AABB) with intersection, containment, and helper utilities.
 */
data class Rect2D(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f
) {
    val left: Float get() = x
    val top: Float get() = y
    val right: Float get() = x + width
    val bottom: Float get() = y + height
    val centerX: Float get() = x + width * 0.5f
    val centerY: Float get() = y + height * 0.5f

    fun set(x: Float, y: Float, width: Float, height: Float): Rect2D {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        return this
    }

    fun set(other: Rect2D): Rect2D {
        this.x = other.x
        this.y = other.y
        this.width = other.width
        this.height = other.height
        return this
    }

    fun setBounds(left: Float, top: Float, right: Float, bottom: Float): Rect2D {
        this.x = left
        this.y = top
        this.width = right - left
        this.height = bottom - top
        return this
    }

    fun contains(px: Float, py: Float): Boolean {
        return px >= left && px <= right && py >= top && py <= bottom
    }

    fun contains(point: Vector2): Boolean = contains(point.x, point.y)

    fun contains(other: Rect2D): Boolean {
        return left <= other.left && right >= other.right && top <= other.top && bottom >= other.bottom
    }

    fun intersects(other: Rect2D): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }

    fun intersects(circle: Circle2D): Boolean {
        val closestX = MathUtils.clamp(circle.x, left, right)
        val closestY = MathUtils.clamp(circle.y, top, bottom)
        val dx = circle.x - closestX
        val dy = circle.y - closestY
        return (dx * dx + dy * dy) <= (circle.radius * circle.radius)
    }

    fun expand(amount: Float): Rect2D {
        x -= amount
        y -= amount
        width += amount * 2f
        height += amount * 2f
        return this
    }

    fun offset(dx: Float, dy: Float): Rect2D {
        x += dx
        y += dy
        return this
    }

    fun copy(): Rect2D = Rect2D(x, y, width, height)

    companion object {
        fun fromCenter(centerX: Float, centerY: Float, width: Float, height: Float): Rect2D {
            return Rect2D(centerX - width * 0.5f, centerY - height * 0.5f, width, height)
        }
    }
}
