package com.example.engine.scene

import com.example.engine.camera.Camera2D
import com.example.engine.core.GameEngine
import com.example.engine.input.TouchEvent
import com.example.engine.physics.PhysicsWorld2D
import com.example.engine.rendering.Renderer
import com.example.engine.ui.UICanvas

/**
 * Base Scene class representing an active game state/level.
 * Encapsulates game objects, camera, physics simulation, UI canvas, and lifecycle events.
 */
abstract class Scene {
    var engine: GameEngine? = null
        internal set

    val camera = Camera2D()
    val physicsWorld = PhysicsWorld2D()
    lateinit var uiCanvas: UICanvas
        private set

    private val rootGameObjects = mutableListOf<GameObject>()
    private val pendingAddObjects = mutableListOf<GameObject>()
    private val pendingRemoveObjects = mutableListOf<GameObject>()

    var isInitialized = false
        private set

    internal fun init(engine: GameEngine) {
        this.engine = engine
        uiCanvas = UICanvas(engine.viewportManager)
        camera.onResize(engine.viewportManager.logicalWidth, engine.viewportManager.logicalHeight)
        onCreate()
        isInitialized = true
    }

    // Lifecycle Callbacks
    open fun onCreate() {}
    open fun onEnter() {}
    open fun onExit() {}
    open fun onPause() {}
    open fun onResume() {}
    open fun onDestroy() {}

    open fun onResize(newWidth: Int, newHeight: Int) {
        engine?.let {
            camera.onResize(it.viewportManager.logicalWidth, it.viewportManager.logicalHeight)
        }
        uiCanvas.updateLayout()
    }

    fun addGameObject(gameObject: GameObject): GameObject {
        gameObject.scene = this
        pendingAddObjects.add(gameObject)

        // Automatically register physics components
        gameObject.getComponent<com.example.engine.components.RigidBody2DComponent>()?.let {
            physicsWorld.addRigidBody(it)
        }
        gameObject.getComponent<com.example.engine.components.Collider2DComponent>()?.let {
            physicsWorld.addCollider(it)
        }

        return gameObject
    }

    fun removeGameObject(gameObject: GameObject) {
        pendingRemoveObjects.add(gameObject)
    }

    open fun onUpdate(dt: Float) {
        processPendingObjects()

        // Update camera
        camera.update(dt)

        // Update game objects
        for (i in 0 until rootGameObjects.size) {
            rootGameObjects[i].update(dt)
        }

        // Update UI
        uiCanvas.update(dt)
    }

    open fun onFixedUpdate(fixedDt: Float) {
        // Step physics simulation
        physicsWorld.step(fixedDt)

        for (i in 0 until rootGameObjects.size) {
            rootGameObjects[i].fixedUpdate(fixedDt)
        }
    }

    open fun onRender(renderer: Renderer) {
        // 1. Render World Space (under camera transformation)
        renderer.beginCamera(camera)
        renderWorld(renderer)
        renderer.endCamera()

        // 2. Render Screen / UI Space (under logical viewport transformation)
        renderUI(renderer)
    }

    protected open fun renderWorld(renderer: Renderer) {
        // Sort objects by layer and zOrder if needed, or render sequentially
        for (i in 0 until rootGameObjects.size) {
            rootGameObjects[i].render(renderer)
        }
    }

    protected open fun renderUI(renderer: Renderer) {
        uiCanvas.render(renderer)
    }

    open fun onTouch(event: TouchEvent): Boolean {
        // 1. UI Canvas gets priority for touch events
        if (uiCanvas.handleTouchEvent(event)) {
            return true
        }
        return false
    }

    private fun processPendingObjects() {
        if (pendingAddObjects.isNotEmpty()) {
            rootGameObjects.addAll(pendingAddObjects)
            pendingAddObjects.clear()
        }

        if (pendingRemoveObjects.isNotEmpty()) {
            for (obj in pendingRemoveObjects) {
                obj.getComponent<com.example.engine.components.RigidBody2DComponent>()?.let {
                    physicsWorld.removeRigidBody(it)
                }
                obj.getComponent<com.example.engine.components.Collider2DComponent>()?.let {
                    physicsWorld.removeCollider(it)
                }
                obj.destroy()
                rootGameObjects.remove(obj)
            }
            pendingRemoveObjects.clear()
        }
    }

    val gameObjectCount: Int get() = rootGameObjects.size
}
