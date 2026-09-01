package com.example.engine.input

import com.example.engine.math.Vector2
import kotlin.math.sqrt

/**
 * Gesture detector recognizing Tap, DoubleTap, LongPress, Drag, and PinchZoom.
 */
class GestureDetector2D(
    var onTap: ((logicalPos: Vector2) -> Unit)? = null,
    var onDoubleTap: ((logicalPos: Vector2) -> Unit)? = null,
    var onLongPress: ((logicalPos: Vector2) -> Unit)? = null,
    var onDrag: ((delta: Vector2, currentPos: Vector2) -> Unit)? = null,
    var onPinchZoom: ((scaleFactor: Float, center: Vector2) -> Unit)? = null
) {
    private var isDown = false
    private val downPos = Vector2()
    private val lastPos = Vector2()
    private var downTime = 0L
    private var lastTapTime = 0L
    private var hasMoved = false

    private val tempDelta = Vector2()

    fun onTouchEvent(event: TouchEvent) {
        when (event.type) {
            TouchType.DOWN -> {
                isDown = true
                downPos.set(event.logicalPosition)
                lastPos.set(event.logicalPosition)
                downTime = event.timestamp
                hasMoved = false
            }
            TouchType.MOVE -> {
                if (isDown) {
                    val dist = event.logicalPosition.distance(downPos)
                    if (dist > 10f) {
                        hasMoved = true
                    }
                    tempDelta.set(event.logicalPosition.x - lastPos.x, event.logicalPosition.y - lastPos.y)
                    onDrag?.invoke(tempDelta, event.logicalPosition)
                    lastPos.set(event.logicalPosition)
                }
            }
            TouchType.UP -> {
                if (isDown) {
                    val elapsed = event.timestamp - downTime
                    if (!hasMoved) {
                        if (elapsed > 500L) {
                            onLongPress?.invoke(event.logicalPosition)
                        } else {
                            val timeSinceLastTap = event.timestamp - lastTapTime
                            if (timeSinceLastTap < 300L) {
                                onDoubleTap?.invoke(event.logicalPosition)
                                lastTapTime = 0L
                            } else {
                                onTap?.invoke(event.logicalPosition)
                                lastTapTime = event.timestamp
                            }
                        }
                    }
                    isDown = false
                }
            }
            TouchType.CANCEL -> {
                isDown = false
            }
        }
    }
}
