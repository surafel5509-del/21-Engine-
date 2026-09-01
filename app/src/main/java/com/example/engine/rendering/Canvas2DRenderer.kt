package com.example.engine.rendering

import android.graphics.*
import com.example.engine.camera.Camera2D
import com.example.engine.math.MathUtils
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import com.example.engine.viewport.ViewportManager

/**
 * Concrete Android Canvas implementation of the Renderer interface.
 * Built with strict zero-allocation design in the render loop using reusable Paint pools,
 * pre-allocated Rect objects, and cached Matrix stacks.
 */
class Canvas2DRenderer(
    val viewportManager: ViewportManager
) : Renderer {

    var canvas: Canvas? = null

    // Reusable Paints
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = Color.WHITE
    }
    private val letterboxPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    // Reusable Rects and Matrices for zero GC allocations
    private val srcRect = Rect()
    private val dstRectF = RectF()
    private val transformMatrix = Matrix()
    private val drawMatrix = Matrix()

    // Global Alpha Stack
    private var globalAlpha = 1.0f
    private val alphaStack = FloatArray(16)
    private var alphaStackPointer = 0

    // Reusable Text Measurement Bounds
    private val textBounds = Rect()

    override fun clear(color: Int) {
        canvas?.drawColor(color)
    }

    override fun beginFrame() {
        val c = canvas ?: return
        c.save()

        // 1. Clear letterbox outer area
        c.drawColor(Color.BLACK)

        // 2. Set viewport clipping & scaling
        c.clipRect(viewportManager.viewportRect.left, viewportManager.viewportRect.top,
            viewportManager.viewportRect.right, viewportManager.viewportRect.bottom)

        c.translate(viewportManager.offsetX, viewportManager.offsetY)
        c.scale(viewportManager.scaleX, viewportManager.scaleY)

        globalAlpha = 1.0f
        alphaStackPointer = 0
    }

    override fun endFrame() {
        val c = canvas ?: return
        c.restore()
    }

    override fun beginCamera(camera: Camera2D) {
        val c = canvas ?: return
        c.save()

        val halfW = camera.viewportWidth * 0.5f
        val halfH = camera.viewportHeight * 0.5f

        // Translate to screen center
        c.translate(halfW, halfH)

        // Apply camera zoom
        c.scale(camera.zoom, camera.zoom)

        // Apply camera rotation + shake angle
        val totalRotation = -(camera.rotationDegrees + camera.shakeAngle)
        if (totalRotation != 0f) {
            c.rotate(totalRotation)
        }

        // Translate camera position + shake offset
        c.translate(-(camera.position.x + camera.shakeOffset.x), -(camera.position.y + camera.shakeOffset.y))
    }

    override fun endCamera() {
        canvas?.restore()
    }

    override fun pushMatrix() {
        canvas?.save()
        if (alphaStackPointer < alphaStack.size) {
            alphaStack[alphaStackPointer++] = globalAlpha
        }
    }

    override fun popMatrix() {
        canvas?.restore()
        if (alphaStackPointer > 0) {
            globalAlpha = alphaStack[--alphaStackPointer]
        }
    }

    override fun translate(dx: Float, dy: Float) {
        canvas?.translate(dx, dy)
    }

    override fun rotate(degrees: Float) {
        canvas?.rotate(degrees)
    }

    override fun rotate(degrees: Float, px: Float, py: Float) {
        canvas?.rotate(degrees, px, py)
    }

    override fun scale(sx: Float, sy: Float) {
        canvas?.scale(sx, sy)
    }

    override fun scale(sx: Float, sy: Float, px: Float, py: Float) {
        canvas?.scale(sx, sy, px, py)
    }

    override fun setGlobalAlpha(alpha: Float) {
        globalAlpha = MathUtils.clamp01(alpha)
    }

    override fun getGlobalAlpha(): Float = globalAlpha

    override fun drawSprite(
        sprite: Sprite,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        rotation: Float,
        scaleX: Float,
        scaleY: Float,
        alpha: Float
    ) {
        val c = canvas ?: return

        srcRect.set(
            sprite.srcRect.x.toInt(),
            sprite.srcRect.y.toInt(),
            sprite.srcRect.right.toInt(),
            sprite.srcRect.bottom.toInt()
        )

        val pivotX = width * sprite.pivot.x
        val pivotY = height * sprite.pivot.y

        val finalAlpha = MathUtils.clamp01(alpha * globalAlpha * sprite.alpha)
        bitmapPaint.alpha = (finalAlpha * 255f).toInt()

        if (rotation == 0f && scaleX == 1f && scaleY == 1f && sprite.flip == FlipMode.NONE) {
            dstRectF.set(x - pivotX, y - pivotY, x - pivotX + width, y - pivotY + height)
            c.drawBitmap(sprite.bitmap, srcRect, dstRectF, bitmapPaint)
        } else {
            c.save()
            c.translate(x, y)
            if (rotation != 0f) c.rotate(rotation)

            var sx = scaleX
            var sy = scaleY
            when (sprite.flip) {
                FlipMode.FLIP_X -> sx = -sx
                FlipMode.FLIP_Y -> sy = -sy
                FlipMode.FLIP_XY -> {
                    sx = -sx
                    sy = -sy
                }
                FlipMode.NONE -> {}
            }
            if (sx != 1f || sy != 1f) c.scale(sx, sy)

            dstRectF.set(-pivotX, -pivotY, -pivotX + width, -pivotY + height)
            c.drawBitmap(sprite.bitmap, srcRect, dstRectF, bitmapPaint)
            c.restore()
        }
    }

    override fun drawBitmap(
        bitmap: Bitmap,
        srcRect: Rect2D?,
        dstRect: Rect2D,
        alpha: Float,
        rotation: Float
    ) {
        val c = canvas ?: return
        val finalAlpha = MathUtils.clamp01(alpha * globalAlpha)
        bitmapPaint.alpha = (finalAlpha * 255f).toInt()

        dstRectF.set(dstRect.x, dstRect.y, dstRect.right, dstRect.bottom)

        if (srcRect != null) {
            this.srcRect.set(srcRect.x.toInt(), srcRect.y.toInt(), srcRect.right.toInt(), srcRect.bottom.toInt())
        } else {
            this.srcRect.set(0, 0, bitmap.width, bitmap.height)
        }

        if (rotation != 0f) {
            c.save()
            c.rotate(rotation, dstRectF.centerX(), dstRectF.centerY())
            c.drawBitmap(bitmap, this.srcRect, dstRectF, bitmapPaint)
            c.restore()
        } else {
            c.drawBitmap(bitmap, this.srcRect, dstRectF, bitmapPaint)
        }
    }

    override fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
        style: Paint.Style,
        strokeWidth: Float,
        cornerRadius: Float
    ) {
        val c = canvas ?: return
        val paint = if (style == Paint.Style.FILL) fillPaint else strokePaint
        paint.color = color
        paint.strokeWidth = strokeWidth
        val alphaVal = (Color.alpha(color) / 255f) * globalAlpha
        paint.alpha = (alphaVal * 255f).toInt()

        dstRectF.set(x, y, x + width, y + height)
        if (cornerRadius > 0f) {
            c.drawRoundRect(dstRectF, cornerRadius, cornerRadius, paint)
        } else {
            c.drawRect(dstRectF, paint)
        }
    }

    override fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
        style: Paint.Style,
        strokeWidth: Float
    ) {
        val c = canvas ?: return
        val paint = if (style == Paint.Style.FILL) fillPaint else strokePaint
        paint.color = color
        paint.strokeWidth = strokeWidth
        val alphaVal = (Color.alpha(color) / 255f) * globalAlpha
        paint.alpha = (alphaVal * 255f).toInt()

        c.drawCircle(x, y, radius, paint)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, strokeWidth: Float) {
        val c = canvas ?: return
        strokePaint.color = color
        strokePaint.strokeWidth = strokeWidth
        val alphaVal = (Color.alpha(color) / 255f) * globalAlpha
        strokePaint.alpha = (alphaVal * 255f).toInt()

        c.drawLine(x1, y1, x2, y2, strokePaint)
    }

    override fun drawText(
        text: String,
        x: Float,
        y: Float,
        fontSize: Float,
        color: Int,
        align: Paint.Align,
        typeface: Typeface?,
        shadowColor: Int,
        shadowRadius: Float
    ) {
        val c = canvas ?: return
        textPaint.textSize = fontSize
        textPaint.color = color
        textPaint.textAlign = align
        textPaint.typeface = typeface ?: Typeface.DEFAULT_BOLD
        val alphaVal = (Color.alpha(color) / 255f) * globalAlpha
        textPaint.alpha = (alphaVal * 255f).toInt()

        if (shadowRadius > 0f && Color.alpha(shadowColor) > 0) {
            textPaint.setShadowLayer(shadowRadius, 2f, 2f, shadowColor)
        } else {
            textPaint.clearShadowLayer()
        }

        c.drawText(text, x, y, textPaint)
    }

    override fun measureTextWidth(text: String, fontSize: Float, typeface: Typeface?): Float {
        textPaint.textSize = fontSize
        textPaint.typeface = typeface ?: Typeface.DEFAULT_BOLD
        return textPaint.measureText(text)
    }
}
