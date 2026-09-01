package com.example.engine.input

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.math.MathUtils
import com.example.engine.math.Vector2
import com.example.engine.rendering.Renderer
import com.example.engine.ui.Anchor
import com.example.engine.ui.UIElement
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Responsive on-screen virtual analog joystick for 2D platformers, top-down shooters, and vehicle games.
 */
class VirtualJoystick(
    var radius: Float = 70f,
    var knobRadius: Float = 30f,
    var deadZone: Float = 0.1f
) : UIElement(width = radius * 2f, height = radius * 2f) {

    val direction = Vector2(0f, 0f) // Normalized direction (-1.0 to 1.0)
    var magnitude: Float = 0f // 0.0 to 1.0
        private set

    val knobPosition = Vector2()
    private var trackingPointerId: Int? = null

    var baseColor: Int = 0x55333333
    var baseStrokeColor: Int = 0xAAFFFFFF.toInt()
    var knobColor: Int = 0xCC2196F3.toInt()

    init {
        layout.anchor = Anchor.BOTTOM_LEFT
        layout.offsetX = 40f
        layout.offsetY = -40f
    }

    override fun update(dt: Float) {
        super.update(dt)
        if (trackingPointerId == null) {
            // Smoothly return knob to center
            knobPosition.lerp(Vector2.ZERO, dt * 20f)
            direction.set(0f, 0f)
            magnitude = 0f
        }
    }

    override fun onTouchEvent(event: TouchEvent): Boolean {
        val centerX = computedPosition.x + width * 0.5f
        val centerY = computedPosition.y + height * 0.5f

        when (event.type) {
            TouchType.DOWN -> {
                val dx = event.logicalPosition.x - centerX
                val dy = event.logicalPosition.y - centerY
                val dist = sqrt(dx * dx + dy * dy)

                if (dist <= radius * 1.5f && trackingPointerId == null) {
                    trackingPointerId = event.pointerId
                    updateKnob(dx, dy)
                    event.consume()
                    return true
                }
            }
            TouchType.MOVE -> {
                if (event.pointerId == trackingPointerId) {
                    val dx = event.logicalPosition.x - centerX
                    val dy = event.logicalPosition.y - centerY
                    updateKnob(dx, dy)
                    event.consume()
                    return true
                }
            }
            TouchType.UP, TouchType.CANCEL -> {
                if (event.pointerId == trackingPointerId) {
                    trackingPointerId = null
                    direction.set(0f, 0f)
                    magnitude = 0f
                    knobPosition.set(0f, 0f)
                    event.consume()
                    return true
                }
            }
        }
        return false
    }

    private fun updateKnob(dx: Float, dy: Float) {
        val dist = sqrt(dx * dx + dy * dy)
        val angle = atan2(dy, dx)

        val clampedDist = MathUtils.clamp(dist, 0f, radius)
        knobPosition.set(cos(angle) * clampedDist, sin(angle) * clampedDist)

        val rawMag = clampedDist / radius
        if (rawMag > deadZone) {
            magnitude = MathUtils.mapRange(rawMag, deadZone, 1f, 0f, 1f)
            direction.set(cos(angle) * magnitude, sin(angle) * magnitude)
        } else {
            magnitude = 0f
            direction.set(0f, 0f)
        }
    }

    override fun onRender(renderer: Renderer) {
        val centerX = computedPosition.x + width * 0.5f
        val centerY = computedPosition.y + height * 0.5f

        // Base Circle
        renderer.drawCircle(centerX, centerY, radius, baseColor, Paint.Style.FILL)
        renderer.drawCircle(centerX, centerY, radius, baseStrokeColor, Paint.Style.STROKE, 3f)

        // Knob
        renderer.drawCircle(
            centerX + knobPosition.x,
            centerY + knobPosition.y,
            knobRadius,
            knobColor,
            Paint.Style.FILL
        )
        renderer.drawCircle(
            centerX + knobPosition.x,
            centerY + knobPosition.y,
            knobRadius,
            0xFFFFFFFF.toInt(),
            Paint.Style.STROKE,
            2f
        )
    }
}
