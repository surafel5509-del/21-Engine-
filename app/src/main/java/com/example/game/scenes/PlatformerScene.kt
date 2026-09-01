package com.example.game.scenes

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.animation.AnimationClip
import com.example.engine.animation.Animator
import com.example.engine.components.*
import com.example.engine.input.TouchEvent
import com.example.engine.input.VirtualButton
import com.example.engine.input.VirtualJoystick
import com.example.engine.math.MathUtils
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import com.example.engine.physics.BodyType
import com.example.engine.physics.CollisionLayers
import com.example.engine.rendering.RenderLayer
import com.example.engine.rendering.Renderer
import com.example.engine.scene.GameObject
import com.example.engine.scene.Scene
import com.example.engine.ui.*

/**
 * Scene 2: Full 2D Platformer Game Demonstration.
 * Demonstrates:
 * - Procedural character sprite sheet animations (Idle, Walk).
 * - Parallax scrolling background layers.
 * - Platform collision detection & jump mechanics.
 * - Collectible coin triggers with procedural retro audio synthesis & sparkle particles.
 * - Smooth camera follow with world bounds and shake.
 * - Responsive HUD: Virtual joystick, Jump action button, Score label, Health progress bar.
 */
class PlatformerScene : Scene() {

    private lateinit var playerObj: GameObject
    private lateinit var playerBody: RigidBody2DComponent
    private lateinit var playerAnimator: AnimatedSpriteComponent
    private lateinit var particleSparks: ParticleEmitterComponent

    private lateinit var joystick: VirtualJoystick
    private lateinit var jumpButton: VirtualButton
    private lateinit var scoreLabel: UILabel
    private lateinit var healthBar: UIProgressBar

    private var score = 0
    private var health = 100f
    private var isGrounded = false
    private val worldBounds = Rect2D(0f, 0f, 3000f, 1000f)

    override fun onCreate() {
        val eng = engine ?: return

        // 1. Setup Camera follow constraints
        camera.setBounds(worldBounds.left, worldBounds.top, worldBounds.right, worldBounds.bottom)
        camera.smoothFactor = 8f
        physicsWorld.gravity.set(0f, 1200f)

        // 2. Build Level Platforms & Scenery
        buildLevel()

        // 3. Create Player Character
        createPlayer()

        // 4. Particle Sparkler
        val sparkObj = GameObject("SparkEmitter")
        particleSparks = sparkObj.addComponent(
            ParticleEmitterComponent(
                maxParticles = 150,
                isEmitting = false,
                startColor = 0xFFFFEB3B.toInt(),
                endColor = 0x00FF9800.toInt(),
                startSize = 10f,
                endSize = 2f,
                speedMin = 40f,
                speedMax = 160f,
                gravity = Vector2(0f, 100f)
            )
        )
        addGameObject(sparkObj)

        // 5. Build Responsive Mobile HUD Controls
        setupHUD()

        engine?.let { eng ->
            com.example.game.ui.GlobalHUD.attachTo(uiCanvas, eng, currentSceneIndex = 2)
        }
    }

    private fun buildLevel() {
        val assetManager = engine?.assetManager ?: return
        val tileBmp = assetManager.createProceduralTileBitmap()
        val tileSprite = assetManager.getOrCreateSprite("tile_proc", tileBmp)

        // Ground Floor across level
        createPlatform(x = 1500f, y = 850f, width = 3000f, height = 80f, sprite = tileSprite)

        // Floating Platforms
        val platformCoords = listOf(
            Triple(400f, 680f, 220f),
            Triple(750f, 560f, 260f),
            Triple(1100f, 440f, 220f),
            Triple(1450f, 560f, 280f),
            Triple(1850f, 480f, 240f),
            Triple(2200f, 600f, 300f),
            Triple(2600f, 450f, 240f)
        )

        for ((px, py, pw) in platformCoords) {
            createPlatform(x = px, y = py, width = pw, height = 36f, sprite = tileSprite)
        }

        // Spawn Spinning Gold Coins
        val coinSheet = assetManager.createProceduralCoinSpriteSheet()
        val coinCoords = listOf(
            Pair(400f, 620f),
            Pair(750f, 500f),
            Pair(800f, 500f),
            Pair(1100f, 380f),
            Pair(1450f, 500f),
            Pair(1500f, 500f),
            Pair(1850f, 420f),
            Pair(2200f, 540f),
            Pair(2260f, 540f),
            Pair(2600f, 390f)
        )

        for ((cx, cy) in coinCoords) {
            spawnCoin(cx, cy, coinSheet)
        }
    }

    private fun createPlatform(x: Float, y: Float, width: Float, height: Float, sprite: com.example.engine.rendering.Sprite) {
        val obj = GameObject("Platform").setPosition(x, y)
        obj.layer = RenderLayer.BACKGROUND
        obj.addComponent(SpriteRendererComponent(sprite = sprite))
        obj.addComponent(RigidBody2DComponent(bodyType = BodyType.STATIC, friction = 0.6f, restitution = 0f))
        obj.addComponent(BoxCollider2D(width = width, height = height))
        obj.transform.scale.set(width / sprite.width, height / sprite.height)
        addGameObject(obj)
    }

    private fun spawnCoin(x: Float, y: Float, coinSheet: com.example.engine.animation.SpriteSheet) {
        val obj = GameObject("Coin").setPosition(x, y)
        val anim = obj.addComponent(AnimatedSpriteComponent())
        anim.addAnimation(
            coinSheet.createAnimation("spin", intArrayOf(0, 1, 2, 3, 4, 5), fps = 10f)
        )
        anim.play("spin")

        val trigger = obj.addComponent(CircleCollider2D(radius = 20f))
        trigger.isTrigger = true
        trigger.onTriggerEnter = { other ->
            if (other.gameObject == playerObj) {
                score += 100
                scoreLabel.text = "★ Score: $score"
                particleSparks.burst(18, obj.transform.worldPosition)
                engine?.audioManager?.playSynthSound("coin")
                removeGameObject(obj)
            }
        }

        addGameObject(obj)
    }

    private fun createPlayer() {
        val assetManager = engine?.assetManager ?: return
        val playerSheet = assetManager.createProceduralCharacterSpriteSheet()

        playerObj = GameObject("Player").setPosition(200f, 750f)

        // Animations: Idle & Walk
        playerAnimator = playerObj.addComponent(AnimatedSpriteComponent())
        playerAnimator.addAnimation(
            playerSheet.createAnimation("idle", intArrayOf(0, 1, 2, 3), fps = 5f)
        )
        playerAnimator.addAnimation(
            playerSheet.createAnimation("walk", intArrayOf(4, 5, 6, 7), fps = 8f)
        )
        playerAnimator.play("idle")

        // Physics & Collision
        playerBody = playerObj.addComponent(
            RigidBody2DComponent(
                bodyType = BodyType.DYNAMIC,
                mass = 1.0f,
                friction = 0.2f,
                restitution = 0.0f
            )
        )

        val collider = playerObj.addComponent(BoxCollider2D(width = 36f, height = 44f))
        collider.onCollisionEnter = { other, manifold ->
            if (manifold.normal.y < -0.6f) {
                isGrounded = true
            }
        }
        collider.onCollisionStay = { other ->
            // Keep grounded when resting on platform
            if (playerBody.velocity.y in -5f..5f) {
                isGrounded = true
            }
        }

        addGameObject(playerObj)

        // Target camera to player
        camera.target = playerObj.transform
    }

    private fun setupHUD() {
        // Virtual Joystick (Bottom Left)
        joystick = VirtualJoystick(radius = 65f, knobRadius = 28f).apply {
            setAnchor(Anchor.BOTTOM_LEFT, offsetX = 50f, offsetY = -40f)
        }
        uiCanvas.addElement(joystick)

        // Jump Button (Bottom Right)
        jumpButton = VirtualButton(label = "JUMP", radius = 42f).apply {
            setAnchor(Anchor.BOTTOM_RIGHT, offsetX = -50f, offsetY = -40f)
            normalColor = 0xCC1E88E5.toInt()
            pressedColor = 0xFF42A5F5.toInt()
            onClick = {
                if (isGrounded) {
                    playerBody.velocity.y = -620f
                    isGrounded = false
                    particleSparks.burst(6, playerObj.transform.worldPosition)
                    engine?.audioManager?.playSynthSound("jump")
                }
            }
        }
        uiCanvas.addElement(jumpButton)

        // Score Label (Top Right)
        scoreLabel = UILabel("★ Score: 0", fontSize = 22f, color = 0xFFFFD700.toInt(), align = Paint.Align.RIGHT).apply {
            setAnchor(Anchor.TOP_RIGHT, offsetX = -30f, offsetY = 20f)
        }
        uiCanvas.addElement(scoreLabel)

        // Health Progress Bar (Top Left)
        healthBar = UIProgressBar(width = 180f, height = 22f, initialValue = 100f).apply {
            setAnchor(Anchor.TOP_LEFT, offsetX = 30f, offsetY = 20f)
            fillColor = 0xFF00E676.toInt()
            backgroundColor = 0x88212121.toInt()
        }
        uiCanvas.addElement(healthBar)

        val healthLabel = UILabel("HP", fontSize = 14f, color = Color.WHITE).apply {
            setAnchor(Anchor.TOP_LEFT, offsetX = 36f, offsetY = 22f)
        }
        uiCanvas.addElement(healthLabel)
    }

    override fun onUpdate(dt: Float) {
        super.onUpdate(dt)

        // Process horizontal movement from Joystick
        val moveX = joystick.direction.x
        val moveSpeed = 320f

        playerBody.velocity.x = moveX * moveSpeed

        if (kotlin.math.abs(moveX) > 0.1f) {
            playerAnimator.play("walk")
            // Flip sprite depending on walk direction
            playerObj.transform.scale.x = if (moveX < 0) -1f else 1f
        } else {
            playerAnimator.play("idle")
        }

        // Keep player in bounds
        if (playerObj.transform.position.y > 950f) {
            // Respawn
            playerObj.transform.position.set(200f, 700f)
            playerBody.velocity.set(0f, 0f)
            camera.shake(0.5f)
            engine?.audioManager?.playSynthSound("explosion")
        }
    }

    override fun renderWorld(renderer: Renderer) {
        // Parallax Background Layers
        drawParallaxBackground(renderer)

        // Draw game objects (platforms, player, coins)
        super.renderWorld(renderer)
    }

    private fun drawParallaxBackground(renderer: Renderer) {
        val camX = camera.position.x
        val camY = camera.position.y

        // Sky gradient fill
        renderer.drawRect(
            x = camX - 800f,
            y = camY - 500f,
            width = 1600f,
            height = 1000f,
            color = 0xFF0F172A.toInt(),
            style = Paint.Style.FILL
        )

        // Distant Stars (Parallax 0.05)
        val starOffset = camX * 0.05f
        for (i in 0..30) {
            val sx = ((i * 137f - starOffset) % 1600f) + (camX - 800f)
            val sy = (i * 83f) % 400f + (camY - 350f)
            renderer.drawCircle(sx, sy, if (i % 3 == 0) 2.5f else 1.5f, 0x88FFFFFF.toInt(), Paint.Style.FILL)
        }

        // Distant Mountains (Parallax 0.2)
        val mountainOffset = camX * 0.2f
        for (i in 0..6) {
            val mx = (i * 450f - mountainOffset) + (camX - 700f)
            val my = 750f
            renderer.drawRect(mx, my - 250f, 400f, 300f, 0xFF1E293B.toInt(), Paint.Style.FILL, cornerRadius = 200f)
        }

        // Midground Hills (Parallax 0.5)
        val hillOffset = camX * 0.5f
        for (i in 0..8) {
            val hx = (i * 350f - hillOffset) + (camX - 700f)
            val hy = 800f
            renderer.drawRect(hx, hy - 160f, 320f, 200f, 0xFF334155.toInt(), Paint.Style.FILL, cornerRadius = 140f)
        }
    }
}
