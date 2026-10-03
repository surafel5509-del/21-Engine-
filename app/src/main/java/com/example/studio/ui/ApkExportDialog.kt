package com.example.studio.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameProject
import com.example.studio.storage.ApkExportConfig
import com.example.studio.storage.ApkExportManager

@Composable
fun ApkExportDialog(
    project: GameProject,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val exportManager = remember { ApkExportManager(context) }

    var appName by remember { mutableStateOf(project.name) }
    var packageName by remember { mutableStateOf("com.apex2d.${project.name.lowercase().replace(" ", "")}") }
    var versionName by remember { mutableStateOf("1.0.0") }
    var isBuilding by remember { mutableStateOf(false) }
    var buildStatusMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export APK Game Package", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Build a standalone APK package directly on Android device:", color = Color(0xFF94A3B8), fontSize = 12.sp)

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Game Title", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package ID (e.g. com.mycompany.game)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = versionName,
                    onValueChange = { versionName = it },
                    label = { Text("Version Name", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isBuilding) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(buildStatusMessage, color = Color(0xFF38BDF8), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isBuilding = true
                    buildStatusMessage = "Bundling engine assets and project JSON..."
                    val config = ApkExportConfig(
                        appName = appName,
                        packageName = packageName,
                        versionName = versionName
                    )
                    val res = exportManager.exportGamePackage(project, config)
                    isBuilding = false
                    if (res.success) {
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                enabled = !isBuilding && appName.isNotBlank()
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Build APK Package", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}
