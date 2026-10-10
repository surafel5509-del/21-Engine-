package com.example.engine.components

import com.example.engine.math.Rect2D
import com.example.engine.scene.Component

class TilemapComponent(
    var cols: Int = 20,
    var rows: Int = 12,
    var tileSize: Float = 32f
) : Component() {

    var tileData: IntArray = IntArray(cols * rows) { 0 }
    var tileColors: Map<Int, Long> = mapOf(
        1 to 0xFF1E293B, // Solid Ground (Slate)
        2 to 0xFF78350F, // Brick (Brown)
        3 to 0xFF15803D, // Grass Top (Green)
        4 to 0xFF0284C7, // Water (Blue)
        5 to 0xFFEAB308  // Coin Block (Yellow)
    )

    fun getTile(col: Int, row: Int): Int {
        if (col !in 0 until cols || row !in 0 until rows) return 0
        return tileData[row * cols + col]
    }

    fun setTile(col: Int, row: Int, tileId: Int) {
        if (col !in 0 until cols || row !in 0 until rows) return
        tileData[row * cols + col] = tileId
    }

    fun clear() {
        tileData.fill(0)
    }

    fun getSolidTileRects(originX: Float, originY: Float): List<Rect2D> {
        val rects = mutableListOf<Rect2D>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val tileId = getTile(c, r)
                if (tileId in 1..3) { // Solid tile IDs
                    val x = originX + c * tileSize
                    val y = originY + r * tileSize
                    rects.add(Rect2D(x, y, tileSize, tileSize))
                }
            }
        }
        return rects
    }
}
