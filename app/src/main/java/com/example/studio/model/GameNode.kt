package com.example.studio.model

import java.util.UUID

enum class NodeType {
    NODE_2D,
    CHARACTER_BODY_2D,
    SPRITE_2D,
    ANIMATED_SPRITE_2D,
    RIGID_BODY_2D,
    STATIC_BODY_2D,
    AREA_2D,
    TILEMAP_2D,
    CPU_PARTICLES_2D,
    CAMERA_2D,
    PARALLAX_BACKGROUND_2D,
    CANVAS_LAYER_UI,
    LABEL_2D,
    SPAWNER_2D
}

enum class BehaviorType {
    NONE,
    PLATFORMER_PLAYER,
    TOPDOWN_PLAYER,
    ENEMY_PATROL,
    ENEMY_CHASER,
    ENEMY_SHOOTER,
    COLLECTABLE_COIN,
    COLLECTABLE_GEM,
    COLLECTABLE_HEART,
    HAZARD_SPIKE,
    VICTORY_FLAG,
    PORTAL_WARP,
    AUTO_PATROL,
    ORBIT_ROTATE,
    FLAPPY_TAP,
    BOUNCE_SCREEN
}

enum class ColliderShapeType {
    BOX,
    CIRCLE
}

data class VisualScriptRule(
    val id: String = UUID.randomUUID().toString(),
    var trigger: String = "ON_COLLISION_ENTER", // ON_START, ON_UPDATE, ON_TOUCH_TAP, ON_COLLISION_ENTER, ON_TRIGGER_ENTER, ON_PLAYER_STOMP, ON_TIMER, ON_HEALTH_ZERO
    var targetTag: String = "Any", // Any, Player, Enemy, Coin, Hazard, Ground, Goal
    var action: String = "PLAY_SOUND", // PLAY_SOUND, ADD_SCORE, TAKE_DAMAGE, HEAL, APPLY_IMPULSE, SPAWN_PREFAB, PLAY_ANIMATION, CAMERA_SHAKE, DESTROY_SELF, RESTART_SCENE, GAME_WIN, GAME_OVER, TELEPORT_TO
    var paramString: String = "coin", // sound name or prefab or clip
    var paramFloat: Float = 100f // score value or impulse force or damage
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
    
    // Visual / Sprite & Custom Texture
    var customTextureKey: String? = null,
    var spriteShape: String = "box", // box, circle, capsule, star, diamond, hero, slime, spaceship, alien, coin, platform, spike, cloud, heart, gem, bullet, trophy, flag
    var color: Long = 0xFF0284C7, // ARGB
    var alpha: Float = 1.0f,
    var width: Float = 64f,
    var height: Float = 64f,
    var flipX: Boolean = false,
    var flipY: Boolean = false,

    // Sprite Frame Animation
    var isAnimated: Boolean = false,
    var animationFps: Float = 8f,
    var animationCols: Int = 4,
    var animationRows: Int = 1,
    var currentAnimation: String = "default",

    // Tilemap
    var tilemapCols: Int = 20,
    var tilemapRows: Int = 12,
    var tilemapTileSize: Float = 32f,
    var tilemapData: String = "", // CSV representation of tile ids
    
    // Physics
    var hasPhysics: Boolean = false,
    var bodyType: String = "DYNAMIC", // DYNAMIC, STATIC, KINEMATIC
    var mass: Float = 1.0f,
    var gravityScale: Float = 1.0f,
    var bounciness: Float = 0.2f,
    var friction: Float = 0.5f,
    var fixedRotation: Boolean = true,
    var isTrigger: Boolean = false,
    var isOneWay: Boolean = false,
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
    
    // Behavior / Script & AI
    var behavior: BehaviorType = BehaviorType.NONE,
    var moveSpeed: Float = 300f,
    var jumpForce: Float = 650f,
    var patrolDistance: Float = 200f,
    var orbitSpeed: Float = 90f,
    var detectionRange: Float = 320f,
    var shootInterval: Float = 2.0f,
    var spawnInterval: Float = 1.5f,
    var spawnPrefab: String = "bullet",
    var scoreValue: Int = 100,
    var soundOnAction: String = "jump",

    // Text GDScript Code
    var scriptCode: String = "",

    // Combat & Health
    var health: Int = 3,
    var maxHealth: Int = 3,
    var attackDamage: Int = 1,

    // Parallax
    var isParallax: Boolean = false,
    var parallaxFactor: Float = 0.5f,
    
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
