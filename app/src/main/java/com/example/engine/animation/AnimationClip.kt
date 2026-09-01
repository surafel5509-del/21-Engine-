package com.example.engine.animation

import android.graphics.Bitmap
import com.example.engine.math.Rect2D
import com.example.engine.rendering.Sprite

enum class LoopMode {
    LOOP,
    ONCE,
    PING_PONG,
    REVERSE
}

data class AnimationFrame(
    val srcRect: Rect2D,
    val duration: Float
)

/**
 * Named animation sequence made up of timed frames.
 */
class AnimationClip(
    val name: String,
    val bitmap: Bitmap,
    val frames: Array<AnimationFrame>,
    val loopMode: LoopMode = LoopMode.LOOP
) {
    val totalDuration: Float = frames.sumOf { it.duration.toDouble() }.toFloat()
    val frameCount: Int get() = frames.size

    fun getFrameIndex(elapsedTime: Float): Int {
        if (frames.isEmpty()) return 0
        if (totalDuration <= 0f) return 0

        var t = elapsedTime
        when (loopMode) {
            LoopMode.ONCE -> {
                if (t >= totalDuration) return frames.size - 1
            }
            LoopMode.LOOP -> {
                t %= totalDuration
            }
            LoopMode.PING_PONG -> {
                val cycle = totalDuration * 2f
                val mod = t % cycle
                t = if (mod < totalDuration) mod else cycle - mod
            }
            LoopMode.REVERSE -> {
                t = totalDuration - (t % totalDuration)
            }
        }

        var accumulated = 0f
        for (i in frames.indices) {
            accumulated += frames[i].duration
            if (t <= accumulated) {
                return i
            }
        }
        return frames.size - 1
    }
}

/**
 * State controller for playing and blending 2D sprite animations.
 */
class Animator {
    private val clips = mutableMapOf<String, AnimationClip>()
    var currentClip: AnimationClip? = null
        private set

    var playbackSpeed: Float = 1.0f
    var isPlaying: Boolean = true
    var elapsedTime: Float = 0f
        private set

    val currentFrameIndex: Int
        get() = currentClip?.getFrameIndex(elapsedTime) ?: 0

    val isFinished: Boolean
        get() {
            val clip = currentClip ?: return true
            return clip.loopMode == LoopMode.ONCE && elapsedTime >= clip.totalDuration
        }

    fun addClip(clip: AnimationClip): Animator {
        clips[clip.name] = clip
        if (currentClip == null) {
            play(clip.name)
        }
        return this
    }

    fun play(clipName: String, restart: Boolean = false) {
        val clip = clips[clipName] ?: return
        if (currentClip?.name != clipName || restart) {
            currentClip = clip
            elapsedTime = 0f
            isPlaying = true
        }
    }

    fun pause() {
        isPlaying = false
    }

    fun resume() {
        isPlaying = true
    }

    fun stop() {
        isPlaying = false
        elapsedTime = 0f
    }

    fun update(dt: Float) {
        if (!isPlaying) return
        val clip = currentClip ?: return
        elapsedTime += dt * playbackSpeed
    }

    fun applyToSprite(sprite: Sprite) {
        val clip = currentClip ?: return
        val frameIdx = getFrameIndex(elapsedTime)
        if (frameIdx in clip.frames.indices) {
            val frame = clip.frames[frameIdx]
            sprite.bitmap = clip.bitmap
            sprite.setSourceRect(frame.srcRect.x, frame.srcRect.y, frame.srcRect.width, frame.srcRect.height)
        }
    }

    private fun getFrameIndex(time: Float): Int = currentClip?.getFrameIndex(time) ?: 0
}
