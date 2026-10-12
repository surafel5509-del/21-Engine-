package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.debug.ConsoleMessage
import com.example.engine.debug.DebugConsoleLog
import com.example.engine.debug.LogLevel

@Composable
fun ConsoleDock(
    modifier: Modifier = Modifier
) {
    var logs by remember { mutableStateOf(DebugConsoleLog.getLogs()) }

    DisposableEffect(Unit) {
        val listener: (List<ConsoleMessage>) -> Unit = { updated ->
            logs = updated
        }
        DebugConsoleLog.addListener(listener)
        onDispose {
            DebugConsoleLog.removeListener(listener)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Console Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                Text("Debug Output & Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = { DebugConsoleLog.clear() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.ClearAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", fontSize = 11.sp, color = Color.White)
            }
        }

        // Log Output List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFF020617), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Text("No logs recorded. System messages, prints, and runtime errors will appear here.", color = Color.Gray, fontSize = 12.sp)
                }
            }

            items(logs) { entry ->
                val levelColor = when (entry.level) {
                    LogLevel.INFO -> Color(0xFF38BDF8)
                    LogLevel.WARNING -> Color(0xFFEAB308)
                    LogLevel.ERROR -> Color(0xFFEF4444)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("[${entry.timestamp}]", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Text("[${entry.tag}]", color = levelColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(entry.message, color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}
