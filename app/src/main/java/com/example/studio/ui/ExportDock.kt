package com.example.studio.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studio.export.ApkExporter
import com.example.studio.model.GameProject

@Composable
fun ExportDock(
    project: GameProject,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportStatusText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Export Game to Standalone APK",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Bundle '${project.name}' into an installable mobile Android app package.",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                isExporting = true
                val exportFile = ApkExporter.exportProjectPackage(context, project)
                isExporting = false
                exportStatusText = "Exported to: ${exportFile.name}"
                Toast.makeText(context, "Game exported successfully!", Toast.LENGTH_LONG).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Build & Export APK", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (exportStatusText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(exportStatusText, color = Color(0xFF38BDF8), fontSize = 12.sp)
        }
    }
}
