package com.example.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun StudioTopAppBar(
    project: GameProject,
    isPlayMode: Boolean,
    onTogglePlayMode: () -> Unit,
    onResetPlay: () -> Unit,
    onSaveProject: () -> Unit,
    onToggleGridSnap: () -> Unit,
    onExportApk: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF0F172A),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Project Name & Engine Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0284C7), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = "Godot 2D Engine",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        project.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        if (isPlayMode) "🟢 LIVE PLAY MODE (60 FPS)" else "✏️ 2D SCENE EDITOR",
                        color = if (isPlayMode) Color(0xFF22C55E) else Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }

            // Central Control / Play & Stop Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grid Snap Toggle (In Editor)
                if (!isPlayMode) {
                    IconButton(
                        onClick = onToggleGridSnap,
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (project.gridSnap) Color(0xFF1E293B) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = "Toggle Grid Snap",
                            tint = if (project.gridSnap) Color(0xFF38BDF8) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Save Project Button
                IconButton(
                    onClick = onSaveProject,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Save Project",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Export APK Button
                IconButton(
                    onClick = onExportApk,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = "Export APK",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset Button (In Play Mode)
                if (isPlayMode) {
                    Button(
                        onClick = onResetPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restart", fontSize = 12.sp)
                    }
                }

                // PLAY / STOP Mode Switcher
                Button(
                    onClick = onTogglePlayMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlayMode) Color(0xFFEF4444) else Color(0xFF22C55E)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        if (isPlayMode) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlayMode) "Stop" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isPlayMode) "STOP" else "RUN (F5)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
