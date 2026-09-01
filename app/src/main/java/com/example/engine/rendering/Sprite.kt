package com.example.engine.rendering

import android.graphics.Bitmap
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2

enum class FlipMode {
    NONE,
    FLIP_X,
    FLIP_Y,
    FLIP_XY
}

/**
 * 2D Sprite referencing a Bitmap or texture sub-region with pivot, tint, and alpha.
 */
class Sprite(
    var bitmap: Bitmap,
    var srcRect: Rect2D = Rect2D(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()),
    var width: Float = srcRect.width,
    var height: Float = srcRect.height,
    var pivot: Vector2 = Vector2(0.5f, 0.5f),
    var tintColor: Int = 0xFFFFFFFF.toInt(),
    var alpha: Float = 1.0f,
    var flip: FlipMode = FlipMode.NONE
) {
    fun setSourceRect(x: Float, y: Float, w: Float, h: Float): Sprite {
        srcRect.set(x, y, w, h)
        width = w
        height = h
        return this
    }

    fun setPivot(px: Float, py: Float): Sprite {
        pivot.set(px, py)
        return this
    }

    fun copy(): Sprite {
        return Sprite(
            bitmap = bitmap,
            srcRect = srcRect.copy(),
            width = width,
            height = height,
            pivot = pivot.copy(),
            tintColor = tintColor,
            alpha = alpha,
            flip = flip
        )
    }
}
