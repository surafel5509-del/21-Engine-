package com.example.engine.components

import com.example.engine.math.Circle2D
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import com.example.engine.physics.CollisionLayers
import com.example.engine.physics.ContactManifold
import com.example.engine.scene.Component

/**
 * Base 2D Collider component with layer filtering and trigger/collision events.
 */
abstract class Collider2DComponent : Component() {
    var layer: Int = CollisionLayers.DEFAULT
    var mask: Int = CollisionLayers.ALL
    var isTrigger: Boolean = false

    val offset = Vector2(0f, 0f)

    var onCollisionEnter: ((other: Collider2DComponent, manifold: ContactManifold) -> Unit)? = null
    var onCollisionStay: ((other: Collider2DComponent) -> Unit)? = null
    var onCollisionExit: ((other: Collider2DComponent) -> Unit)? = null

    var onTriggerEnter: ((other: Collider2DComponent) -> Unit)? = null
    var onTriggerStay: ((other: Collider2DComponent) -> Unit)? = null
    var onTriggerExit: ((other: Collider2DComponent) -> Unit)? = null

    abstract fun getBoundingBox(outRect: Rect2D): Rect2D
    abstract fun containsPoint(worldPoint: Vector2): Boolean

    fun canCollideWith(other: Collider2DComponent): Boolean {
        return (this.layer and other.mask) != 0 && (other.layer and this.mask) != 0
    }
}

/**
 * Axis-Aligned Box Collider component.
 */
class BoxCollider2D(
    var width: Float = 50f,
    var height: Float = 50f
) : Collider2DComponent() {

    private val cachedAABB = Rect2D()

    override fun getBoundingBox(outRect: Rect2D): Rect2D {
        val tr = transform
        val px = tr?.worldPosition?.x ?: 0f
        val py = tr?.worldPosition?.y ?: 0f
        val sx = tr?.worldScale?.x ?: 1f
        val sy = tr?.worldScale?.y ?: 1f
        val pivotX = tr?.pivot?.x ?: 0.5f
        val pivotY = tr?.pivot?.y ?: 0.5f

        val w = width * sx
        val h = height * sy
        val left = px + offset.x - (w * pivotX)
        val top = py + offset.y - (h * pivotY)

        outRect.set(left, top, w, h)
        return outRect
    }

    override fun containsPoint(worldPoint: Vector2): Boolean {
        return getBoundingBox(cachedAABB).contains(worldPoint)
    }
}

/**
 * Circle Collider component.
 */
class CircleCollider2D(
    var radius: Float = 25f
) : Collider2DComponent() {

    private val cachedCircle = Circle2D()
    private val cachedAABB = Rect2D()

    fun getCircle(outCircle: Circle2D): Circle2D {
        val tr = transform
        val px = (tr?.worldPosition?.x ?: 0f) + offset.x
        val py = (tr?.worldPosition?.y ?: 0f) + offset.y
        val scale = tr?.worldScale?.x ?: 1f
        outCircle.set(px, py, radius * scale)
        return outCircle
    }

    override fun getBoundingBox(outRect: Rect2D): Rect2D {
        val c = getCircle(cachedCircle)
        outRect.setBounds(c.x - c.radius, c.y - c.radius, c.x + c.radius, c.y + c.radius)
        return outRect
    }

    override fun containsPoint(worldPoint: Vector2): Boolean {
        val c = getCircle(cachedCircle)
        return c.contains(worldPoint)
    }
}
