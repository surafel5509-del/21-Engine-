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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

@Composable
fun AiAssistantDock(
    project: GameProject,
    onProjectModified: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var config by remember { mutableStateOf(AiAgentConfig()) }
    val agent = remember(config) { AiGameDevAgent(config) }

    var messages by remember {
        mutableStateOf(
            listOf(
                AiMessage(
                    role = "assistant",
                    content = "Hello! I am your AI Game Development Agent. How can I help you build your game scene today?"
                )
            )
        )
    }

    var userInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        // TOP AGENT BAR / CONFIG SWITCHER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Game Dev Assistant", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = config.providerType.displayName,
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = { showSettings = !showSettings },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "AI Settings", tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }

        // PROVIDER CONFIG DIALOG / EXPANDABLE PANEL
        if (showSettings) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("AI Provider Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Provider:", fontSize = 10.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AiProviderType.entries.forEach { provider ->
                            FilterChip(
                                selected = config.providerType == provider,
                                onClick = { config = config.copy(providerType = provider) },
                                label = { Text(provider.displayName, fontSize = 9.sp) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = config.apiKey,
                        onValueChange = { config = config.copy(apiKey = it) },
                        label = { Text("API Key (Optional for offline demo)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // CHAT HISTORY
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isUser) Color(0xFF0284C7) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = if (isUser) "You" else "AI Agent",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) Color.LightGray else Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.content,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = if (!isUser && msg.content.contains("```")) FontFamily.Monospace else FontFamily.Default
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // INPUT PROMPT ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Ask AI to create node, generate script...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = {
                    if (userInput.isNotBlank() && !isProcessing) {
                        val promptText = userInput
                        userInput = ""
                        messages = messages + AiMessage("user", promptText)
                        isProcessing = true

                        coroutineScope.launch {
                            val result = agent.processUserPrompt(promptText, project)
                            messages = messages + AiMessage("assistant", result.responseText)
                            if (result.appliedActionDescription != null) {
                                onProjectModified()
                            }
                            isProcessing = false
                        }
                    }
                },
                enabled = userInput.isNotBlank() && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
