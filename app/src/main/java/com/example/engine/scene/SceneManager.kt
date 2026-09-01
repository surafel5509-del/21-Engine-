package com.example.engine.scene

import com.example.engine.core.GameEngine
import com.example.engine.input.TouchEvent
import com.example.engine.rendering.Renderer
import com.example.engine.utils.Logger

/**
 * SceneManager controls scene lifecycles, scene switching, transitions, and stacking.
 */
class SceneManager(val engine: GameEngine) {
    companion object {
        private const val TAG = "SceneManager"
    }

    var currentScene: Scene? = null
        private set

    private val sceneStack = mutableListOf<Scene>()

    private var activeTransition: SceneTransition? = null
    private var transitionFromScene: Scene? = null
    private var transitionToScene: Scene? = null

    fun setScene(newScene: Scene, transition: SceneTransition = InstantTransition()) {
        Logger.d(TAG, "Switching scene to: ${newScene::class.java.simpleName}")

        if (transition is InstantTransition) {
            currentScene?.onExit()
            currentScene?.onDestroy()
            currentScene = newScene
            newScene.init(engine)
            newScene.onEnter()
            newScene.onResize(engine.viewportManager.screenWidth, engine.viewportManager.screenHeight)
            engine.inputManager.activeCamera = newScene.camera
        } else {
            transitionFromScene = currentScene
            transitionToScene = newScene
            newScene.init(engine)
            transition.start()
            activeTransition = transition
        }
    }

    fun pushScene(scene: Scene) {
        currentScene?.let {
            it.onPause()
            sceneStack.add(it)
        }
        currentScene = scene
        scene.init(engine)
        scene.onEnter()
        scene.onResize(engine.viewportManager.screenWidth, engine.viewportManager.screenHeight)
        engine.inputManager.activeCamera = scene.camera
    }

    fun popScene(): Boolean {
        if (sceneStack.isEmpty()) return false

        currentScene?.onExit()
        currentScene?.onDestroy()

        val previous = sceneStack.removeAt(sceneStack.size - 1)
        currentScene = previous
        previous.onResume()
        previous.onResize(engine.viewportManager.screenWidth, engine.viewportManager.screenHeight)
        engine.inputManager.activeCamera = previous.camera
        return true
    }

    fun update(dt: Float) {
        val trans = activeTransition
        if (trans != null) {
            trans.update(dt)
            if (trans.isFinished) {
                transitionFromScene?.onExit()
                transitionFromScene?.onDestroy()
                currentScene = transitionToScene
                currentScene?.onEnter()
                currentScene?.onResize(engine.viewportManager.screenWidth, engine.viewportManager.screenHeight)
                currentScene?.let { engine.inputManager.activeCamera = it.camera }

                activeTransition = null
                transitionFromScene = null
                transitionToScene = null
            }
        } else {
            currentScene?.onUpdate(dt)
        }
    }

    fun fixedUpdate(fixedDt: Float) {
        if (activeTransition == null) {
            currentScene?.onFixedUpdate(fixedDt)
        }
    }

    fun render(renderer: Renderer) {
        val trans = activeTransition
        val to = transitionToScene
        if (trans != null && to != null) {
            trans.render(renderer, transitionFromScene, to)
        } else {
            currentScene?.onRender(renderer)
        }
    }

    fun onResize(width: Int, height: Int) {
        currentScene?.onResize(width, height)
    }

    fun handleTouchEvent(event: TouchEvent): Boolean {
        if (activeTransition != null) return false
        return currentScene?.onTouch(event) ?: false
    }

    fun onPause() {
        currentScene?.onPause()
    }

    fun onResume() {
        currentScene?.onResume()
    }

    fun onDestroy() {
        currentScene?.onDestroy()
        for (scene in sceneStack) {
            scene.onDestroy()
        }
        sceneStack.clear()
    }
}
