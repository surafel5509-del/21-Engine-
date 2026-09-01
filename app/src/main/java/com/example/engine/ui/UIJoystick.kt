package com.example.engine.ui

import android.graphics.Paint
import com.example.engine.input.TouchEvent
import com.example.engine.input.TouchType
import com.example.engine.math.MathUtils
import com.example.engine.math.Vector2
import com.example.engine.rendering.Renderer
import kotlin.math.hypot

/**
 * Responsive Virtual Touch Joystick for on-screen movement and top-down/platformer control.
 */
class UIJoystick(
    var baseRadius: Float = 60f,
    var stickRadius: Float = 30f
) : UIElement(width = baseRadius * 2f, height = baseRadius * 2f) {

    var baseColor: Int = 0x44FFFFFF
    var stickColor: Int = 0xAA0284C7.toInt()
    var strokeColor: Int = 0x8838BDF8.toInt()

    val value = Vector2(0f, 0f) // Normalized -1..1
    private val stickOffset = Vector2(0f, 0f)
    private var trackingPointerId: Int? = null

    override fun onTouchEvent(event: TouchEvent): Boolean {
        val centerX = computedPosition.x + baseRadius
        val centerY = computedPosition.y + baseRadius

        when (event.type) {
            TouchType.DOWN -> {
                val dist = hypot(event.logicalPosition.x - centerX, event.logicalPosition.y - centerY)
                if (dist <= baseRadius * 1.5f && trackingPointerId == null) {
                    trackingPointerId = event.pointerId
                    updateStick(event.logicalPosition.x - centerX, event.logicalPosition.y - centerY)
                    event.consume()
                    return true
                }
            }
            TouchType.MOVE -> {
                if (event.pointerId == trackingPointerId) {
                    updateStick(event.logicalPosition.x - centerX, event.logicalPosition.y - centerY)
                    event.consume()
                    return true
                }
            }
            TouchType.UP, TouchType.CANCEL -> {
                if (event.pointerId == trackingPointerId) {
                    trackingPointerId = null
                    stickOffset.set(0f, 0f)
                    value.set(0f, 0f)
                    event.consume()
                    return true
                }
            }
        }
        return false
    }

    private fun updateStick(dx: Float, dy: Float) {
        val dist = hypot(dx, dy)
        if (dist > baseRadius) {
            val scale = baseRadius / dist
            stickOffset.set(dx * scale, dy * scale)
            value.set(stickOffset.x / baseRadius, stickOffset.y / baseRadius)
        } else {
            stickOffset.set(dx, dy)
            value.set(if (baseRadius > 0f) dx / baseRadius else 0f, if (baseRadius > 0f) dy / baseRadius else 0f)
        }
    }

    override fun onRender(renderer: Renderer) {
        val centerX = computedPosition.x + baseRadius
        val centerY = computedPosition.y + baseRadius

        // Base Outer Ring
        renderer.drawCircle(centerX, centerY, baseRadius, baseColor, Paint.Style.FILL)
        renderer.drawCircle(centerX, centerY, baseRadius, strokeColor, Paint.Style.STROKE, 2f)

        // Inner Stick Knob
        renderer.drawCircle(centerX + stickOffset.x, centerY + stickOffset.y, stickRadius, stickColor, Paint.Style.FILL)
        renderer.drawCircle(centerX + stickOffset.x, centerY + stickOffset.y, stickRadius, 0xEEFFFFFF.toInt(), Paint.Style.STROKE, 2f)
    }
}
