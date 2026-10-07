package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
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
fun CodeEditorDock(
    selectedNode: GameNode?,
    onScriptSaved: (GameNode) -> Unit,
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
            Text("Select a node in hierarchy to edit attached GDScript/Lua code", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    var scriptText by remember(selectedNode) { mutableStateOf(selectedNode.scriptCode) }
    var evalOutput by remember { mutableStateOf("Script ready.") }

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
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Code Editor - ${selectedNode.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = {
                    selectedNode.scriptCode = scriptText
                    onScriptSaved(selectedNode)
                    evalOutput = "Script saved to node '${selectedNode.name}' successfully!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Script", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = scriptText,
            onValueChange = { scriptText = it },
            placeholder = {
                Text(
                    "# GDScript / Lua Script Example\nextends Node2D\n\nfunc _process(delta):\n    position.x += 10 * delta",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Console: $evalOutput",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF38BDF8),
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
