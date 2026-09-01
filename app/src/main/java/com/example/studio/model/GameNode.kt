package com.example.studio.model

import java.util.UUID

enum class NodeType {
    NODE_2D,
    SPRITE_2D,
    RIGID_BODY_2D,
    STATIC_BODY_2D,
    AREA_2D,
    CPU_PARTICLES_2D,
    CAMERA_2D,
    LABEL_2D,
    SPAWNER_2D,
    LIGHT_2D
}

enum class BehaviorType {
    NONE,
    PLATFORMER_PLAYER,
    TOPDOWN_PLAYER,
    AUTO_PATROL,
    ORBIT_ROTATE,
    SPAWNER,
    COLLECTABLE_COIN,
    HAZARD_SPIKE,
    FLAPPY_TAP,
    BOUNCE_SCREEN,
    FOLLOW_TARGET
}

enum class ColliderShapeType {
    BOX,
    CIRCLE
}

data class VisualScriptRule(
    val id: String = UUID.randomUUID().toString(),
    var trigger: String = "ON_COLLISION_ENTER", // ON_START, ON_UPDATE, ON_TOUCH_TAP, ON_COLLISION_ENTER, ON_TIMER, ON_JOYSTICK
    var targetTag: String = "Any", // Any, Player, Enemy, Coin, Hazard, Ground
    var action: String = "PLAY_SOUND", // PLAY_SOUND, ADD_SCORE, SPAWN_PARTICLES, APPLY_FORCE, DESTROY_SELF, RESTART_SCENE, CHANGE_COLOR
    var paramString: String = "coin", // sound name or color or prefab
    var paramFloat: Float = 100f // score value or impulse force
)

data class GameNode(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Node2D",
    var nodeType: NodeType = NodeType.SPRITE_2D,
    var tag: String = "Untagged",
    
    // Transform
    var posX: Float = 640f,
    var posY: Float = 360f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var rotation: Float = 0f,
    var zIndex: Int = 0,
    var isVisible: Boolean = true,
    var isLocked: Boolean = false,
    
    // Hierarchy
    var parentId: String? = null,
    val childrenIds: MutableList<String> = mutableListOf(),
    
    // Visual / Sprite
    var spriteShape: String = "box", // box, circle, capsule, star, diamond, triangle, hero, spaceship, alien, coin, platform, spike, cloud, heart, gem, bullet
    var color: Long = 0xFF0284C7, // ARGB
    var alpha: Float = 1.0f,
    var width: Float = 64f,
    var height: Float = 64f,
    var flipX: Boolean = false,
    var flipY: Boolean = false,
    
    // Physics
    var hasPhysics: Boolean = false,
    var bodyType: String = "DYNAMIC", // DYNAMIC, STATIC, KINEMATIC
    var mass: Float = 1.0f,
    var gravityScale: Float = 1.0f,
    var bounciness: Float = 0.2f,
    var friction: Float = 0.5f,
    var fixedRotation: Boolean = false,
    var isTrigger: Boolean = false,
    var colliderShape: ColliderShapeType = ColliderShapeType.BOX,
    var colliderWidth: Float = 64f,
    var colliderHeight: Float = 64f,
    var colliderRadius: Float = 32f,
    
    // Particles
    var isParticleEmitter: Boolean = false,
    var emissionRate: Float = 40f,
    var maxParticles: Int = 150,
    var particleLifetime: Float = 1.0f,
    var speedMin: Float = 50f,
    var speedMax: Float = 150f,
    var angleSpread: Float = 360f,
    var startColor: Long = 0xFFFFD600,
    var endColor: Long = 0x00FF3D00,
    var startSize: Float = 10f,
    var endSize: Float = 2f,
    var particleGravityY: Float = 0f,
    
    // Behavior / Script
    var behavior: BehaviorType = BehaviorType.NONE,
    var moveSpeed: Float = 300f,
    var jumpForce: Float = 650f,
    var patrolDistance: Float = 200f,
    var orbitSpeed: Float = 90f,
    var spawnInterval: Float = 1.5f,
    var spawnPrefab: String = "bullet",
    var scoreValue: Int = 100,
    var soundOnAction: String = "jump",
    
    // Visual Script Rules
    val customRules: MutableList<VisualScriptRule> = mutableListOf()
) {
    fun duplicate(): GameNode {
        return copy(
            id = UUID.randomUUID().toString(),
            name = "${name}_Copy",
            childrenIds = mutableListOf(),
            customRules = customRules.map { it.copy(id = UUID.randomUUID().toString()) }.toMutableList()
        )
    }
}
