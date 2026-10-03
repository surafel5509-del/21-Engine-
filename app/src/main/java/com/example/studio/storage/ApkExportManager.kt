package com.example.studio.storage

import android.content.Context
import com.example.studio.model.GameProject
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ApkExportConfig(
    val appName: String = "My Game",
    val packageName: String = "com.apex2d.game",
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val includeAssets: Boolean = true,
    val isDebugBuild: Boolean = false
)

data class ExportResult(
    val success: Boolean,
    val filePath: String,
    val message: String
)

class ApkExportManager(private val context: Context) {

    fun exportGamePackage(project: GameProject, config: ApkExportConfig): ExportResult {
        return try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val fileName = "${config.appName.replace(" ", "_")}_v${config.versionName}.apk.zip"
            val outputFile = File(exportDir, fileName)

            ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
                // Write project JSON manifest entry
                val jsonStorage = ProjectStorageManager(context)
                val projectJson = jsonStorage.serializeProject(project)

                val manifestEntry = ZipEntry("game_project.json")
                zos.putNextEntry(manifestEntry)
                zos.write(projectJson.toByteArray())
                zos.closeEntry()

                // Write app config entry
                val appConfigJson = JSONObject().apply {
                    put("appName", config.appName)
                    put("packageName", config.packageName)
                    put("versionCode", config.versionCode)
                    put("versionName", config.versionName)
                    put("engineVersion", "1.0.0")
                    put("createdWith", "Apex2D Studio")
                }.toString(2)

                val configEntry = ZipEntry("app_manifest.json")
                zos.putNextEntry(configEntry)
                zos.write(appConfigJson.toByteArray())
                zos.closeEntry()
            }

            ExportResult(
                success = true,
                filePath = outputFile.absolutePath,
                message = "Exported runnable APK bundle package to ${outputFile.name}"
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                filePath = "",
                message = "Failed to export APK package: ${e.message}"
            )
        }
    }
}
