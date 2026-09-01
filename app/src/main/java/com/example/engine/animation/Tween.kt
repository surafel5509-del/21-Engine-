package com.example.engine.animation

import com.example.engine.math.MathUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Standard easing functions for smooth UI animations, particle fades, and object transitions.
 */
object Easing {
    fun linear(t: Float): Float = t

    fun quadIn(t: Float): Float = t * t
    fun quadOut(t: Float): Float = t * (2f - t)
    fun quadInOut(t: Float): Float = if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t

    fun cubicIn(t: Float): Float = t * t * t
    fun cubicOut(t: Float): Float = (t - 1f) * (t - 1f) * (t - 1f) + 1f
    fun cubicInOut(t: Float): Float = if (t < 0.5f) 4f * t * t * t else (t - 1f) * (2f * t - 2f) * (2f * t - 2f) + 1f

    fun sineIn(t: Float): Float = 1f - cos((t * MathUtils.PI) / 2f)
    fun sineOut(t: Float): Float = sin((t * MathUtils.PI) / 2f)
    fun sineInOut(t: Float): Float = -(cos(MathUtils.PI * t) - 1f) / 2f

    fun bounceOut(t: Float): Float {
        var n1 = 7.5625f
        var d1 = 2.75f
        var x = t

        return when {
            x < 1f / d1 -> n1 * x * x
            x < 2f / d1 -> {
                x -= 1.5f / d1
                n1 * x * x + 0.75f
            }
            x < 2.5f / d1 -> {
                x -= 2.25f / d1
                n1 * x * x + 0.9375f
            }
            else -> {
                x -= 2.625f / d1
                n1 * x * x + 0.984375f
            }
        }
    }

    fun bounceIn(t: Float): Float = 1f - bounceOut(1f - t)
}

/**
 * Lightweight Tween utility for animating numerical values smoothly.
 */
class Tween(
    var startValue: Float = 0f,
    var endValue: Float = 1f,
    var duration: Float = 1f,
    var easing: (Float) -> Float = Easing::quadOut,
    var onUpdate: ((Float) -> Unit)? = null,
    var onComplete: (() -> Unit)? = null
) {
    var elapsedTime: Float = 0f
        private set
    var isFinished: Boolean = false
        private set
    var currentValue: Float = startValue
        private set

    fun reset(start: Float = startValue, end: Float = endValue, dur: Float = duration) {
        startValue = start
        endValue = end
        duration = dur
        elapsedTime = 0f
        isFinished = false
        currentValue = start
    }

    fun update(dt: Float): Float {
        if (isFinished) return currentValue

        elapsedTime += dt
        val progress = MathUtils.clamp01(elapsedTime / duration)
        val eased = easing(progress)
        currentValue = MathUtils.lerp(startValue, endValue, eased)
        onUpdate?.invoke(currentValue)

        if (progress >= 1f) {
            isFinished = true
            onComplete?.invoke()
        }
        return currentValue
    }
}
