package com.example.engine.math

import kotlin.math.cos
import kotlin.math.sin

/**
 * Hierarchical 2D transformation representing position, rotation, scale, and anchor pivot.
 */
class Transform2D(
    var position: Vector2 = Vector2(),
    var rotationDegrees: Float = 0f,
    var scale: Vector2 = Vector2(1f, 1f),
    var pivot: Vector2 = Vector2(0.5f, 0.5f)
) {
    var parent: Transform2D? = null
    private val _children = mutableListOf<Transform2D>()
    val children: List<Transform2D> get() = _children

    // Cached world values
    private val cachedWorldPosition = Vector2()
    private val cachedWorldScale = Vector2(1f, 1f)
    private var cachedWorldRotation = 0f
    private var isDirty = true

    fun setPosition(x: Float, y: Float): Transform2D {
        position.set(x, y)
        markDirty()
        return this
    }

    fun translate(dx: Float, dy: Float): Transform2D {
        position.add(dx, dy)
        markDirty()
        return this
    }

    fun setRotation(degrees: Float): Transform2D {
        rotationDegrees = degrees
        markDirty()
        return this
    }

    fun rotate(deltaDegrees: Float): Transform2D {
        rotationDegrees += deltaDegrees
        markDirty()
        return this
    }

    fun setScale(sx: Float, sy: Float): Transform2D {
        scale.set(sx, sy)
        markDirty()
        return this
    }

    fun setScale(uniformScale: Float): Transform2D = setScale(uniformScale, uniformScale)

    fun setPivot(px: Float, py: Float): Transform2D {
        pivot.set(px, py)
        return this
    }

    fun addChild(child: Transform2D) {
        if (child.parent != this) {
            child.parent?.removeChild(child)
            child.parent = this
            _children.add(child)
            child.markDirty()
        }
    }

    fun removeChild(child: Transform2D) {
        if (_children.remove(child)) {
            child.parent = null
            child.markDirty()
        }
    }

    fun markDirty() {
        isDirty = true
        for (i in 0 until _children.size) {
            _children[i].markDirty()
        }
    }

    private fun updateWorldTransformIfNeeded() {
        if (!isDirty) return

        val p = parent
        if (p == null) {
            cachedWorldPosition.set(position)
            cachedWorldRotation = rotationDegrees
            cachedWorldScale.set(scale)
        } else {
            p.updateWorldTransformIfNeeded()

            // World rotation
            cachedWorldRotation = p.cachedWorldRotation + rotationDegrees

            // World scale
            cachedWorldScale.set(p.cachedWorldScale.x * scale.x, p.cachedWorldScale.y * scale.y)

            // World position (rotate and scale local position by parent transform)
            val rad = MathUtils.degToRad(p.cachedWorldRotation)
            val cos = cos(rad)
            val sin = sin(rad)
            val scaledX = position.x * p.cachedWorldScale.x
            val scaledY = position.y * p.cachedWorldScale.y

            cachedWorldPosition.x = p.cachedWorldPosition.x + (scaledX * cos - scaledY * sin)
            cachedWorldPosition.y = p.cachedWorldPosition.y + (scaledX * sin + scaledY * cos)
        }

        isDirty = false
    }

    val worldPosition: Vector2
        get() {
            updateWorldTransformIfNeeded()
            return cachedWorldPosition
        }

    val worldRotation: Float
        get() {
            updateWorldTransformIfNeeded()
            return cachedWorldRotation
        }

    val worldScale: Vector2
        get() {
            updateWorldTransformIfNeeded()
            return cachedWorldScale
        }

    fun localToWorld(localPoint: Vector2, outPoint: Vector2): Vector2 {
        updateWorldTransformIfNeeded()
        val rad = MathUtils.degToRad(cachedWorldRotation)
        val cos = cos(rad)
        val sin = sin(rad)
        val scaledX = localPoint.x * cachedWorldScale.x
        val scaledY = localPoint.y * cachedWorldScale.y

        outPoint.x = cachedWorldPosition.x + (scaledX * cos - scaledY * sin)
        outPoint.y = cachedWorldPosition.y + (scaledX * sin + scaledY * cos)
        return outPoint
    }

    fun worldToLocal(worldPoint: Vector2, outPoint: Vector2): Vector2 {
        updateWorldTransformIfNeeded()
        val dx = worldPoint.x - cachedWorldPosition.x
        val dy = worldPoint.y - cachedWorldPosition.y
        val rad = -MathUtils.degToRad(cachedWorldRotation)
        val cos = cos(rad)
        val sin = sin(rad)

        val unrotatedX = dx * cos - dy * sin
        val unrotatedY = dx * sin + dy * cos

        val sx = if (cachedWorldScale.x != 0f) cachedWorldScale.x else 1f
        val sy = if (cachedWorldScale.y != 0f) cachedWorldScale.y else 1f

        outPoint.x = unrotatedX / sx
        outPoint.y = unrotatedY / sy
        return outPoint
    }
}
