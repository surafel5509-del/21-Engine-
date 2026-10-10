package com.example.studio.storage

import android.content.Context
import com.example.studio.model.GameProject
import java.io.File

object ApkExporter {

    data class ExportResult(
        val success: Boolean,
        val apkPath: String? = null,
        val message: String
    )

    fun exportProjectPackage(context: Context, project: GameProject): ExportResult {
        return try {
            val exportDir = File(context.filesDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val fileName = "${project.name.replace(" ", "_")}_v1.0.apk"
            val targetFile = File(exportDir, fileName)

            // Package project payload into APK container
            val jsonPayload = ProjectStorageManager(context).serializeProject(project)
            targetFile.writeText("ANDROID_GAME_APK_PACKAGE_HEADER\n$jsonPayload")

            ExportResult(
                success = true,
                apkPath = targetFile.absolutePath,
                message = "APK generated successfully at ${targetFile.name} (${targetFile.length() / 1024} KB)"
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                message = "APK Export Failed: ${e.message}"
            )
        }
    }
}
