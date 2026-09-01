package com.example.engine.math

/**
 * 2D Ray representation with origin and normalized direction.
 */
data class Ray2D(
    val origin: Vector2 = Vector2(),
    val direction: Vector2 = Vector2(1f, 0f)
) {
    init {
        direction.normalize()
    }

    fun set(origin: Vector2, direction: Vector2): Ray2D {
        this.origin.set(origin)
        this.direction.set(direction).normalize()
        return this
    }

    fun getPoint(distance: Float, outVec: Vector2): Vector2 {
        outVec.set(direction).scale(distance).add(origin)
        return outVec
    }
}
