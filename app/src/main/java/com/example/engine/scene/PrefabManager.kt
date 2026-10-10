package com.example.engine.scene

import com.example.studio.model.BehaviorType
import com.example.studio.model.GameNode
import com.example.studio.model.NodeType

object PrefabManager {

    val availablePrefabs = listOf(
        "player" to "Player Character",
        "enemy_patrol" to "Patrol Enemy",
        "enemy_chaser" to "Chaser Enemy",
        "coin" to "Collectible Coin",
        "gem" to "Collectible Gem",
        "heart" to "Health Heart",
        "bullet" to "Projectile Bullet",
        "spike" to "Hazard Spike",
        "platform" to "Static Platform",
        "tilemap_ground" to "Tilemap Ground",
        "particle_fire" to "Fire Particle Emitter"
    )

    fun instantiate(prefabName: String, posX: Float, posY: Float): GameNode {
        return when (prefabName.lowercase()) {
            "player" -> GameNode(
                name = "Player",
                nodeType = NodeType.CHARACTER_BODY_2D,
                tag = "Player",
                posX = posX,
                posY = posY,
                spriteShape = "hero",
                color = 0xFF0284C7,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                behavior = BehaviorType.PLATFORMER_PLAYER,
                moveSpeed = 350f,
                jumpForce = 680f,
                health = 3
            )
            "enemy_patrol" -> GameNode(
                name = "Enemy_Patrol",
                nodeType = NodeType.CHARACTER_BODY_2D,
                tag = "Enemy",
                posX = posX,
                posY = posY,
                spriteShape = "slime",
                color = 0xFFDC2626,
                hasPhysics = true,
                bodyType = "KINEMATIC",
                behavior = BehaviorType.ENEMY_PATROL,
                moveSpeed = 120f,
                patrolDistance = 220f
            )
            "enemy_chaser" -> GameNode(
                name = "Enemy_Chaser",
                nodeType = NodeType.CHARACTER_BODY_2D,
                tag = "Enemy",
                posX = posX,
                posY = posY,
                spriteShape = "alien",
                color = 0xFF9333EA,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                behavior = BehaviorType.ENEMY_CHASER,
                moveSpeed = 180f,
                detectionRange = 350f
            )
            "coin" -> GameNode(
                name = "Coin",
                nodeType = NodeType.AREA_2D,
                tag = "Coin",
                posX = posX,
                posY = posY,
                width = 36f,
                height = 36f,
                spriteShape = "coin",
                color = 0xFFEAB308,
                behavior = BehaviorType.COLLECTABLE_COIN,
                scoreValue = 100,
                soundOnAction = "coin"
            )
            "gem" -> GameNode(
                name = "Gem",
                nodeType = NodeType.AREA_2D,
                tag = "Coin",
                posX = posX,
                posY = posY,
                width = 36f,
                height = 36f,
                spriteShape = "gem",
                color = 0xFF3B82F6,
                behavior = BehaviorType.COLLECTABLE_GEM,
                scoreValue = 250,
                soundOnAction = "coin"
            )
            "heart" -> GameNode(
                name = "Heart",
                nodeType = NodeType.AREA_2D,
                tag = "Collectable",
                posX = posX,
                posY = posY,
                width = 36f,
                height = 36f,
                spriteShape = "heart",
                color = 0xFFEF4444,
                behavior = BehaviorType.COLLECTABLE_HEART,
                scoreValue = 0,
                soundOnAction = "heal"
            )
            "bullet" -> GameNode(
                name = "Bullet",
                nodeType = NodeType.RIGID_BODY_2D,
                tag = "Bullet",
                posX = posX,
                posY = posY,
                width = 16f,
                height = 16f,
                spriteShape = "bullet",
                color = 0xFFF97316,
                hasPhysics = true,
                bodyType = "DYNAMIC",
                gravityScale = 0f,
                moveSpeed = 600f
            )
            "spike" -> GameNode(
                name = "Spike",
                nodeType = NodeType.STATIC_BODY_2D,
                tag = "Hazard",
                posX = posX,
                posY = posY,
                width = 48f,
                height = 48f,
                spriteShape = "spike",
                color = 0xFFEF4444,
                hasPhysics = true,
                bodyType = "STATIC",
                behavior = BehaviorType.HAZARD_SPIKE,
                attackDamage = 1
            )
            "platform" -> GameNode(
                name = "Platform",
                nodeType = NodeType.STATIC_BODY_2D,
                tag = "Ground",
                posX = posX,
                posY = posY,
                width = 250f,
                height = 40f,
                spriteShape = "platform",
                color = 0xFF334155,
                hasPhysics = true,
                bodyType = "STATIC"
            )
            "particle_fire" -> GameNode(
                name = "FireParticles",
                nodeType = NodeType.CPU_PARTICLES_2D,
                posX = posX,
                posY = posY,
                isParticleEmitter = true,
                emissionRate = 50f,
                startColor = 0xFFFFD600,
                endColor = 0x00FF3D00
            )
            else -> GameNode(
                name = prefabName,
                posX = posX,
                posY = posY
            )
        }
    }
}
