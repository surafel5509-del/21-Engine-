package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
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
    onNodeUpdated: (GameNode) -> Unit,
    onAiFixRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedNode == null) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Select a node from Hierarchy to write or edit script.", color = Color.Gray, fontSize = 13.sp)
        }
        return
    }

    var codeText by remember(selectedNode.id) { mutableStateOf(selectedNode.scriptContent) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Code Editor Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Text(
                    text = "${selectedNode.name}.gd",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAiFixRequest(codeText) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Fix Code", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        selectedNode.scriptContent = codeText
                        onNodeUpdated(selectedNode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Save Script", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Script Editor Area
        OutlinedTextField(
            value = codeText,
            onValueChange = {
                codeText = it
                selectedNode.scriptContent = it
                onNodeUpdated(selectedNode)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFF020617), shape = RoundedCornerShape(8.dp)),
            textStyle = LocalTextStyle.current.copy(
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            placeholder = { Text("Write GDScript/Lua logic here...\n\nfunc _ready():\n    pass\n\nfunc _process(delta):\n    position.x += 100 * delta", color = Color(0xFF475569)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF1E293B)
            )
        )
    }
}
