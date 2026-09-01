package com.example.engine.scene

import com.example.engine.math.Transform2D
import com.example.engine.math.Vector2
import com.example.engine.rendering.RenderLayer
import com.example.engine.rendering.Renderer
import kotlin.reflect.KClass

/**
 * Fundamental Entity in the game world.
 * Holds a Transform2D, tag, layer, z-order, child GameObjects, and attached Components.
 */
class GameObject(
    var name: String = "GameObject",
    var tag: String = "Untagged"
) {
    val transform = Transform2D()
    var layer: RenderLayer = RenderLayer.WORLD_MAIN
    var zOrder: Int = 0

    var isActive: Boolean = true
    var isVisible: Boolean = true
    var isDestroyed: Boolean = false
        private set

    var scene: Scene? = null
        internal set

    var parent: GameObject? = null
        private set
    private val children = mutableListOf<GameObject>()

    @PublishedApi
    internal val componentList = mutableListOf<Component>()
    private var hasStarted = false

    fun setPosition(x: Float, y: Float): GameObject {
        transform.setPosition(x, y)
        return this
    }

    fun setPosition(pos: Vector2): GameObject = setPosition(pos.x, pos.y)

    fun setRotation(degrees: Float): GameObject {
        transform.setRotation(degrees)
        return this
    }

    fun setScale(sx: Float, sy: Float): GameObject {
        transform.setScale(sx, sy)
        return this
    }

    fun setScale(uniform: Float): GameObject = setScale(uniform, uniform)

    fun addChild(child: GameObject): GameObject {
        if (child.parent != this) {
            child.parent?.removeChild(child)
            child.parent = this
            children.add(child)
            transform.addChild(child.transform)
            if (scene != null && child.scene == null) {
                scene?.addGameObject(child)
            }
        }
        return this
    }

    fun removeChild(child: GameObject) {
        if (children.remove(child)) {
            child.parent = null
            transform.removeChild(child.transform)
        }
    }

    val childCount: Int get() = children.size
    fun getChildAt(index: Int): GameObject = children[index]

    fun <T : Component> addComponent(component: T): T {
        component.gameObject = this
        componentList.add(component)
        component.onAttach()
        if (hasStarted && component.isEnabled) {
            component.onStart()
        }
        return component
    }

    inline fun <reified T : Component> getComponent(): T? {
        for (i in 0 until componentList.size) {
            val c = componentList[i]
            if (c is T) return c
        }
        return null
    }

    inline fun <reified T : Component> getComponents(): List<T> {
        val result = mutableListOf<T>()
        for (i in 0 until componentList.size) {
            val c = componentList[i]
            if (c is T) result.add(c)
        }
        return result
    }

    fun removeComponent(component: Component) {
        if (componentList.remove(component)) {
            component.onDetach()
            component.gameObject = null
        }
    }

    internal fun start() {
        if (hasStarted) return
        hasStarted = true
        for (i in 0 until componentList.size) {
            val c = componentList[i]
            if (c.isEnabled) c.onStart()
        }
    }

    internal fun update(dt: Float) {
        if (!isActive || isDestroyed) return
        if (!hasStarted) start()

        for (i in 0 until componentList.size) {
            val c = componentList[i]
            if (c.isEnabled) c.onUpdate(dt)
        }

        for (i in 0 until children.size) {
            children[i].update(dt)
        }
    }

    internal fun fixedUpdate(fixedDt: Float) {
        if (!isActive || isDestroyed) return

        for (i in 0 until componentList.size) {
            val c = componentList[i]
            if (c.isEnabled) c.onFixedUpdate(fixedDt)
        }

        for (i in 0 until children.size) {
            children[i].fixedUpdate(fixedDt)
        }
    }

    internal fun render(renderer: Renderer) {
        if (!isActive || !isVisible || isDestroyed) return

        for (i in 0 until componentList.size) {
            val c = componentList[i]
            if (c.isEnabled) c.onRender(renderer)
        }

        for (i in 0 until children.size) {
            children[i].render(renderer)
        }
    }

    fun destroy() {
        if (isDestroyed) return
        isDestroyed = true

        for (i in 0 until componentList.size) {
            componentList[i].onDestroy()
            componentList[i].onDetach()
            componentList[i].gameObject = null
        }
        componentList.clear()

        for (i in 0 until children.size) {
            children[i].destroy()
        }
        children.clear()
    }
}
