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

data class LogMessage(
    val level: String,
    val text: String,
    val time: String = "00:01"
)

@Composable
fun DebugConsoleDock(
    modifier: Modifier = Modifier
) {
    val logList = remember {
        mutableStateListOf(
            LogMessage("INFO", "Game Engine initialized in 60 FPS Letterbox mode"),
            LogMessage("INFO", "Godot Studio Viewport Ready"),
            LogMessage("DEBUG", "Loaded project with 12 active scene nodes"),
            LogMessage("WARN", "Physics body 'Enemy' fixedRotation is true"),
            LogMessage("INFO", "GdScriptEngine compilation completed: 0 errors")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        // Console Header & Metrics Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Debug Console & Profiler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            IconButton(
                onClick = { logList.clear() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear Logs", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Performance Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadge(label = "FPS", value = "60.0", color = Color(0xFF10B981))
            MetricBadge(label = "Draw Calls", value = "14", color = Color(0xFF38BDF8))
            MetricBadge(label = "Phys Entities", value = "8", color = Color(0xFFF59E0B))
            MetricBadge(label = "Memory", value = "18.4 MB", color = Color(0xFFA855F7))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Log Stream
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logList) { log ->
                val color = when (log.level) {
                    "ERROR" -> Color(0xFFEF4444)
                    "WARN" -> Color(0xFFF59E0B)
                    "DEBUG" -> Color(0xFF38BDF8)
                    else -> Color(0xFF10B981)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[${log.level}] ",
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = log.text,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$label: ", fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
