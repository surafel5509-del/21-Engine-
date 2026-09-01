package com.example.engine.rendering

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.camera.Camera2D
import com.example.engine.math.Circle2D
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2

/**
 * Abstract 2D Renderer interface.
 * Designed modularly so concrete implementations can use Android Canvas or OpenGL ES / Vulkan backends.
 */
interface Renderer {
    /** Clear screen with solid color */
    fun clear(color: Int)

    /** Begin frame */
    fun beginFrame()

    /** End frame */
    fun endFrame()

    /** Apply camera transformation to world coordinates */
    fun beginCamera(camera: Camera2D)

    /** Reset to screen/logical coordinate space (for UI / HUD rendering) */
    fun endCamera()

    // Matrix Stack
    fun pushMatrix()
    fun popMatrix()
    fun translate(dx: Float, dy: Float)
    fun rotate(degrees: Float)
    fun rotate(degrees: Float, px: Float, py: Float)
    fun scale(sx: Float, sy: Float)
    fun scale(sx: Float, sy: Float, px: Float, py: Float)

    // Alpha / Global State
    fun setGlobalAlpha(alpha: Float)
    fun getGlobalAlpha(): Float

    // Sprite Drawing
    fun drawSprite(
        sprite: Sprite,
        x: Float,
        y: Float,
        width: Float = sprite.width,
        height: Float = sprite.height,
        rotation: Float = 0f,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        alpha: Float = sprite.alpha
    )

    fun drawBitmap(
        bitmap: Bitmap,
        srcRect: Rect2D?,
        dstRect: Rect2D,
        alpha: Float = 1f,
        rotation: Float = 0f
    )

    // Shapes
    fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 1f,
        cornerRadius: Float = 0f
    )

    fun drawRect(
        rect: Rect2D,
        color: Int,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 1f,
        cornerRadius: Float = 0f
    ) = drawRect(rect.x, rect.y, rect.width, rect.height, color, style, strokeWidth, cornerRadius)

    fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 1f
    )

    fun drawCircle(
        circle: Circle2D,
        color: Int,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 1f
    ) = drawCircle(circle.x, circle.y, circle.radius, color, style, strokeWidth)

    fun drawLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
        strokeWidth: Float = 2f
    )

    fun drawLine(
        p1: Vector2,
        p2: Vector2,
        color: Int,
        strokeWidth: Float = 2f
    ) = drawLine(p1.x, p1.y, p2.x, p2.y, color, strokeWidth)

    // Text
    fun drawText(
        text: String,
        x: Float,
        y: Float,
        fontSize: Float = 24f,
        color: Int = 0xFFFFFFFF.toInt(),
        align: Paint.Align = Paint.Align.LEFT,
        typeface: Typeface? = null,
        shadowColor: Int = 0x00000000,
        shadowRadius: Float = 0f
    )

    fun measureTextWidth(text: String, fontSize: Float, typeface: Typeface? = null): Float
}
