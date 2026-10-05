package com.example.studio.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.studio.model.GameProject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ApkExportManagerTest {

    @Test
    fun testApkExportManagerGeneratesBundle() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = ApkExportManager(context)
        val project = GameProject(name = "ExportTestGame")

        val config = ApkExportConfig(
            appName = "ExportTestGame",
            packageName = "com.godotstudio.exporttestgame"
        )

        var progressUpdatesCount = 0
        val result = manager.exportProjectToApk(project, config) { prog, status ->
            progressUpdatesCount++
            assertTrue(prog in 0f..1f)
            assertTrue(status.isNotEmpty())
        }

        assertTrue(result.success)
        assertNotNull(result.apkFile)
        assertTrue(result.apkFile!!.exists())
        assertTrue(progressUpdatesCount >= 3)
    }
}
