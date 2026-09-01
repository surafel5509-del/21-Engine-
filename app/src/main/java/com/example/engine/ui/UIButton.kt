package com.example.engine.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.input.TouchEvent
import com.example.engine.input.TouchType
import com.example.engine.rendering.Renderer

/**
 * Responsive UI Button with click animation, normal/pressed/disabled colors, and callbacks.
 */
class UIButton(
    var text: String = "Button",
    width: Float = 140f,
    height: Float = 48f,
    var onClick: (() -> Unit)? = null
) : UIElement(width, height) {

    var normalBackgroundColor: Int = 0xFF1E88E5.toInt()
    var pressedBackgroundColor: Int = 0xFF1565C0.toInt()
    var disabledBackgroundColor: Int = 0xFF757575.toInt()

    var textColor: Int = Color.WHITE
    var fontSize: Float = 20f
    var cornerRadius: Float = 8f
    var strokeColor: Int = 0x66FFFFFF
    var strokeWidth: Float = 1.5f

    var isPressed: Boolean = false
        private set
    private var trackingPointerId: Int? = null

    override fun onTouchEvent(event: TouchEvent): Boolean {
        when (event.type) {
            TouchType.DOWN -> {
                if (containsPoint(event.logicalPosition.x, event.logicalPosition.y) && trackingPointerId == null) {
                    trackingPointerId = event.pointerId
                    isPressed = true
                    event.consume()
                    return true
                }
            }
            TouchType.MOVE -> {
                if (event.pointerId == trackingPointerId) {
                    isPressed = containsPoint(event.logicalPosition.x, event.logicalPosition.y)
                    event.consume()
                    return true
                }
            }
            TouchType.UP, TouchType.CANCEL -> {
                if (event.pointerId == trackingPointerId) {
                    val wasPressed = isPressed
                    trackingPointerId = null
                    isPressed = false
                    if (wasPressed && event.type == TouchType.UP) {
                        onClick?.invoke()
                    }
                    event.consume()
                    return true
                }
            }
        }
        return false
    }

    override fun onRender(renderer: Renderer) {
        val bgColor = when {
            !isEnabled -> disabledBackgroundColor
            isPressed -> pressedBackgroundColor
            else -> normalBackgroundColor
        }

        // Draw button background
        renderer.drawRect(
            x = computedPosition.x,
            y = computedPosition.y,
            width = width,
            height = height,
            color = bgColor,
            style = Paint.Style.FILL,
            cornerRadius = cornerRadius
        )

        // Draw border
        if (strokeWidth > 0f) {
            renderer.drawRect(
                x = computedPosition.x,
                y = computedPosition.y,
                width = width,
                height = height,
                color = strokeColor,
                style = Paint.Style.STROKE,
                strokeWidth = strokeWidth,
                cornerRadius = cornerRadius
            )
        }

        // Draw button text
        if (text.isNotEmpty()) {
            val textY = computedPosition.y + height * 0.5f + (fontSize * 0.35f)
            renderer.drawText(
                text = text,
                x = computedPosition.x + width * 0.5f,
                y = textY,
                fontSize = fontSize,
                color = if (isEnabled) textColor else 0xFFAAAAAA.toInt(),
                align = Paint.Align.CENTER,
                typeface = Typeface.DEFAULT_BOLD,
                shadowColor = 0x88000000.toInt(),
                shadowRadius = 2f
            )
        }
    }
}
