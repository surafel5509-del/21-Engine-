package com.example.engine.ui

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.input.TouchEvent
import com.example.engine.input.TouchType
import com.example.engine.math.MathUtils
import com.example.engine.rendering.Renderer

/**
 * Responsive UI Slider for real-time parameter tweaking (e.g. Gravity, Audio Volume, Speed).
 */
class UISlider(
    width: Float = 180f,
    height: Float = 36f,
    var minValue: Float = 0f,
    var maxValue: Float = 1f,
    initialValue: Float = 0.5f,
    var onValueChanged: ((Float) -> Unit)? = null
) : UIElement(width, height) {

    var value: Float = initialValue
        set(v) {
            val clamped = MathUtils.clamp(v, minValue, maxValue)
            if (field != clamped) {
                field = clamped
                onValueChanged?.invoke(clamped)
            }
        }

    var trackColor: Int = 0xFF455A64.toInt()
    var trackFillColor: Int = 0xFF00BCD4.toInt()
    var thumbColor: Int = 0xFFFFFFFF.toInt()
    var thumbRadius: Float = 12f

    private var isDragging = false
    private var trackingPointerId: Int? = null

    override fun onTouchEvent(event: TouchEvent): Boolean {
        when (event.type) {
            TouchType.DOWN -> {
                if (containsPoint(event.logicalPosition.x, event.logicalPosition.y) && trackingPointerId == null) {
                    trackingPointerId = event.pointerId
                    isDragging = true
                    updateValueFromTouch(event.logicalPosition.x)
                    event.consume()
                    return true
                }
            }
            TouchType.MOVE -> {
                if (event.pointerId == trackingPointerId) {
                    updateValueFromTouch(event.logicalPosition.x)
                    event.consume()
                    return true
                }
            }
            TouchType.UP, TouchType.CANCEL -> {
                if (event.pointerId == trackingPointerId) {
                    trackingPointerId = null
                    isDragging = false
                    event.consume()
                    return true
                }
            }
        }
        return false
    }

    private fun updateValueFromTouch(touchX: Float) {
        val relX = touchX - (computedPosition.x + thumbRadius)
        val trackWidth = width - thumbRadius * 2f
        val pct = MathUtils.clamp01(if (trackWidth > 0f) relX / trackWidth else 0f)
        value = MathUtils.lerp(minValue, maxValue, pct)
    }

    override fun onRender(renderer: Renderer) {
        val centerY = computedPosition.y + height * 0.5f
        val trackLeft = computedPosition.x + thumbRadius
        val trackRight = computedPosition.x + width - thumbRadius
        val trackWidth = trackRight - trackLeft

        val pct = if (maxValue > minValue) (value - minValue) / (maxValue - minValue) else 0f
        val thumbX = trackLeft + trackWidth * pct

        // Draw track background
        renderer.drawRect(
            x = trackLeft,
            y = centerY - 3f,
            width = trackWidth,
            height = 6f,
            color = trackColor,
            style = Paint.Style.FILL,
            cornerRadius = 3f
        )

        // Draw track active fill
        if (pct > 0f) {
            renderer.drawRect(
                x = trackLeft,
                y = centerY - 3f,
                width = trackWidth * pct,
                height = 6f,
                color = trackFillColor,
                style = Paint.Style.FILL,
                cornerRadius = 3f
            )
        }

        // Draw thumb knob
        renderer.drawCircle(thumbX, centerY, thumbRadius, thumbColor, Paint.Style.FILL)
        renderer.drawCircle(thumbX, centerY, thumbRadius, 0x66000000, Paint.Style.STROKE, 2f)
    }
}
