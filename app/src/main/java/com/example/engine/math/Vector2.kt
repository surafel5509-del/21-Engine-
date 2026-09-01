package com.example.engine.math

import kotlin.math.*

/**
 * 2D Vector with complete mathematical operations and mutable object pooling
 * to avoid allocations in high-frequency game loops.
 */
data class Vector2(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun set(newX: Float, newY: Float): Vector2 {
        x = newX
        y = newY
        return this
    }

    fun set(other: Vector2): Vector2 {
        x = other.x
        y = other.y
        return this
    }

    fun add(other: Vector2): Vector2 {
        x += other.x
        y += other.y
        return this
    }

    fun add(dx: Float, dy: Float): Vector2 {
        x += dx
        y += dy
        return this
    }

    fun subtract(other: Vector2): Vector2 {
        x -= other.x
        y -= other.y
        return this
    }

    fun subtract(dx: Float, dy: Float): Vector2 {
        x -= dx
        y -= dy
        return this
    }

    fun scale(factor: Float): Vector2 {
        x *= factor
        y *= factor
        return this
    }

    fun divide(divisor: Float): Vector2 {
        if (divisor != 0f) {
            x /= divisor
            y /= divisor
        }
        return this
    }

    fun dot(other: Vector2): Float = x * other.x + y * other.y

    fun dot(ox: Float, oy: Float): Float = x * ox + y * oy

    fun cross(other: Vector2): Float = x * other.y - y * other.x

    fun lengthSquared(): Float = x * x + y * y

    fun length(): Float = sqrt(x * x + y * y)

    fun distanceSquared(other: Vector2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return dx * dx + dy * dy
    }

    fun distance(other: Vector2): Float = sqrt(distanceSquared(other))

    fun distance(ox: Float, oy: Float): Float {
        val dx = x - ox
        val dy = y - oy
        return sqrt(dx * dx + dy * dy)
    }

    fun normalize(): Vector2 {
        val len = length()
        if (len > 0.00001f) {
            x /= len
            y /= len
        } else {
            x = 0f
            y = 0f
        }
        return this
    }

    fun normalized(): Vector2 {
        val result = Vector2(x, y)
        result.normalize()
        return result
    }

    fun angle(): Float = atan2(y, x)

    fun angleDegrees(): Float = MathUtils.radToDeg(atan2(y, x))

    fun setAngle(radians: Float): Vector2 {
        val len = length()
        x = cos(radians) * len
        y = sin(radians) * len
        return this
    }

    fun setAngleDegrees(degrees: Float): Vector2 = setAngle(MathUtils.degToRad(degrees))

    fun rotate(radians: Float): Vector2 {
        val cos = cos(radians)
        val sin = sin(radians)
        val newX = x * cos - y * sin
        val newY = x * sin + y * cos
        x = newX
        y = newY
        return this
    }

    fun rotateDegrees(degrees: Float): Vector2 = rotate(MathUtils.degToRad(degrees))

    fun lerp(target: Vector2, t: Float): Vector2 {
        val clampedT = MathUtils.clamp01(t)
        x += (target.x - x) * clampedT
        y += (target.y - y) * clampedT
        return this
    }

    fun clampLength(maxLength: Float): Vector2 {
        val lenSq = lengthSquared()
        if (lenSq > maxLength * maxLength && lenSq > 0f) {
            val factor = maxLength / sqrt(lenSq)
            x *= factor
            y *= factor
        }
        return this
    }

    fun reflect(normal: Vector2): Vector2 {
        val d = 2f * dot(normal)
        x -= d * normal.x
        y -= d * normal.y
        return this
    }

    fun copy(): Vector2 = Vector2(x, y)

    fun isZero(epsilon: Float = 0.0001f): Boolean = abs(x) < epsilon && abs(y) < epsilon

    override fun toString(): String = "Vector2(x=%.2f, y=%.2f)".format(x, y)

    companion object {
        val ZERO: Vector2 get() = Vector2(0f, 0f)
        val ONE: Vector2 get() = Vector2(1f, 1f)
        val UP: Vector2 get() = Vector2(0f, -1f)
        val DOWN: Vector2 get() = Vector2(0f, 1f)
        val LEFT: Vector2 get() = Vector2(-1f, 0f)
        val RIGHT: Vector2 get() = Vector2(1f, 0f)

        fun distance(a: Vector2, b: Vector2): Float = a.distance(b)

        fun lerp(a: Vector2, b: Vector2, t: Float): Vector2 {
            val clampedT = MathUtils.clamp01(t)
            return Vector2(
                a.x + (b.x - a.x) * clampedT,
                a.y + (b.y - a.y) * clampedT
            )
        }

        fun fromAngle(radians: Float, length: Float = 1f): Vector2 {
            return Vector2(cos(radians) * length, sin(radians) * length)
        }

        fun fromAngleDegrees(degrees: Float, length: Float = 1f): Vector2 {
            return fromAngle(MathUtils.degToRad(degrees), length)
        }
    }
}
