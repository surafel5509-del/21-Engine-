package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.ai.AiConfig
import com.example.studio.ai.AiGameAgent
import com.example.studio.ai.AiProviderType
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val text: String
)

@Composable
fun AiAssistantDock(
    project: GameProject,
    onProjectUpdated: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var aiConfig by remember { mutableStateOf(AiConfig()) }
    val agent = remember { AiGameAgent(aiConfig) }

    var userPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("AI", "Hello! I am your AI Game Development Assistant. I can generate scripts, spawn prefabs, fix code, and help build your 2D game.")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // AI Header & Provider Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Game Co-Pilot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = aiConfig.providerType.name,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { showSettingsModal = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "AI Settings", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Log Stream
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0B1120), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.sender == "User"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF0284C7) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = msg.sender,
                                color = if (isUser) Color.White else Color(0xFFA855F7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Prompts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Add Coin", "Add Platform", "Add Enemy", "Gen Script").forEach { label ->
                AssistChip(
                    onClick = { userPrompt = label },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (userPrompt.isNotBlank() && !isThinking) {
                        val promptText = userPrompt
                        userPrompt = ""
                        chatMessages.add(ChatMessage("User", promptText))
                        isThinking = true

                        coroutineScope.launch {
                            val result = agent.processUserPrompt(promptText, project)
                            chatMessages.add(ChatMessage("AI", result.replyText))
                            if (result.modifiedProject != null) {
                                onProjectUpdated(result.modifiedProject)
                            }
                            isThinking = false
                        }
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFA855F7), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }

    // Provider Config Modal
    if (showSettingsModal) {
        AlertDialog(
            onDismissRequest = { showSettingsModal = false },
            containerColor = Color(0xFF0F172A),
            title = { Text("AI Provider Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select AI Provider:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AiProviderType.values().forEach { pType ->
                            FilterChip(
                                selected = aiConfig.providerType == pType,
                                onClick = {
                                    val newCfg = aiConfig.copy(providerType = pType)
                                    aiConfig = newCfg
                                    agent.updateConfig(newCfg)
                                },
                                label = { Text(pType.name, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = aiConfig.apiKey,
                        onValueChange = {
                            val newCfg = aiConfig.copy(apiKey = it)
                            aiConfig = newCfg
                            agent.updateConfig(newCfg)
                        },
                        label = { Text("API Key") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = aiConfig.baseUrl,
                        onValueChange = {
                            val newCfg = aiConfig.copy(baseUrl = it)
                            aiConfig = newCfg
                            agent.updateConfig(newCfg)
                        },
                        label = { Text("Custom Base URL (e.g. Local LLM / Ollama)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsModal = false }) {
                    Text("Done", color = Color(0xFF38BDF8))
                }
            }
        )
    }
}
