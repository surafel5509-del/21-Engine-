package com.example.engine.tilemap

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.engine.math.Rect2D

enum class TileType(val id: Int, val color: Int, val isSolid: Boolean) {
    EMPTY(0, Color.TRANSPARENT, false),
    DIRT(1, 0xFF8B4513.toInt(), true),
    GRASS(2, 0xFF228B22.toInt(), true),
    STONE(3, 0xFF708090.toInt(), true),
    BRICK(4, 0xFFB22222.toInt(), true),
    WATER(5, 0xFF1E90FF.toInt(), false),
    HAZARD_SPIKE(6, 0xFFDC143C.toInt(), false),
    COIN_BLOCK(7, 0xFFFFD700.toInt(), true);

    companion object {
        fun fromId(id: Int): TileType {
            return values().firstOrNull { it.id == id } ?: EMPTY
        }
    }
}

/**
 * Grid-based tilemap rendering layer and collision matrix.
 */
class TilemapLayer(
    val cols: Int = 32,
    val rows: Int = 18,
    val tileSize: Float = 40f
) {
    val tiles: Array<IntArray> = Array(rows) { IntArray(cols) { 0 } }
    private val tilePaint = Paint().apply { style = Paint.Style.FILL }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = 0x33FFFFFF.toInt()
        strokeWidth = 1f
    }

    fun setTile(col: Int, row: Int, tileTypeId: Int) {
        if (col in 0 until cols && row in 0 until rows) {
            tiles[row][col] = tileTypeId
        }
    }

    fun getTile(col: Int, row: Int): TileType {
        if (col in 0 until cols && row in 0 until rows) {
            return TileType.fromId(tiles[row][col])
        }
        return TileType.EMPTY
    }

    fun render(canvas: Canvas, offsetX: Float = 0f, offsetY: Float = 0f) {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val tileId = tiles[r][c]
                val x = offsetX + c * tileSize
                val y = offsetY + r * tileSize

                if (tileId != TileType.EMPTY.id) {
                    val tile = TileType.fromId(tileId)
                    tilePaint.color = tile.color
                    canvas.drawRect(x, y, x + tileSize, y + tileSize, tilePaint)
                }
            }
        }
    }

    fun checkCollision(box: Rect2D): Boolean {
        val minCol = (box.x / tileSize).toInt().coerceIn(0, cols - 1)
        val maxCol = ((box.x + box.width) / tileSize).toInt().coerceIn(0, cols - 1)
        val minRow = (box.y / tileSize).toInt().coerceIn(0, rows - 1)
        val maxRow = ((box.y + box.height) / tileSize).toInt().coerceIn(0, rows - 1)

        for (r in minRow..maxRow) {
            for (c in minCol..maxCol) {
                val tile = getTile(c, r)
                if (tile.isSolid) {
                    return true
                }
            }
        }
        return false
    }
}
