package com.example.studio

import androidx.test.core.app.ApplicationProvider
import com.example.studio.export.ApkExporter
import com.example.studio.model.GameProject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApkExporterTest {

    @Test
    fun testApkExporterBuildsPackage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val exporter = ApkExporter(context)
        val project = GameProject(name = "TestPlatformer")

        val result = exporter.exportGamePackage(project, "TestPlatformer")
        assertTrue(result.success)
        assertTrue(result.outputFilePath.contains("TestPlatformer.apk"))
    }
}
