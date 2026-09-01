package com.example.studio.engine_bridge

import android.graphics.Paint
import android.graphics.Typeface
import com.example.engine.camera.Camera2D
import com.example.engine.components.*
import com.example.engine.input.TouchEvent
import com.example.engine.input.TouchType
import com.example.engine.math.Vector2
import com.example.engine.physics.BodyType
import com.example.engine.rendering.Renderer
import com.example.engine.scene.Component
import com.example.engine.scene.GameObject
import com.example.engine.scene.Scene
import com.example.engine.ui.Anchor
import com.example.engine.ui.UIButton
import com.example.engine.ui.UIJoystick
import com.example.engine.ui.UILabel
import com.example.engine.ui.UIPanel
import com.example.studio.model.*

class StudioRuntimeScene(
    var project: GameProject,
    var onNodeSelectedListener: ((GameNode?) -> Unit)? = null
) : Scene() {

    var isPlayMode: Boolean = false
        private set

    // Editor Viewport Navigation
    val editorCamera = Camera2D()
    private var isDraggingNode = false
    private val dragStartNodePos = Vector2()
    private val dragStartTouchWorld = Vector2()
    private val lastTouchScreenPos = Vector2()
    private val tempVec = Vector2()

    // Gameplay Runtime State
    private var playerObject: GameObject? = null
    private var playerBody: RigidBody2DComponent? = null
    private var isGrounded = false
    var score: Int = 0
    var health: Int = 3
    private var scoreLabel: UILabel? = null
    private var healthLabel: UILabel? = null
    private var virtualJoystick: UIJoystick? = null
    private var jumpButton: UIButton? = null
    private var shootButton: UIButton? = null

    private val playObjects = mutableListOf<GameObject>()

    override fun onCreate() {
        super.onCreate()
        camera.setBounds(-3000f, -3000f, 6000f, 6000f)
        editorCamera.viewportWidth = 1280f
        editorCamera.viewportHeight = 720f
        editorCamera.position.set(640f, 360f)
        editorCamera.zoom = 1.0f

        if (isPlayMode) {
            setupPlayMode()
        } else {
            setupEditorMode()
        }
    }

    fun enterPlayMode() {
        isPlayMode = true
        cleanupPlayMode()
        setupPlayMode()
    }

    fun enterEditorMode() {
        isPlayMode = false
        cleanupPlayMode()
        setupEditorMode()
    }

    fun resetPlayMode() {
        if (isPlayMode) {
            enterPlayMode()
        }
    }

    private fun cleanupPlayMode() {
        playObjects.forEach { removeGameObject(it) }
        playObjects.clear()
        uiCanvas.clear()
        playerObject = null
        playerBody = null
    }

    private fun setupEditorMode() {
        physicsWorld.gravity.set(0f, 0f)
        camera.target = null
        camera.position.set(editorCamera.position.x, editorCamera.position.y)
        camera.zoom = editorCamera.zoom
    }

    private fun setupPlayMode() {
        score = 0
        health = 3
        physicsWorld.gravity.set(project.gravityX, project.gravityY)

        // Build Engine GameObjects from Project Nodes
        val nodeToObjMap = mutableMapOf<String, GameObject>()

        project.nodes.filter { it.isVisible }.forEach { node ->
            val obj = GameObject(node.name)
            obj.transform.position.set(node.posX, node.posY)
            obj.transform.scale.set(node.scaleX, node.scaleY)
            obj.transform.rotationDegrees = node.rotation

            // Visual Renderer
            val nodeColor = node.color.toInt()
            val nodeAlpha = node.alpha
            val shapeName = node.spriteShape
            val nw = node.width
            val nh = node.height

            // Custom procedural render component
            obj.addComponent(object : Component() {
                override fun onRender(renderer: Renderer) {
                    val tr = transform ?: return
                    ShapeDrawer.drawNodeVisual(
                        renderer = renderer,
                        shape = shapeName,
                        x = tr.worldPosition.x,
                        y = tr.worldPosition.y,
                        width = nw * tr.worldScale.x,
                        height = nh * tr.worldScale.y,
                        colorInt = nodeColor,
                        alpha = nodeAlpha,
                        rotation = tr.worldRotation
                    )
                }
            })

            // Physics Component
            if (node.hasPhysics) {
                val bType = when (node.bodyType) {
                    "STATIC" -> BodyType.STATIC
                    "KINEMATIC" -> BodyType.KINEMATIC
                    else -> BodyType.DYNAMIC
                }
                val rb = obj.addComponent(RigidBody2DComponent(
                    bodyType = bType,
                    mass = node.mass,
                    restitution = node.bounciness,
                    friction = node.friction,
                    gravityScale = node.gravityScale,
                    isFixedRotation = node.fixedRotation
                ))

                val col: Collider2DComponent = if (node.colliderShape == ColliderShapeType.CIRCLE) {
                    obj.addComponent(CircleCollider2D(radius = node.colliderRadius).apply {
                        this.isTrigger = node.isTrigger
                    })
                } else {
                    obj.addComponent(BoxCollider2D(width = node.colliderWidth, height = node.colliderHeight).apply {
                        this.isTrigger = node.isTrigger
                    })
                }

                // Setup Collision Callbacks
                if (node.tag == "Player" || node.behavior == BehaviorType.PLATFORMER_PLAYER || node.behavior == BehaviorType.TOPDOWN_PLAYER) {
                    playerObject = obj
                    playerBody = rb

                    col.onCollisionEnter = { other, manifold ->
                        if (manifold.normal.y < -0.5f) {
                            isGrounded = true
                        }
                    }
                    col.onCollisionStay = { other ->
                        if (rb.velocity.y in -10f..10f) {
                            isGrounded = true
                        }
                    }
                    col.onTriggerEnter = { triggerCol ->
                        val hitTag = triggerCol.gameObject?.name ?: ""
                        if (hitTag.contains("Coin", ignoreCase = true)) {
                            score += 100
                            scoreLabel?.text = "SCORE: $score"
                            engine?.audioManager?.playSynthSound("coin")
                            triggerCol.gameObject?.let { removeGameObject(it) }
                        } else if (hitTag.contains("Spike", ignoreCase = true) || hitTag.contains("Hazard", ignoreCase = true)) {
                            health = (health - 1).coerceAtLeast(0)
                            healthLabel?.text = "HP: $health"
                            engine?.audioManager?.playSynthSound("hit")
                        }
                    }
                }
            }

            // Particle Emitter
            if (node.isParticleEmitter) {
                obj.addComponent(ParticleEmitterComponent(
                    maxParticles = node.maxParticles,
                    emissionRate = node.emissionRate,
                    particleLifetimeMin = node.particleLifetime * 0.7f,
                    particleLifetimeMax = node.particleLifetime * 1.3f,
                    speedMin = node.speedMin,
                    speedMax = node.speedMax,
                    angleSpreadDegrees = node.angleSpread,
                    startColor = node.startColor.toInt(),
                    endColor = node.endColor.toInt(),
                    startSize = node.startSize,
                    endSize = node.endSize,
                    gravity = Vector2(0f, node.particleGravityY),
                    isEmitting = true
                ))
            }

            // Attached Behaviors
            when (node.behavior) {
                BehaviorType.AUTO_PATROL -> {
                    val startX = node.posX
                    val dist = node.patrolDistance
                    val speed = node.moveSpeed
                    var dir = 1f
                    obj.addComponent(object : Component() {
                        override fun onUpdate(dt: Float) {
                            val tr = transform ?: return
                            tr.position.x += dir * speed * dt
                            if (tr.position.x > startX + dist) {
                                dir = -1f
                            } else if (tr.position.x < startX - dist) {
                                dir = 1f
                            }
                        }
                    })
                }
                BehaviorType.ORBIT_ROTATE -> {
                    val rotSpeed = node.orbitSpeed
                    obj.addComponent(object : Component() {
                        override fun onUpdate(dt: Float) {
                            val tr = transform ?: return
                            tr.rotationDegrees += rotSpeed * dt
                        }
                    })
                }
                else -> {}
            }

            addGameObject(obj)
            playObjects.add(obj)
            nodeToObjMap[node.id] = obj
        }

        // Camera Follow Player
        val followNodeId = project.cameraFollowId ?: project.nodes.find { it.behavior == BehaviorType.PLATFORMER_PLAYER || it.behavior == BehaviorType.TOPDOWN_PLAYER }?.id
        if (followNodeId != null && nodeToObjMap.containsKey(followNodeId)) {
            val targetTransform = nodeToObjMap[followNodeId]?.transform
            if (targetTransform != null) {
                camera.target = targetTransform
                camera.smoothFactor = 8f
            }
        }

        // Build On-Screen Game Controls (Play HUD)
        setupPlayHUD()
    }

    private fun setupPlayHUD() {
        val eng = engine ?: return

        // Score & Health Top Panel
        val hudPanel = UIPanel(width = 300f, height = 48f).apply {
            setAnchor(Anchor.TOP_LEFT, offsetX = 16f, offsetY = 16f)
            backgroundColor = 0xCC0F172A.toInt()
            borderColor = 0xFF0284C7.toInt()
            borderWidth = 1.5f
            cornerRadius = 14f
        }
        uiCanvas.addElement(hudPanel)

        scoreLabel = UILabel("SCORE: 0", fontSize = 16f, color = 0xFFFFD700.toInt(), typeface = Typeface.DEFAULT_BOLD).apply {
            setAnchor(Anchor.CENTER_LEFT, offsetX = 16f)
        }
        hudPanel.addChild(scoreLabel!!)

        healthLabel = UILabel("HP: 3", fontSize = 16f, color = 0xFFEF4444.toInt(), typeface = Typeface.DEFAULT_BOLD).apply {
            setAnchor(Anchor.CENTER_RIGHT, offsetX = -16f)
        }
        hudPanel.addChild(healthLabel!!)

        // Virtual Joystick (Bottom Left)
        virtualJoystick = UIJoystick(baseRadius = 60f, stickRadius = 30f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 30f, offsetY = -30f)
            baseColor = 0x44FFFFFF
            stickColor = 0xAA0284C7.toInt()
        }
        uiCanvas.addElement(virtualJoystick!!)

        // Jump Button (Bottom Right)
        jumpButton = UIButton("JUMP", width = 84f, height = 84f).apply {
            setAnchor(Anchor.BOTTOM_RIGHT, offsetX = -30f, offsetY = -30f)
            normalBackgroundColor = 0xDD0284C7.toInt()
            fontSize = 15f
            cornerRadius = 42f
            onClick = {
                val pBody = playerBody
                if (pBody != null && isGrounded) {
                    pBody.velocity.y = -720f
                    isGrounded = false
                    eng.audioManager.playSynthSound("jump")
                }
            }
        }
        uiCanvas.addElement(jumpButton!!)

        // Action / Shoot Button
        shootButton = UIButton("FIRE", width = 74f, height = 74f).apply {
            setAnchor(Anchor.BOTTOM_RIGHT, offsetX = -130f, offsetY = -35f)
            normalBackgroundColor = 0xDDEF4444.toInt()
            fontSize = 14f
            cornerRadius = 37f
            onClick = {
                eng.audioManager.playSynthSound("laser")
                spawnLaserBullet()
            }
        }
        uiCanvas.addElement(shootButton!!)
    }

    private fun spawnLaserBullet() {
        val player = playerObject ?: return
        val px = player.transform.worldPosition.x
        val py = player.transform.worldPosition.y
        val bullet = GameObject("Bullet").setPosition(px + 30f, py)
        bullet.addComponent(object : Component() {
            override fun onRender(renderer: Renderer) {
                val tr = transform ?: return
                renderer.drawCircle(tr.worldPosition.x, tr.worldPosition.y, 8f, 0xFFFF0055.toInt(), Paint.Style.FILL)
            }
        })
        val rb = bullet.addComponent(RigidBody2DComponent(bodyType = BodyType.DYNAMIC, gravityScale = 0f))
        rb.velocity.set(800f, 0f)
        bullet.addComponent(CircleCollider2D(radius = 8f).apply { isTrigger = true })
        addGameObject(bullet)
        playObjects.add(bullet)
    }

    override fun onUpdate(dt: Float) {
        if (isPlayMode) {
            super.onUpdate(dt)

            // Update Player Joystick Controls
            val joystick = virtualJoystick
            val pBody = playerBody
            if (joystick != null && pBody != null) {
                val inputX = joystick.value.x
                val inputY = joystick.value.y

                val isPlatformer = project.nodes.any { it.behavior == BehaviorType.PLATFORMER_PLAYER }
                if (isPlatformer) {
                    pBody.velocity.x = inputX * 360f
                } else {
                    // Top-Down movement
                    pBody.velocity.x = inputX * 420f
                    pBody.velocity.y = inputY * 420f
                }
            }
        } else {
            // Editor Camera update
            camera.target = null
            camera.position.set(editorCamera.position.x, editorCamera.position.y)
            camera.zoom = editorCamera.zoom
            super.onUpdate(dt)
        }
    }

    override fun onRender(renderer: Renderer) {
        // Clear background
        renderer.clear(project.backgroundColor.toInt())

        if (isPlayMode) {
            super.onRender(renderer)
        } else {
            // Render Editor World (Grid, Nodes, Selection Gizmo)
            renderEditorWorld(renderer)
        }
    }

    private fun renderEditorWorld(renderer: Renderer) {
        renderer.beginCamera(camera)

        // 1. Draw Godot Editor Grid
        val gSize = project.gridSize
        val viewLeft = camera.position.x - (1280f / camera.zoom) * 0.6f
        val viewRight = camera.position.x + (1280f / camera.zoom) * 0.6f
        val viewTop = camera.position.y - (720f / camera.zoom) * 0.6f
        val viewBottom = camera.position.y + (720f / camera.zoom) * 0.6f

        val startX = (viewLeft / gSize).toInt() * gSize
        val startY = (viewTop / gSize).toInt() * gSize

        var gx = startX
        while (gx <= viewRight) {
            val isMajor = (gx.toInt() % (gSize.toInt() * 4)) == 0
            val gridColor = if (isMajor) 0x33475569 else 0x18334155
            renderer.drawLine(gx, viewTop, gx, viewBottom, gridColor, 1f)
            gx += gSize
        }

        var gy = startY
        while (gy <= viewBottom) {
            val isMajor = (gy.toInt() % (gSize.toInt() * 4)) == 0
            val gridColor = if (isMajor) 0x33475569 else 0x18334155
            renderer.drawLine(viewLeft, gy, viewRight, gy, gridColor, 1f)
            gy += gSize
        }

        // Axis crosshair (0,0)
        renderer.drawLine(-500f, 0f, 500f, 0f, 0x88EF4444.toInt(), 2f)
        renderer.drawLine(0f, -500f, 0f, 500f, 0x8822C55E.toInt(), 2f)

        // Viewport Frame (0,0 to 1280,720)
        renderer.drawRect(0f, 0f, 1280f, 720f, 0x440284C7, Paint.Style.STROKE, 2f)

        // 2. Render all Nodes
        project.nodes.filter { it.isVisible }.forEach { node ->
            ShapeDrawer.drawNodeVisual(
                renderer = renderer,
                shape = node.spriteShape,
                x = node.posX,
                y = node.posY,
                width = node.width * node.scaleX,
                height = node.height * node.scaleY,
                colorInt = node.color.toInt(),
                alpha = node.alpha,
                rotation = node.rotation
            )

            // Draw Physics Collider Outlines in Editor
            if (node.hasPhysics) {
                if (node.colliderShape == ColliderShapeType.CIRCLE) {
                    renderer.drawCircle(node.posX, node.posY, node.colliderRadius * node.scaleX, 0x8822C55E.toInt(), Paint.Style.STROKE, 1.5f)
                } else {
                    val cw = node.colliderWidth * node.scaleX
                    val ch = node.colliderHeight * node.scaleY
                    renderer.drawRect(node.posX - cw * 0.5f, node.posY - ch * 0.5f, cw, ch, 0x8822C55E.toInt(), Paint.Style.STROKE, 1.5f)
                }
            }
        }

        // 3. Render Selection Gizmo for Selected Node
        val selected = project.findNode(project.selectedNodeId)
        if (selected != null) {
            val sw = selected.width * selected.scaleX
            val sh = selected.height * selected.scaleY
            val left = selected.posX - sw * 0.5f
            val top = selected.posY - sh * 0.5f

            // Bounding box
            renderer.drawRect(left - 4f, top - 4f, sw + 8f, sh + 8f, 0xFF38BDF8.toInt(), Paint.Style.STROKE, 2f)

            // Corner Handles
            val handleSize = 10f / camera.zoom
            val corners = listOf(
                Pair(left - 4f, top - 4f),
                Pair(left + sw + 4f, top - 4f),
                Pair(left - 4f, top + sh + 4f),
                Pair(left + sw + 4f, top + sh + 4f)
            )
            corners.forEach { (cx, cy) ->
                renderer.drawRect(cx - handleSize * 0.5f, cy - handleSize * 0.5f, handleSize, handleSize, 0xFFFFFFFF.toInt(), Paint.Style.FILL)
                renderer.drawRect(cx - handleSize * 0.5f, cy - handleSize * 0.5f, handleSize, handleSize, 0xFF0284C7.toInt(), Paint.Style.STROKE, 1.5f)
            }

            // Node Name Label in Editor
            renderer.drawText(selected.name, left, top - 10f, 13f / camera.zoom, 0xFF38BDF8.toInt(), Paint.Align.LEFT, Typeface.DEFAULT_BOLD)
        }

        renderer.endCamera()
    }

    override fun onTouch(event: TouchEvent): Boolean {
        if (isPlayMode) {
            return super.onTouch(event)
        }

        // Editor Mode Touch Handling (Selection, Dragging, Pan, Zoom)
        val worldPos = camera.logicalToWorld(event.logicalPosition.x, event.logicalPosition.y, tempVec)

        when (event.type) {
            TouchType.DOWN -> {
                lastTouchScreenPos.set(event.logicalPosition.x, event.logicalPosition.y)

                // Check if tapped a node
                val hitNode = project.nodes.reversed().find { node ->
                    if (node.isLocked || !node.isVisible) return@find false
                    val hw = (node.width * node.scaleX) * 0.5f + 16f
                    val hh = (node.height * node.scaleY) * 0.5f + 16f
                    worldPos.x in (node.posX - hw)..(node.posX + hw) &&
                        worldPos.y in (node.posY - hh)..(node.posY + hh)
                }

                if (hitNode != null) {
                    project.selectedNodeId = hitNode.id
                    isDraggingNode = true
                    dragStartNodePos.set(hitNode.posX, hitNode.posY)
                    dragStartTouchWorld.set(worldPos.x, worldPos.y)
                    onNodeSelectedListener?.invoke(hitNode)
                    engine?.audioManager?.playSynthSound("click")
                } else {
                    isDraggingNode = false
                }
                return true
            }

            TouchType.MOVE -> {
                if (isDraggingNode) {
                    val selected = project.findNode(project.selectedNodeId)
                    if (selected != null && !selected.isLocked) {
                        val dx = worldPos.x - dragStartTouchWorld.x
                        val dy = worldPos.y - dragStartTouchWorld.y
                        var targetX = dragStartNodePos.x + dx
                        var targetY = dragStartNodePos.y + dy

                        if (project.gridSnap) {
                            val g = project.gridSize
                            targetX = (Math.round(targetX / g) * g).toFloat()
                            targetY = (Math.round(targetY / g) * g).toFloat()
                        }

                        selected.posX = targetX
                        selected.posY = targetY
                        onNodeSelectedListener?.invoke(selected)
                    }
                } else {
                    // Pan Editor Camera
                    val screenDx = (event.logicalPosition.x - lastTouchScreenPos.x) / camera.zoom
                    val screenDy = (event.logicalPosition.y - lastTouchScreenPos.y) / camera.zoom
                    editorCamera.position.x -= screenDx
                    editorCamera.position.y -= screenDy
                }
                lastTouchScreenPos.set(event.logicalPosition.x, event.logicalPosition.y)
                return true
            }

            TouchType.UP, TouchType.CANCEL -> {
                isDraggingNode = false
                return true
            }
        }
    }
}
