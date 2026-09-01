package com.example.engine.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.rendering.Renderer

/**
 * Responsive UI Text Label with alignment, shadows, and autowrap metrics.
 */
class UILabel(
    var text: String = "",
    var fontSize: Float = 22f,
    var color: Int = Color.WHITE,
    var align: Paint.Align = Paint.Align.LEFT,
    var typeface: Typeface? = Typeface.DEFAULT_BOLD,
    var shadowColor: Int = 0xAA000000.toInt(),
    var shadowRadius: Float = 3f
) : UIElement(width = 200f, height = 30f) {

    override fun onRender(renderer: Renderer) {
        if (text.isEmpty()) return

        val drawX = when (align) {
            Paint.Align.LEFT -> computedPosition.x
            Paint.Align.CENTER -> computedPosition.x + width * 0.5f
            Paint.Align.RIGHT -> computedPosition.x + width
        }

        val drawY = computedPosition.y + height * 0.5f + (fontSize * 0.35f)

        renderer.drawText(
            text = text,
            x = drawX,
            y = drawY,
            fontSize = fontSize,
            color = color,
            align = align,
            typeface = typeface,
            shadowColor = shadowColor,
            shadowRadius = shadowRadius
        )
    }
}
