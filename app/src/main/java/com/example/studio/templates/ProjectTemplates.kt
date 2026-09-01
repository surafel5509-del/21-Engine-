package com.example.studio.templates

import com.example.studio.model.*
import java.util.UUID

object ProjectTemplates {

    fun createPlatformerTemplate(): GameProject {
        val proj = GameProject(
            id = "template_platformer",
            name = "2D Platformer Adventure",
            description = "Godot-style platformer with hero character, jump physics, coins, moving platforms and hazards.",
            gravityY = 1200f,
            backgroundColor = 0xFF0B1120
        )

        // 1. Hero Player
        val player = GameNode(
            id = "player_hero",
            name = "PlayerHero",
            nodeType = NodeType.RIGID_BODY_2D,
            tag = "Player",
            posX = 200f,
            posY = 480f,
            width = 48f,
            height = 64f,
            spriteShape = "hero",
            color = 0xFF38BDF8,
            hasPhysics = true,
            bodyType = "DYNAMIC",
            mass = 1.0f,
            friction = 0.4f,
            bounciness = 0.0f,
            fixedRotation = true,
            colliderShape = ColliderShapeType.BOX,
            colliderWidth = 44f,
            colliderHeight = 60f,
            behavior = BehaviorType.PLATFORMER_PLAYER,
            moveSpeed = 360f,
            jumpForce = 720f,
            soundOnAction = "jump"
        )
        proj.addNode(player)
        proj.cameraFollowId = player.id

        // 2. Ground Floor
        val ground = GameNode(
            id = "ground_main",
            name = "GroundFloor",
            nodeType = NodeType.STATIC_BODY_2D,
            tag = "Ground",
            posX = 640f,
            posY = 670f,
            width = 1280f,
            height = 80f,
            spriteShape = "platform",
            color = 0xFF1E293B,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.8f,
            bounciness = 0.0f,
            colliderShape = ColliderShapeType.BOX,
            colliderWidth = 1280f,
            colliderHeight = 80f
        )
        proj.addNode(ground)

        // 3. Floating Platforms
        val plat1 = GameNode(
            name = "Platform_Left",
            nodeType = NodeType.STATIC_BODY_2D,
            tag = "Ground",
            posX = 380f,
            posY = 500f,
            width = 180f,
            height = 36f,
            spriteShape = "platform",
            color = 0xFF334155,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.8f,
            colliderWidth = 180f,
            colliderHeight = 36f
        )
        proj.addNode(plat1)

        val plat2 = GameNode(
            name = "Platform_Moving",
            nodeType = NodeType.STATIC_BODY_2D,
            tag = "Ground",
            posX = 660f,
            posY = 390f,
            width = 160f,
            height = 36f,
            spriteShape = "platform",
            color = 0xFF0284C7,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.8f,
            colliderWidth = 160f,
            colliderHeight = 36f,
            behavior = BehaviorType.AUTO_PATROL,
            patrolDistance = 140f,
            moveSpeed = 120f
        )
        proj.addNode(plat2)

        val plat3 = GameNode(
            name = "Platform_Right",
            nodeType = NodeType.STATIC_BODY_2D,
            tag = "Ground",
            posX = 960f,
            posY = 280f,
            width = 200f,
            height = 36f,
            spriteShape = "platform",
            color = 0xFF334155,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.8f,
            colliderWidth = 200f,
            colliderHeight = 36f
        )
        proj.addNode(plat3)

        // 4. Gold Coins
        val coins = listOf(
            Pair(380f, 440f),
            Pair(660f, 330f),
            Pair(960f, 220f),
            Pair(1020f, 220f)
        )
        coins.forEachIndexed { i, pos ->
            val coin = GameNode(
                name = "GoldCoin_$i",
                nodeType = NodeType.AREA_2D,
                tag = "Coin",
                posX = pos.first,
                posY = pos.second,
                width = 36f,
                height = 36f,
                spriteShape = "coin",
                color = 0xFFFFD700,
                hasPhysics = true,
                bodyType = "STATIC",
                isTrigger = true,
                colliderShape = ColliderShapeType.CIRCLE,
                colliderRadius = 18f,
                behavior = BehaviorType.COLLECTABLE_COIN,
                scoreValue = 100,
                soundOnAction = "coin"
            )
            proj.addNode(coin)
        }

        // 5. Hazard Spikes
        val spike = GameNode(
            name = "HazardSpikes",
            nodeType = NodeType.AREA_2D,
            tag = "Hazard",
            posX = 640f,
            posY = 615f,
            width = 140f,
            height = 32f,
            spriteShape = "spike",
            color = 0xFFEF4444,
            hasPhysics = true,
            bodyType = "STATIC",
            isTrigger = true,
            colliderWidth = 140f,
            colliderHeight = 32f,
            behavior = BehaviorType.HAZARD_SPIKE,
            soundOnAction = "hit"
        )
        proj.addNode(spike)

        // 6. Magic Particle Portal
        val portal = GameNode(
            name = "GoalPortal",
            nodeType = NodeType.CPU_PARTICLES_2D,
            tag = "Goal",
            posX = 1020f,
            posY = 200f,
            width = 50f,
            height = 50f,
            spriteShape = "gem",
            color = 0xFFA855F7,
            isParticleEmitter = true,
            emissionRate = 45f,
            maxParticles = 120,
            startColor = 0xFFA855F7,
            endColor = 0x0038BDF8,
            speedMin = 40f,
            speedMax = 120f,
            angleSpread = 360f,
            particleLifetime = 1.2f
        )
        proj.addNode(portal)

        proj.selectedNodeId = player.id
        return proj
    }

    fun createSpaceShooterTemplate(): GameProject {
        val proj = GameProject(
            id = "template_spaceshooter",
            name = "Galactic Space Defender",
            description = "Top-down 2D space shooter with photon laser cannons, alien UFOs, asteroid physics and explosive bursts.",
            gravityY = 0f,
            backgroundColor = 0xFF050814
        )

        // 1. Player Starship
        val ship = GameNode(
            id = "player_ship",
            name = "PlayerSpaceship",
            nodeType = NodeType.RIGID_BODY_2D,
            tag = "Player",
            posX = 640f,
            posY = 560f,
            width = 64f,
            height = 64f,
            spriteShape = "spaceship",
            color = 0xFF00E5FF,
            hasPhysics = true,
            bodyType = "DYNAMIC",
            mass = 1f,
            friction = 0.2f,
            fixedRotation = true,
            colliderWidth = 56f,
            colliderHeight = 56f,
            behavior = BehaviorType.TOPDOWN_PLAYER,
            moveSpeed = 420f,
            soundOnAction = "laser"
        )
        proj.addNode(ship)

        // Thruster Particles (Child)
        val thruster = GameNode(
            name = "ShipThruster",
            nodeType = NodeType.CPU_PARTICLES_2D,
            posX = 640f,
            posY = 600f,
            width = 20f,
            height = 20f,
            spriteShape = "circle",
            color = 0xFFFF9100,
            isParticleEmitter = true,
            emissionRate = 60f,
            maxParticles = 100,
            startColor = 0xFFFF9100,
            endColor = 0x00FF3D00,
            speedMin = 60f,
            speedMax = 160f,
            particleGravityY = 80f,
            angleSpread = 40f,
            particleLifetime = 0.6f
        )
        proj.addNode(thruster)

        // 2. Alien UFO Enemies
        val aliens = listOf(
            Pair(340f, 180f),
            Pair(640f, 140f),
            Pair(940f, 180f)
        )
        aliens.forEachIndexed { i, pos ->
            val alien = GameNode(
                name = "AlienUFO_$i",
                nodeType = NodeType.RIGID_BODY_2D,
                tag = "Enemy",
                posX = pos.first,
                posY = pos.second,
                width = 60f,
                height = 50f,
                spriteShape = "alien",
                color = 0xFF22C55E,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                mass = 2f,
                bounciness = 0.6f,
                colliderShape = ColliderShapeType.CIRCLE,
                colliderRadius = 25f,
                behavior = BehaviorType.AUTO_PATROL,
                patrolDistance = 120f,
                moveSpeed = 100f + i * 30f,
                scoreValue = 250,
                soundOnAction = "explosion"
            )
            proj.addNode(alien)
        }

        // 3. Floating Asteroids
        val asteroids = listOf(
            Pair(200f, 320f),
            Pair(800f, 360f),
            Pair(1080f, 280f)
        )
        asteroids.forEachIndexed { i, pos ->
            val ast = GameNode(
                name = "Asteroid_$i",
                nodeType = NodeType.RIGID_BODY_2D,
                tag = "Asteroid",
                posX = pos.first,
                posY = pos.second,
                width = 54f,
                height = 54f,
                spriteShape = "circle",
                color = 0xFF64748B,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                mass = 3f,
                bounciness = 0.7f,
                colliderShape = ColliderShapeType.CIRCLE,
                colliderRadius = 27f,
                behavior = BehaviorType.ORBIT_ROTATE,
                orbitSpeed = 45f + i * 20f
            )
            proj.addNode(ast)
        }

        proj.selectedNodeId = ship.id
        return proj
    }

    fun createPhysicsSandboxTemplate(): GameProject {
        val proj = GameProject(
            id = "template_physics",
            name = "Physics Sandbox & Dominoes",
            description = "Interactive physics simulation with high-restitution bouncy balls, stacking crate towers and domino chain reactions.",
            gravityY = 980f,
            backgroundColor = 0xFF0F172A
        )

        // Ground and Walls
        val ground = GameNode(
            name = "Ground",
            nodeType = NodeType.STATIC_BODY_2D,
            posX = 640f,
            posY = 680f,
            width = 1280f,
            height = 60f,
            spriteShape = "platform",
            color = 0xFF1E293B,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.7f,
            colliderWidth = 1280f,
            colliderHeight = 60f
        )
        proj.addNode(ground)

        val leftWall = GameNode(
            name = "LeftWall",
            nodeType = NodeType.STATIC_BODY_2D,
            posX = 20f,
            posY = 360f,
            width = 40f,
            height = 720f,
            spriteShape = "box",
            color = 0xFF1E293B,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.5f,
            colliderWidth = 40f,
            colliderHeight = 720f
        )
        proj.addNode(leftWall)

        val rightWall = GameNode(
            name = "RightWall",
            nodeType = NodeType.STATIC_BODY_2D,
            posX = 1260f,
            posY = 360f,
            width = 40f,
            height = 720f,
            spriteShape = "box",
            color = 0xFF1E293B,
            hasPhysics = true,
            bodyType = "STATIC",
            friction = 0.5f,
            colliderWidth = 40f,
            colliderHeight = 720f
        )
        proj.addNode(rightWall)

        // Dominoes
        for (i in 0 until 8) {
            val domino = GameNode(
                name = "Domino_$i",
                nodeType = NodeType.RIGID_BODY_2D,
                posX = 260f + i * 42f,
                posY = 600f,
                width = 16f,
                height = 80f,
                spriteShape = "box",
                color = 0xFFF59E0B,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                mass = 1.2f,
                friction = 0.4f,
                bounciness = 0.1f,
                colliderWidth = 16f,
                colliderHeight = 80f
            )
            proj.addNode(domino)
        }

        // Bouncy Balls
        val colors = listOf(0xFF00E5FF, 0xFFFF4081, 0xFF76FF03, 0xFFFFD600)
        for (i in 0 until 4) {
            val ball = GameNode(
                name = "SuperBouncyBall_$i",
                nodeType = NodeType.RIGID_BODY_2D,
                posX = 700f + i * 80f,
                posY = 200f - i * 50f,
                width = 44f,
                height = 44f,
                spriteShape = "circle",
                color = colors[i],
                hasPhysics = true,
                bodyType = "DYNAMIC",
                mass = 1.5f,
                friction = 0.1f,
                bounciness = 0.92f,
                colliderShape = ColliderShapeType.CIRCLE,
                colliderRadius = 22f
            )
            proj.addNode(ball)
        }

        // Stacking Crate Tower
        for (row in 0 until 3) {
            for (col in 0..(2 - row)) {
                val box = GameNode(
                    name = "Crate_${row}_$col",
                    nodeType = NodeType.RIGID_BODY_2D,
                    posX = 1040f + col * 46f + row * 23f,
                    posY = 620f - row * 46f,
                    width = 42f,
                    height = 42f,
                    spriteShape = "box",
                    color = 0xFFD97706,
                    hasPhysics = true,
                    bodyType = "DYNAMIC",
                    mass = 2f,
                    friction = 0.6f,
                    bounciness = 0.15f,
                    colliderWidth = 42f,
                    colliderHeight = 42f
                )
                proj.addNode(box)
            }
        }

        return proj
    }

    fun createFlappyTemplate(): GameProject {
        val proj = GameProject(
            id = "template_flappy",
            name = "Sky Flap Quest",
            description = "One-tap airborne physics game with obstacle pipes, score triggers and particle wind trails.",
            gravityY = 1400f,
            backgroundColor = 0xFF0284C7
        )

        // Flappy Bird
        val bird = GameNode(
            id = "player_bird",
            name = "FlappyBird",
            nodeType = NodeType.RIGID_BODY_2D,
            tag = "Player",
            posX = 320f,
            posY = 360f,
            width = 48f,
            height = 38f,
            spriteShape = "hero",
            color = 0xFFFFD600,
            hasPhysics = true,
            bodyType = "DYNAMIC",
            mass = 1.0f,
            bounciness = 0.0f,
            fixedRotation = false,
            colliderShape = ColliderShapeType.CIRCLE,
            colliderRadius = 20f,
            behavior = BehaviorType.FLAPPY_TAP,
            jumpForce = 520f,
            soundOnAction = "jump"
        )
        proj.addNode(bird)

        // Pipes Obstacles
        val pipeGaps = listOf(Pair(600f, 300f), Pair(900f, 420f), Pair(1200f, 250f))
        pipeGaps.forEachIndexed { index, (px, gapCenter) ->
            val topPipeHeight = gapCenter - 90f
            val botPipeY = gapCenter + 90f
            val botPipeHeight = 720f - botPipeY

            // Top Pipe
            val topPipe = GameNode(
                name = "PipeTop_$index",
                nodeType = NodeType.STATIC_BODY_2D,
                tag = "Hazard",
                posX = px,
                posY = topPipeHeight / 2f,
                width = 70f,
                height = topPipeHeight,
                spriteShape = "platform",
                color = 0xFF15803D,
                hasPhysics = true,
                bodyType = "STATIC",
                colliderWidth = 70f,
                colliderHeight = topPipeHeight,
                behavior = BehaviorType.AUTO_PATROL,
                patrolDistance = 20f,
                moveSpeed = 60f
            )
            proj.addNode(topPipe)

            // Bottom Pipe
            val botPipe = GameNode(
                name = "PipeBottom_$index",
                nodeType = NodeType.STATIC_BODY_2D,
                tag = "Hazard",
                posX = px,
                posY = botPipeY + botPipeHeight / 2f,
                width = 70f,
                height = botPipeHeight,
                spriteShape = "platform",
                color = 0xFF15803D,
                hasPhysics = true,
                bodyType = "STATIC",
                colliderWidth = 70f,
                colliderHeight = botPipeHeight,
                behavior = BehaviorType.AUTO_PATROL,
                patrolDistance = 20f,
                moveSpeed = 60f
            )
            proj.addNode(botPipe)

            // Coin inside gap
            val coin = GameNode(
                name = "ScoreRing_$index",
                nodeType = NodeType.AREA_2D,
                tag = "Coin",
                posX = px,
                posY = gapCenter,
                width = 32f,
                height = 32f,
                spriteShape = "coin",
                color = 0xFFFFEA00,
                hasPhysics = true,
                bodyType = "STATIC",
                isTrigger = true,
                colliderShape = ColliderShapeType.CIRCLE,
                colliderRadius = 16f,
                behavior = BehaviorType.COLLECTABLE_COIN,
                scoreValue = 10,
                soundOnAction = "coin"
            )
            proj.addNode(coin)
        }

        proj.selectedNodeId = bird.id
        return proj
    }

    fun createBlankTemplate(): GameProject {
        val proj = GameProject(
            id = "template_blank",
            name = "New 2D World",
            description = "Clean empty canvas ready for game design and node construction.",
            gravityY = 980f,
            backgroundColor = 0xFF0F172A
        )

        val player = GameNode(
            name = "MyHero",
            nodeType = NodeType.RIGID_BODY_2D,
            tag = "Player",
            posX = 640f,
            posY = 360f,
            width = 56f,
            height = 56f,
            spriteShape = "hero",
            color = 0xFF38BDF8,
            hasPhysics = true,
            bodyType = "DYNAMIC",
            colliderWidth = 56f,
            colliderHeight = 56f,
            behavior = BehaviorType.PLATFORMER_PLAYER
        )
        proj.addNode(player)

        val ground = GameNode(
            name = "Ground",
            nodeType = NodeType.STATIC_BODY_2D,
            posX = 640f,
            posY = 650f,
            width = 800f,
            height = 50f,
            spriteShape = "platform",
            color = 0xFF334155,
            hasPhysics = true,
            bodyType = "STATIC",
            colliderWidth = 800f,
            colliderHeight = 50f
        )
        proj.addNode(ground)

        return proj
    }
}
