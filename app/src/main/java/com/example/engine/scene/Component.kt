package com.example.engine.scene

import com.example.engine.rendering.Renderer

/**
 * Base Component class in the Entity-Component-System architecture.
 * Components contain reusable logic, rendering, physics, or data attached to a GameObject.
 */
abstract class Component {
    var gameObject: GameObject? = null
        internal set

    var isEnabled: Boolean = true

    open fun onAttach() {}
    open fun onDetach() {}
    open fun onStart() {}
    open fun onUpdate(dt: Float) {}
    open fun onFixedUpdate(fixedDt: Float) {}
    open fun onRender(renderer: Renderer) {}
    open fun onDestroy() {}

    val transform get() = gameObject?.transform
    val scene get() = gameObject?.scene
}
