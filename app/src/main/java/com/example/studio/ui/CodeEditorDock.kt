package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.engine.scripting.ScriptEngine
import com.example.engine.scripting.ScriptError
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
            Text(
                "Select a Node in Hierarchy to edit its GDScript code.",
                color = Color(0xFF64748B),
                fontSize = 13.sp
            )
        }
        return
    }

    var codeText by remember(selectedNode.id) { mutableStateOf(selectedNode.scriptCode) }
    var errors by remember { mutableStateOf<List<ScriptError>>(emptyList()) }

    LaunchedEffect(codeText) {
        selectedNode.scriptCode = codeText
        onNodeUpdated(selectedNode)
        errors = ScriptEngine.parseAndValidate(codeText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        // TOP TOOLBAR & TEMPLATE BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Script: ${selectedNode.name}.gd",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = {
                        codeText = """
                            # GDScript for ${selectedNode.name}
                            func _ready():
                                print("Node initialized: " + "${selectedNode.name}")
                                play_sound("jump")

                            func _process(delta):
                                position_x += move_speed * delta
                        """.trimIndent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Basic Template", fontSize = 10.sp, color = Color(0xFF38BDF8))
                }

                Button(
                    onClick = {
                        codeText = """
                            # Player Movement Script
                            func _ready():
                                print("Player Ready!")

                            func _process(delta):
                                position_x += 120 * delta
                                if position_x > 1200:
                                    position_x = 100
                        """.trimIndent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Player Template", fontSize = 10.sp, color = Color(0xFF10B981))
                }
            }
        }

        // SYNTAX ERROR BANNER
        if (errors.isNotEmpty()) {
            Surface(
                color = Color(0x33EF4444),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Line ${errors.first().lineNumber}: ${errors.first().message}",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // TEXT EDITOR BOX WITH LINE NUMBERS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF030712), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            // Line Numbers
            val lineCount = codeText.lines().size.coerceAtLeast(1)
            LazyColumn(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.End
            ) {
                items(lineCount) { idx ->
                    Text(
                        text = "${idx + 1}",
                        color = Color(0xFF475569),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Code Input Field
            BasicTextField(
                value = codeText,
                onValueChange = { codeText = it },
                modifier = Modifier
                    .fillMaxSize(),
                textStyle = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                ),
                cursorBrush = SolidColor(Color(0xFF38BDF8))
            )
        }
    }
}
