package com.example.engine.scene

import android.graphics.Color
import android.graphics.Paint
import com.example.engine.animation.Easing
import com.example.engine.math.MathUtils
import com.example.engine.rendering.Renderer

/**
 * Scene transition effect interface.
 */
interface SceneTransition {
    val duration: Float
    val isFinished: Boolean

    fun start()
    fun update(dt: Float)
    fun render(renderer: Renderer, fromScene: Scene?, toScene: Scene)
}

class InstantTransition : SceneTransition {
    override val duration: Float = 0f
    override var isFinished: Boolean = false

    override fun start() {
        isFinished = true
    }

    override fun update(dt: Float) {}

    override fun render(renderer: Renderer, fromScene: Scene?, toScene: Scene) {
        toScene.onRender(renderer)
    }
}

class FadeTransition(
    override val duration: Float = 0.5f,
    val fadeColor: Int = Color.BLACK
) : SceneTransition {
    private var elapsed = 0f
    override var isFinished: Boolean = false

    override fun start() {
        elapsed = 0f
        isFinished = false
    }

    override fun update(dt: Float) {
        elapsed += dt
        if (elapsed >= duration) {
            isFinished = true
        }
    }

    override fun render(renderer: Renderer, fromScene: Scene?, toScene: Scene) {
        val halfDur = duration * 0.5f
        val t = elapsed / duration

        if (elapsed < halfDur) {
            fromScene?.onRender(renderer)
            val alpha = Easing.quadIn(elapsed / halfDur)
            renderer.clear(Color.argb((alpha * 255f).toInt(), Color.red(fadeColor), Color.green(fadeColor), Color.blue(fadeColor)))
        } else {
            toScene.onRender(renderer)
            val alpha = 1f - Easing.quadOut((elapsed - halfDur) / halfDur)
            renderer.clear(Color.argb((alpha * 255f).toInt(), Color.red(fadeColor), Color.green(fadeColor), Color.blue(fadeColor)))
        }
    }
}
