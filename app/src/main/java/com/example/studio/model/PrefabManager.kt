package com.example.studio.model

import java.util.UUID

/**
 * Manages reusable prefab node templates for rapid level construction and AI generation.
 */
object PrefabManager {

    val defaultPrefabs: List<GameNode> = listOf(
        GameNode(
            name = "PlayerCharacter",
            nodeType = NodeType.CHARACTER_BODY_2D,
            tag = "Player",
            spriteShape = "hero",
            color = 0xFF0284C7,
            width = 64f,
            height = 64f,
            hasPhysics = true,
            bodyType = "DYNAMIC",
            behavior = BehaviorType.PLATFORMER_PLAYER,
            moveSpeed = 350f,
            jumpForce = 700f,
            health = 5,
            maxHealth = 5
        ),
        GameNode(
            name = "EnemySlime",
            nodeType = NodeType.CHARACTER_BODY_2D,
            tag = "Enemy",
            spriteShape = "slime",
            color = 0xFF22C55E,
            width = 56f,
            height = 48f,
            hasPhysics = true,
            bodyType = "DYNAMIC",
            behavior = BehaviorType.ENEMY_PATROL,
            moveSpeed = 120f,
            health = 2,
            attackDamage = 1
        ),
        GameNode(
            name = "FloatingPlatform",
            nodeType = NodeType.STATIC_BODY_2D,
            tag = "Ground",
            spriteShape = "platform",
            color = 0xFF475569,
            width = 240f,
            height = 36f,
            hasPhysics = true,
            bodyType = "STATIC"
        ),
        GameNode(
            name = "GoldCoin",
            nodeType = NodeType.AREA_2D,
            tag = "Coin",
            spriteShape = "coin",
            color = 0xFFEAB308,
            width = 36f,
            height = 36f,
            hasPhysics = true,
            bodyType = "STATIC",
            isTrigger = true,
            behavior = BehaviorType.COLLECTABLE_COIN,
            scoreValue = 100
        ),
        GameNode(
            name = "SpikeTrap",
            nodeType = NodeType.AREA_2D,
            tag = "Hazard",
            spriteShape = "spike",
            color = 0xFFEF4444,
            width = 48f,
            height = 32f,
            hasPhysics = true,
            bodyType = "STATIC",
            isTrigger = true,
            behavior = BehaviorType.HAZARD_SPIKE,
            attackDamage = 1
        ),
        GameNode(
            name = "MagicParticleEmitter",
            nodeType = NodeType.CPU_PARTICLES_2D,
            isParticleEmitter = true,
            emissionRate = 50f,
            particleLifetime = 1.2f,
            startColor = 0xFF38BDF8,
            endColor = 0x00818CF8
        )
    )

    fun instantiatePrefab(prefabName: String, x: Float, y: Float): GameNode? {
        val prefab = defaultPrefabs.firstOrNull { it.name.equals(prefabName, ignoreCase = true) }
        return prefab?.duplicate()?.apply {
            id = UUID.randomUUID().toString()
            posX = x
            posY = y
        }
    }
}
