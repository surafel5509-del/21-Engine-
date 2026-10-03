package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameNode

@Composable
fun CodeEditorDock(
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
            Text("Select a Node to edit script code", color = Color(0xFF64748B), fontSize = 13.sp)
        }
        return
    }

    var codeText by remember(selectedNode.id) {
        mutableStateOf(
            """
            extends ${selectedNode.nodeType.name}

            var speed = ${selectedNode.moveSpeed}
            var health = ${selectedNode.health}

            func _ready():
                print("${selectedNode.name} ready!")

            func _process(delta):
                # Custom GDScript Logic
                position.x += speed * delta
            """.trimIndent()
        )
    }

    var statusMessage by remember { mutableStateOf("Script Ready") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
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
                Text(
                    text = "${selectedNode.name}.gd",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        statusMessage = "Script Saved & Attached!"
                        onNodeUpdated(selectedNode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Script", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Editor Canvas with Line Numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0B1120), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            // Line Numbers
            val lineCount = codeText.lines().size
            Column(
                modifier = Modifier
                    .width(28.dp)
                    .padding(end = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    Text(
                        text = "$i",
                        color = Color(0xFF475569),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Divider(
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code Text Input
            BasicTextField(
                value = codeText,
                onValueChange = { codeText = it },
                textStyle = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                ),
                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(statusMessage, color = Color(0xFF10B981), fontSize = 11.sp)
    }
}
