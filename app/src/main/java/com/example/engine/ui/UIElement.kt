package com.example.engine.ui

import com.example.engine.input.TouchEvent
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import com.example.engine.rendering.Renderer
import com.example.engine.viewport.SafeAreaInsets

/**
 * Base abstract class for responsive UI Elements.
 */
abstract class UIElement(
    var width: Float = 100f,
    var height: Float = 40f
) {
    val layout = LayoutPosition()
    val computedPosition = Vector2()
    val bounds = Rect2D()

    var isVisible: Boolean = true
    var isEnabled: Boolean = true
    var parent: UIElement? = null
    val children = mutableListOf<UIElement>()

    fun setSize(w: Float, h: Float): UIElement {
        width = w
        height = h
        return this
    }

    fun setAnchor(anchor: Anchor, offsetX: Float = 0f, offsetY: Float = 0f): UIElement {
        layout.anchor = anchor
        layout.offsetX = offsetX
        layout.offsetY = offsetY
        return this
    }

    fun addChild(child: UIElement): UIElement {
        if (child.parent != this) {
            child.parent?.removeChild(child)
            child.parent = this
            children.add(child)
        }
        return this
    }

    fun removeChild(child: UIElement) {
        if (children.remove(child)) {
            child.parent = null
        }
    }

    open fun updateLayout(containerWidth: Float, containerHeight: Float, safeArea: SafeAreaInsets) {
        layout.calculate(containerWidth, containerHeight, safeArea, width, height, computedPosition)
        bounds.set(computedPosition.x, computedPosition.y, width, height)

        for (i in 0 until children.size) {
            children[i].updateLayout(width, height, safeArea)
        }
    }

    open fun update(dt: Float) {
        if (!isVisible) return
        for (i in 0 until children.size) {
            children[i].update(dt)
        }
    }

    open fun render(renderer: Renderer) {
        if (!isVisible) return
        onRender(renderer)
        for (i in 0 until children.size) {
            children[i].render(renderer)
        }
    }

    protected abstract fun onRender(renderer: Renderer)

    open fun handleTouchEvent(event: TouchEvent): Boolean {
        if (!isVisible || !isEnabled) return false

        // Children get first priority
        for (i in children.size - 1 downTo 0) {
            if (children[i].handleTouchEvent(event)) {
                return true
            }
        }

        return onTouchEvent(event)
    }

    protected open fun onTouchEvent(event: TouchEvent): Boolean = false

    fun containsPoint(logicalX: Float, logicalY: Float): Boolean {
        return bounds.contains(logicalX, logicalY)
    }
}
