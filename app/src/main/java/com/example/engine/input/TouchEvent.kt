package com.example.engine.input

import com.example.engine.math.Vector2

enum class TouchType {
    DOWN,
    MOVE,
    UP,
    CANCEL
}

/**
 * Clean multi-touch event abstraction independent of Android MotionEvent.
 * Contains physical screen position, logical game resolution position, and world position.
 */
data class TouchEvent(
    var type: TouchType = TouchType.DOWN,
    var pointerId: Int = 0,
    val screenPosition: Vector2 = Vector2(),
    val logicalPosition: Vector2 = Vector2(),
    val worldPosition: Vector2 = Vector2(),
    var pressure: Float = 1.0f,
    var timestamp: Long = 0L,
    var isConsumed: Boolean = false
) {
    fun set(
        type: TouchType,
        pointerId: Int,
        screenX: Float,
        screenY: Float,
        logicalX: Float,
        logicalY: Float,
        worldX: Float,
        worldY: Float,
        pressure: Float = 1.0f,
        timestamp: Long = System.currentTimeMillis()
    ): TouchEvent {
        this.type = type
        this.pointerId = pointerId
        this.screenPosition.set(screenX, screenY)
        this.logicalPosition.set(logicalX, logicalY)
        this.worldPosition.set(worldX, worldY)
        this.pressure = pressure
        this.timestamp = timestamp
        this.isConsumed = false
        return this
    }

    fun consume() {
        isConsumed = true
    }
}
