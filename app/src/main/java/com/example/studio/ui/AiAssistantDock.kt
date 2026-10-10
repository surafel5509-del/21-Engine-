package com.example.studio.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.ai.AiAgent
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDock(
    project: GameProject,
    onProjectUpdated: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val agent = remember { AiAgent() }

    var selectedProvider by remember { mutableStateOf(agent.providers.first()) }
    var apiKeyText by remember { mutableStateOf("") }
    var userPromptText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("AI", "Hello! I am your AI Game Engine Copilot. Ask me to create nodes, write scripts, fix errors, or design game mechanics.")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        // AI PROVIDER SELECTOR & CONFIG
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("AI Provider", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    agent.providers.take(3).forEach { provider ->
                        val isSel = selectedProvider.id == provider.id
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedProvider = provider
                                agent.activeProvider = provider
                            },
                            label = { Text(provider.displayName.substringBefore(" "), fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }

                if (selectedProvider.requiresApiKey) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = {
                            apiKeyText = it
                            agent.apiKey = it
                        },
                        label = { Text("API Key for ${selectedProvider.displayName}", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // PRESET PROMPT SUGGESTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Create Player", "Add Coins", "Write GDScript", "Fix Errors").forEach { preset ->
                AssistChip(
                    onClick = { userPromptText = preset },
                    label = { Text(preset, fontSize = 9.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // CHAT STREAM BOX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF030712), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chatMessages) { msg ->
                    val isUser = msg.sender == "USER"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (isUser) Color(0xFF0284C7) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // INPUT FIELD & SEND BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPromptText,
                onValueChange = { userPromptText = it },
                placeholder = { Text("Ask AI to create nodes or write code...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = {
                    if (userPromptText.isNotBlank() && !isProcessing) {
                        val prompt = userPromptText
                        userPromptText = ""
                        chatMessages.add(ChatMessage("USER", prompt))
                        isProcessing = true

                        coroutineScope.launch {
                            val result = agent.processUserPrompt(prompt, project)
                            chatMessages.add(ChatMessage("AI", result.responseText))
                            if (result.projectModified) {
                                onProjectUpdated(project)
                                Toast.makeText(context, "AI modified project!", Toast.LENGTH_SHORT).show()
                            }
                            isProcessing = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
