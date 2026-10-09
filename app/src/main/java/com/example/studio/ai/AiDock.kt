package com.example.studio.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val action: AiAction? = null
)

@Composable
fun AiDock(
    project: GameProject,
    aiAgent: AiAgent,
    lastErrorLog: String = "",
    onProjectUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var promptText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "AI Assistant",
                text = "Hello! I am your AI Game Engine Copilot. Ask me to create characters, platforms, coins, generate scripts, fix errors, or inspect project nodes.",
                isUser = false
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // AI Header & Provider Switcher Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "AI Copilot Agent",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        aiAgent.config.providerType.displayName,
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "AI Settings",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Suggestion Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Create Player",
                "Add Platform",
                "Generate Script",
                "Fix Error"
            ).forEach { quickPrompt ->
                AssistChip(
                    onClick = {
                        promptText = quickPrompt
                    },
                    label = { Text(quickPrompt, fontSize = 10.sp, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Conversation List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.isUser) Color(0xFF0284C7) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                msg.sender,
                                color = if (msg.isUser) Color.White else Color(0xFFA855F7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            msg.text,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // If AI Response has an executable action button
                        msg.action?.let { act ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val resultMsg = aiAgent.executeAction(act, project)
                                    onProjectUpdated()
                                    chatMessages.add(
                                        ChatMessage("System", resultMsg, isUser = false)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Apply Action to Scene", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Prompt Input Field & Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                placeholder = { Text("Ask AI to generate objects, scripts, fix errors...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = {
                    if (promptText.isNotBlank() && !isGenerating) {
                        val currentPrompt = promptText
                        promptText = ""
                        chatMessages.add(ChatMessage("You", currentPrompt, isUser = true))
                        isGenerating = true

                        coroutineScope.launch {
                            try {
                                val response = aiAgent.processPrompt(currentPrompt, project, lastErrorLog)
                                isGenerating = false
                                val primaryAction = response.actions.firstOrNull()
                                chatMessages.add(
                                    ChatMessage("AI Assistant", response.text, isUser = false, action = primaryAction)
                                )
                            } catch (e: Exception) {
                                isGenerating = false
                                chatMessages.add(
                                    ChatMessage("AI Assistant", "Error processing request: ${e.message}", isUser = false)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send Prompt", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // AI Provider Configuration Dialog Modal
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text("AI Provider Settings", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Provider:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    AiProviderType.entries.forEach { pType ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (aiAgent.config.providerType == pType) Color(0xFF1E293B) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(6.dp)
                        ) {
                            RadioButton(
                                selected = (aiAgent.config.providerType == pType),
                                onClick = {
                                    aiAgent.config = aiAgent.config.copy(providerType = pType)
                                }
                            )
                            Text(pType.displayName, color = Color.White, fontSize = 12.sp)
                        }
                    }

                    if (aiAgent.config.providerType != AiProviderType.LOCAL_HEURISTIC) {
                        OutlinedTextField(
                            value = aiAgent.config.apiKey,
                            onValueChange = { aiAgent.config = aiAgent.config.copy(apiKey = it) },
                            label = { Text("API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = aiAgent.config.baseUrl,
                            onValueChange = { aiAgent.config = aiAgent.config.copy(baseUrl = it) },
                            label = { Text("Base URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Save & Close", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
