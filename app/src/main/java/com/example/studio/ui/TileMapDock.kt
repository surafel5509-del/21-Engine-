package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType

data class TilePreset(
    val id: String,
    val name: String,
    val color: Long,
    val hasCollision: Boolean = true,
    val isHazard: Boolean = false
)

val DEFAULT_TILES = listOf(
    TilePreset("grass", "Grass", 0xFF10B981),
    TilePreset("dirt", "Dirt", 0xFFB45309),
    TilePreset("stone", "Stone", 0xFF64748B),
    TilePreset("brick", "Brick", 0xFFEF4444),
    TilePreset("water", "Water", 0xFF0284C7, hasCollision = false),
    TilePreset("lava", "Lava", 0xFFDC2626, isHazard = true),
    TilePreset("wood", "Plank", 0xFFD97706),
    TilePreset("spike", "Spike", 0xFF991B1B, isHazard = true)
)

@Composable
fun TileMapDock(
    project: GameProject,
    onProjectUpdated: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTile by remember { mutableStateOf(DEFAULT_TILES.first()) }
    var isEraserMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TileMap Grid Painter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            FilterChip(
                selected = isEraserMode,
                onClick = { isEraserMode = !isEraserMode },
                label = { Text(if (isEraserMode) "Eraser Active" else "Paint Mode", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDC2626),
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Select Tile Palette:", color = Color(0xFF94A3B8), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))

        // Tile Palette Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(DEFAULT_TILES) { tile ->
                val isSel = selectedTile.id == tile.id && !isEraserMode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) Color(0xFFFFD600) else Color(0xFF334155),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            selectedTile = tile
                            isEraserMode = false
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color(tile.color), shape = RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(tile.name, fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Tile Placement Buttons into Project
        Button(
            onClick = {
                val tileNode = GameNode(
                    name = "Tile_${selectedTile.name}_${project.nodes.size + 1}",
                    nodeType = if (selectedTile.hasCollision) NodeType.STATIC_BODY_2D else NodeType.SPRITE_2D,
                    posX = 640f + (project.nodes.size % 8) * 32f,
                    posY = 500f,
                    width = 32f,
                    height = 32f,
                    color = selectedTile.color,
                    hasPhysics = selectedTile.hasCollision,
                    bodyType = "STATIC"
                )
                project.addNode(tileNode)
                onProjectUpdated(project)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Place '${selectedTile.name}' Tile Block on Scene Grid", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
