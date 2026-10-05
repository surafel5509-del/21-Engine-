package com.example.studio.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameProject
import java.text.SimpleDateFormat
import java.util.*

enum class LogLevel(val label: String, val color: Color) {
    INFO("INFO", Color(0xFF38BDF8)),
    WARN("WARN", Color(0xFFF59E0B)),
    ERROR("ERROR", Color(0xFFEF4444)),
    PHYSICS("PHYSICS", Color(0xFF10B981)),
    AI("AI", Color(0xFFA855F7))
}

data class ConsoleLogItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String
)

@Composable
fun DebugConsoleDock(
    project: GameProject,
    onExplainErrorWithAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    val logs = remember {
        mutableStateListOf(
            ConsoleLogItem(level = LogLevel.INFO, tag = "Engine", message = "GodotStudio Runtime Engine initialized."),
            ConsoleLogItem(level = LogLevel.PHYSICS, tag = "PhysicsWorld2D", message = "Physics 2D World active with gravity = -980.0."),
            ConsoleLogItem(level = LogLevel.INFO, tag = "Scene", message = "Loaded active project '${project.name}' (${project.nodes.size} nodes)."),
            ConsoleLogItem(level = LogLevel.WARN, tag = "Renderer", message = "Texture key 'player_run.png' not cached; using procedural shape fallback."),
            ConsoleLogItem(level = LogLevel.AI, tag = "AiAgent", message = "AI Development Agent ready with multi-provider support.")
        )
    }

    var filterLevel by remember { mutableStateOf<LogLevel?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
            .padding(10.dp)
    ) {
        // HEADER & DIAGNOSTICS STATS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Debug Console", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Quick Actions: Clear & Test Error
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        logs.add(
                            ConsoleLogItem(
                                level = LogLevel.ERROR,
                                tag = "ScriptRuntime",
                                message = "NullPointerException in Player.gd line 14: velocity is null"
                            )
                        )
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = "Simulate Error", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = { logs.clear() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // METRICS COUNTER CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem("FPS", "60", Color(0xFF10B981))
                MetricItem("Draw Calls", "${project.nodes.size * 2 + 3}", Color(0xFF38BDF8))
                MetricItem("Bodies", "${project.nodes.count { it.hasPhysics }}", Color(0xFFFFD600))
                MetricItem("Particles", "${project.nodes.filter { it.isParticleEmitter }.size * 32}", Color(0xFFA855F7))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FILTER CHIPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = filterLevel == null,
                onClick = { filterLevel = null },
                label = { Text("ALL (${logs.size})", fontSize = 10.sp) },
                modifier = Modifier.height(26.dp)
            )

            LogLevel.values().forEach { lvl ->
                val count = logs.count { it.level == lvl }
                FilterChip(
                    selected = filterLevel == lvl,
                    onClick = { filterLevel = if (filterLevel == lvl) null else lvl },
                    label = { Text("${lvl.label} ($count)", fontSize = 10.sp, color = lvl.color) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // LOG FEED LIST
        val filteredLogs = logs.filter { filterLevel == null || it.level == filterLevel }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070C18)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLogs) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = timeFormatter.format(Date(item.timestamp)),
                            color = Color(0xFF475569),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(75.dp)
                        )

                        Text(
                            text = "[${item.level.label}] ",
                            color = item.level.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(60.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.tag}: ${item.message}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            if (item.level == LogLevel.ERROR) {
                                TextButton(
                                    onClick = {
                                        onExplainErrorWithAi("Explain and fix this error: [${item.tag}] ${item.message}")
                                        Toast.makeText(context, "Sent error log to AI Agent!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ask AI to Fix Error", fontSize = 10.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, color = Color(0xFF64748B), fontSize = 10.sp)
    }
}
