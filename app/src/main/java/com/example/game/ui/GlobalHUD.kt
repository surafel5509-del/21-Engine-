package com.example.game.ui

import android.graphics.Color
import com.example.engine.core.GameEngine
import com.example.engine.scene.FadeTransition
import com.example.engine.ui.Anchor
import com.example.engine.ui.UIButton
import com.example.engine.ui.UICanvas
import com.example.engine.ui.UIPanel
import com.example.game.scenes.PhysicsSandboxScene
import com.example.game.scenes.PlatformerScene
import com.example.game.scenes.StressBenchmarkScene

/**
 * Top persistent navigation toolbar allowing one-tap switching between sample scenes.
 */
object GlobalHUD {
    fun attachTo(canvas: UICanvas, engine: GameEngine, currentSceneIndex: Int) {
        val navPanel = UIPanel(width = 540f, height = 44f).apply {
            setAnchor(Anchor.TOP_CENTER, offsetY = 8f)
            backgroundColor = 0xDD0F172A.toInt()
            borderColor = 0x88334155.toInt()
            borderWidth = 1.5f
            cornerRadius = 22f
        }
        canvas.addElement(navPanel)

        val btnScene1 = UIButton("1. Physics", width = 120f, height = 34f).apply {
            setAnchor(Anchor.CENTER_LEFT, offsetX = 8f)
            normalBackgroundColor = if (currentSceneIndex == 1) 0xFF0284C7.toInt() else 0x441E293B.toInt()
            fontSize = 14f
            cornerRadius = 17f
            onClick = {
                if (currentSceneIndex != 1) {
                    engine.sceneManager.setScene(PhysicsSandboxScene(), FadeTransition(0.3f))
                    engine.audioManager.playSynthSound("click")
                }
            }
        }
        navPanel.addChild(btnScene1)

        val btnScene2 = UIButton("2. Platformer", width = 125f, height = 34f).apply {
            setAnchor(Anchor.CENTER_LEFT, offsetX = 136f)
            normalBackgroundColor = if (currentSceneIndex == 2) 0xFF0284C7.toInt() else 0x441E293B.toInt()
            fontSize = 14f
            cornerRadius = 17f
            onClick = {
                if (currentSceneIndex != 2) {
                    engine.sceneManager.setScene(PlatformerScene(), FadeTransition(0.3f))
                    engine.audioManager.playSynthSound("click")
                }
            }
        }
        navPanel.addChild(btnScene2)

        val btnScene3 = UIButton("3. Benchmark", width = 125f, height = 34f).apply {
            setAnchor(Anchor.CENTER_LEFT, offsetX = 269f)
            normalBackgroundColor = if (currentSceneIndex == 3) 0xFF0284C7.toInt() else 0x441E293B.toInt()
            fontSize = 14f
            cornerRadius = 17f
            onClick = {
                if (currentSceneIndex != 3) {
                    engine.sceneManager.setScene(StressBenchmarkScene(), FadeTransition(0.3f))
                    engine.audioManager.playSynthSound("click")
                }
            }
        }
        navPanel.addChild(btnScene3)

        val btnDebug = UIButton("⚙ Debug", width = 105f, height = 34f).apply {
            setAnchor(Anchor.CENTER_RIGHT, offsetX = -8f)
            normalBackgroundColor = if (engine.config.showSafeAreas) 0xFF10B981.toInt() else 0x441E293B.toInt()
            fontSize = 13f
            cornerRadius = 17f
            onClick = {
                engine.config.showSafeAreas = !engine.config.showSafeAreas
                normalBackgroundColor = if (engine.config.showSafeAreas) 0xFF10B981.toInt() else 0x441E293B.toInt()
                engine.audioManager.playSynthSound("click")
            }
        }
        navPanel.addChild(btnDebug)
    }
}
