package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.utils.Logger

enum class LogLevel { INFO, WARN, ERROR }

data class EngineLogMessage(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun DebugConsoleDock(
    modifier: Modifier = Modifier
) {
    var logs by remember {
        mutableStateOf(
            listOf(
                EngineLogMessage(LogLevel.INFO, "ApexEngine", "Engine initialized @ 60 FPS Target."),
                EngineLogMessage(LogLevel.INFO, "PhysicsWorld2D", "Physics world active: Gravity Y=980."),
                EngineLogMessage(LogLevel.INFO, "AudioManager", "Audio synthesizer ready."),
                EngineLogMessage(LogLevel.WARN, "Renderer", "Viewport scaled with ScaleMode.FIT_LETTERBOX.")
            )
        )
    }

    var selectedFilter by remember { mutableStateOf<LogLevel?>(null) }

    val filteredLogs = logs.filter { selectedFilter == null || it.level == selectedFilter }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Debug Engine Console", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = selectedFilter == LogLevel.ERROR,
                    onClick = { selectedFilter = if (selectedFilter == LogLevel.ERROR) null else LogLevel.ERROR },
                    label = { Text("Errors", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEF4444), selectedLabelColor = Color.White)
                )
                IconButton(
                    onClick = { logs = emptyList() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Logs", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Log Console Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLogs) { log ->
                    val color = when (log.level) {
                        LogLevel.INFO -> Color(0xFF38BDF8)
                        LogLevel.WARN -> Color(0xFFFFD600)
                        LogLevel.ERROR -> Color(0xFFEF4444)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("[${log.level.name}] ", color = color, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${log.tag}: ${log.message}", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
