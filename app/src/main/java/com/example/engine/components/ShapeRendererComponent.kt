package com.example.engine.components

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.rendering.Renderer
import com.example.engine.scene.Component

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE
}

/**
 * Renders geometric shapes (Rectangles, Rounded Rects, Circles) with fill/stroke styles.
 */
class ShapeRendererComponent(
    var shapeType: ShapeType = ShapeType.RECTANGLE,
    var width: Float = 50f,
    var height: Float = 50f,
    var radius: Float = 25f,
    var color: Int = Color.CYAN,
    var style: Paint.Style = Paint.Style.FILL,
    var strokeWidth: Float = 2f,
    var cornerRadius: Float = 8f
) : Component() {

    override fun onRender(renderer: Renderer) {
        val tr = transform ?: return
        val pos = tr.worldPosition

        when (shapeType) {
            ShapeType.RECTANGLE -> {
                val pivotX = width * tr.pivot.x
                val pivotY = height * tr.pivot.y
                renderer.drawRect(
                    x = pos.x - pivotX,
                    y = pos.y - pivotY,
                    width = width * tr.worldScale.x,
                    height = height * tr.worldScale.y,
                    color = color,
                    style = style,
                    strokeWidth = strokeWidth
                )
            }
            ShapeType.ROUNDED_RECTANGLE -> {
                val pivotX = width * tr.pivot.x
                val pivotY = height * tr.pivot.y
                renderer.drawRect(
                    x = pos.x - pivotX,
                    y = pos.y - pivotY,
                    width = width * tr.worldScale.x,
                    height = height * tr.worldScale.y,
                    color = color,
                    style = style,
                    strokeWidth = strokeWidth,
                    cornerRadius = cornerRadius
                )
            }
            ShapeType.CIRCLE -> {
                renderer.drawCircle(
                    x = pos.x,
                    y = pos.y,
                    radius = radius * tr.worldScale.x,
                    color = color,
                    style = style,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}
