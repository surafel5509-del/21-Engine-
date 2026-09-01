package com.example.game.scenes

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.components.ParticleEmitterComponent
import com.example.engine.components.ShapeRendererComponent
import com.example.engine.components.ShapeType
import com.example.engine.core.Time
import com.example.engine.math.MathUtils
import com.example.engine.math.Vector2
import com.example.engine.rendering.Renderer
import com.example.engine.scene.GameObject
import com.example.engine.scene.Scene
import com.example.engine.ui.*
import com.example.engine.viewport.ScaleMode

/**
 * Scene 3: Device Adaptation, Scale Modes & Performance Benchmark.
 * Demonstrates:
 * - Real-time switching between ScaleModes (Fit Letterbox, Fill Crop, Stretch, Extend Viewport, Integer Scale).
 * - Visual Anchor Verification testing all 9 responsive layout anchors with Safe-Area insets.
 * - Hierarchical Parent-Child Matrix Transform stress test (nested orbiting celestial bodies).
 * - Massive Particle Performance test (1000+ active pooled particles at 60 FPS).
 * - Real-time TimeScale controller (0.1x Slow-Mo to 3.0x Hyper-Speed).
 */
class StressBenchmarkScene : Scene() {

    private lateinit var particleEmitter1: ParticleEmitterComponent
    private lateinit var particleEmitter2: ParticleEmitterComponent
    private lateinit var scaleModeLabel: UILabel
    private lateinit var sunObj: GameObject

    private var currentScaleModeIndex = 0
    private val scaleModes = listOf(
        ScaleMode.FIT_LETTERBOX,
        ScaleMode.FILL_CROP,
        ScaleMode.STRETCH,
        ScaleMode.EXTEND_VIEWPORT,
        ScaleMode.INTEGER_SCALE
    )

    override fun onCreate() {
        camera.setPosition(640f, 360f)

        // 1. Build Hierarchical Solar System (testing parent-child transform matrices & dirty flags)
        buildSolarSystem()

        // 2. High Density Particle Emitters
        val emitterObj1 = GameObject("EmitterLeft").setPosition(400f, 360f)
        particleEmitter1 = emitterObj1.addComponent(
            ParticleEmitterComponent(
                maxParticles = 500,
                emissionRate = 120f,
                speedMin = 80f,
                speedMax = 260f,
                startColor = 0xFF00E5FF.toInt(),
                endColor = 0x007C4DFF.toInt(),
                startSize = 12f,
                endSize = 2f,
                gravity = Vector2(0f, 80f)
            )
        )
        addGameObject(emitterObj1)

        val emitterObj2 = GameObject("EmitterRight").setPosition(880f, 360f)
        particleEmitter2 = emitterObj2.addComponent(
            ParticleEmitterComponent(
                maxParticles = 500,
                emissionRate = 120f,
                speedMin = 80f,
                speedMax = 260f,
                startColor = 0xFFFF4081.toInt(),
                endColor = 0x00FFD740.toInt(),
                startSize = 12f,
                endSize = 2f,
                gravity = Vector2(0f, -80f)
            )
        )
        addGameObject(emitterObj2)

        // 3. Build UI Controls & Responsive Anchor Markers
        setupUI()

        engine?.let { eng ->
            com.example.game.ui.GlobalHUD.attachTo(uiCanvas, eng, currentSceneIndex = 3)
        }
    }

    private fun buildSolarSystem() {
        // Sun
        sunObj = GameObject("Sun").setPosition(640f, 360f)
        sunObj.addComponent(ShapeRendererComponent(ShapeType.CIRCLE, radius = 35f, color = 0xFFFFD600.toInt()))
        addGameObject(sunObj)

        // Planet 1 (Child of Sun)
        val planet1 = GameObject("Planet1").setPosition(120f, 0f)
        planet1.addComponent(ShapeRendererComponent(ShapeType.CIRCLE, radius = 14f, color = 0xFF00E676.toInt()))
        sunObj.addChild(planet1)

        // Moon 1 (Child of Planet 1)
        val moon1 = GameObject("Moon1").setPosition(28f, 0f)
        moon1.addComponent(ShapeRendererComponent(ShapeType.CIRCLE, radius = 6f, color = 0xFFECEFF1.toInt()))
        planet1.addChild(moon1)

        // Planet 2 (Child of Sun)
        val planet2 = GameObject("Planet2").setPosition(220f, 0f)
        planet2.addComponent(ShapeRendererComponent(ShapeType.CIRCLE, radius = 18f, color = 0xFFFF5722.toInt()))
        sunObj.addChild(planet2)

        // Moon 2 (Child of Planet 2)
        val moon2 = GameObject("Moon2").setPosition(34f, 0f)
        moon2.addComponent(ShapeRendererComponent(ShapeType.CIRCLE, radius = 7f, color = 0xFFB0BEC5.toInt()))
        planet2.addChild(moon2)
    }

    private fun setupUI() {
        // 1. Anchor Markers on all 9 positions to visually verify screen adaptation
        for (anchor in Anchor.values()) {
            val marker = UIPanel(width = 90f, height = 30f).apply {
                setAnchor(anchor, offsetX = 0f, offsetY = 0f)
                backgroundColor = 0x881E293B.toInt()
                borderColor = 0xAA64748B.toInt()
                cornerRadius = 4f
            }
            val label = UILabel(anchor.name.replace("_", " "), fontSize = 11f, color = 0xFFE2E8F0.toInt(), align = Paint.Align.CENTER).apply {
                setAnchor(Anchor.CENTER)
            }
            marker.addChild(label)
            uiCanvas.addElement(marker)
        }

        // 2. Control Panel
        val panel = UIPanel(width = 460f, height = 150f).apply {
            setAnchor(Anchor.BOTTOM_CENTER, offsetY = -40f)
            backgroundColor = 0xEE0F172A.toInt()
            borderColor = 0xFF334155.toInt()
            cornerRadius = 14f
        }
        uiCanvas.addElement(panel)

        // ScaleMode Switcher
        scaleModeLabel = UILabel("ScaleMode: FIT_LETTERBOX", fontSize = 16f, color = 0xFF38BDF8.toInt(), align = Paint.Align.CENTER).apply {
            setAnchor(Anchor.TOP_CENTER, offsetY = 12f)
        }
        panel.addChild(scaleModeLabel)

        val btnNextMode = UIButton("Next Scale Mode ➔", width = 200f, height = 36f).apply {
            setAnchor(Anchor.TOP_CENTER, offsetY = 38f)
            normalBackgroundColor = 0xFF0284C7.toInt()
            fontSize = 14f
            onClick = {
                val eng = engine
                if (eng != null) {
                    currentScaleModeIndex = (currentScaleModeIndex + 1) % scaleModes.size
                    val newMode = scaleModes[currentScaleModeIndex]
                    eng.viewportManager.scaleMode = newMode
                    scaleModeLabel.text = "ScaleMode: ${newMode.name}"
                    eng.audioManager.playSynthSound("click")
                }
            }
        }
        panel.addChild(btnNextMode)

        // TimeScale Slider
        val timeScaleLabel = UILabel("Time Scale: 1.0x", fontSize = 14f, color = 0xFFE2E8F0.toInt()).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 20f, offsetY = -42f)
        }
        panel.addChild(timeScaleLabel)

        val sliderTimeScale = UISlider(width = 420f, height = 24f, minValue = 0.1f, maxValue = 3.0f, initialValue = 1.0f).apply {
            setAnchor(Anchor.BOTTOM_CENTER, offsetY = -14f)
            onValueChanged = { v ->
                Time.timeScale = v
                timeScaleLabel.text = "Time Scale: %.2fx".format(v)
            }
        }
        panel.addChild(sliderTimeScale)
    }

    override fun onUpdate(dt: Float) {
        super.onUpdate(dt)

        // Orbit celestial bodies
        sunObj.transform.rotationDegrees += dt * 30f
        sunObj.transform.children.forEachIndexed { index, planet ->
            planet.rotationDegrees += dt * (60f + index * 40f)
            planet.children.forEach { moon ->
                moon.rotationDegrees += dt * 140f
            }
        }
    }
}
