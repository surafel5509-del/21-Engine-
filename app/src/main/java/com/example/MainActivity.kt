package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.engine.core.EngineConfig
import com.example.engine.core.GameEngine
import com.example.engine.core.GameSurfaceView
import com.example.engine.viewport.ScaleMode
import com.example.game.scenes.PhysicsSandboxScene

class MainActivity : ComponentActivity() {

    private lateinit var gameEngine: GameEngine
    private lateinit var gameSurfaceView: GameSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configure Game Engine
        val config = EngineConfig(
            logicalWidth = 1280f,
            logicalHeight = 720f,
            targetFps = 60,
            scaleMode = ScaleMode.FIT_LETTERBOX,
            isDebugMode = true,
            showFps = true,
            showSafeAreas = false
        )

        // 2. Instantiate GameEngine & GameSurfaceView
        gameEngine = GameEngine(this, config)
        gameSurfaceView = GameSurfaceView(this)
        gameEngine.attachView(gameSurfaceView)

        // 3. Set Initial Scene (Physics Sandbox)
        gameEngine.setScene(PhysicsSandboxScene())

        // 4. Set Content View
        setContentView(gameSurfaceView)

        // 5. Enable immersive full-screen display after view hierarchy is initialized
        hideSystemUI()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        gameEngine.resume()
        gameEngine.start()
    }

    override fun onPause() {
        super.onPause()
        gameEngine.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameEngine.destroy()
    }

    private fun hideSystemUI() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
