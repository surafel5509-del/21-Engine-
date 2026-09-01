package com.example.engine.physics

import com.example.engine.components.BoxCollider2D
import com.example.engine.components.CircleCollider2D
import com.example.engine.components.Collider2DComponent
import com.example.engine.components.RigidBody2DComponent
import com.example.engine.math.Circle2D
import com.example.engine.math.MathUtils
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 2D Physics World handling rigid body dynamics, gravity, impulse collision resolution,
 * trigger zones, and raycasting.
 */
class PhysicsWorld2D(
    var gravity: Vector2 = Vector2(0f, 980f) // standard 2D gravity (pixels/s^2)
) {
    private val rigidBodies = mutableListOf<RigidBody2DComponent>()
    private val colliders = mutableListOf<Collider2DComponent>()

    // Tracking active collision and trigger pairs across frames for Enter/Stay/Exit events
    private val activeCollisionPairs = mutableSetOf<Pair<Collider2DComponent, Collider2DComponent>>()
    private val currentCollisionPairs = mutableSetOf<Pair<Collider2DComponent, Collider2DComponent>>()

    private val activeTriggerPairs = mutableSetOf<Pair<Collider2DComponent, Collider2DComponent>>()
    private val currentTriggerPairs = mutableSetOf<Pair<Collider2DComponent, Collider2DComponent>>()

    // Reusable buffers for zero-allocation checks
    private val tempAABB1 = Rect2D()
    private val tempAABB2 = Rect2D()
    private val tempCircle1 = Circle2D()
    private val tempCircle2 = Circle2D()
    private val manifold = ContactManifold()

    fun addRigidBody(body: RigidBody2DComponent) {
        if (!rigidBodies.contains(body)) {
            rigidBodies.add(body)
        }
    }

    fun removeRigidBody(body: RigidBody2DComponent) {
        rigidBodies.remove(body)
    }

    fun addCollider(collider: Collider2DComponent) {
        if (!colliders.contains(collider)) {
            colliders.add(collider)
        }
    }

    fun removeCollider(collider: Collider2DComponent) {
        colliders.remove(collider)
    }

    fun clear() {
        rigidBodies.clear()
        colliders.clear()
        activeCollisionPairs.clear()
        currentCollisionPairs.clear()
        activeTriggerPairs.clear()
        currentTriggerPairs.clear()
    }

    fun step(dt: Float) {
        // 1. Integrate forces (gravity, user forces) into velocity
        for (i in 0 until rigidBodies.size) {
            val body = rigidBodies[i]
            if (body.isEnabled && body.gameObject?.isActive == true) {
                body.integrateForces(gravity, dt)
            }
        }

        // 2. Integrate velocities into positions
        for (i in 0 until rigidBodies.size) {
            val body = rigidBodies[i]
            if (body.isEnabled && body.gameObject?.isActive == true) {
                body.integrateVelocity(dt)
            }
        }

        // 3. Collision detection and resolution
        currentCollisionPairs.clear()
        currentTriggerPairs.clear()

        val colliderCount = colliders.size
        for (i in 0 until colliderCount) {
            val cA = colliders[i]
            if (!cA.isEnabled || cA.gameObject?.isActive != true) continue

            for (j in i + 1 until colliderCount) {
                val cB = colliders[j]
                if (!cB.isEnabled || cB.gameObject?.isActive != true) continue
                if (!cA.canCollideWith(cB)) continue

                // Broadphase: AABB overlap check
                cA.getBoundingBox(tempAABB1)
                cB.getBoundingBox(tempAABB2)

                if (!tempAABB1.intersects(tempAABB2)) continue

                // Narrowphase
                if (testCollision(cA, cB, manifold)) {
                    val pair = Pair(cA, cB)
                    val isTrigger = cA.isTrigger || cB.isTrigger

                    if (isTrigger) {
                        currentTriggerPairs.add(pair)
                        if (!activeTriggerPairs.contains(pair)) {
                            cA.onTriggerEnter?.invoke(cB)
                            cB.onTriggerEnter?.invoke(cA)
                        } else {
                            cA.onTriggerStay?.invoke(cB)
                            cB.onTriggerStay?.invoke(cA)
                        }
                    } else {
                        currentCollisionPairs.add(pair)
                        resolveCollision(cA, cB, manifold)

                        if (!activeCollisionPairs.contains(pair)) {
                            cA.onCollisionEnter?.invoke(cB, manifold)
                            cB.onCollisionEnter?.invoke(cA, manifold)
                        } else {
                            cA.onCollisionStay?.invoke(cB)
                            cB.onCollisionStay?.invoke(cA)
                        }
                    }
                }
            }
        }

        // 4. Trigger & Collision Exits
        for (pair in activeTriggerPairs) {
            if (!currentTriggerPairs.contains(pair)) {
                pair.first.onTriggerExit?.invoke(pair.second)
                pair.second.onTriggerExit?.invoke(pair.first)
            }
        }
        activeTriggerPairs.clear()
        activeTriggerPairs.addAll(currentTriggerPairs)

        for (pair in activeCollisionPairs) {
            if (!currentCollisionPairs.contains(pair)) {
                pair.first.onCollisionExit?.invoke(pair.second)
                pair.second.onCollisionExit?.invoke(pair.first)
            }
        }
        activeCollisionPairs.clear()
        activeCollisionPairs.addAll(currentCollisionPairs)
    }

    private fun testCollision(cA: Collider2DComponent, cB: Collider2DComponent, outManifold: ContactManifold): Boolean {
        return when {
            cA is BoxCollider2D && cB is BoxCollider2D -> testBoxBox(cA, cB, outManifold)
            cA is CircleCollider2D && cB is CircleCollider2D -> testCircleCircle(cA, cB, outManifold)
            cA is BoxCollider2D && cB is CircleCollider2D -> testBoxCircle(cA, cB, outManifold)
            cA is CircleCollider2D && cB is BoxCollider2D -> {
                val hit = testBoxCircle(cB, cA, outManifold)
                if (hit) {
                    outManifold.normal.scale(-1f)
                }
                hit
            }
            else -> false
        }
    }

    private fun testBoxBox(boxA: BoxCollider2D, boxB: BoxCollider2D, outManifold: ContactManifold): Boolean {
        boxA.getBoundingBox(tempAABB1)
        boxB.getBoundingBox(tempAABB2)

        val dx = tempAABB2.centerX - tempAABB1.centerX
        val px = (tempAABB1.width + tempAABB2.width) * 0.5f - abs(dx)
        if (px <= 0f) return false

        val dy = tempAABB2.centerY - tempAABB1.centerY
        val py = (tempAABB1.height + tempAABB2.height) * 0.5f - abs(dy)
        if (py <= 0f) return false

        if (px < py) {
            val sx = if (dx < 0) -1f else 1f
            outManifold.normal.set(sx, 0f)
            outManifold.penetration = px
            outManifold.contactPoint.set(if (dx < 0) tempAABB1.left else tempAABB1.right, tempAABB1.centerY)
        } else {
            val sy = if (dy < 0) -1f else 1f
            outManifold.normal.set(0f, sy)
            outManifold.penetration = py
            outManifold.contactPoint.set(tempAABB1.centerX, if (dy < 0) tempAABB1.top else tempAABB1.bottom)
        }
        return true
    }

    private fun testCircleCircle(circleA: CircleCollider2D, circleB: CircleCollider2D, outManifold: ContactManifold): Boolean {
        circleA.getCircle(tempCircle1)
        circleB.getCircle(tempCircle2)

        val dx = tempCircle2.x - tempCircle1.x
        val dy = tempCircle2.y - tempCircle1.y
        val distSq = dx * dx + dy * dy
        val rSum = tempCircle1.radius + tempCircle2.radius

        if (distSq >= rSum * rSum) return false

        val dist = sqrt(distSq)
        if (dist > 0.0001f) {
            outManifold.normal.set(dx / dist, dy / dist)
            outManifold.penetration = rSum - dist
            outManifold.contactPoint.set(
                tempCircle1.x + outManifold.normal.x * tempCircle1.radius,
                tempCircle1.y + outManifold.normal.y * tempCircle1.radius
            )
        } else {
            outManifold.normal.set(0f, -1f)
            outManifold.penetration = tempCircle1.radius
            outManifold.contactPoint.set(tempCircle1.x, tempCircle1.y)
        }
        return true
    }

    private fun testBoxCircle(box: BoxCollider2D, circle: CircleCollider2D, outManifold: ContactManifold): Boolean {
        box.getBoundingBox(tempAABB1)
        circle.getCircle(tempCircle1)

        val closestX = MathUtils.clamp(tempCircle1.x, tempAABB1.left, tempAABB1.right)
        val closestY = MathUtils.clamp(tempCircle1.y, tempAABB1.top, tempAABB1.bottom)

        var dx = tempCircle1.x - closestX
        var dy = tempCircle1.y - closestY
        var distSq = dx * dx + dy * dy

        // Circle center is inside the box
        val inside = closestX == tempCircle1.x && closestY == tempCircle1.y
        if (inside) {
            val distLeft = tempCircle1.x - tempAABB1.left
            val distRight = tempAABB1.right - tempCircle1.x
            val distTop = tempCircle1.y - tempAABB1.top
            val distBottom = tempAABB1.bottom - tempCircle1.y

            val minDist = min(min(distLeft, distRight), min(distTop, distBottom))
            when (minDist) {
                distLeft -> {
                    outManifold.normal.set(-1f, 0f)
                    outManifold.penetration = tempCircle1.radius + distLeft
                }
                distRight -> {
                    outManifold.normal.set(1f, 0f)
                    outManifold.penetration = tempCircle1.radius + distRight
                }
                distTop -> {
                    outManifold.normal.set(0f, -1f)
                    outManifold.penetration = tempCircle1.radius + distTop
                }
                distBottom -> {
                    outManifold.normal.set(0f, 1f)
                    outManifold.penetration = tempCircle1.radius + distBottom
                }
            }
            outManifold.contactPoint.set(closestX, closestY)
            return true
        }

        if (distSq > tempCircle1.radius * tempCircle1.radius) return false

        val dist = sqrt(distSq)
        outManifold.normal.set(dx / dist, dy / dist)
        outManifold.penetration = tempCircle1.radius - dist
        outManifold.contactPoint.set(closestX, closestY)
        return true
    }

    private fun resolveCollision(cA: Collider2DComponent, cB: Collider2DComponent, manifold: ContactManifold) {
        val bodyA = cA.gameObject?.getComponent<RigidBody2DComponent>()
        val bodyB = cB.gameObject?.getComponent<RigidBody2DComponent>()

        val invMassA = bodyA?.inverseMass ?: 0f
        val invMassB = bodyB?.inverseMass ?: 0f
        val totalInvMass = invMassA + invMassB

        if (totalInvMass <= 0.00001f) return // Both are static/infinite mass

        // 1. Positional correction (Penetration separation)
        val percent = 0.8f // Penetration resolution rate
        val slop = 0.01f // Positional allowance
        val correctionMagnitude = (max(manifold.penetration - slop, 0f) / totalInvMass) * percent

        val transA = cA.transform
        val transB = cB.transform

        if (bodyA?.bodyType == BodyType.DYNAMIC && transA != null) {
            transA.position.subtract(
                manifold.normal.x * correctionMagnitude * invMassA,
                manifold.normal.y * correctionMagnitude * invMassA
            )
            transA.markDirty()
        }

        if (bodyB?.bodyType == BodyType.DYNAMIC && transB != null) {
            transB.position.add(
                manifold.normal.x * correctionMagnitude * invMassB,
                manifold.normal.y * correctionMagnitude * invMassB
            )
            transB.markDirty()
        }

        // 2. Velocity resolution (Impulse)
        val velA = bodyA?.velocity ?: Vector2.ZERO
        val velB = bodyB?.velocity ?: Vector2.ZERO

        val relVelX = velB.x - velA.x
        val relVelY = velB.y - velA.y
        val velAlongNormal = relVelX * manifold.normal.x + relVelY * manifold.normal.y

        // Moving apart already
        if (velAlongNormal > 0f) return

        val restA = bodyA?.restitution ?: 0.5f
        val restB = bodyB?.restitution ?: 0.5f
        val restitution = min(restA, restB)

        val impulseMagnitude = -(1f + restitution) * velAlongNormal / totalInvMass
        val impulseX = manifold.normal.x * impulseMagnitude
        val impulseY = manifold.normal.y * impulseMagnitude

        if (bodyA?.bodyType == BodyType.DYNAMIC) {
            bodyA.velocity.subtract(impulseX * invMassA, impulseY * invMassA)
        }
        if (bodyB?.bodyType == BodyType.DYNAMIC) {
            bodyB.velocity.add(impulseX * invMassB, impulseY * invMassB)
        }

        // 3. Friction impulse
        val frictionA = bodyA?.friction ?: 0.2f
        val frictionB = bodyB?.friction ?: 0.2f
        val friction = sqrt(frictionA * frictionB)

        // Tangent vector
        val tangentX = relVelX - (manifold.normal.x * velAlongNormal)
        val tangentY = relVelY - (manifold.normal.y * velAlongNormal)
        val tangentLen = sqrt(tangentX * tangentX + tangentY * tangentY)

        if (tangentLen > 0.0001f) {
            val normTangentX = tangentX / tangentLen
            val normTangentY = tangentY / tangentLen

            val frictionImpulseMag = -(relVelX * normTangentX + relVelY * normTangentY) / totalInvMass
            val clampedFriction = MathUtils.clamp(frictionImpulseMag, -impulseMagnitude * friction, impulseMagnitude * friction)

            if (bodyA?.bodyType == BodyType.DYNAMIC) {
                bodyA.velocity.subtract(normTangentX * clampedFriction * invMassA, normTangentY * clampedFriction * invMassA)
            }
            if (bodyB?.bodyType == BodyType.DYNAMIC) {
                bodyB.velocity.add(normTangentX * clampedFriction * invMassB, normTangentY * clampedFriction * invMassB)
            }
        }
    }

    fun raycast(origin: Vector2, direction: Vector2, maxDistance: Float = 1000f, layerMask: Int = CollisionLayers.ALL): RaycastHit? {
        var closestHit: RaycastHit? = null
        var minDistance = maxDistance

        val normDir = direction.normalized()

        for (collider in colliders) {
            if (!collider.isEnabled || collider.gameObject?.isActive != true) continue
            if ((collider.layer and layerMask) == 0) continue

            when (collider) {
                is CircleCollider2D -> {
                    collider.getCircle(tempCircle1)
                    val ox = origin.x - tempCircle1.x
                    val oy = origin.y - tempCircle1.y
                    val b = 2f * (normDir.x * ox + normDir.y * oy)
                    val c = ox * ox + oy * oy - tempCircle1.radius * tempCircle1.radius
                    val disc = b * b - 4f * c
                    if (disc >= 0f) {
                        val t = (-b - sqrt(disc)) / 2f
                        if (t in 0f..minDistance) {
                            minDistance = t
                            val hitPoint = Vector2(origin.x + normDir.x * t, origin.y + normDir.y * t)
                            val hitNormal = Vector2(hitPoint.x - tempCircle1.x, hitPoint.y - tempCircle1.y).normalize()
                            closestHit = RaycastHit(collider, hitPoint, hitNormal, t)
                        }
                    }
                }
                is BoxCollider2D -> {
                    collider.getBoundingBox(tempAABB1)
                    var tmin = (tempAABB1.left - origin.x) / if (normDir.x != 0f) normDir.x else 0.00001f
                    var tmax = (tempAABB1.right - origin.x) / if (normDir.x != 0f) normDir.x else 0.00001f
                    if (tmin > tmax) {
                        val tmp = tmin; tmin = tmax; tmax = tmp
                    }

                    var tymin = (tempAABB1.top - origin.y) / if (normDir.y != 0f) normDir.y else 0.00001f
                    var tymax = (tempAABB1.bottom - origin.y) / if (normDir.y != 0f) normDir.y else 0.00001f
                    if (tymin > tymax) {
                        val tmp = tymin; tymin = tymax; tymax = tmp
                    }

                    if (tmin <= tymax && tymin <= tmax) {
                        val tEntry = max(tmin, tymin)
                        if (tEntry in 0f..minDistance) {
                            minDistance = tEntry
                            val hitPoint = Vector2(origin.x + normDir.x * tEntry, origin.y + normDir.y * tEntry)
                            val hitNormal = if (tEntry == tmin) {
                                Vector2(if (normDir.x < 0) 1f else -1f, 0f)
                            } else {
                                Vector2(0f, if (normDir.y < 0) 1f else -1f)
                            }
                            closestHit = RaycastHit(collider, hitPoint, hitNormal, tEntry)
                        }
                    }
                }
            }
        }
        return closestHit
    }
}
