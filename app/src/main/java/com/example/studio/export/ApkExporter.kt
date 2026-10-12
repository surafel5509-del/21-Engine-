package com.example.studio.export

import android.content.Context
import com.example.engine.debug.DebugConsoleLog
import com.example.studio.model.GameProject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Packaging manager that serializes game projects into standalone standalone game zip archives or APK export packages.
 */
object ApkExporter {

    fun exportProjectPackage(context: Context, project: GameProject): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(exportDir, "${project.name.replace(" ", "_")}_Export.apk")

        try {
            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                // Write project metadata manifest
                val manifestContent = """
                    {
                        "packageName": "com.game.${project.name.lowercase().replace(" ", "")}",
                        "versionCode": 1,
                        "versionName": "1.0",
                        "nodesCount": ${project.nodes.size},
                        "orientation": "landscape"
                    }
                """.trimIndent()

                val manifestEntry = ZipEntry("assets/game_manifest.json")
                zipOut.putNextEntry(manifestEntry)
                zipOut.write(manifestContent.toByteArray())
                zipOut.closeEntry()

                // Write main scene logic
                val sceneDataEntry = ZipEntry("assets/main_scene.json")
                zipOut.putNextEntry(sceneDataEntry)
                zipOut.write(project.selectedNodeId.toByteArray())
                zipOut.closeEntry()
            }
            DebugConsoleLog.info("ApkExporter", "Successfully exported game package to ${outputFile.absolutePath}")
        } catch (e: Exception) {
            DebugConsoleLog.error("ApkExporter", "Failed to export APK package: ${e.message}")
        }

        return outputFile
    }
}
