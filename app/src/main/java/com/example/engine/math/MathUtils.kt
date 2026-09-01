package com.example.engine.math

import kotlin.math.*

object MathUtils {
    const val PI = Math.PI.toFloat()
    const val TWO_PI = (Math.PI * 2.0).toFloat()
    const val HALF_PI = (Math.PI / 2.0).toFloat()
    const val DEG_TO_RAD = (Math.PI / 180.0).toFloat()
    const val RAD_TO_DEG = (180.0 / Math.PI).toFloat()

    fun degToRad(deg: Float): Float = deg * DEG_TO_RAD
    fun radToDeg(rad: Float): Float = rad * RAD_TO_DEG

    fun clamp(value: Float, min: Float, max: Float): Float = max(min, min(max, value))
    fun clamp(value: Int, min: Int, max: Int): Int = max(min, min(max, value))
    fun clamp01(value: Float): Float = clamp(value, 0f, 1f)

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * clamp01(t)
    fun lerpUnclamped(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    fun inverseLerp(a: Float, b: Float, value: Float): Float {
        if (abs(b - a) < 0.00001f) return 0f
        return clamp01((value - a) / (b - a))
    }

    fun mapRange(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
        val t = inverseLerp(inMin, inMax, value)
        return lerp(outMin, outMax, t)
    }

    fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
        val t = clamp01((x - edge0) / (edge1 - edge0))
        return t * t * (3f - 2f * t)
    }

    fun approach(current: Float, target: Float, maxDelta: Float): Float {
        return if (current < target) {
            min(current + maxDelta, target)
        } else {
            max(current - maxDelta, target)
        }
    }

    fun wrapAngleDegrees(degrees: Float): Float {
        var result = degrees % 360f
        if (result < 0f) result += 360f
        return result
    }

    fun wrapAngleRadians(radians: Float): Float {
        var result = radians % TWO_PI
        if (result < 0f) result += TWO_PI
        return result
    }

    fun randomRange(min: Float, max: Float): Float {
        return min + (Math.random().toFloat() * (max - min))
    }

    fun randomRange(min: Int, max: Int): Int {
        return min + (Math.random() * (max - min + 1)).toInt()
    }
}
