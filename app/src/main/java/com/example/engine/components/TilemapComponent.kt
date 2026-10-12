package com.example.engine.components

import android.graphics.Canvas
import android.graphics.Paint
import com.example.engine.scene.Component
import com.example.engine.tilemap.Tilemap

/**
 * Component responsible for drawing and colliding with a 2D tilemap grid.
 */
class TilemapComponent(
    val tilemap: Tilemap = Tilemap()
) : Component() {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    fun renderTilemap(canvas: Canvas) {
        val obj = gameObject ?: return
        val startX = obj.transform.position.x
        val startY = obj.transform.position.y

        for (r in 0 until tilemap.rows) {
            for (c in 0 until tilemap.columns) {
                val tileId = tilemap.getTile(c, r)
                if (tileId > 0) {
                    paint.color = when (tileId) {
                        1 -> 0xFF334155.toInt() // Wall / Stone
                        2 -> 0xFF16A34A.toInt() // Grass
                        3 -> 0xFF0284C7.toInt() // Water
                        4 -> 0xFFD97706.toInt() // Dirt / Brick
                        else -> 0xFF64748B.toInt()
                    }
                    val left = startX + c * tilemap.tileSize
                    val top = startY + r * tilemap.tileSize
                    canvas.drawRect(left, top, left + tilemap.tileSize, top + tilemap.tileSize, paint)
                }
            }
        }
    }
}
