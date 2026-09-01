package com.example.engine.core

import android.graphics.Color
import com.example.engine.viewport.ScaleMode

/**
 * Engine configuration options.
 */
data class EngineConfig(
    var logicalWidth: Float = 1280f,
    var logicalHeight: Float = 720f,
    var targetFps: Int = 60,
    var fixedTimeStep: Float = 1f / 60f,
    var maxSubSteps: Int = 5,
    var scaleMode: ScaleMode = ScaleMode.FIT_LETTERBOX,
    var clearColor: Int = Color.BLACK,
    var isDebugMode: Boolean = true,
    var showFps: Boolean = true,
    var showColliders: Boolean = false,
    var showSafeAreas: Boolean = false
)
