package com.example.engine.core

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.assets.AssetManager
import com.example.engine.audio.AudioManager
import com.example.engine.input.InputManager
import com.example.engine.rendering.Canvas2DRenderer
import com.example.engine.rendering.Renderer
import com.example.engine.scene.Scene
import com.example.engine.scene.SceneManager
import com.example.engine.utils.FpsCounter
import com.example.engine.utils.Logger
import com.example.engine.viewport.ViewportManager

/**
 * Central GameEngine class coordinating rendering, scene management, viewport adaptation,
 * inputs, audio, and asset caches.
 */
class GameEngine(
    val context: Context,
    val config: EngineConfig = EngineConfig()
) {
    companion object {
        private const val TAG = "GameEngine"
    }

    val viewportManager = ViewportManager(
        logicalWidth = config.logicalWidth,
        logicalHeight = config.logicalHeight,
        scaleMode = config.scaleMode
    )

    val renderer = Canvas2DRenderer(viewportManager)
    val sceneManager = SceneManager(this)
    val inputManager = InputManager(viewportManager)
    val audioManager = AudioManager(context)
    val assetManager = AssetManager(context)
    val fpsCounter = FpsCounter()

    private val gameLoop = GameLoop(this)

    var gameView: GameSurfaceView? = null
    var surfaceReady = false
        internal set

    init {
        inputManager.addTouchListener { event ->
            sceneManager.handleTouchEvent(event)
        }
        viewportManager.addResizeListener { w, h ->
            sceneManager.onResize(w, h)
        }
        Logger.i(TAG, "GameEngine initialized with logical resolution: ${config.logicalWidth}x${config.logicalHeight}")
    }

    fun attachView(view: GameSurfaceView) {
        this.gameView = view
        view.engine = this
    }

    fun start() {
        gameLoop.start()
        audioManager.resumeAll()
    }

    fun pause() {
        gameLoop.pause()
        audioManager.pauseAll()
        sceneManager.onPause()
    }

    fun resume() {
        gameLoop.resume()
        audioManager.resumeAll()
        sceneManager.onResume()
    }

    fun stop() {
        gameLoop.stop()
        audioManager.pauseAll()
    }

    fun destroy() {
        stop()
        sceneManager.onDestroy()
        audioManager.release()
        assetManager.clear()
        Logger.i(TAG, "GameEngine destroyed and resources released")
    }

    fun setScene(scene: Scene) {
        sceneManager.setScene(scene)
    }

    internal fun update(dt: Float) {
        fpsCounter.onFrame(dt)
        sceneManager.update(dt)
    }

    internal fun fixedUpdate(fixedDt: Float) {
        sceneManager.fixedUpdate(fixedDt)
    }

    internal fun render() {
        val view = gameView
        if (view != null) {
            view.drawFrame()
        } else {
            renderDirect()
        }
    }

    internal fun renderDirect() {
        if (!surfaceReady) return
        val canvas = renderer.canvas ?: return

        renderer.beginFrame()

        // 1. Clear background
        renderer.drawRect(0f, 0f, viewportManager.logicalWidth, viewportManager.logicalHeight, config.clearColor)

        // 2. Render active scene & UI
        sceneManager.render(renderer)

        // 3. Render Debug Overlays if enabled
        if (config.isDebugMode) {
            renderDebugOverlay(renderer)
        }

        renderer.endFrame()
    }

    private fun renderDebugOverlay(renderer: Renderer) {
        // FPS & Performance stats
        if (config.showFps) {
            val stats = "FPS: %.1f | %.2f ms | Objects: %d".format(
                fpsCounter.currentFps,
                fpsCounter.averageFrameTimeMs,
                sceneManager.currentScene?.gameObjectCount ?: 0
            )

            renderer.drawRect(
                x = 10f,
                y = 10f,
                width = renderer.measureTextWidth(stats, 18f, Typeface.MONOSPACE) + 16f,
                height = 28f,
                color = 0xAA000000.toInt(),
                style = Paint.Style.FILL,
                cornerRadius = 4f
            )

            renderer.drawText(
                text = stats,
                x = 18f,
                y = 30f,
                fontSize = 18f,
                color = 0xFF4CAF50.toInt(),
                align = Paint.Align.LEFT,
                typeface = Typeface.MONOSPACE
            )
        }

        // Safe Area outline
        if (config.showSafeAreas && !viewportManager.logicalSafeArea.isZero()) {
            val sa = viewportManager.logicalSafeArea
            renderer.drawRect(
                x = sa.left,
                y = sa.top,
                width = viewportManager.logicalWidth - sa.left - sa.right,
                height = viewportManager.logicalHeight - sa.top - sa.bottom,
                color = 0x88FFEB3B.toInt(),
                style = Paint.Style.STROKE,
                strokeWidth = 2f
            )
        }
    }
}
