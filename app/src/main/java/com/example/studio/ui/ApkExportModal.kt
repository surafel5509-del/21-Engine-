package com.example.studio.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import com.example.studio.export.ApkExportConfig
import com.example.studio.export.ApkExportManager
import com.example.studio.model.GameProject
import kotlinx.coroutines.launch

@Composable
fun ApkExportModal(
    project: GameProject,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val exportManager = remember { ApkExportManager(context) }

    var config by remember {
        mutableStateOf(
            ApkExportConfig(
                appName = project.name,
                packageName = "com.godotstudio.${project.name.replace(" ", "_").lowercase()}"
            )
        )
    }

    var isBuilding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var exportSuccessMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isBuilding) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Android APK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (exportSuccessMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🎉 Export Successful!", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(exportSuccessMsg!!, color = Color.White, fontSize = 11.sp)
                        }
                    }
                } else if (isBuilding) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(statusText, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    OutlinedTextField(
                        value = config.appName,
                        onValueChange = { config = config.copy(appName = it) },
                        label = { Text("Game App Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = config.packageName,
                        onValueChange = { config = config.copy(packageName = it) },
                        label = { Text("Package Name (ID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = config.versionName,
                            onValueChange = { config = config.copy(versionName = it) },
                            label = { Text("Version") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Orientation chip
                        Button(
                            onClick = {
                                config = config.copy(orientation = if (config.orientation == "LANDSCAPE") "PORTRAIT" else "LANDSCAPE")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp).align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                if (config.orientation == "LANDSCAPE") Icons.Default.StayCurrentLandscape else Icons.Default.StayCurrentPortrait,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(config.orientation, fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (exportSuccessMsg != null) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Done")
                }
            } else if (!isBuilding) {
                Button(
                    onClick = {
                        isBuilding = true
                        coroutineScope.launch {
                            val result = exportManager.exportProjectToApk(project, config) { prog, status ->
                                progress = prog
                                statusText = status
                            }
                            isBuilding = false
                            if (result.success) {
                                exportSuccessMsg = result.message
                            } else {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Build APK Now")
                }
            }
        },
        dismissButton = {
            if (!isBuilding && exportSuccessMsg == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
