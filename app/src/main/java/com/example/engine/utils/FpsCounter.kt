package com.example.engine.utils

/**
 * Rolling average FPS counter and frame timing diagnostics.
 */
class FpsCounter(private val sampleCount: Int = 30) {
    private val frameTimes = FloatArray(sampleCount)
    private var sampleIndex = 0
    private var isFilled = false

    var currentFps: Float = 60f
        private set

    var averageFrameTimeMs: Float = 16.6f
        private set

    var minFrameTimeMs: Float = 16.6f
        private set

    var maxFrameTimeMs: Float = 16.6f
        private set

    fun onFrame(dt: Float) {
        val dtMs = dt * 1000f
        frameTimes[sampleIndex] = dtMs
        sampleIndex = (sampleIndex + 1) % sampleCount
        if (sampleIndex == 0) isFilled = true

        val count = if (isFilled) sampleCount else sampleIndex
        if (count == 0) return

        var sum = 0f
        var min = Float.MAX_VALUE
        var max = 0f

        for (i in 0 until count) {
            val t = frameTimes[i]
            sum += t
            if (t < min) min = t
            if (t > max) max = t
        }

        averageFrameTimeMs = sum / count
        minFrameTimeMs = min
        maxFrameTimeMs = max
        currentFps = if (averageFrameTimeMs > 0.001f) 1000f / averageFrameTimeMs else 0f
    }
}
