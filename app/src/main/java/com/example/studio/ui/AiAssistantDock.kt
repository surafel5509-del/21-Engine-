package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.ai.*
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

@Composable
fun AiAssistantDock(
    assistant: EngineAiAssistant,
    project: GameProject,
    selectedNode: GameNode?,
    onProjectUpdated: (GameProject, GameNode?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var inputPrompt by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<AiMessage>()) }
    var isThinking by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    val activeProvider = assistant.getActiveProvider()
    val activeConfig = assistant.getProviderConfig(activeProvider.id)

    var baseUrlInput by remember { mutableStateOf(activeConfig.baseUrl) }
    var apiKeyInput by remember { mutableStateOf(activeConfig.apiKey) }
    var modelInput by remember { mutableStateOf(activeConfig.modelName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
            .padding(8.dp)
    ) {
        // TOP HEADER BAR: Provider Switcher & Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = activeProvider.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (activeProvider.isOfflineCapable) "Offline Mode Enabled" else "Requires API / Network",
                            color = if (activeProvider.isOfflineCapable) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Provider dropdown button
                    IconButton(
                        onClick = {
                            val all = assistant.registry.getAllProviders()
                            val nextIdx = (all.indexOfFirst { it.id == activeProvider.id } + 1) % all.size
                            assistant.setActiveProvider(all[nextIdx].id)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Provider", tint = Color.White)
                    }

                    // Provider Config Modal Trigger
                    IconButton(
                        onClick = {
                            val cfg = assistant.getProviderConfig(activeProvider.id)
                            baseUrlInput = cfg.baseUrl
                            apiKeyInput = cfg.apiKey
                            modelInput = cfg.modelName
                            showConfigDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Config AI", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CHAT MESSAGE HISTORY
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI Game Development Assistant", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Ask to create game objects, write scripts, fix bugs, or search nodes.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            items(messages) { msg ->
                AiMessageBubble(
                    message = msg,
                    onExecuteAction = { action ->
                        val (updatedProject, updatedSelectedNode) = assistant.executeAction(action, project, selectedNode)
                        onProjectUpdated(updatedProject, updatedSelectedNode)

                        // Mark message action as executed
                        messages = messages.map { m ->
                            if (m.id == msg.id) m.copy(isActionExecuted = true) else m
                        }
                    }
                )
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF38BDF8), strokeWidth = 2.dp)
                        Text("AI Agent processing request...", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // INPUT PROMPT FIELD & SEND BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("e.g. 'create platform', 'add player script'", fontSize = 12.sp, color = Color(0xFF64748B)) },
                modifier = Modifier.weight(1.0f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Button(
                onClick = {
                    if (inputPrompt.isBlank() || isThinking) return@Button
                    val promptText = inputPrompt
                    inputPrompt = ""

                    val userMessage = AiMessage(role = AiRole.USER, content = promptText)
                    messages = messages + userMessage
                    isThinking = true

                    coroutineScope.launch {
                        val context = AiContext(
                            project = project,
                            selectedNode = selectedNode,
                            history = messages
                        )

                        val response = assistant.processUserPrompt(promptText, context)

                        val assistantMessage = AiMessage(
                            role = AiRole.ASSISTANT,
                            content = response.replyText,
                            pendingAction = response.suggestedAction
                        )

                        messages = messages + assistantMessage
                        isThinking = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Prompt", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }

    // PROVIDER CONFIG DIALOG
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = { Text("Configure ${activeProvider.name}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Base URL / Endpoint:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    OutlinedTextField(
                        value = baseUrlInput,
                        onValueChange = { baseUrlInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("API Key:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Model Name:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        assistant.updateProviderConfig(
                            ProviderConfig(
                                providerId = activeProvider.id,
                                baseUrl = baseUrlInput,
                                apiKey = apiKeyInput,
                                modelName = modelInput
                            )
                        )
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save Config", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun AiMessageBubble(
    message: AiMessage,
    onExecuteAction: (AiAction) -> Unit
) {
    val isUser = message.role == AiRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isUser) Color(0xFF0369A1) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = if (isUser) "You" else "AI Assistant",
                color = if (isUser) Color(0xFFBAE6FD) else Color(0xFF38BDF8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.content,
                color = Color.White,
                fontSize = 12.sp
            )

            // ACTION BUTTON OVERLAY
            message.pendingAction?.let { action ->
                Spacer(modifier = Modifier.height(8.dp))
                if (message.isActionExecuted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Text("Action Executed", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onExecuteAction(action) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXECUTE ENGINE ACTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
