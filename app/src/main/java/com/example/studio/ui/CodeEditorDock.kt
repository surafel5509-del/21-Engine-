package com.example.studio.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    val context = LocalContext.current

    if (selectedNode == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a Node in Hierarchy to edit its attached code", color = Color(0xFF64748B), fontSize = 13.sp)
        }
        return
    }

    var codeText by remember(selectedNode.id) { mutableStateOf(selectedNode.customScriptCode.ifEmpty { getDefaultScriptForNode(selectedNode) }) }
    var scriptLanguage by remember { mutableStateOf("GDScript") } // GDScript, Lua, Kotlin

    LaunchedEffect(codeText) {
        selectedNode.customScriptCode = codeText
        onNodeUpdated(selectedNode)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
            .padding(10.dp)
    ) {
        // TOP TOOLBAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${selectedNode.name}.gd", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Language selector chip
                AssistChip(
                    onClick = {
                        scriptLanguage = if (scriptLanguage == "GDScript") "Lua" else if (scriptLanguage == "Lua") "Kotlin" else "GDScript"
                    },
                    label = { Text(scriptLanguage, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color(0xFFFFD600)),
                    modifier = Modifier.height(28.dp)
                )

                // AI Code Fix Button
                Button(
                    onClick = {
                        onAiFixRequest("Analyze and optimize this ${scriptLanguage} code for '${selectedNode.name}':\n$codeText")
                        Toast.makeText(context, "Sent code to AI Agent!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // TEMPLATE PRESETS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val templates = listOf("Player Controller", "Platform Patrol", "Coin Pickup", "Bullet Spawner")
            templates.forEach { tName ->
                FilterChip(
                    selected = false,
                    onClick = {
                        codeText = getTemplateCode(tName, selectedNode.name, scriptLanguage)
                        Toast.makeText(context, "Loaded $tName template", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text(tName, fontSize = 10.sp) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // CODE EDITOR AREA WITH LINE NUMBERS & SYNTAX HIGHLIGHTING
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070C18)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Line Numbers Margin
                val lineCount = codeText.split("\n").size.coerceAtLeast(1)
                LazyColumn(
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF0F172A))
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    items(lineCount) { idx ->
                        Text(
                            text = "${idx + 1}",
                            color = Color(0xFF475569),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                Divider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = Color(0xFF1E293B))

                // Editable Code Input Field
                OutlinedTextField(
                    value = codeText,
                    onValueChange = { codeText = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        lineHeight = 18.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

private fun getDefaultScriptForNode(node: GameNode): String {
    return """
        extends ${node.nodeType.name}

        # Script attached to ${node.name}
        var speed = ${node.moveSpeed}
        var gravity = 980.0

        func _ready():
            print("${node.name} initialized!")

        func _physics_process(delta):
            # Process frame physics
            pass

        func _on_body_entered(body):
            print("Collision with ", body.name)
    """.trimIndent()
}

private fun getTemplateCode(templateName: String, nodeName: String, lang: String): String {
    return when (templateName) {
        "Player Controller" -> """
            extends CharacterBody2D

            var speed = 350.0
            var jump_velocity = -650.0

            func _physics_process(delta):
                if not is_on_floor():
                    velocity.y += 980.0 * delta

                if Input.is_action_just_pressed("jump") and is_on_floor():
                    velocity.y = jump_velocity
                    play_sound("jump")

                move_and_slide()
        """.trimIndent()

        "Platform Patrol" -> """
            extends StaticBody2D

            var start_x = 0.0
            var move_range = 200.0
            var direction = 1

            func _ready():
                start_x = position.x

            func _physics_process(delta):
                position.x += direction * 100.0 * delta
                if abs(position.x - start_x) > move_range:
                    direction *= -1
        """.trimIndent()

        "Coin Pickup" -> """
            extends Area2D

            func _on_body_entered(body):
                if body.name.begins_with("Player"):
                    play_sound("coin")
                    add_score(100)
                    queue_free()
        """.trimIndent()

        else -> """
            extends Node2D

            var spawn_timer = 0.0

            func _physics_process(delta):
                spawn_timer += delta
                if spawn_timer >= 2.0:
                    spawn_timer = 0.0
                    spawn_prefab("Bullet", position.x, position.y)
        """.trimIndent()
    }
}
