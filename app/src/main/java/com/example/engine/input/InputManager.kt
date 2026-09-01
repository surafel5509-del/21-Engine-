package com.example.engine.input

import android.view.KeyEvent
import android.view.MotionEvent
import com.example.engine.camera.Camera2D
import com.example.engine.math.Vector2
import com.example.engine.utils.Pool
import com.example.engine.viewport.ViewportManager

/**
 * Engine input manager tracking multi-touch pointers, gestures, and keyboard/gamepad states.
 */
class InputManager(
    val viewportManager: ViewportManager
) {
    private val activeTouches = mutableMapOf<Int, TouchEvent>()
    private val touchListeners = mutableListOf<(TouchEvent) -> Unit>()
    private val pressedKeys = mutableSetOf<Int>()

    private val touchEventPool = Pool(
        initialCapacity = 16,
        maxCapacity = 64,
        factory = { TouchEvent() }
    )

    private val tempVec1 = Vector2()
    private val tempVec2 = Vector2()

    var activeCamera: Camera2D? = null

    fun addTouchListener(listener: (TouchEvent) -> Unit) {
        if (!touchListeners.contains(listener)) {
            touchListeners.add(listener)
        }
    }

    fun removeTouchListener(listener: (TouchEvent) -> Unit) {
        touchListeners.remove(listener)
    }

    fun processMotionEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        val actionIndex = event.actionIndex
        val pointerCount = event.pointerCount

        when (actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = event.getPointerId(actionIndex)
                val screenX = event.getX(actionIndex)
                val screenY = event.getY(actionIndex)
                dispatchTouch(TouchType.DOWN, pointerId, screenX, screenY, event.getPressure(actionIndex))
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until pointerCount) {
                    val pointerId = event.getPointerId(i)
                    val screenX = event.getX(i)
                    val screenY = event.getY(i)
                    dispatchTouch(TouchType.MOVE, pointerId, screenX, screenY, event.getPressure(i))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.getPointerId(actionIndex)
                val screenX = event.getX(actionIndex)
                val screenY = event.getY(actionIndex)
                dispatchTouch(TouchType.UP, pointerId, screenX, screenY, event.getPressure(actionIndex))
            }
            MotionEvent.ACTION_CANCEL -> {
                for (i in 0 until pointerCount) {
                    val pointerId = event.getPointerId(i)
                    val screenX = event.getX(i)
                    val screenY = event.getY(i)
                    dispatchTouch(TouchType.CANCEL, pointerId, screenX, screenY, event.getPressure(i))
                }
                activeTouches.clear()
            }
        }
        return true
    }

    private fun dispatchTouch(type: TouchType, pointerId: Int, screenX: Float, screenY: Float, pressure: Float) {
        viewportManager.screenToLogical(screenX, screenY, tempVec1)

        val cam = activeCamera
        if (cam != null) {
            cam.logicalToWorld(tempVec1.x, tempVec1.y, tempVec2)
        } else {
            tempVec2.set(tempVec1)
        }

        val touchEvent = touchEventPool.obtain().set(
            type = type,
            pointerId = pointerId,
            screenX = screenX,
            screenY = screenY,
            logicalX = tempVec1.x,
            logicalY = tempVec1.y,
            worldX = tempVec2.x,
            worldY = tempVec2.y,
            pressure = pressure
        )

        if (type == TouchType.DOWN || type == TouchType.MOVE) {
            activeTouches[pointerId] = touchEvent
        } else {
            activeTouches.remove(pointerId)
        }

        for (i in 0 until touchListeners.size) {
            touchListeners[i](touchEvent)
        }

        touchEventPool.free(touchEvent)
    }

    fun isPointerDown(pointerId: Int = 0): Boolean = activeTouches.containsKey(pointerId)

    fun getPointerLogicalPosition(pointerId: Int = 0, outVec: Vector2): Boolean {
        val touch = activeTouches[pointerId] ?: return false
        outVec.set(touch.logicalPosition)
        return true
    }

    fun getPointerWorldPosition(pointerId: Int = 0, outVec: Vector2): Boolean {
        val touch = activeTouches[pointerId] ?: return false
        outVec.set(touch.worldPosition)
        return true
    }

    val activePointerCount: Int get() = activeTouches.size

    // Keyboard & Gamepad
    fun processKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            pressedKeys.add(event.keyCode)
        } else if (event.action == KeyEvent.ACTION_UP) {
            pressedKeys.remove(event.keyCode)
        }
        return false
    }

    fun isKeyPressed(keyCode: Int): Boolean = pressedKeys.contains(keyCode)
}
