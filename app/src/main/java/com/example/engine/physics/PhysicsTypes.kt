package com.example.engine.physics

import com.example.engine.math.Vector2

enum class BodyType {
    /** Affected by forces, gravity, and collisions with other bodies */
    DYNAMIC,
    /** Unmovable body (e.g. ground, walls, platforms). Infinite mass. */
    STATIC,
    /** Moved manually via velocity, not affected by forces or gravity, pushes dynamic bodies */
    KINEMATIC
}

object CollisionLayers {
    const val ALL: Int = 0xFFFFFFFF.toInt()
    const val NONE: Int = 0
    const val DEFAULT: Int = 1 shl 0
    const val PLAYER: Int = 1 shl 1
    const val ENEMY: Int = 1 shl 2
    const val PROJECTILE: Int = 1 shl 3
    const val ENVIRONMENT: Int = 1 shl 4
    const val TRIGGER: Int = 1 shl 5
    const val PICKUP: Int = 1 shl 6
}

data class ContactManifold(
    val normal: Vector2 = Vector2(),
    var penetration: Float = 0f,
    val contactPoint: Vector2 = Vector2()
)

data class RaycastHit(
    val collider: com.example.engine.components.Collider2DComponent,
    val point: Vector2 = Vector2(),
    val normal: Vector2 = Vector2(),
    val distance: Float = 0f
)
