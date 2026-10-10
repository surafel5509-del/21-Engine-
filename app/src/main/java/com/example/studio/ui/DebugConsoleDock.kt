package com.example.studio.ui

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

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO", // INFO, WARN, ERROR
    val message: String
)

object DebugConsoleLogger {
    val logs = mutableStateListOf<LogEntry>(
        LogEntry(level = "INFO", message = "2D Engine initialized successfully."),
        LogEntry(level = "INFO", message = "Renderer initialized with Canvas2D Hardware Acceleration.")
    )

    fun log(message: String, level: String = "INFO") {
        logs.add(LogEntry(level = level, message = message))
        if (logs.size > 200) {
            logs.removeAt(0)
        }
    }

    fun clear() {
        logs.clear()
    }
}

@Composable
fun DebugConsoleDock(
    project: GameProject,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(DebugConsoleLogger.logs.size, selectedFilter) {
        when (selectedFilter) {
            "INFO" -> DebugConsoleLogger.logs.filter { it.level == "INFO" }
            "WARN" -> DebugConsoleLogger.logs.filter { it.level == "WARN" }
            "ERROR" -> DebugConsoleLogger.logs.filter { it.level == "ERROR" }
            else -> DebugConsoleLogger.logs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        // ENGINE METRICS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("FPS: 60", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Nodes: ${project.nodes.size}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                Text("Physics Bodies: ${project.nodes.count { it.hasPhysics }}", color = Color(0xFFF59E0B), fontSize = 11.sp)
            }

            IconButton(
                onClick = { DebugConsoleLogger.clear() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FILTER CHIPS
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            listOf("ALL", "INFO", "WARN", "ERROR").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.height(26.dp)
                )
            }
        }

        // LOG LIST
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF030712), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Text(
                    "No log output available.",
                    color = Color(0xFF475569),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredLogs) { entry ->
                        val color = when (entry.level) {
                            "ERROR" -> Color(0xFFEF4444)
                            "WARN" -> Color(0xFFF59E0B)
                            else -> Color(0xFF38BDF8)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "[${entry.level}]",
                                color = color,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(52.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = entry.message,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
