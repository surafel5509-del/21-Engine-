package com.example.engine.components

import com.example.engine.math.Vector2
import com.example.engine.physics.BodyType
import com.example.engine.scene.Component

/**
 * 2D Physics Body Component.
 * Supports velocity, acceleration, gravity scaling, mass, restitution (bounciness), friction, and linear/angular damping.
 */
class RigidBody2DComponent(
    var bodyType: BodyType = BodyType.DYNAMIC,
    var mass: Float = 1.0f,
    var gravityScale: Float = 1.0f,
    var restitution: Float = 0.5f, // Bounciness (0 = no bounce, 1 = perfectly elastic)
    var friction: Float = 0.2f,
    var linearDamping: Float = 0.05f,
    var isFixedRotation: Boolean = true
) : Component() {

    val velocity = Vector2(0f, 0f)
    val acceleration = Vector2(0f, 0f)
    private val forces = Vector2(0f, 0f)

    var angularVelocity: Float = 0f
    var torque: Float = 0f

    val inverseMass: Float
        get() = if (bodyType == BodyType.DYNAMIC && mass > 0.0001f) 1f / mass else 0f

    fun applyForce(force: Vector2) {
        if (bodyType != BodyType.DYNAMIC) return
        forces.add(force)
    }

    fun applyForce(fx: Float, fy: Float) {
        if (bodyType != BodyType.DYNAMIC) return
        forces.add(fx, fy)
    }

    fun applyImpulse(impulse: Vector2) {
        if (bodyType != BodyType.DYNAMIC) return
        velocity.add(impulse.x * inverseMass, impulse.y * inverseMass)
    }

    fun applyImpulse(ix: Float, iy: Float) {
        if (bodyType != BodyType.DYNAMIC) return
        velocity.add(ix * inverseMass, iy * inverseMass)
    }

    fun setVelocity(vx: Float, vy: Float): RigidBody2DComponent {
        velocity.set(vx, vy)
        return this
    }

    internal fun integrateForces(gravity: Vector2, dt: Float) {
        if (bodyType != BodyType.DYNAMIC) return

        // a = F / m + g * gravityScale
        acceleration.set(forces).scale(inverseMass)
        acceleration.add(gravity.x * gravityScale, gravity.y * gravityScale)

        // Integrate velocity: v = v + a * dt
        velocity.add(acceleration.x * dt, acceleration.y * dt)

        // Linear damping
        if (linearDamping > 0f) {
            val damp = (1f - linearDamping * dt).coerceAtLeast(0f)
            velocity.scale(damp)
        }

        // Clear forces
        forces.set(0f, 0f)
    }

    internal fun integrateVelocity(dt: Float) {
        val tr = transform ?: return
        if (bodyType == BodyType.STATIC) return

        // Position update: p = p + v * dt
        tr.position.add(velocity.x * dt, velocity.y * dt)

        if (!isFixedRotation && bodyType == BodyType.DYNAMIC) {
            tr.rotationDegrees += angularVelocity * dt
        }

        tr.markDirty()
    }
}
