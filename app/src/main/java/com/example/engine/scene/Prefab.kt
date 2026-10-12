package com.example.engine.scene

import com.example.studio.model.GameNode
import com.example.studio.model.NodeType

/**
 * Prefab template for cloning reusable game object instances.
 */
data class Prefab(
    val id: String,
    val name: String,
    val templateNode: GameNode
) {
    fun instantiate(posX: Float, posY: Float): GameNode {
        val copy = templateNode.duplicate()
        copy.posX = posX
        copy.posY = posY
        return copy
    }

    companion object {
        fun createCoinPrefab(): Prefab {
            val node = GameNode(
                name = "CoinPrefab",
                nodeType = NodeType.AREA_2D,
                colorHex = "#EAB308",
                width = 24f,
                height = 24f
            )
            return Prefab("prefab_coin", "Gold Coin", node)
        }

        fun createPlatformPrefab(): Prefab {
            val node = GameNode(
                name = "PlatformPrefab",
                nodeType = NodeType.STATIC_BODY_2D,
                colorHex = "#475569",
                width = 160f,
                height = 24f,
                hasPhysics = true,
                bodyType = "STATIC"
            )
            return Prefab("prefab_platform", "Stone Platform", node)
        }

        fun createEnemyPrefab(): Prefab {
            val node = GameNode(
                name = "EnemyPrefab",
                nodeType = NodeType.RIGID_BODY_2D,
                colorHex = "#EF4444",
                width = 32f,
                height = 32f,
                hasPhysics = true,
                bodyType = "DYNAMIC"
            )
            return Prefab("prefab_enemy", "Slime Enemy", node)
        }
    }
}
