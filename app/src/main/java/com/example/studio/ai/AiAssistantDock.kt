package com.example.studio.ai

import androidx.compose.foundation.background
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
    val sender: String, // "User" or "AI"
    val text: String,
    val toolResult: AiToolResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDock(
    project: GameProject,
    onProjectUpdated: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    var config by remember { mutableStateOf(AiProviderConfig()) }
    var provider by remember { mutableStateOf(AiProviderFactory.createProvider(config)) }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    sender = "AI",
                    text = " 👋 Hi! I am your AI Game Engine Assistant. Ask me to create scenes, add nodes, generate scripts, search the project, or fix errors!"
                )
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val toolExecutor = remember { AiEngineToolExecutor() }

    fun sendPrompt(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(sender = "User", text = userText)
        messages = messages + userMsg
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            val response = provider.generateResponse(userText, project)
            var toolRes: AiToolResult? = null

            if (response.toolCommand != null) {
                toolRes = toolExecutor.executeCommand(response.toolCommand, project)
                onProjectUpdated(project)
            }

            val aiMsg = ChatMessage(
                sender = "AI",
                text = response.textMessage,
                toolResult = toolRes
            )
            messages = messages + aiMsg
            isThinking = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("AI Game Studio Agent", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Provider: ${config.providerType.displayName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }

            IconButton(
                onClick = { showSettingsModal = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "AI Settings", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { sendPrompt("Create a 2D Player Character Node with physics and platformer movement") },
                label = { Text("➕ Player Node", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color.White)
            )
            AssistChip(
                onClick = { sendPrompt("Create a collectable gold coin node") },
                label = { Text("🪙 Coin Node", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color.White)
            )
            AssistChip(
                onClick = { sendPrompt("Run test simulation and verify project nodes") },
                label = { Text("🧪 Run Test", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "User"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF0284C7) else Color(0xFF131D31)
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            if (msg.toolResult != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (msg.toolResult.success) Color(0xFF065F46) else Color(0xFF991B1B)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "⚡ Engine Action Executed: ${msg.toolResult.message}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF38BDF8), strokeWidth = 2.dp)
                        Text("AI Agent is reasoning & modifying project...", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Field & Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask AI to build nodes, write code, fix physics...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF0284C7)
                ),
                maxLines = 2,
                singleLine = false
            )

            IconButton(
                onClick = { sendPrompt(inputText) },
                enabled = inputText.isNotBlank() && !isThinking,
                modifier = Modifier
                    .size(42.dp)
                    .background(if (inputText.isNotBlank()) Color(0xFF0284C7) else Color(0xFF334155), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }

    // AI Provider Configuration Dialog
    if (showSettingsModal) {
        AlertDialog(
            onDismissRequest = { showSettingsModal = false },
            title = { Text("AI Provider Configuration", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Provider:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    AiProviderType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (config.providerType == type) Color(0xFF0284C7) else Color(0xFF1E293B), shape = RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.providerType == type,
                                onClick = {
                                    config = config.copy(
                                        providerType = type,
                                        modelName = type.defaultModel,
                                        baseUrl = type.defaultBaseUrl
                                    )
                                }
                            )
                            Text(type.displayName, color = Color.White, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = config.apiKey,
                        onValueChange = { config = config.copy(apiKey = it) },
                        label = { Text("API Key (Optional for Local LLM)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = config.baseUrl,
                        onValueChange = { config = config.copy(baseUrl = it) },
                        label = { Text("Base Endpoint URL", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        provider = AiProviderFactory.createProvider(config)
                        showSettingsModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save & Apply")
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
