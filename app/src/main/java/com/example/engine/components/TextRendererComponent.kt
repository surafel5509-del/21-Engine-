package com.example.engine.components

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.rendering.Renderer
import com.example.engine.scene.Component

/**
 * Draws formatted text in the world coordinate space.
 */
class TextRendererComponent(
    var text: String = "",
    var fontSize: Float = 28f,
    var color: Int = Color.WHITE,
    var align: Paint.Align = Paint.Align.CENTER,
    var typeface: Typeface? = null,
    var shadowColor: Int = Color.BLACK,
    var shadowRadius: Float = 4f
) : Component() {

    override fun onRender(renderer: Renderer) {
        if (text.isEmpty()) return
        val tr = transform ?: return
        val pos = tr.worldPosition

        renderer.drawText(
            text = text,
            x = pos.x,
            y = pos.y,
            fontSize = fontSize * tr.worldScale.x,
            color = color,
            align = align,
            typeface = typeface,
            shadowColor = shadowColor,
            shadowRadius = shadowRadius
        )
    }
}
