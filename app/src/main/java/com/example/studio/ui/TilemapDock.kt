package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode
import com.example.studio.model.NodeType

@Composable
fun TilemapDock(
    selectedNode: GameNode?,
    onTilemapUpdated: (GameNode) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedNode == null || selectedNode.nodeType != NodeType.TILEMAP) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select a TileMap Node in hierarchy to edit tiles", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }

    var tileSize by remember(selectedNode) { mutableStateOf("32") }
    var selectedTileIndex by remember { mutableStateOf(0) }

    val tilePalette = listOf(
        Color(0xFF10B981) to "Grass",
        Color(0xFFD97706) to "Dirt",
        Color(0xFF64748B) to "Stone",
        Color(0xFF0284C7) to "Water",
        Color(0xFFEF4444) to "Lava",
        Color(0xFF8B5CF6) to "Magic Block"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        Text("TileMap Painter - ${selectedNode.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = tileSize,
                onValueChange = { tileSize = it },
                label = { Text("Tile Size (px)", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val size = tileSize.toFloatOrNull() ?: 32f
                    selectedNode.width = size
                    selectedNode.height = size
                    onTilemapUpdated(selectedNode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Apply Size", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Tile Palette", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(tilePalette.size) { index ->
                val (color, name) = tilePalette[index]
                val isSelected = selectedTileIndex == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clickable { selectedTileIndex = index }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, shape = RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(name, fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
