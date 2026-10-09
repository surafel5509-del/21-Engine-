package com.example.studio.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.BehaviorType
import com.example.studio.model.ColliderShapeType
import com.example.studio.model.GameNode
import com.example.studio.model.NodeType
import com.example.studio.storage.CustomAssetManager

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
        // Header with Node Name & Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Inspector: ${selectedNode.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Type: ${selectedNode.nodeType.name}", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
            NumberField(label = "Z-Index (Order)", value = selectedNode.zIndex.toFloat()) {
                selectedNode.zIndex = it.toInt()
                onNodeUpdated(selectedNode)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TILEMAP SPECIFIC SECTION
        if (selectedNode.nodeType == NodeType.TILE_MAP_2D) {
            InspectorSection(title = "Tilemap Grid & Palette", icon = Icons.Default.GridOn) {
                NumberField(label = "Tile Size (px)", value = selectedNode.tileSize.toFloat()) {
                    selectedNode.tileSize = it.toInt().coerceIn(16, 128)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Grid Columns", value = selectedNode.tileCols.toFloat()) {
                    selectedNode.tileCols = it.toInt().coerceIn(1, 100)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Grid Rows", value = selectedNode.tileRows.toFloat()) {
                    selectedNode.tileRows = it.toInt().coerceIn(1, 100)
                    onNodeUpdated(selectedNode)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Tile Brush Palette:", color = Color(0xFF94A3B8), fontSize = 12.sp)

                val tiles = listOf("grass", "brick", "stone", "water", "dirt", "metal")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tiles.forEach { tileName ->
                        Button(
                            onClick = {
                                // Fill central tile grid sample
                                selectedNode.tileDataMap["0,0"] = tileName
                                selectedNode.tileDataMap["1,0"] = tileName
                                selectedNode.tileDataMap["2,0"] = tileName
                                onNodeUpdated(selectedNode)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(tileName.capitalize(), fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. SPRITE & TEXTURE & ANIMATION SECTION
        InspectorSection(title = "Sprite, Texture & Animation", icon = Icons.Default.Palette) {
            // Attached Custom Texture
            val customBmp = CustomAssetManager.getBitmap(selectedNode.customTextureKey)
            if (customBmp != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = customBmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Custom Texture Attached", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(selectedNode.customTextureKey ?: "", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    IconButton(
                        onClick = {
                            selectedNode.customTextureKey = null
                            onNodeUpdated(selectedNode)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Detach", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Sprite Frame Animation Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sprite Animation Engine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = selectedNode.isAnimated,
                    onCheckedChange = {
                        selectedNode.isAnimated = it
                        onNodeUpdated(selectedNode)
                    }
                )
            }

            if (selectedNode.isAnimated) {
                NumberField(label = "Frame Columns (H)", value = selectedNode.animationCols.toFloat()) {
                    selectedNode.animationCols = it.toInt().coerceAtLeast(1)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Frame Rows (V)", value = selectedNode.animationRows.toFloat()) {
                    selectedNode.animationRows = it.toInt().coerceAtLeast(1)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Playback Speed (FPS)", value = selectedNode.animationFps) {
                    selectedNode.animationFps = it.coerceIn(1f, 60f)
                    onNodeUpdated(selectedNode)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Procedural Shape Selection
            val shapes = listOf("hero", "slime", "platform", "coin", "spike", "flag", "spaceship", "alien", "gem", "heart", "circle", "box")
            Text("Built-In Sprite Model:", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                shapes.take(6).forEach { s ->
                    val isSelected = selectedNode.spriteShape.equals(s, ignoreCase = true)
                    AssistChip(
                        onClick = {
                            selectedNode.spriteShape = s
                            onNodeUpdated(selectedNode)
                        },
                        label = { Text(s.capitalize(), fontSize = 10.sp) },
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
                shapes.drop(6).forEach { s ->
                    val isSelected = selectedNode.spriteShape.equals(s, ignoreCase = true)
                    AssistChip(
                        onClick = {
                            selectedNode.spriteShape = s
                            onNodeUpdated(selectedNode)
                        },
                        label = { Text(s.capitalize(), fontSize = 10.sp) },
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
            NumberField(label = "Alpha (0..1)", value = selectedNode.alpha) {
                selectedNode.alpha = it.coerceIn(0f, 1f)
                onNodeUpdated(selectedNode)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 2D PHYSICS & COLLIDERS
        InspectorSection(title = "2D Physics & Collider", icon = Icons.Default.SportsBasketball) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Physics Simulation", color = Color.White, fontSize = 12.sp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Trigger (Area/Sensor)", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = selectedNode.isTrigger,
                        onCheckedChange = {
                            selectedNode.isTrigger = it
                            onNodeUpdated(selectedNode)
                        }
                    )
                }

                NumberField(label = "Bounciness (0..1)", value = selectedNode.bounciness) {
                    selectedNode.bounciness = it.coerceIn(0f, 1f)
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Gravity Scale", value = selectedNode.gravityScale) {
                    selectedNode.gravityScale = it
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Mass (kg)", value = selectedNode.mass) {
                    selectedNode.mass = it.coerceAtLeast(0.1f)
                    onNodeUpdated(selectedNode)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. BEHAVIOR & AI CONTROLLER
        InspectorSection(title = "Behavior & Enemy AI", icon = Icons.Default.Psychology) {
            Text("Active Behavior:", color = Color(0xFF94A3B8), fontSize = 12.sp)

            val behaviors = listOf(
                BehaviorType.NONE,
                BehaviorType.PLATFORMER_PLAYER,
                BehaviorType.TOPDOWN_PLAYER,
                BehaviorType.ENEMY_PATROL,
                BehaviorType.ENEMY_CHASER,
                BehaviorType.ENEMY_SHOOTER,
                BehaviorType.COLLECTABLE_COIN,
                BehaviorType.COLLECTABLE_GEM,
                BehaviorType.HAZARD_SPIKE,
                BehaviorType.VICTORY_FLAG
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
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSel, onClick = {
                            selectedNode.behavior = b
                            onNodeUpdated(selectedNode)
                        })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(b.name, color = Color.White, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (selectedNode.behavior == BehaviorType.PLATFORMER_PLAYER || selectedNode.behavior == BehaviorType.TOPDOWN_PLAYER) {
                NumberField(label = "Move Speed", value = selectedNode.moveSpeed) {
                    selectedNode.moveSpeed = it
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Jump Force", value = selectedNode.jumpForce) {
                    selectedNode.jumpForce = it
                    onNodeUpdated(selectedNode)
                }
            } else if (selectedNode.behavior == BehaviorType.ENEMY_PATROL) {
                NumberField(label = "Patrol Distance", value = selectedNode.patrolDistance) {
                    selectedNode.patrolDistance = it
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Move Speed", value = selectedNode.moveSpeed) {
                    selectedNode.moveSpeed = it
                    onNodeUpdated(selectedNode)
                }
            } else if (selectedNode.behavior == BehaviorType.ENEMY_CHASER) {
                NumberField(label = "Detection Range", value = selectedNode.detectionRange) {
                    selectedNode.detectionRange = it
                    onNodeUpdated(selectedNode)
                }
                NumberField(label = "Chase Speed", value = selectedNode.moveSpeed) {
                    selectedNode.moveSpeed = it
                    onNodeUpdated(selectedNode)
                }
            } else if (selectedNode.behavior == BehaviorType.ENEMY_SHOOTER) {
                NumberField(label = "Shoot Interval (s)", value = selectedNode.shootInterval) {
                    selectedNode.shootInterval = it.coerceAtLeast(0.5f)
                    onNodeUpdated(selectedNode)
                }
            }

            NumberField(label = "Health Points (HP)", value = selectedNode.health.toFloat()) {
                selectedNode.health = it.toInt().coerceAtLeast(1)
                selectedNode.maxHealth = selectedNode.health
                onNodeUpdated(selectedNode)
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
