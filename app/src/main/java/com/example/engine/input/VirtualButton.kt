package com.example.engine.input

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.rendering.Renderer
import com.example.engine.ui.Anchor
import com.example.engine.ui.UIElement
import kotlin.math.sqrt

/**
 * On-screen virtual button for actions like Jump, Attack, Dash, Pause.
 */
class VirtualButton(
    var label: String = "A",
    var radius: Float = 36f,
    var normalColor: Int = 0xAAFF5722.toInt(),
    var pressedColor: Int = 0xFFFF7043.toInt(),
    var onClick: (() -> Unit)? = null
) : UIElement(width = radius * 2f, height = radius * 2f) {

    var isPressed: Boolean = false
        private set
    private var trackingPointerId: Int? = null

    init {
        layout.anchor = Anchor.BOTTOM_RIGHT
        layout.offsetX = -40f
        layout.offsetY = -40f
    }

    override fun onTouchEvent(event: TouchEvent): Boolean {
        val centerX = computedPosition.x + radius
        val centerY = computedPosition.y + radius

        when (event.type) {
            TouchType.DOWN -> {
                val dx = event.logicalPosition.x - centerX
                val dy = event.logicalPosition.y - centerY
                if (sqrt(dx * dx + dy * dy) <= radius * 1.2f && trackingPointerId == null) {
                    trackingPointerId = event.pointerId
                    isPressed = true
                    event.consume()
                    return true
                }
            }
            TouchType.MOVE -> {
                if (event.pointerId == trackingPointerId) {
                    val dx = event.logicalPosition.x - centerX
                    val dy = event.logicalPosition.y - centerY
                    isPressed = sqrt(dx * dx + dy * dy) <= radius * 1.5f
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
        val centerX = computedPosition.x + radius
        val centerY = computedPosition.y + radius
        val curRadius = if (isPressed) radius * 0.92f else radius

        val color = if (isPressed) pressedColor else normalColor
        renderer.drawCircle(centerX, centerY, curRadius, color, Paint.Style.FILL)
        renderer.drawCircle(centerX, centerY, curRadius, Color.WHITE, Paint.Style.STROKE, 2.5f)

        if (label.isNotEmpty()) {
            renderer.drawText(
                text = label,
                x = centerX,
                y = centerY + 8f,
                fontSize = 24f,
                color = Color.WHITE,
                align = Paint.Align.CENTER,
                shadowColor = Color.BLACK,
                shadowRadius = 4f
            )
        }
    }
}
