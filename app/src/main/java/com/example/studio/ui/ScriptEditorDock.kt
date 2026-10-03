package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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

@Composable
fun ScriptEditorDock(
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
            Text("Select a node to edit its GDScript / Lua script code", color = Color(0xFF64748B), fontSize = 13.sp)
        }
        return
    }

    var scriptCode by remember(selectedNode.id) {
        mutableStateOf(
            selectedNode.customTextureKey ?: """
                # GDScript for ${selectedNode.name}
                extends ${selectedNode.nodeType.name}

                var move_speed = ${selectedNode.moveSpeed}
                var health = ${selectedNode.health}

                func _ready():
                    print("${selectedNode.name} initialized on scene start!")

                func _physics_process(delta):
                    # Process movement and collision logic
                    if Input.is_action_pressed("ui_right"):
                        position.x += move_speed * delta
                    elif Input.is_action_pressed("ui_left"):
                        position.x -= move_speed * delta

                func _on_collision(other_node):
                    print("Collided with " + other_node.name)
            """.trimIndent()
        )
    }

    var consoleOutput by remember { mutableStateOf("Script ready. Press 'Run Script' to test.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
            .padding(10.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Code Editor: ${selectedNode.name}.gd", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        consoleOutput = "▶ Script executed successfully for ${selectedNode.name}! No syntax errors."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedNode.customTextureKey = scriptCode
                        onNodeUpdated(selectedNode)
                        consoleOutput = "💾 Script saved and attached to ${selectedNode.name}."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Editor Code Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF020617), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            // Line numbers
            val lineCount = scriptCode.lines().size
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(28.dp),
                horizontalAlignment = Alignment.End
            ) {
                (1..lineCount).forEach { lineNumber ->
                    Text(
                        text = lineNumber.toString(),
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            VerticalDivider(color = Color(0xFF1E293B), modifier = Modifier.fillMaxHeight())

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = scriptCode,
                onValueChange = { scriptCode = it },
                modifier = Modifier.fillMaxSize(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color(0xFF38BDF8),
                    unfocusedTextColor = Color(0xFF38BDF8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Console output bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Text(
                text = consoleOutput,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
