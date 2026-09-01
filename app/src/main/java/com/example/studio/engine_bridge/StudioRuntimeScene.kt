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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class EditorToolMode {
    SELECT_MOVE,
    ROTATE,
    SCALE,
    BRUSH_PLACE
}

class StudioRuntimeScene(
    var project: GameProject,
    var onNodeSelectedListener: ((GameNode?) -> Unit)? = null
) : Scene() {

    var isPlayMode: Boolean = false
        private set

    var isGameOver: Boolean = false
    var isGameWin: Boolean = false
    var isPaused: Boolean = false

    // Editor Viewport Navigation & Tools
    val editorCamera = Camera2D()
    var activeTool: EditorToolMode = EditorToolMode.SELECT_MOVE
    var brushPreset: String = "platform" // platform, coin, slime, spike
    var showColliders: Boolean = true

    private var isDraggingNode = false
    private val dragStartNodePos = Vector2()
    private val dragStartTouchWorld = Vector2()
    private val lastTouchScreenPos = Vector2()
    private val tempVec = Vector2()

    // Gameplay Runtime State
    private var totalPlayTime: Float = 0f
    private var playerObject: GameObject? = null
    private var playerBody: RigidBody2DComponent? = null
    private var isGrounded = false
    var score: Int = 0
    var health: Int = 3
    var maxHealth: Int = 3

    // HUD Elements
    private var scoreLabel: UILabel? = null
    private var healthLabel: UILabel? = null
    private var statusBanner: UILabel? = null
    private var virtualJoystick: UIJoystick? = null
    private var jumpButton: UIButton? = null
    private var shootButton: UIButton? = null
    private var dashButton: UIButton? = null

    private val playObjects = mutableListOf<GameObject>()

    override fun onCreate() {
        super.onCreate()
        camera.setBounds(-8000f, -8000f, 16000f, 16000f)
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
        isGameOver = false
        isGameWin = false
        isPaused = false
        totalPlayTime = 0f
        cleanupPlayMode()
        setupPlayMode()
    }

    fun enterEditorMode() {
        isPlayMode = false
        isGameOver = false
        isGameWin = false
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
        maxHealth = 3
        physicsWorld.gravity.set(project.gravityX, project.gravityY)

        val nodeToObjMap = mutableMapOf<String, GameObject>()

        // 1. Instantiate Nodes
        val sortedNodes = project.nodes.filter { it.isVisible }.sortedBy { it.zIndex }

        sortedNodes.forEach { node ->
            val obj = GameObject(node.name)
            obj.transform.position.set(node.posX, node.posY)
            obj.transform.scale.set(node.scaleX, node.scaleY)
            obj.transform.rotationDegrees = node.rotation

            val nodeCopy = node.copy()

            // Custom procedural & texture render component
            obj.addComponent(object : Component() {
                override fun onRender(renderer: Renderer) {
                    val tr = transform ?: return
                    val renderNode = nodeCopy.copy(
                        posX = tr.worldPosition.x,
                        posY = tr.worldPosition.y,
                        scaleX = tr.worldScale.x,
                        scaleY = tr.worldScale.y,
                        rotation = tr.worldRotation
                    )
                    ShapeDrawer.drawNode(renderer, renderNode, totalPlayTime)
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

                // Setup Player
                if (node.tag == "Player" || node.behavior == BehaviorType.PLATFORMER_PLAYER || node.behavior == BehaviorType.TOPDOWN_PLAYER) {
                    playerObject = obj
                    playerBody = rb
                    health = node.health
                    maxHealth = node.maxHealth

                    col.onCollisionEnter = { other, manifold ->
                        if (manifold.normal.y < -0.4f) {
                            isGrounded = true
                        }
                    }
                    col.onCollisionStay = { other ->
                        if (rb.velocity.y in -15f..15f) {
                            isGrounded = true
                        }
                    }
                    col.onTriggerEnter = { triggerCol ->
                        handlePlayerTrigger(triggerCol)
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

            // Attached AI & Behaviors
            attachNodeBehaviors(node, obj)

            addGameObject(obj)
            playObjects.add(obj)
            nodeToObjMap[node.id] = obj
        }

        // Camera Follow Setup
        val followTarget = playerObject?.transform ?: nodeToObjMap.values.firstOrNull()?.transform
        if (followTarget != null) {
            camera.target = followTarget
            camera.smoothFactor = 6f
        }

        // Setup In-Game HUD Controls
        setupPlayHUD()
    }

    private fun handlePlayerTrigger(triggerCol: Collider2DComponent) {
        val hitTag = triggerCol.gameObject?.name ?: ""
        val eng = engine

        if (hitTag.contains("Coin", ignoreCase = true)) {
            score += 100
            scoreLabel?.text = "SCORE: $score"
            eng?.audioManager?.playSynthSound("coin")
            camera.shake(0.2f)
            triggerCol.gameObject?.let { removeGameObject(it) }
        } else if (hitTag.contains("Gem", ignoreCase = true)) {
            score += 500
            scoreLabel?.text = "SCORE: $score"
            eng?.audioManager?.playSynthSound("powerup")
            camera.shake(0.3f)
            triggerCol.gameObject?.let { removeGameObject(it) }
        } else if (hitTag.contains("Heart", ignoreCase = true) || hitTag.contains("Potion", ignoreCase = true)) {
            health = (health + 1).coerceAtMost(maxHealth)
            healthLabel?.text = "HP: $health / $maxHealth"
            eng?.audioManager?.playSynthSound("powerup")
            triggerCol.gameObject?.let { removeGameObject(it) }
        } else if (hitTag.contains("Spike", ignoreCase = true) || hitTag.contains("Hazard", ignoreCase = true) || hitTag.contains("Lava", ignoreCase = true)) {
            health = (health - 1).coerceAtLeast(0)
            healthLabel?.text = "HP: $health / $maxHealth"
            eng?.audioManager?.playSynthSound("hit")
            camera.shake(0.6f)
            if (health <= 0) {
                triggerGameOver()
            }
        } else if (hitTag.contains("Flag", ignoreCase = true) || hitTag.contains("Trophy", ignoreCase = true) || hitTag.contains("Goal", ignoreCase = true)) {
            triggerGameWin()
        }
    }

    private fun attachNodeBehaviors(node: GameNode, obj: GameObject) {
        when (node.behavior) {
            BehaviorType.ENEMY_PATROL, BehaviorType.AUTO_PATROL -> {
                val startX = node.posX
                val dist = node.patrolDistance
                val speed = node.moveSpeed
                var dir = 1f
                obj.addComponent(object : Component() {
                    override fun onUpdate(dt: Float) {
                        if (isPaused || isGameOver || isGameWin) return
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
            BehaviorType.ENEMY_CHASER -> {
                val speed = node.moveSpeed * 0.8f
                val range = node.detectionRange
                obj.addComponent(object : Component() {
                    override fun onUpdate(dt: Float) {
                        if (isPaused || isGameOver || isGameWin) return
                        val tr = transform ?: return
                        val pTr = playerObject?.transform ?: return
                        val dist = hypot(pTr.worldPosition.x - tr.worldPosition.x, pTr.worldPosition.y - tr.worldPosition.y)
                        if (dist < range) {
                            val angle = atan2(pTr.worldPosition.y - tr.worldPosition.y, pTr.worldPosition.x - tr.worldPosition.x)
                            tr.position.x += cos(angle) * speed * dt
                            tr.position.y += sin(angle) * speed * dt
                        }
                    }
                })
            }
            BehaviorType.ENEMY_SHOOTER -> {
                var shootTimer = 0f
                val interval = node.shootInterval
                obj.addComponent(object : Component() {
                    override fun onUpdate(dt: Float) {
                        if (isPaused || isGameOver || isGameWin) return
                        shootTimer += dt
                        if (shootTimer >= interval) {
                            shootTimer = 0f
                            spawnEnemyProjectile(transform?.worldPosition ?: return)
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
    }

    private fun spawnEnemyProjectile(pos: Vector2) {
        val proj = GameObject("EnemyBullet").setPosition(pos.x, pos.y + 10f)
        proj.addComponent(object : Component() {
            override fun onRender(renderer: Renderer) {
                val tr = transform ?: return
                renderer.drawCircle(tr.worldPosition.x, tr.worldPosition.y, 7f, 0xFFFF0055.toInt(), Paint.Style.FILL)
            }
        })
        val rb = proj.addComponent(RigidBody2DComponent(bodyType = BodyType.DYNAMIC, gravityScale = 0f))
        val pTr = playerObject?.transform
        if (pTr != null) {
            val angle = atan2(pTr.worldPosition.y - pos.y, pTr.worldPosition.x - pos.x)
            rb.velocity.set(cos(angle) * 350f, sin(angle) * 350f)
        } else {
            rb.velocity.set(-300f, 0f)
        }
        val col = proj.addComponent(CircleCollider2D(radius = 7f).apply { isTrigger = true })
        col.onTriggerEnter = { other ->
            if (other.gameObject == playerObject) {
                health = (health - 1).coerceAtLeast(0)
                healthLabel?.text = "HP: $health / $maxHealth"
                engine?.audioManager?.playSynthSound("hit")
                camera.shake(0.5f)
                removeGameObject(proj)
                if (health <= 0) triggerGameOver()
            }
        }
        addGameObject(proj)
        playObjects.add(proj)
    }

    private fun triggerGameOver() {
        isGameOver = true
        engine?.audioManager?.playSynthSound("explosion")
        camera.shake(0.8f)
        statusBanner?.text = "GAME OVER - TAP TO RESTART"
        statusBanner?.isVisible = true
    }

    private fun triggerGameWin() {
        isGameWin = true
        engine?.audioManager?.playSynthSound("powerup")
        camera.shake(0.4f)
        statusBanner?.text = "VICTORY! LEVEL COMPLETE!"
        statusBanner?.isVisible = true
    }

    private fun setupPlayHUD() {
        val eng = engine ?: return

        // 1. Top HUD Bar
        val hudPanel = UIPanel(width = 340f, height = 48f).apply {
            setAnchor(Anchor.TOP_LEFT, offsetX = 16f, offsetY = 16f)
            backgroundColor = 0xDD0F172A.toInt()
            borderColor = 0xFF0284C7.toInt()
            borderWidth = 1.5f
            cornerRadius = 14f
        }
        uiCanvas.addElement(hudPanel)

        scoreLabel = UILabel("SCORE: 0", fontSize = 16f, color = 0xFFFFD700.toInt(), typeface = Typeface.DEFAULT_BOLD).apply {
            setAnchor(Anchor.CENTER_LEFT, offsetX = 16f)
        }
        hudPanel.addChild(scoreLabel!!)

        healthLabel = UILabel("HP: $health / $maxHealth", fontSize = 16f, color = 0xFFEF4444.toInt(), typeface = Typeface.DEFAULT_BOLD).apply {
            setAnchor(Anchor.CENTER_RIGHT, offsetX = -16f)
        }
        hudPanel.addChild(healthLabel!!)

        // Status Center Banner (Game Over / Victory)
        statusBanner = UILabel("", fontSize = 24f, color = 0xFFFFFFFF.toInt(), typeface = Typeface.DEFAULT_BOLD).apply {
            setAnchor(Anchor.CENTER, offsetY = -40f)
            isVisible = false
        }
        uiCanvas.addElement(statusBanner!!)

        // 2. Virtual Touch Joystick (Bottom Left)
        virtualJoystick = UIJoystick(baseRadius = 64f, stickRadius = 32f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 32f, offsetY = -32f)
            baseColor = 0x44FFFFFF
            stickColor = 0xCC0284C7.toInt()
        }
        uiCanvas.addElement(virtualJoystick!!)

        // 3. Jump Button (Bottom Right)
        jumpButton = UIButton("JUMP", width = 86f, height = 86f).apply {
            setAnchor(Anchor.BOTTOM_RIGHT, offsetX = -28f, offsetY = -28f)
            normalBackgroundColor = 0xDD0284C7.toInt()
            fontSize = 15f
            cornerRadius = 43f
            onClick = {
                if (isGameOver || isGameWin) {
                    resetPlayMode()
                } else {
                    val pBody = playerBody
                    if (pBody != null && isGrounded) {
                        val force = project.nodes.find { it.behavior == BehaviorType.PLATFORMER_PLAYER }?.jumpForce ?: 650f
                        pBody.velocity.y = -force
                        isGrounded = false
                        eng.audioManager.playSynthSound("jump")
                    }
                }
            }
        }
        uiCanvas.addElement(jumpButton!!)

        // 4. Fire Laser Button
        shootButton = UIButton("FIRE", width = 74f, height = 74f).apply {
            setAnchor(Anchor.BOTTOM_RIGHT, offsetX = -130f, offsetY = -34f)
            normalBackgroundColor = 0xDDEF4444.toInt()
            fontSize = 14f
            cornerRadius = 37f
            onClick = {
                if (!isGameOver && !isGameWin) {
                    eng.audioManager.playSynthSound("laser")
                    spawnLaserBullet()
                }
            }
        }
        uiCanvas.addElement(shootButton!!)

        // 5. Dash / Action Button
        dashButton = UIButton("DASH", width = 64f, height = 64f).apply {
            setAnchor(Anchor.BOTTOM_RIGHT, offsetX = -220f, offsetY = -38f)
            normalBackgroundColor = 0xDD10B981.toInt()
            fontSize = 13f
            cornerRadius = 32f
            onClick = {
                val pBody = playerBody
                if (pBody != null && !isGameOver && !isGameWin) {
                    val dirX = if (pBody.velocity.x >= 0) 1f else -1f
                    pBody.velocity.x = dirX * 900f
                    eng.audioManager.playSynthSound("powerup")
                    camera.shake(0.3f)
                }
            }
        }
        uiCanvas.addElement(dashButton!!)
    }

    private fun spawnLaserBullet() {
        val player = playerObject ?: return
        val px = player.transform.worldPosition.x
        val py = player.transform.worldPosition.y
        val bullet = GameObject("PlayerBullet").setPosition(px + 32f, py)
        bullet.addComponent(object : Component() {
            override fun onRender(renderer: Renderer) {
                val tr = transform ?: return
                renderer.drawCircle(tr.worldPosition.x, tr.worldPosition.y, 9f, 0xFF38BDF8.toInt(), Paint.Style.FILL)
                renderer.drawCircle(tr.worldPosition.x, tr.worldPosition.y, 4f, 0xFFFFFFFF.toInt(), Paint.Style.FILL)
            }
        })
        val rb = bullet.addComponent(RigidBody2DComponent(bodyType = BodyType.DYNAMIC, gravityScale = 0f))
        rb.velocity.set(850f, 0f)
        val col = bullet.addComponent(CircleCollider2D(radius = 9f).apply { isTrigger = true })
        col.onTriggerEnter = { other ->
            val otherName = other.gameObject?.name ?: ""
            if (otherName.contains("Enemy", ignoreCase = true) || otherName.contains("Slime", ignoreCase = true) || otherName.contains("Alien", ignoreCase = true)) {
                score += 200
                scoreLabel?.text = "SCORE: $score"
                engine?.audioManager?.playSynthSound("explosion")
                camera.shake(0.4f)
                other.gameObject?.let { removeGameObject(it) }
                removeGameObject(bullet)
            }
        }
        addGameObject(bullet)
        playObjects.add(bullet)
    }

    override fun onUpdate(dt: Float) {
        totalPlayTime += dt

        if (isPlayMode) {
            if (!isPaused && !isGameOver && !isGameWin) {
                super.onUpdate(dt)

                // Update Player Joystick Controls
                val joystick = virtualJoystick
                val pBody = playerBody
                if (joystick != null && pBody != null) {
                    val inputX = joystick.value.x
                    val inputY = joystick.value.y

                    val isPlatformer = project.nodes.any { it.behavior == BehaviorType.PLATFORMER_PLAYER }
                    if (isPlatformer) {
                        pBody.velocity.x = inputX * 380f
                    } else {
                        // Top-Down 8-way movement
                        pBody.velocity.x = inputX * 450f
                        pBody.velocity.y = inputY * 450f
                    }
                }

                // Check pit fall
                if (playerObject != null && (playerObject?.transform?.worldPosition?.y ?: 0f) > 2000f) {
                    health = 0
                    healthLabel?.text = "HP: 0 / $maxHealth"
                    triggerGameOver()
                }
            }
        } else {
            camera.target = null
            camera.position.set(editorCamera.position.x, editorCamera.position.y)
            camera.zoom = editorCamera.zoom
            super.onUpdate(dt)
        }
    }

    override fun onRender(renderer: Renderer) {
        renderer.clear(project.backgroundColor.toInt())

        if (isPlayMode) {
            super.onRender(renderer)
        } else {
            renderEditorWorld(renderer)
        }
    }

    private fun renderEditorWorld(renderer: Renderer) {
        renderer.beginCamera(camera)

        // 1. Editor Grid
        val gSize = project.gridSize
        val viewLeft = camera.position.x - (1280f / camera.zoom) * 0.7f
        val viewRight = camera.position.x + (1280f / camera.zoom) * 0.7f
        val viewTop = camera.position.y - (720f / camera.zoom) * 0.7f
        val viewBottom = camera.position.y + (720f / camera.zoom) * 0.7f

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

        // Axes (0,0)
        renderer.drawLine(-2000f, 0f, 2000f, 0f, 0x88EF4444.toInt(), 2f)
        renderer.drawLine(0f, -2000f, 0f, 2000f, 0x8822C55E.toInt(), 2f)

        // Viewport Frame (0,0 to 1280,720)
        renderer.drawRect(0f, 0f, 1280f, 720f, 0x440284C7, Paint.Style.STROKE, 2f)

        // 2. Render all Nodes sorted by zIndex
        project.nodes.filter { it.isVisible }.sortedBy { it.zIndex }.forEach { node ->
            ShapeDrawer.drawNode(renderer, node, totalPlayTime)

            // Draw Physics Collider Outlines
            if (showColliders && node.hasPhysics) {
                if (node.colliderShape == ColliderShapeType.CIRCLE) {
                    renderer.drawCircle(node.posX, node.posY, node.colliderRadius * node.scaleX, 0x8822C55E.toInt(), Paint.Style.STROKE, 1.5f)
                } else {
                    val cw = node.colliderWidth * node.scaleX
                    val ch = node.colliderHeight * node.scaleY
                    renderer.drawRect(node.posX - cw * 0.5f, node.posY - ch * 0.5f, cw, ch, 0x8822C55E.toInt(), Paint.Style.STROKE, 1.5f)
                }
            }
        }

        // 3. Render Selection Gizmo & Handles
        val selected = project.findNode(project.selectedNodeId)
        if (selected != null) {
            val sw = selected.width * selected.scaleX
            val sh = selected.height * selected.scaleY
            val left = selected.posX - sw * 0.5f
            val top = selected.posY - sh * 0.5f

            // Bounding box
            renderer.drawRect(left - 4f, top - 4f, sw + 8f, sh + 8f, 0xFF38BDF8.toInt(), Paint.Style.STROKE, 2f)

            // Scale Handles
            val handleSize = 12f / camera.zoom
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

            // Node Name Label
            renderer.drawText(
                "${selected.name} (${selected.nodeType.name})",
                left,
                top - 12f,
                14f / camera.zoom,
                0xFF38BDF8.toInt(),
                Paint.Align.LEFT,
                Typeface.DEFAULT_BOLD
            )
        }

        renderer.endCamera()
    }

    override fun onTouch(event: TouchEvent): Boolean {
        if (isPlayMode) {
            if ((isGameOver || isGameWin) && event.type == TouchType.UP) {
                resetPlayMode()
                return true
            }
            return super.onTouch(event)
        }

        val worldPos = camera.logicalToWorld(event.logicalPosition.x, event.logicalPosition.y, tempVec)

        when (event.type) {
            TouchType.DOWN -> {
                lastTouchScreenPos.set(event.logicalPosition.x, event.logicalPosition.y)

                if (activeTool == EditorToolMode.BRUSH_PLACE) {
                    placeBrushNodeAt(worldPos.x, worldPos.y)
                    return true
                }

                // Check selection / dragging
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
                    // Pan Camera
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

    private fun placeBrushNodeAt(wx: Float, wy: Float) {
        var placeX = wx
        var placeY = wy
        if (project.gridSnap) {
            val g = project.gridSize
            placeX = (Math.round(placeX / g) * g).toFloat()
            placeY = (Math.round(placeY / g) * g).toFloat()
        }

        val newNode = when (brushPreset) {
            "platform" -> GameNode(
                name = "Ground_${project.nodes.size + 1}",
                nodeType = NodeType.STATIC_BODY_2D,
                spriteShape = "platform",
                posX = placeX,
                posY = placeY,
                width = 128f,
                height = 32f,
                hasPhysics = true,
                bodyType = "STATIC"
            )
            "coin" -> GameNode(
                name = "Coin_${project.nodes.size + 1}",
                nodeType = NodeType.AREA_2D,
                spriteShape = "coin",
                posX = placeX,
                posY = placeY,
                width = 36f,
                height = 36f,
                hasPhysics = true,
                isTrigger = true,
                behavior = BehaviorType.COLLECTABLE_COIN
            )
            "slime" -> GameNode(
                name = "SlimeEnemy_${project.nodes.size + 1}",
                nodeType = NodeType.CHARACTER_BODY_2D,
                spriteShape = "slime",
                posX = placeX,
                posY = placeY,
                width = 48f,
                height = 48f,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                behavior = BehaviorType.ENEMY_PATROL
            )
            "spike" -> GameNode(
                name = "Spike_${project.nodes.size + 1}",
                nodeType = NodeType.AREA_2D,
                spriteShape = "spike",
                posX = placeX,
                posY = placeY,
                width = 64f,
                height = 32f,
                hasPhysics = true,
                isTrigger = true,
                behavior = BehaviorType.HAZARD_SPIKE
            )
            else -> GameNode(
                name = "Block_${project.nodes.size + 1}",
                posX = placeX,
                posY = placeY
            )
        }

        project.addNode(newNode)
        onNodeSelectedListener?.invoke(newNode)
        engine?.audioManager?.playSynthSound("click")
    }
}
