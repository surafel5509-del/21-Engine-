package com.example.engine.animation

import android.graphics.Bitmap
import com.example.engine.math.Rect2D
import com.example.engine.rendering.Sprite

/**
 * Slices a texture or spritesheet into individual animation frames.
 */
class SpriteSheet(
    val bitmap: Bitmap,
    val frameWidth: Int,
    val frameHeight: Int,
    val spacing: Int = 0,
    val margin: Int = 0
) {
    val columns: Int = (bitmap.width - margin * 2 + spacing) / (frameWidth + spacing)
    val rows: Int = (bitmap.height - margin * 2 + spacing) / (frameHeight + spacing)
    val totalFrames: Int = columns * rows

    fun getFrameRect(frameIndex: Int, outRect: Rect2D): Rect2D {
        val col = frameIndex % columns
        val row = frameIndex / columns
        val x = margin + col * (frameWidth + spacing)
        val y = margin + row * (frameHeight + spacing)
        outRect.set(x.toFloat(), y.toFloat(), frameWidth.toFloat(), frameHeight.toFloat())
        return outRect
    }

    fun createSprite(frameIndex: Int): Sprite {
        val rect = Rect2D()
        getFrameRect(frameIndex, rect)
        return Sprite(bitmap, rect, frameWidth.toFloat(), frameHeight.toFloat())
    }

    fun createAnimation(
        name: String,
        frameIndices: IntArray,
        fps: Float = 10f,
        loopMode: LoopMode = LoopMode.LOOP
    ): AnimationClip {
        val frameDuration = 1f / fps
        val frames = Array(frameIndices.size) { i ->
            val rect = Rect2D()
            getFrameRect(frameIndices[i], rect)
            AnimationFrame(rect, frameDuration)
        }
        return AnimationClip(name, bitmap, frames, loopMode)
    }
}
