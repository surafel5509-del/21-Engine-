package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.BehaviorType
import com.example.studio.model.ColliderShapeType
import com.example.studio.model.GameNode

@Composable
fun InspectorDock(
    selectedNode: GameNode?,
    onNodeUpdated: (GameNode) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedNode == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select a Node in Scene to inspect", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        // Header with Node Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Inspector: ${selectedNode.name}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. TRANSFORM SECTION
        InspectorSection(title = "Transform 2D", icon = Icons.Default.OpenWith) {
            NumberField(label = "Position X", value = selectedNode.posX) {
                selectedNode.posX = it
                onNodeUpdated(selectedNode)
            }
            NumberField(label = "Position Y", value = selectedNode.posY) {
                selectedNode.posY = it
                onNodeUpdated(selectedNode)
            }
            NumberField(label = "Scale X", value = selectedNode.scaleX) {
                selectedNode.scaleX = it
                onNodeUpdated(selectedNode)
            }
            NumberField(label = "Scale Y", value = selectedNode.scaleY) {
                selectedNode.scaleY = it
                onNodeUpdated(selectedNode)
            }
            NumberField(label = "Rotation (°)", value = selectedNode.rotation) {
                selectedNode.rotation = it
                onNodeUpdated(selectedNode)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. SPRITE & VISUAL SECTION
        InspectorSection(title = "Sprite & Visual", icon = Icons.Default.Palette) {
            // Sprite Shape Selection
            val shapes = listOf("box", "circle", "hero", "spaceship", "alien", "coin", "platform", "spike", "gem", "heart")
            Text("Sprite Shape:", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                shapes.take(5).forEach { s ->
                    val isSelected = selectedNode.spriteShape.equals(s, ignoreCase = true)
                    AssistChip(
                        onClick = {
                            selectedNode.spriteShape = s
                            onNodeUpdated(selectedNode)
                        },
                        label = { Text(s.capitalize(), fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                            labelColor = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                shapes.drop(5).forEach { s ->
                    val isSelected = selectedNode.spriteShape.equals(s, ignoreCase = true)
                    AssistChip(
                        onClick = {
                            selectedNode.spriteShape = s
                            onNodeUpdated(selectedNode)
                        },
                        label = { Text(s.capitalize(), fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                            labelColor = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Color Palette
            Text("Tint Color:", color = Color(0xFF94A3B8), fontSize = 12.sp)
            val colors = listOf(
                0xFF38BDF8L, 0xFF22C55EL, 0xFFEF4444L, 0xFFFFD700L,
                0xFFA855F7L, 0xFFFF6B6BL, 0xFFFFFFFFL, 0xFF64748BL
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { c ->
                    val isChosen = selectedNode.color == c
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .clickable {
                                selectedNode.color = c
                                onNodeUpdated(selectedNode)
                            }
                    ) {
                        if (isChosen) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            NumberField(label = "Width", value = selectedNode.width) {
                selectedNode.width = it
                onNodeUpdated(selectedNode)
            }
            NumberField(label = "Height", value = selectedNode.height) {
                selectedNode.height = it
                onNodeUpdated(selectedNode)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. PHYSICS & COLLIDER SECTION
        InspectorSection(title = "RigidBody2D & Collider", icon = Icons.Default.SportsBasketball) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Physics", color = Color.White, fontSize = 13.sp)
                Switch(
                    checked = selectedNode.hasPhysics,
                    onCheckedChange = {
                        selectedNode.hasPhysics = it
                        onNodeUpdated(selectedNode)
                    }
                )
            }

            if (selectedNode.hasPhysics) {
                Text("Body Type:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DYNAMIC", "STATIC", "KINEMATIC").forEach { bt ->
                        val isSel = selectedNode.bodyType == bt
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedNode.bodyType = bt
                                onNodeUpdated(selectedNode)
                            },
                            label = { Text(bt, fontSize = 11.sp) }
                        )
                    }
                }

                NumberField(label = "Bounciness (0..1)", value = selectedNode.bounciness) {
                    selectedNode.bounciness = it.coerceIn(0f, 1f)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Mass (kg)", value = selectedNode.mass) {
                    selectedNode.mass = it.coerceAtLeast(0.1f)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Friction", value = selectedNode.friction) {
                    selectedNode.friction = it.coerceIn(0f, 1f)
                    onNodeUpdated(selectedNode)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. SCRIPT & BEHAVIORS
        InspectorSection(title = "Script & Behavior", icon = Icons.Default.Code) {
            Text("Attached Behavior:", color = Color(0xFF94A3B8), fontSize = 12.sp)

            val behaviors = listOf(
                BehaviorType.NONE,
                BehaviorType.PLATFORMER_PLAYER,
                BehaviorType.TOPDOWN_PLAYER,
                BehaviorType.AUTO_PATROL,
                BehaviorType.ORBIT_ROTATE,
                BehaviorType.COLLECTABLE_COIN,
                BehaviorType.HAZARD_SPIKE,
                BehaviorType.FLAPPY_TAP
            )

            behaviors.forEach { b ->
                val isSel = selectedNode.behavior == b
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            selectedNode.behavior = b
                            onNodeUpdated(selectedNode)
                        },
                    colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF1E293B) else Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSel, onClick = {
                            selectedNode.behavior = b
                            onNodeUpdated(selectedNode)
                        })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(b.name, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            if (selectedNode.behavior == BehaviorType.PLATFORMER_PLAYER || selectedNode.behavior == BehaviorType.TOPDOWN_PLAYER) {
                NumberField(label = "Move Speed (px/s)", value = selectedNode.moveSpeed) {
                    selectedNode.moveSpeed = it
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Jump Force", value = selectedNode.jumpForce) {
                    selectedNode.jumpForce = it
                    onNodeUpdated(selectedNode)
                }
            } else if (selectedNode.behavior == BehaviorType.AUTO_PATROL) {
                NumberField(label = "Patrol Distance", value = selectedNode.patrolDistance) {
                    selectedNode.patrolDistance = it
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Patrol Speed", value = selectedNode.moveSpeed) {
                    selectedNode.moveSpeed = it
                    onNodeUpdated(selectedNode)
                }
            }
        }
    }
}

@Composable
private fun InspectorSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(String.format("%.1f", value)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                it.toFloatOrNull()?.let { num -> onValueChange(num) }
            },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
            modifier = Modifier
                .width(100.dp)
                .height(44.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            )
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
