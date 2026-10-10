package com.example.studio.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.model.GameProject
import com.example.studio.storage.ApkExporter
import com.example.studio.storage.ProjectStorageManager

@Composable
fun ProjectManagerDock(
    project: GameProject,
    onProjectUpdated: (GameProject) -> Unit,
    storageManager: ProjectStorageManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nameText by remember(project.id) { mutableStateOf(project.name) }
    var gravityYText by remember(project.id) { mutableStateOf(project.gravityY.toString()) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        Text("Project Settings & Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // PROJECT METADATA CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Project Metadata", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        project.name = it
                        onProjectUpdated(project)
                    },
                    label = { Text("Game Title", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = gravityYText,
                    onValueChange = {
                        gravityYText = it
                        it.toFloatOrNull()?.let { gY ->
                            project.gravityY = gY
                            onProjectUpdated(project)
                        }
                    },
                    label = { Text("Gravity Y (px/s²)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // APK EXPORT CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Android APK Export", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Compile and bundle your 2D game directly into a standalone Android APK file.",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val result = ApkExporter.exportProjectPackage(context, project)
                        exportStatusMessage = result.message
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Standalone APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                exportStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(msg, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SAVE & RESET ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    storageManager.saveActiveProject(project)
                    Toast.makeText(context, "Project Saved!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Project", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    val newProj = GameProject(name = "New 2D Game")
                    onProjectUpdated(newProj)
                    storageManager.saveActiveProject(newProj)
                    Toast.makeText(context, "Created New Project!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Project", fontSize = 11.sp)
            }
        }
    }
}
