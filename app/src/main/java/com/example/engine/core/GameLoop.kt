package com.example.engine.core

import android.os.Process
import com.example.engine.utils.Logger
import kotlin.math.min

/**
 * Dedicated high-performance Game Loop thread.
 * Implements the standard canonical fixed timestep accumulator pattern for deterministic physics updates,
 * interpolated rendering, battery-aware thread sleep, and pause/resume lifecycle.
 */
class GameLoop(
    private val engine: GameEngine
) : Runnable {
    companion object {
        private const val TAG = "GameLoop"
    }

    @Volatile
    private var isRunning = false

    @Volatile
    private var isPaused = false

    private val pauseLock = java.lang.Object()
    private var gameThread: Thread? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false
        gameThread = Thread(this, "GameEngineLoopThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
        Logger.d(TAG, "Game loop started")
    }

    fun pause() {
        synchronized(pauseLock) {
            isPaused = true
        }
        Logger.d(TAG, "Game loop paused")
    }

    fun resume() {
        synchronized(pauseLock) {
            isPaused = false
            pauseLock.notifyAll()
        }
        Logger.d(TAG, "Game loop resumed")
    }

    fun stop() {
        isRunning = false
        resume()
        try {
            gameThread?.join(500)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null
        Logger.d(TAG, "Game loop stopped")
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)

        val targetFrameDurationNs = (1_000_000_000.0 / engine.config.targetFps).toLong()
        val fixedDeltaTimeSec = engine.config.fixedTimeStep
        val fixedDeltaTimeNs = (fixedDeltaTimeSec * 1_000_000_000L).toLong()
        val maxAccumulatedNs = (fixedDeltaTimeSec * engine.config.maxSubSteps * 1_000_000_000L).toLong()

        var lastTimeNs = System.nanoTime()
        var accumulatorNs = 0L

        Time.fixedDeltaTime = fixedDeltaTimeSec

        while (isRunning) {
            synchronized(pauseLock) {
                while (isPaused && isRunning) {
                    try {
                        pauseLock.wait()
                        lastTimeNs = System.nanoTime()
                        accumulatorNs = 0L
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return
                    }
                }
            }

            if (!isRunning) break

            val currentTimeNs = System.nanoTime()
            var elapsedNs = currentTimeNs - lastTimeNs
            lastTimeNs = currentTimeNs

            // Prevent spiral of death on long lag spikes
            if (elapsedNs > maxAccumulatedNs) {
                elapsedNs = maxAccumulatedNs
            }

            val unscaledDt = elapsedNs / 1_000_000_000f
            val scaledDt = unscaledDt * Time.timeScale

            Time.unscaledDeltaTime = unscaledDt
            Time.deltaTime = scaledDt
            Time.totalRealTime += unscaledDt
            Time.totalTime += scaledDt
            Time.frameCount++

            accumulatorNs += (scaledDt * 1_000_000_000f).toLong()

            // 1. Fixed Timestep Updates (Physics simulation)
            var subSteps = 0
            while (accumulatorNs >= fixedDeltaTimeNs && subSteps < engine.config.maxSubSteps) {
                engine.fixedUpdate(fixedDeltaTimeSec)
                accumulatorNs -= fixedDeltaTimeNs
                subSteps++
            }

            // 2. Variable Timestep Updates (Animations, Input, Scripts)
            engine.update(scaledDt)

            // 3. Render
            engine.render()

            // 4. Frame Limiting / Battery Sleeping
            val frameTimeNs = System.nanoTime() - currentTimeNs
            val sleepTimeNs = targetFrameDurationNs - frameTimeNs
            if (sleepTimeNs > 2_000_000L) {
                try {
                    Thread.sleep(sleepTimeNs / 1_000_000L, (sleepTimeNs % 1_000_000L).toInt())
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }
}
