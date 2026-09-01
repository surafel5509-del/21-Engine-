package com.example.studio.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode
import com.example.studio.model.VisualScriptRule

@Composable
fun VisualScriptDock(
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
            Text("Select a Node to attach visual scripts", color = Color(0xFF64748B), fontSize = 13.sp)
        }
        return
    }

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
                Icon(Icons.Default.Javascript, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Visual Script: ${selectedNode.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Button(
                onClick = {
                    selectedNode.customRules.add(VisualScriptRule())
                    onNodeUpdated(selectedNode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Rule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Script Logic Flow Blocks
        if (selectedNode.customRules.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 How Visual Scripting Works", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Attach Event-Action blocks like Godot Signals:\n• On Collision with [Enemy] -> Play Sound [Explosion] & Destroy\n• On Touch Tap -> Apply Jump Impulse [700]\n• On Timer -> Spawn Bullet Prefab",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedNode.customRules) { rule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡ WHEN (EVENT)", color = Color(0xFFFFD600), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            IconButton(
                                onClick = {
                                    selectedNode.customRules.remove(rule)
                                    onNodeUpdated(selectedNode)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete Rule", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }

                        // Trigger Selector
                        val triggers = listOf("ON_COLLISION_ENTER", "ON_TOUCH_TAP", "ON_TIMER", "ON_UPDATE")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            triggers.forEach { trig ->
                                val isSel = rule.trigger == trig
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        rule.trigger = trig
                                        onNodeUpdated(selectedNode)
                                    },
                                    label = { Text(trig.replace("ON_", ""), fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("🎯 THEN DO (ACTION)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)

                        val actions = listOf("PLAY_SOUND", "ADD_SCORE", "APPLY_FORCE", "SPAWN_PARTICLES", "DESTROY_SELF")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            actions.take(3).forEach { act ->
                                val isSel = rule.action == act
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        rule.action = act
                                        onNodeUpdated(selectedNode)
                                    },
                                    label = { Text(act.replace("_", " "), fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Script Code Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("GDScript / Kotlin Code Preview", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            """
                            extends ${selectedNode.nodeType.name}
                            
                            func _ready():
                                print("${selectedNode.name} initialized")
                                
                            func _physics_process(delta):
                                # Behavior: ${selectedNode.behavior.name}
                                velocity.x = ${selectedNode.moveSpeed}
                            """.trimIndent(),
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
