package com.example.engine.ui

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.math.MathUtils
import com.example.engine.rendering.Renderer

/**
 * Responsive Health / Progress bar.
 */
class UIProgressBar(
    width: Float = 200f,
    height: Float = 20f,
    var minValue: Float = 0f,
    var maxValue: Float = 100f,
    initialValue: Float = 100f
) : UIElement(width, height) {

    var value: Float = initialValue
        set(v) {
            field = MathUtils.clamp(v, minValue, maxValue)
        }

    var fillColor: Int = 0xFF4CAF50.toInt()
    var backgroundColor: Int = 0xAA212121.toInt()
    var borderColor: Int = 0xFFFFFFFF.toInt()
    var borderWidth: Float = 1.5f
    var cornerRadius: Float = 4f

    override fun onRender(renderer: Renderer) {
        val progress = if (maxValue > minValue) (value - minValue) / (maxValue - minValue) else 0f
        val fillWidth = width * MathUtils.clamp01(progress)

        // Background
        renderer.drawRect(
            x = computedPosition.x,
            y = computedPosition.y,
            width = width,
            height = height,
            color = backgroundColor,
            style = Paint.Style.FILL,
            cornerRadius = cornerRadius
        )

        // Fill
        if (fillWidth > 0f) {
            renderer.drawRect(
                x = computedPosition.x,
                y = computedPosition.y,
                width = fillWidth,
                height = height,
                color = fillColor,
                style = Paint.Style.FILL,
                cornerRadius = cornerRadius
            )
        }

        // Border
        if (borderWidth > 0f) {
            renderer.drawRect(
                x = computedPosition.x,
                y = computedPosition.y,
                width = width,
                height = height,
                color = borderColor,
                style = Paint.Style.STROKE,
                strokeWidth = borderWidth,
                cornerRadius = cornerRadius
            )
        }
    }
}
