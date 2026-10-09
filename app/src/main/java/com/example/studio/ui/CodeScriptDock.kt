package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode

@Composable
fun CodeScriptDock(
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
            Text("Select a Node in Hierarchy to edit its GDScript code", color = Color(0xFF64748B), fontSize = 13.sp)
        }
        return
    }

    var scriptText by remember(selectedNode.id) {
        mutableStateOf(
            selectedNode.scriptCode.ifBlank {
                """
                extends ${selectedNode.nodeType.name}

                var speed = 250.0
                var jump_impulse = -550.0

                func _ready():
                    print("Initialized ${selectedNode.name}")

                func _physics_process(delta):
                    # Movement logic
                    if Input.is_action_pressed("ui_right"):
                        position.x += speed * delta
                    elif Input.is_action_pressed("ui_left"):
                        position.x -= speed * delta
                """.trimIndent()
            }
        )
    }

    var consoleOutput by remember { mutableStateOf("Script syntax valid. Ready to attach.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(10.dp)
    ) {
        // Script Header Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "GDScript: ${selectedNode.name}.gd",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        selectedNode.scriptCode = scriptText
                        onNodeUpdated(selectedNode)
                        consoleOutput = "Saved GDScript to '${selectedNode.name}' successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Attach Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Template Quick Insert Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "_ready()" to "func _ready():\n    pass\n",
                "_process()" to "\nfunc _process(delta):\n    pass\n",
                "on_body_entered()" to "\nfunc _on_body_entered(body):\n    queue_free()\n"
            ).forEach { (label, snippet) ->
                AssistChip(
                    onClick = {
                        scriptText += snippet
                        selectedNode.scriptCode = scriptText
                        onNodeUpdated(selectedNode)
                    },
                    label = { Text("+ $label", fontSize = 10.sp, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Editor Input Area
        OutlinedTextField(
            value = scriptText,
            onValueChange = {
                scriptText = it
                selectedNode.scriptCode = it
                onNodeUpdated(selectedNode)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF030712), shape = RoundedCornerShape(8.dp)),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0284C7),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF030712),
                unfocusedContainerColor = Color(0xFF030712)
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Console Output / Validation Status Footer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF111827),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Terminal,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    consoleOutput,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
