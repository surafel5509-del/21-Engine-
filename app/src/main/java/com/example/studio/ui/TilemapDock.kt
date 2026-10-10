package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.components.TilemapComponent
import com.example.studio.model.GameNode
import com.example.studio.model.NodeType

data class TilePaletteItem(
    val id: Int,
    val name: String,
    val color: Color
)

@Composable
fun TilemapDock(
    selectedNode: GameNode?,
    onNodeUpdated: (GameNode) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedNode == null || selectedNode.nodeType != NodeType.TILEMAP_2D) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Select or create a Tilemap2D Node to edit the Tilemap.",
                color = Color(0xFF64748B),
                fontSize = 13.sp
            )
        }
        return
    }

    val palette = remember {
        listOf(
            TilePaletteItem(0, "Eraser", Color(0xFF334155)),
            TilePaletteItem(1, "Solid Ground", Color(0xFF1E293B)),
            TilePaletteItem(2, "Brick", Color(0xFF78350F)),
            TilePaletteItem(3, "Grass Top", Color(0xFF15803D)),
            TilePaletteItem(4, "Water", Color(0xFF0284C7)),
            TilePaletteItem(5, "Coin Block", Color(0xFFEAB308))
        )
    }

    var selectedTileId by remember { mutableStateOf(1) }

    val tilemapComp = remember(selectedNode.id) {
        TilemapComponent(selectedNode.tilemapCols, selectedNode.tilemapRows, selectedNode.tilemapTileSize)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        // TOP PALETTE SELECTION
        Text("Tile Palette", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            palette.forEach { item ->
                val isChosen = selectedTileId == item.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(item.color, shape = RoundedCornerShape(6.dp))
                        .border(
                            width = if (isChosen) 2.dp else 1.dp,
                            color = if (isChosen) Color(0xFF38BDF8) else Color(0xFF475569),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedTileId = item.id },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.name,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // GRID CANVAS PAINTER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grid Painter (${selectedNode.tilemapCols} x ${selectedNode.tilemapRows})", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Button(
                onClick = {
                    tilemapComp.clear()
                    selectedNode.tilemapData = ""
                    onNodeUpdated(selectedNode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Clear Map", fontSize = 10.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(selectedNode.tilemapCols),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF030712), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            items(selectedNode.tilemapCols * selectedNode.tilemapRows) { idx ->
                val col = idx % selectedNode.tilemapCols
                val row = idx / selectedNode.tilemapCols
                val tileId = tilemapComp.getTile(col, row)
                val tileColor = palette.find { it.id == tileId }?.color ?: Color.Transparent

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .background(tileColor, shape = RoundedCornerShape(2.dp))
                        .border(0.5.dp, Color(0x3364748B))
                        .clickable {
                            tilemapComp.setTile(col, row, selectedTileId)
                            selectedNode.tilemapData = tilemapComp.tileData.joinToString(",")
                            onNodeUpdated(selectedNode)
                        }
                )
            }
        }
    }
}
