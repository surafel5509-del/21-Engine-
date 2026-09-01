package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType

@Composable
fun SceneTreeDock(
    project: GameProject,
    selectedNodeId: String?,
    onSelectNode: (GameNode) -> Unit,
    onAddNode: (NodeType) -> Unit,
    onDeleteNode: (String) -> Unit,
    onDuplicateNode: (GameNode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = "Scene Tree",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Scene Hierarchy (${project.nodes.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Node", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Node", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Node List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(project.nodes) { node ->
                val isSelected = node.id == selectedNodeId
                val itemBg = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31)
                val borderCol = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNode(node) },
                    colors = CardDefaults.cardColors(containerColor = itemBg),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Node Icon
                        val nodeIcon = when (node.nodeType) {
                            NodeType.RIGID_BODY_2D -> Icons.Default.SportsBasketball
                            NodeType.STATIC_BODY_2D -> Icons.Default.Layers
                            NodeType.AREA_2D -> Icons.Default.Adjust
                            NodeType.CPU_PARTICLES_2D -> Icons.Default.AutoAwesome
                            NodeType.CAMERA_2D -> Icons.Default.Videocam
                            else -> Icons.Default.Widgets
                        }
                        Icon(
                            nodeIcon,
                            contentDescription = node.nodeType.name,
                            tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                node.name,
                                color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Text(
                                "${node.nodeType.name} • (${node.posX.toInt()}, ${node.posY.toInt()})",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }

                        // Visibility Toggle
                        IconButton(
                            onClick = { node.isVisible = !node.isVisible },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (node.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = if (node.isVisible) Color(0xFF22C55E) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Duplicate
                        IconButton(
                            onClick = { onDuplicateNode(node) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = { onDeleteNode(node.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Node Modal Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add Godot-style Node", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NodeTypeOption(
                        title = "Sprite2D (Visual Entity)",
                        desc = "Graphic shape, character or object visual",
                        icon = Icons.Default.Image
                    ) {
                        onAddNode(NodeType.SPRITE_2D)
                        showAddDialog = false
                    }

                    NodeTypeOption(
                        title = "RigidBody2D (Dynamic Physics)",
                        desc = "Full gravity, collisions, bounce and impulse",
                        icon = Icons.Default.SportsBasketball
                    ) {
                        onAddNode(NodeType.RIGID_BODY_2D)
                        showAddDialog = false
                    }

                    NodeTypeOption(
                        title = "StaticBody2D (Platform / Wall)",
                        desc = "Immovable collision barrier and ground",
                        icon = Icons.Default.Layers
                    ) {
                        onAddNode(NodeType.STATIC_BODY_2D)
                        showAddDialog = false
                    }

                    NodeTypeOption(
                        title = "Area2D (Trigger / Collectable)",
                        desc = "Coin, score trigger, hazard spikes or portal",
                        icon = Icons.Default.Adjust
                    ) {
                        onAddNode(NodeType.AREA_2D)
                        showAddDialog = false
                    }

                    NodeTypeOption(
                        title = "CPUParticles2D (FX Emitter)",
                        desc = "Sparkles, fire bursts, magic trails and smoke",
                        icon = Icons.Default.AutoAwesome
                    ) {
                        onAddNode(NodeType.CPU_PARTICLES_2D)
                        showAddDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun NodeTypeOption(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }
    }
}
