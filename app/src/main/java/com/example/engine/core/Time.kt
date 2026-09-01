package com.example.engine.core

import com.example.engine.math.MathUtils

/**
 * Global game time provider.
 */
object Time {
    /** Delta time in seconds of the current frame (scaled by timeScale) */
    var deltaTime: Float = 0.016f
        internal set

    /** Unscaled delta time in seconds (real-world frame duration) */
    var unscaledDeltaTime: Float = 0.016f
        internal set

    /** Fixed delta time in seconds for physics steps */
    var fixedDeltaTime: Float = 1f / 60f
        internal set

    /** Time scale modifier (1.0 = normal, 0.5 = slow motion, 2.0 = double speed, 0.0 = paused) */
    var timeScale: Float = 1.0f
        set(value) {
            field = MathUtils.clamp(value, 0f, 10f)
        }

    /** Total elapsed scaled game time in seconds */
    var totalTime: Float = 0f
        internal set

    /** Total elapsed real-world time in seconds */
    var totalRealTime: Float = 0f
        internal set

    /** Total rendered frame count since engine startup */
    var frameCount: Long = 0L
        internal set
}
