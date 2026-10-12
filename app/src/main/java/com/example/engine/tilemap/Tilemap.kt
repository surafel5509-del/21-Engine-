package com.example.engine.tilemap

import android.graphics.Color

/**
 * Tile definition inside a Tilemap grid.
 */
data class Tile(
    val id: Int,
    val name: String,
    val color: Int = Color.GRAY,
    val isSolid: Boolean = true
)

/**
 * Grid layout representation for a 2D tilemap scene object.
 */
class Tilemap(
    val columns: Int = 20,
    val rows: Int = 12,
    val tileSize: Float = 32f
) {
    private val grid = Array(rows) { IntArray(columns) { 0 } }

    fun setTile(col: Int, row: Int, tileId: Int) {
        if (col in 0 until columns && row in 0 until rows) {
            grid[row][col] = tileId
        }
    }

    fun getTile(col: Int, row: Int): Int {
        if (col in 0 until columns && row in 0 until rows) {
            return grid[row][col]
        }
        return 0
    }

    fun clear() {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                grid[r][c] = 0
            }
        }
    }
}
