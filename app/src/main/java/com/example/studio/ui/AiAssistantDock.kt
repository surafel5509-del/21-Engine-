package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.ai.AiResponse
import com.example.studio.ai.ProviderType
import com.example.studio.ai.StudioAiAgent
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val responseData: AiResponse? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDock(
    aiAgent: StudioAiAgent,
    project: GameProject,
    selectedNode: GameNode?,
    onProjectUpdated: (GameProject) -> Unit,
    onScriptGenerated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var promptText by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedProviderType by remember { mutableStateOf(aiAgent.currentProviderType) }
    var apiKeyText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                Text("AI Assistant Agent", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                AssistChip(
                    onClick = { showSettingsDialog = true },
                    label = { Text(selectedProviderType.displayName, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color(0xFF38BDF8))
                )
            }

            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Provider Settings", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }

        // Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Create Player", "Platformer Script", "Fix Errors", "Spawn Enemy").forEach { action ->
                AssistChip(
                    onClick = {
                        promptText = action
                    },
                    label = { Text(action, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF020617), labelColor = Color.LightGray)
                )
            }
        }

        // Chat Log
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFF020617), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Text(
                        "Ask the AI agent to write scripts, create game objects, explain errors, or modify your 2D scene.\n\nWorks completely offline with Local Agent or connects to cloud models.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            items(chatMessages) { msg ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (msg.isUser) Color(0xFF0284C7) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(msg.text, color = Color.White, fontSize = 12.sp)

                            msg.responseData?.suggestedScript?.let { script ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onScriptGenerated(script) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Apply Generated Script", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                modifier = Modifier.weight(1.0f),
                placeholder = { Text("Ask AI Agent...", fontSize = 12.sp, color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF334155)
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (promptText.isNotBlank()) {
                        val userPrompt = promptText
                        promptText = ""
                        chatMessages = chatMessages + ChatMessage("User", userPrompt, isUser = true)

                        coroutineScope.launch {
                            val resp = if (userPrompt.lowercase().contains("script")) {
                                aiAgent.generateScript(userPrompt, selectedNode)
                            } else if (userPrompt.lowercase().contains("create") || userPrompt.lowercase().contains("spawn")) {
                                aiAgent.createObject(userPrompt, project)
                            } else {
                                aiAgent.generateScript(userPrompt, selectedNode)
                            }

                            resp.modifiedProject?.let { updated -> onProjectUpdated(updated) }
                            chatMessages = chatMessages + ChatMessage("AI Agent", resp.text, isUser = false, responseData = resp)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFA855F7), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }

    // Provider Configuration Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Select AI Provider", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProviderType.values().forEach { providerType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selectedProviderType == providerType) Color(0xFF1E293B) else Color.Transparent, shape = RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(providerType.displayName, fontSize = 12.sp, color = Color.White)
                            RadioButton(
                                selected = (selectedProviderType == providerType),
                                onClick = {
                                    selectedProviderType = providerType
                                    aiAgent.currentProviderType = providerType
                                }
                            )
                        }
                    }

                    if (selectedProviderType != ProviderType.LOCAL_HEURISTIC) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = apiKeyText,
                            onValueChange = { apiKeyText = it },
                            label = { Text("API Key", fontSize = 11.sp) },
                            placeholder = { Text("sk-...", fontSize = 11.sp) },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        aiAgent.configureProvider(selectedProviderType, apiKeyText)
                        showSettingsDialog = false
                    }
                ) {
                    Text("Save Config", color = Color(0xFF38BDF8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
