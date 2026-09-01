package com.example.engine.camera

import com.example.engine.math.MathUtils
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 2D Game Camera with zoom, rotation, smooth target tracking, world boundary clamping,
 * screen shake effects, and viewport frustum culling.
 */
class Camera2D(
    var viewportWidth: Float = 1280f,
    var viewportHeight: Float = 720f
) {
    /** World space position of camera center */
    val position = Vector2(0f, 0f)

    /** Camera zoom (1.0 = normal, 2.0 = 2x zoom in, 0.5 = 2x zoom out) */
    var zoom: Float = 1.0f
        set(value) {
            field = MathUtils.clamp(value, 0.05f, 20.0f)
        }

    /** Camera rotation in degrees */
    var rotationDegrees: Float = 0f

    /** World limits / boundary clamping (optional) */
    var hasBounds: Boolean = false
    val worldBounds = Rect2D()

    /** Smooth target follow properties */
    var target: com.example.engine.math.Transform2D? = null
    var targetPosition: Vector2? = null
    var followSmoothness: Float = 5.0f // higher = faster lerp
    var smoothFactor: Float
        get() = followSmoothness
        set(value) {
            followSmoothness = value
        }
    var followLeadFactor: Float = 0f
    val followOffset = Vector2(0f, 0f)

    /** Camera shake system */
    private var shakeTrauma: Float = 0f
    var shakeDecay: Float = 2.5f // trauma loss per second
    var maxShakeOffset: Float = 30f // max pixel translation on shake
    var maxShakeAngle: Float = 5f // max degrees on shake
    val shakeOffset = Vector2(0f, 0f)
    var shakeAngle: Float = 0f
        private set

    /** Cached visible world bounding box for culling */
    private val cachedVisibleWorldBounds = Rect2D()
    private var isBoundsDirty = true

    fun setPosition(x: Float, y: Float): Camera2D {
        position.set(x, y)
        clampToBounds()
        isBoundsDirty = true
        return this
    }

    fun setBounds(left: Float, top: Float, right: Float, bottom: Float): Camera2D {
        hasBounds = true
        worldBounds.setBounds(left, top, right, bottom)
        clampToBounds()
        isBoundsDirty = true
        return this
    }

    fun clearBounds(): Camera2D {
        hasBounds = false
        return this
    }

    fun follow(target: Vector2, smoothness: Float = 5.0f, offset: Vector2 = Vector2.ZERO): Camera2D {
        this.targetPosition = target
        this.followSmoothness = smoothness
        this.followOffset.set(offset)
        return this
    }

    fun stopFollowing(): Camera2D {
        targetPosition = null
        return this
    }

    fun shake(intensity: Float) {
        shakeTrauma = MathUtils.clamp01(shakeTrauma + intensity)
    }

    fun update(dt: Float) {
        // 1. Follow target with smooth damp / lerp
        val tr = target
        if (tr != null) {
            targetPosition = tr.worldPosition
        }
        val target = targetPosition
        if (target != null) {
            val desiredX = target.x + followOffset.x
            val desiredY = target.y + followOffset.y
            val t = MathUtils.clamp01(dt * followSmoothness)
            position.x = MathUtils.lerp(position.x, desiredX, t)
            position.y = MathUtils.lerp(position.y, desiredY, t)
            clampToBounds()
            isBoundsDirty = true
        }

        // 2. Process camera shake (Trauma squared algorithm)
        if (shakeTrauma > 0f) {
            val shakeAmount = shakeTrauma * shakeTrauma
            val angle = MathUtils.randomRange(0f, MathUtils.TWO_PI)
            val dist = shakeAmount * maxShakeOffset
            shakeOffset.x = cos(angle) * dist
            shakeOffset.y = sin(angle) * dist
            shakeAngle = (Math.random().toFloat() * 2f - 1f) * shakeAmount * maxShakeAngle

            shakeTrauma = MathUtils.approach(shakeTrauma, 0f, shakeDecay * dt)
        } else {
            shakeOffset.set(0f, 0f)
            shakeAngle = 0f
        }
    }

    private fun clampToBounds() {
        if (!hasBounds) return
        val halfW = (viewportWidth / zoom) * 0.5f
        val halfH = (viewportHeight / zoom) * 0.5f

        val minCamX = worldBounds.left + halfW
        val maxCamX = worldBounds.right - halfW
        val minCamY = worldBounds.top + halfH
        val maxCamY = worldBounds.bottom - halfH

        if (minCamX <= maxCamX) {
            position.x = MathUtils.clamp(position.x, minCamX, maxCamX)
        } else {
            position.x = worldBounds.centerX
        }

        if (minCamY <= maxCamY) {
            position.y = MathUtils.clamp(position.y, minCamY, maxCamY)
        } else {
            position.y = worldBounds.centerY
        }
    }

    fun onResize(newViewportWidth: Float, newViewportHeight: Float) {
        viewportWidth = newViewportWidth
        viewportHeight = newViewportHeight
        clampToBounds()
        isBoundsDirty = true
    }

    // =========================================================================
    // Coordinate conversions (World <-> Logical)
    // =========================================================================

    /**
     * Converts logical screen position (e.g. 0..1280, 0..720) to world coordinates.
     */
    fun logicalToWorld(logicalX: Float, logicalY: Float, outVec: Vector2): Vector2 {
        val halfW = viewportWidth * 0.5f
        val halfH = viewportHeight * 0.5f

        // Center-relative coordinates
        var relX = (logicalX - halfW) / zoom
        var relY = (logicalY - halfH) / zoom

        // Apply inverse rotation
        val totalRotation = -(rotationDegrees + shakeAngle)
        if (totalRotation != 0f) {
            val rad = MathUtils.degToRad(totalRotation)
            val cos = cos(rad)
            val sin = sin(rad)
            val rx = relX * cos - relY * sin
            val ry = relX * sin + relY * cos
            relX = rx
            relY = ry
        }

        outVec.x = position.x + shakeOffset.x + relX
        outVec.y = position.y + shakeOffset.y + relY
        return outVec
    }

    /**
     * Converts world coordinates to logical screen coordinates.
     */
    fun worldToLogical(worldX: Float, worldY: Float, outVec: Vector2): Vector2 {
        var relX = worldX - (position.x + shakeOffset.x)
        var relY = worldY - (position.y + shakeOffset.y)

        // Apply camera rotation
        val totalRotation = rotationDegrees + shakeAngle
        if (totalRotation != 0f) {
            val rad = MathUtils.degToRad(totalRotation)
            val cos = cos(rad)
            val sin = sin(rad)
            val rx = relX * cos - relY * sin
            val ry = relX * sin + relY * cos
            relX = rx
            relY = ry
        }

        val halfW = viewportWidth * 0.5f
        val halfH = viewportHeight * 0.5f

        outVec.x = halfW + (relX * zoom)
        outVec.y = halfH + (relY * zoom)
        return outVec
    }

    /**
     * Returns the bounding box of world coordinates currently visible through the camera.
     */
    fun getVisibleWorldBounds(): Rect2D {
        val halfW = (viewportWidth / zoom) * 0.5f
        val halfH = (viewportHeight / zoom) * 0.5f
        val camX = position.x + shakeOffset.x
        val camY = position.y + shakeOffset.y

        // Add safety margin for rotated camera
        val diag = sqrt(halfW * halfW + halfH * halfH)
        cachedVisibleWorldBounds.setBounds(
            camX - diag,
            camY - diag,
            camX + diag,
            camY + diag
        )
        return cachedVisibleWorldBounds
    }

    /**
     * Frustum culling check: returns true if the AABB overlaps the camera view.
     */
    fun isVisible(worldAABB: Rect2D): Boolean {
        return getVisibleWorldBounds().intersects(worldAABB)
    }
}
