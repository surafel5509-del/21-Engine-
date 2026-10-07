package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode

@Composable
fun SpriteAnimationDock(
    selectedNode: GameNode?,
    onAnimationUpdated: (GameNode) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedNode == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a node to configure sprite animations", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    var isAnimated by remember(selectedNode) { mutableStateOf(selectedNode.isAnimated) }
    var colsText by remember(selectedNode) { mutableStateOf(selectedNode.animationCols.toString()) }
    var rowsText by remember(selectedNode) { mutableStateOf(selectedNode.animationRows.toString()) }
    var fpsText by remember(selectedNode) { mutableStateOf(selectedNode.animationFps.toString()) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sprite Animation Editor - ${selectedNode.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Switch(
                checked = isAnimated,
                onCheckedChange = {
                    isAnimated = it
                    selectedNode.isAnimated = it
                    onAnimationUpdated(selectedNode)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isAnimated) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = colsText,
                    onValueChange = { colsText = it },
                    label = { Text("Grid Columns", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = rowsText,
                    onValueChange = { rowsText = it },
                    label = { Text("Grid Rows", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = fpsText,
                    onValueChange = { fpsText = it },
                    label = { Text("FPS", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        selectedNode.animationCols = colsText.toIntOrNull() ?: 1
                        selectedNode.animationRows = rowsText.toIntOrNull() ?: 1
                        selectedNode.animationFps = fpsText.toFloatOrNull() ?: 8f
                        onAnimationUpdated(selectedNode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Settings", fontSize = 11.sp)
                }

                Button(
                    onClick = { isPreviewPlaying = !isPreviewPlaying },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPreviewPlaying) Color(0xFFEF4444) else Color(0xFF10B981)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPreviewPlaying) "Stop" else "Preview", fontSize = 11.sp)
                }
            }
        }
    }
}
