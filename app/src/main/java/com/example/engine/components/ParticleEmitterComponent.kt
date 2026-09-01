package com.example.engine.components

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.math.MathUtils
import com.example.engine.math.Vector2
import com.example.engine.rendering.Renderer
import com.example.engine.scene.Component
import com.example.engine.utils.Pool
import kotlin.math.cos
import kotlin.math.sin

class Particle {
    val position = Vector2()
    val velocity = Vector2()
    val acceleration = Vector2()
    var startColor: Int = Color.WHITE
    var endColor: Int = Color.TRANSPARENT
    var startSize: Float = 10f
    var endSize: Float = 0f
    var currentSize: Float = 10f
    var currentColor: Int = Color.WHITE
    var lifetime: Float = 1.0f
    var age: Float = 0f
    var isAlive: Boolean = false

    fun reset() {
        position.set(0f, 0f)
        velocity.set(0f, 0f)
        acceleration.set(0f, 0f)
        age = 0f
        isAlive = false
    }
}

/**
 * High performance 2D particle emitter with continuous emission, burst effects, and object pooling.
 */
class ParticleEmitterComponent(
    var maxParticles: Int = 200,
    var emissionRate: Float = 50f, // particles per second
    var particleLifetimeMin: Float = 0.5f,
    var particleLifetimeMax: Float = 1.2f,
    var speedMin: Float = 50f,
    var speedMax: Float = 150f,
    var angleSpreadDegrees: Float = 360f,
    var baseAngleDegrees: Float = 0f,
    var startColor: Int = 0xFFFF9800.toInt(),
    var endColor: Int = 0x00FF5722.toInt(),
    var startSize: Float = 12f,
    var endSize: Float = 2f,
    var gravity: Vector2 = Vector2(0f, 150f),
    var isEmitting: Boolean = true
) : Component() {

    private val particlePool = Pool(
        initialCapacity = 64,
        maxCapacity = maxParticles,
        factory = { Particle() },
        reset = { it.reset() }
    )

    private val activeParticles = ArrayList<Particle>(maxParticles)
    private var emissionAccumulator = 0f

    override fun onUpdate(dt: Float) {
        val tr = transform
        val emitPos = tr?.worldPosition ?: Vector2.ZERO

        // Continuous emission
        if (isEmitting && emissionRate > 0f) {
            emissionAccumulator += dt * emissionRate
            val countToEmit = emissionAccumulator.toInt()
            if (countToEmit > 0) {
                emissionAccumulator -= countToEmit
                burst(countToEmit, emitPos)
            }
        }

        // Update active particles
        var i = 0
        while (i < activeParticles.size) {
            val p = activeParticles[i]
            p.age += dt
            if (p.age >= p.lifetime) {
                p.isAlive = false
                particlePool.free(p)
                activeParticles.removeAt(i)
                continue
            }

            // Physics update
            p.velocity.add(p.acceleration.x * dt, p.acceleration.y * dt)
            p.velocity.add(gravity.x * dt, gravity.y * dt)
            p.position.add(p.velocity.x * dt, p.velocity.y * dt)

            // Interpolate size & color
            val t = MathUtils.clamp01(p.age / p.lifetime)
            p.currentSize = MathUtils.lerp(p.startSize, p.endSize, t)

            val startA = Color.alpha(p.startColor)
            val startR = Color.red(p.startColor)
            val startG = Color.green(p.startColor)
            val startB = Color.blue(p.startColor)

            val endA = Color.alpha(p.endColor)
            val endR = Color.red(p.endColor)
            val endG = Color.green(p.endColor)
            val endB = Color.blue(p.endColor)

            val a = MathUtils.lerp(startA.toFloat(), endA.toFloat(), t).toInt()
            val r = MathUtils.lerp(startR.toFloat(), endR.toFloat(), t).toInt()
            val g = MathUtils.lerp(startG.toFloat(), endG.toFloat(), t).toInt()
            val b = MathUtils.lerp(startB.toFloat(), endB.toFloat(), t).toInt()

            p.currentColor = Color.argb(a, r, g, b)
            i++
        }
    }

    fun burst(count: Int, worldPosition: Vector2? = null) {
        val origin = worldPosition ?: transform?.worldPosition ?: Vector2.ZERO
        val spawnCount = minOf(count, maxParticles - activeParticles.size)

        for (i in 0 until spawnCount) {
            val p = particlePool.obtain()
            p.position.set(origin)
            val angle = MathUtils.degToRad(baseAngleDegrees + MathUtils.randomRange(-angleSpreadDegrees * 0.5f, angleSpreadDegrees * 0.5f))
            val speed = MathUtils.randomRange(speedMin, speedMax)
            p.velocity.set(cos(angle) * speed, sin(angle) * speed)
            p.lifetime = MathUtils.randomRange(particleLifetimeMin, particleLifetimeMax)
            p.startColor = startColor
            p.endColor = endColor
            p.startSize = startSize
            p.endSize = endSize
            p.currentSize = startSize
            p.currentColor = startColor
            p.age = 0f
            p.isAlive = true
            activeParticles.add(p)
        }
    }

    override fun onRender(renderer: Renderer) {
        for (i in 0 until activeParticles.size) {
            val p = activeParticles[i]
            if (p.isAlive) {
                renderer.drawCircle(
                    x = p.position.x,
                    y = p.position.y,
                    radius = p.currentSize * 0.5f,
                    color = p.currentColor,
                    style = Paint.Style.FILL
                )
            }
        }
    }

    override fun onDestroy() {
        for (p in activeParticles) {
            particlePool.free(p)
        }
        activeParticles.clear()
    }
}
