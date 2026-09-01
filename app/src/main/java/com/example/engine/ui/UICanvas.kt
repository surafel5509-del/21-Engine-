package com.example.engine.ui

import com.example.engine.input.TouchEvent
import com.example.engine.rendering.Renderer
import com.example.engine.viewport.ViewportManager

/**
 * Root container for UI Elements.
 * Renders in screen/logical UI space independently of world camera transformations.
 */
class UICanvas(
    val viewportManager: ViewportManager
) {
    private val elements = mutableListOf<UIElement>()

    fun addElement(element: UIElement): UIElement {
        if (!elements.contains(element)) {
            elements.add(element)
        }
        return element
    }

    fun removeElement(element: UIElement) {
        elements.remove(element)
    }

    fun clear() {
        elements.clear()
    }

    fun updateLayout() {
        val w = viewportManager.logicalWidth
        val h = viewportManager.logicalHeight
        val safeArea = viewportManager.logicalSafeArea

        for (i in 0 until elements.size) {
            elements[i].updateLayout(w, h, safeArea)
        }
    }

    fun update(dt: Float) {
        for (i in 0 until elements.size) {
            elements[i].update(dt)
        }
    }

    fun render(renderer: Renderer) {
        // UI renders in logical screen coordinates (camera not active)
        for (i in 0 until elements.size) {
            elements[i].render(renderer)
        }
    }

    fun handleTouchEvent(event: TouchEvent): Boolean {
        // Topmost elements get first touch events
        for (i in elements.size - 1 downTo 0) {
            if (elements[i].handleTouchEvent(event)) {
                return true
            }
        }
        return false
    }
}
