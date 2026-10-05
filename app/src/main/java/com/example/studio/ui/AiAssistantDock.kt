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
import com.example.studio.ai.*
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDock(
    project: GameProject,
    onProjectUpdated: (GameProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val agentEngine = remember { AiAgentEngine() }

    var providerConfig by remember { mutableStateOf(AiProviderConfig()) }
    var userPromptText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showConfigModal by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            AiMessage("assistant", "👋 Hi! I am GodotStudio AI agent. I can create nodes, configure physics, generate scripts, search the project, or fix errors. How can I help with '${project.name}'?")
        )
    }

    val pendingActions = remember { mutableStateListOf<AiAgentAction>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // TOP AI HEADER & PROVIDER CHIP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Agent", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            AssistChip(
                onClick = { showConfigModal = true },
                label = { Text("${providerConfig.providerType.displayName} (${providerConfig.modelName})", fontSize = 10.sp) },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(12.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFF38BDF8)
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // QUICK PROMPT SUGGESTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val suggestions = listOf("Spawn Player", "Add Platform", "Generate Script", "Fix Code")
            suggestions.forEach { label ->
                FilterChip(
                    selected = false,
                    onClick = {
                        userPromptText = when (label) {
                            "Spawn Player" -> "Create a CharacterBody2D node named Player with physics"
                            "Add Platform" -> "Add a static ground platform node"
                            "Generate Script" -> "Generate player movement GDScript with jump"
                            "Fix Code" -> "Analyze selected node and fix syntax errors"
                            else -> label
                        }
                    },
                    label = { Text(label, fontSize = 10.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CHAT & ACTION STREAM
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF0284C7) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                if (isUser) " You" else "🤖 AI Agent",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) Color.White.copy(alpha = 0.8f) else Color(0xFFA855F7)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(msg.content, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // PENDING AI ACTIONS CARDS
            if (pendingActions.isNotEmpty()) {
                item {
                    Text("⚡ Generated AI Actions:", color = Color(0xFFFFD600), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                items(pendingActions) { action ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "[${action.type}] ${action.targetNodeName.ifEmpty { action.nodeType }}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (action.description.isNotEmpty()) {
                                    Text(action.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    val resultMsg = agentEngine.applyActionToProject(action, project)
                                    onProjectUpdated(project)
                                    pendingActions.remove(action)
                                    Toast.makeText(context, resultMsg, Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // INPUT PROMPT BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = userPromptText,
                onValueChange = { userPromptText = it },
                placeholder = { Text("Ask AI to build nodes, generate code, fix bugs...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0B1120),
                    unfocusedContainerColor = Color(0xFF0B1120),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Button(
                onClick = {
                    if (userPromptText.isBlank() || isProcessing) return@Button
                    val prompt = userPromptText
                    userPromptText = ""
                    chatMessages.add(AiMessage("user", prompt))
                    isProcessing = true

                    coroutineScope.launch {
                        val response = agentEngine.processUserPrompt(prompt, project, providerConfig, chatMessages)
                        chatMessages.add(AiMessage("assistant", response.replyText))
                        if (response.actions.isNotEmpty()) {
                            pendingActions.clear()
                            pendingActions.addAll(response.actions)
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(10.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send Prompt", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // AI PROVIDER CONFIGURATION MODAL DIALOG
    if (showConfigModal) {
        AlertDialog(
            onDismissRequest = { showConfigModal = false },
            title = { Text("Configure AI Provider", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Provider:", color = Color(0xFF94A3B8), fontSize = 11.sp)

                    // Provider Dropdown selection
                    var expandedDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = providerConfig.providerType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            AiProviderType.values().forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text(prov.displayName) },
                                    onClick = {
                                        providerConfig = providerConfig.copy(
                                            providerType = prov,
                                            apiEndpoint = prov.defaultEndpoint,
                                            modelName = prov.defaultModel
                                        )
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = providerConfig.apiEndpoint,
                        onValueChange = { providerConfig = providerConfig.copy(apiEndpoint = it) },
                        label = { Text("API Endpoint URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = providerConfig.modelName,
                        onValueChange = { providerConfig = providerConfig.copy(modelName = it) },
                        label = { Text("Model Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = providerConfig.apiKey,
                        onValueChange = { providerConfig = providerConfig.copy(apiKey = it) },
                        label = { Text("API Key (Leave empty for Offline mode)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfigModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save & Close")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
