package com.example.engine.physics

import com.example.engine.components.BoxCollider2D
import com.example.engine.components.RigidBody2DComponent
import com.example.engine.math.Vector2
import com.example.engine.scene.GameObject
import org.junit.Assert.*
import org.junit.Test

class PhysicsWorld2DTest {

    @Test
    fun testPhysicsIntegrationAndGravity() {
        val physicsWorld = PhysicsWorld2D(gravity = Vector2(0f, 100f))

        val go = GameObject("FallingObject")
        val body = go.addComponent(RigidBody2DComponent().apply {
            bodyType = BodyType.DYNAMIC
            useGravity = true
        })

        physicsWorld.addRigidBody(body)

        // Step 1 second
        physicsWorld.step(1f)

        // Velocity after 1 second under gravity=100 should be (0, 100)
        assertEquals(0f, body.velocity.x, 0.001f)
        assertEquals(100f, body.velocity.y, 0.001f)
        // Position after 1 second should be (0, 100)
        assertEquals(0f, go.transform.position.x, 0.001f)
        assertEquals(100f, go.transform.position.y, 0.001f)
    }

    @Test
    fun testRaycastBoxHit() {
        val physicsWorld = PhysicsWorld2D()

        val go = GameObject("TargetBox")
        go.setPosition(100f, 0f)
        val box = go.addComponent(BoxCollider2D().apply {
            width = 40f
            height = 40f
        })
        physicsWorld.addCollider(box)

        val hit = physicsWorld.raycast(origin = Vector2(0f, 0f), direction = Vector2(1f, 0f), maxDistance = 200f)

        assertNotNull(hit)
        assertEquals(box, hit?.collider)
        assertEquals(80f, hit?.hitPoint?.x ?: 0f, 0.01f)
    }
}
