package com.example.engine.ui

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.rendering.Renderer

/**
 * Container UI panel with background styling, borders, and rounded corners.
 */
class UIPanel(
    width: Float = 300f,
    height: Float = 200f,
    var backgroundColor: Int = 0xDD1E293B.toInt(),
    var borderColor: Int = 0x66475569,
    var borderWidth: Float = 2f,
    var cornerRadius: Float = 12f
) : UIElement(width, height) {

    override fun onRender(renderer: Renderer) {
        // Draw background
        if (Color.alpha(backgroundColor) > 0) {
            renderer.drawRect(
                x = computedPosition.x,
                y = computedPosition.y,
                width = width,
                height = height,
                color = backgroundColor,
                style = Paint.Style.FILL,
                cornerRadius = cornerRadius
            )
        }

        // Draw border
        if (borderWidth > 0f && Color.alpha(borderColor) > 0) {
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
