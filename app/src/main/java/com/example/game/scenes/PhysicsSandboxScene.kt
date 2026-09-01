package com.example.game.scenes

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.camera.Camera2D
import com.example.engine.components.*
import com.example.engine.core.Time
import com.example.engine.input.TouchEvent
import com.example.engine.input.TouchType
import com.example.engine.math.MathUtils
import com.example.engine.math.Vector2
import com.example.engine.physics.BodyType
import com.example.engine.physics.CollisionLayers
import com.example.engine.rendering.Renderer
import com.example.engine.scene.GameObject
import com.example.engine.scene.Scene
import com.example.engine.ui.*

/**
 * Scene 1: Physics Sandbox & Collision Playground.
 * Demonstrates:
 * - Dynamic rigid bodies with gravity, mass, restitution, friction.
 * - Box-Box, Circle-Circle, and Box-Circle narrowphase collisions.
 * - Interactive multi-touch dragging and throwing.
 * - Real-time raycasting visualizer with hit normals.
 * - Particle explosions on collision impacts.
 * - Responsive UI controls with gravity slider, spawn buttons, and camera shake.
 */
class PhysicsSandboxScene : Scene() {

    private var draggedObject: GameObject? = null
    private var draggedBody: RigidBody2DComponent? = null
    private var dragTouchId: Int? = null
    private val lastTouchPos = Vector2()
    private val touchVelocity = Vector2()

    private val rayOrigin = Vector2(640f, 100f)
    private val rayDir = Vector2(0f, 1f)
    private var rayAngle = 0f

    private lateinit var particleExploder: ParticleEmitterComponent

    override fun onCreate() {
        camera.setPosition(640f, 360f)
        physicsWorld.gravity.set(0f, 980f)

        // 1. Create static floor and walls
        createStaticBoundary(x = 640f, y = 700f, width = 1200f, height = 40f, color = 0xFF37474F.toInt()) // Floor
        createStaticBoundary(x = 60f, y = 360f, width = 40f, height = 680f, color = 0xFF37474F.toInt())  // Left Wall
        createStaticBoundary(x = 1220f, y = 360f, width = 40f, height = 680f, color = 0xFF37474F.toInt()) // Right Wall
        createStaticBoundary(x = 400f, y = 450f, width = 300f, height = 24f, color = 0xFF455A64.toInt()) // Inclined platform
        createStaticBoundary(x = 880f, y = 380f, width = 280f, height = 24f, color = 0xFF455A64.toInt()) // High platform

        // 2. Spawn initial bouncy balls & boxes
        for (i in 0 until 6) {
            spawnBouncyBall(300f + i * 80f, 150f + (i % 2) * 50f, radius = 22f)
        }
        for (i in 0 until 5) {
            spawnBox(500f + i * 60f, 200f, size = 44f)
        }

        // 3. Particle Emitter for impacts
        val emitterObj = GameObject("ParticleExploder")
        particleExploder = emitterObj.addComponent(
            ParticleEmitterComponent(
                maxParticles = 300,
                isEmitting = false,
                startColor = 0xFFFFC107.toInt(),
                endColor = 0x00FF5722.toInt(),
                startSize = 14f,
                endSize = 2f,
                speedMin = 60f,
                speedMax = 220f
            )
        )
        addGameObject(emitterObj)

        // 4. Build Responsive HUD Controls
        setupUI()

        engine?.let { eng ->
            com.example.game.ui.GlobalHUD.attachTo(uiCanvas, eng, currentSceneIndex = 1)
        }
    }

    private fun setupUI() {
        // Control Panel
        val panel = UIPanel(width = 380f, height = 140f).apply {
            setAnchor(Anchor.BOTTOM_CENTER, offsetY = -20f)
            backgroundColor = 0xDD111827.toInt()
            borderColor = 0xFF374151.toInt()
            cornerRadius = 12f
        }
        uiCanvas.addElement(panel)

        // Gravity Label & Slider
        val gravLabel = UILabel("Gravity: 980 px/s²", fontSize = 16f, color = 0xFFE2E8F0.toInt()).apply {
            setAnchor(Anchor.TOP_LEFT, offsetX = 16f, offsetY = 12f)
        }
        panel.addChild(gravLabel)

        val gravSlider = UISlider(width = 348f, height = 28f, minValue = 0f, maxValue = 2000f, initialValue = 980f).apply {
            setAnchor(Anchor.TOP_LEFT, offsetX = 16f, offsetY = 36f)
            onValueChanged = { g ->
                physicsWorld.gravity.set(0f, g)
                gravLabel.text = "Gravity: ${g.toInt()} px/s²"
            }
        }
        panel.addChild(gravSlider)

        // Spawn Ball Button
        val btnSpawnBall = UIButton("+ Ball", width = 80f, height = 36f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 16f, offsetY = -12f)
            normalBackgroundColor = 0xFF0284C7.toInt()
            fontSize = 15f
            onClick = {
                spawnBouncyBall(
                    x = MathUtils.randomRange(200f, 1000f),
                    y = 100f,
                    radius = MathUtils.randomRange(16f, 32f)
                )
                engine?.audioManager?.playSynthSound("click")
            }
        }
        panel.addChild(btnSpawnBall)

        // Spawn Box Button
        val btnSpawnBox = UIButton("+ Box", width = 80f, height = 36f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 104f, offsetY = -12f)
            normalBackgroundColor = 0xFFD97706.toInt()
            fontSize = 15f
            onClick = {
                spawnBox(
                    x = MathUtils.randomRange(200f, 1000f),
                    y = 100f,
                    size = MathUtils.randomRange(30f, 50f)
                )
                engine?.audioManager?.playSynthSound("click")
            }
        }
        panel.addChild(btnSpawnBox)

        // Camera Shake Button
        val btnShake = UIButton("⚡ Shake", width = 80f, height = 36f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 192f, offsetY = -12f)
            normalBackgroundColor = 0xFFDC2626.toInt()
            fontSize = 15f
            onClick = {
                camera.shake(0.6f)
                particleExploder.burst(40, Vector2(640f, 360f))
                engine?.audioManager?.playSynthSound("explosion")
            }
        }
        panel.addChild(btnShake)

        // Reset Scene Button
        val btnReset = UIButton("↺ Reset", width = 80f, height = 36f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 280f, offsetY = -12f)
            normalBackgroundColor = 0xFF4B5563.toInt()
            fontSize = 15f
            onClick = {
                engine?.setScene(PhysicsSandboxScene())
            }
        }
        panel.addChild(btnReset)

        // Info Banner
        val infoLabel = UILabel("Drag & Throw objects with touch • Laser scans colliders in real time", fontSize = 16f, color = 0xAA94A3B8.toInt(), align = Paint.Align.CENTER).apply {
            setAnchor(Anchor.TOP_CENTER, offsetY = 50f)
        }
        uiCanvas.addElement(infoLabel)
    }

    private fun createStaticBoundary(x: Float, y: Float, width: Float, height: Float, color: Int): GameObject {
        val obj = GameObject("Wall").setPosition(x, y)
        obj.addComponent(ShapeRendererComponent(
            shapeType = ShapeType.ROUNDED_RECTANGLE,
            width = width,
            height = height,
            color = color,
            cornerRadius = 6f
        ))
        obj.addComponent(RigidBody2DComponent(bodyType = BodyType.STATIC, restitution = 0.4f, friction = 0.3f))
        obj.addComponent(BoxCollider2D(width = width, height = height))
        return addGameObject(obj)
    }

    private fun spawnBouncyBall(x: Float, y: Float, radius: Float): GameObject {
        val obj = GameObject("Ball").setPosition(x, y)
        val colors = intArrayOf(0xFF00E676.toInt(), 0xFF00B0FF.toInt(), 0xFFFF5252.toInt(), 0xFFFFD740.toInt(), 0xFFE040FB.toInt())
        val col = colors[(Math.random() * colors.size).toInt()]

        obj.addComponent(ShapeRendererComponent(
            shapeType = ShapeType.CIRCLE,
            radius = radius,
            color = col,
            style = Paint.Style.FILL
        ))
        val body = obj.addComponent(RigidBody2DComponent(
            bodyType = BodyType.DYNAMIC,
            mass = radius * 0.1f,
            restitution = 0.85f,
            friction = 0.1f
        ))
        val collider = obj.addComponent(CircleCollider2D(radius = radius))

        collider.onCollisionEnter = { other, manifold ->
            if (body.velocity.length() > 150f) {
                particleExploder.burst(8, manifold.contactPoint)
                engine?.audioManager?.playSynthSound("click", 0.3f)
            }
        }

        return addGameObject(obj)
    }

    private fun spawnBox(x: Float, y: Float, size: Float): GameObject {
        val obj = GameObject("Box").setPosition(x, y)
        val colors = intArrayOf(0xFFFF9100.toInt(), 0xFFFF3D00.toInt(), 0xFF7C4DFF.toInt(), 0xFF00E5FF.toInt())
        val col = colors[(Math.random() * colors.size).toInt()]

        obj.addComponent(ShapeRendererComponent(
            shapeType = ShapeType.ROUNDED_RECTANGLE,
            width = size,
            height = size,
            color = col,
            cornerRadius = 4f
        ))
        val body = obj.addComponent(RigidBody2DComponent(
            bodyType = BodyType.DYNAMIC,
            mass = size * 0.15f,
            restitution = 0.5f,
            friction = 0.4f
        ))
        val collider = obj.addComponent(BoxCollider2D(width = size, height = size))

        collider.onCollisionEnter = { other, manifold ->
            if (body.velocity.length() > 180f) {
                particleExploder.burst(10, manifold.contactPoint)
                engine?.audioManager?.playSynthSound("click", 0.4f)
            }
        }

        return addGameObject(obj)
    }

    override fun onUpdate(dt: Float) {
        super.onUpdate(dt)

        // Rotate laser scanning beam
        rayAngle += dt * 45f
        val rad = MathUtils.degToRad(rayAngle)
        rayDir.set(kotlin.math.cos(rad), kotlin.math.sin(rad))

        // Dragged object physics update
        val body = draggedBody
        val obj = draggedObject
        if (body != null && obj != null) {
            body.velocity.set(touchVelocity)
        }
    }

    override fun onRender(renderer: Renderer) {
        super.onRender(renderer)

        // Draw real-time raycast laser scanner
        renderer.beginCamera(camera)
        val hit = physicsWorld.raycast(rayOrigin, rayDir, maxDistance = 900f)

        if (hit != null) {
            // Laser beam to hit point
            renderer.drawLine(rayOrigin, hit.point, 0xCCFF1744.toInt(), strokeWidth = 3f)
            // Hit point circle
            renderer.drawCircle(hit.point.x, hit.point.y, 6f, 0xFFFF5252.toInt(), Paint.Style.FILL)
            // Normal reflection vector
            renderer.drawLine(
                hit.point.x,
                hit.point.y,
                hit.point.x + hit.normal.x * 25f,
                hit.point.y + hit.normal.y * 25f,
                0xFF00E5FF.toInt(),
                strokeWidth = 2.5f
            )
        } else {
            // Laser beam to max distance
            val endPoint = Vector2()
            rayDir.copy().scale(900f).add(rayOrigin).let { endPoint.set(it) }
            renderer.drawLine(rayOrigin, endPoint, 0x55FF1744.toInt(), strokeWidth = 2f)
        }

        // Ray emitter hub
        renderer.drawCircle(rayOrigin.x, rayOrigin.y, 10f, 0xFFE0E0E0.toInt(), Paint.Style.FILL)
        renderer.drawCircle(rayOrigin.x, rayOrigin.y, 10f, 0xFFFF1744.toInt(), Paint.Style.STROKE, 2f)

        renderer.endCamera()
    }

    override fun onTouch(event: TouchEvent): Boolean {
        if (super.onTouch(event)) return true

        when (event.type) {
            TouchType.DOWN -> {
                if (dragTouchId == null) {
                    val hit = physicsWorld.raycast(event.worldPosition, Vector2(0f, 1f), maxDistance = 5f)
                        ?: physicsWorld.raycast(event.worldPosition, Vector2(1f, 0f), maxDistance = 5f)

                    if (hit != null && hit.collider.gameObject?.getComponent<RigidBody2DComponent>()?.bodyType == BodyType.DYNAMIC) {
                        draggedObject = hit.collider.gameObject
                        draggedBody = hit.collider.gameObject?.getComponent()
                        dragTouchId = event.pointerId
                        lastTouchPos.set(event.worldPosition)
                        touchVelocity.set(0f, 0f)
                        return true
                    }
                }
            }
            TouchType.MOVE -> {
                if (event.pointerId == dragTouchId) {
                    val obj = draggedObject
                    if (obj != null) {
                        val dx = event.worldPosition.x - lastTouchPos.x
                        val dy = event.worldPosition.y - lastTouchPos.y
                        obj.transform.position.add(dx, dy)
                        obj.transform.markDirty()

                        val dt = Time.unscaledDeltaTime.coerceAtLeast(0.001f)
                        touchVelocity.set(dx / dt, dy / dt)
                        lastTouchPos.set(event.worldPosition)
                    }
                    return true
                }
            }
            TouchType.UP, TouchType.CANCEL -> {
                if (event.pointerId == dragTouchId) {
                    draggedBody?.velocity?.set(touchVelocity)
                    draggedObject = null
                    draggedBody = null
                    dragTouchId = null
                    return true
                }
            }
        }
        return false
    }
}
