package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.tilemap.Tilemap

enum class TileTool {
    DRAW,
    ERASE,
    FILL
}

@Composable
fun TilemapEditorDock(
    tilemap: Tilemap,
    onTilemapChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTileId by remember { mutableIntStateOf(1) }
    var activeTool by remember { mutableStateOf(TileTool.DRAW) }

    val paletteTiles = listOf(
        Triple(1, "Stone Wall", Color(0xFF334155)),
        Triple(2, "Grass Block", Color(0xFF16A34A)),
        Triple(3, "Water Tile", Color(0xFF0284C7)),
        Triple(4, "Dirt / Brick", Color(0xFFD97706)),
        Triple(5, "Lava Hazard", Color(0xFFDC2626))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Text("Tilemap Palette", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { activeTool = TileTool.DRAW },
                    modifier = Modifier.size(32.dp).background(if (activeTool == TileTool.DRAW) Color(0xFF0284C7) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.Brush, contentDescription = "Draw", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { activeTool = TileTool.ERASE },
                    modifier = Modifier.size(32.dp).background(if (activeTool == TileTool.ERASE) Color(0xFFDC2626) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Erase", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { activeTool = TileTool.FILL },
                    modifier = Modifier.size(32.dp).background(if (activeTool == TileTool.FILL) Color(0xFFD97706) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.FormatPaint, contentDescription = "Fill All", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Palette Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            paletteTiles.forEach { (id, label, color) ->
                val isSelected = activeTileId == id
                Row(
                    modifier = Modifier
                        .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF020617), shape = RoundedCornerShape(6.dp))
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), shape = RoundedCornerShape(6.dp))
                        .clickable { activeTileId = id }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(14.dp).background(color, shape = RoundedCornerShape(2.dp)))
                    Text(label, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Representation
        Text("Grid View (Tap cells to edit):", color = Color.Gray, fontSize = 11.sp)
        LazyVerticalGrid(
            columns = GridCells.Fixed(tilemap.columns),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFF020617), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            items(tilemap.rows * tilemap.columns) { index ->
                val r = index / tilemap.columns
                val c = index % tilemap.columns
                val tileId = tilemap.getTile(c, r)

                val cellColor = when (tileId) {
                    1 -> Color(0xFF334155)
                    2 -> Color(0xFF16A34A)
                    3 -> Color(0xFF0284C7)
                    4 -> Color(0xFFD97706)
                    5 -> Color(0xFFDC2626)
                    else -> Color(0xFF0F172A)
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1.0f)
                        .padding(1.dp)
                        .background(cellColor, shape = RoundedCornerShape(2.dp))
                        .border(0.5.dp, Color(0xFF1E293B))
                        .clickable {
                            when (activeTool) {
                                TileTool.DRAW -> tilemap.setTile(c, r, activeTileId)
                                TileTool.ERASE -> tilemap.setTile(c, r, 0)
                                TileTool.FILL -> {
                                    for (row in 0 until tilemap.rows) {
                                        for (col in 0 until tilemap.columns) {
                                            tilemap.setTile(col, row, activeTileId)
                                        }
                                    }
                                }
                            }
                            onTilemapChanged()
                        }
                )
            }
        }
    }
}
