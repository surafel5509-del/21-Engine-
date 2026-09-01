package com.example.engine.components

import android.graphics.Color
import com.example.engine.animation.AnimationClip
import com.example.engine.animation.Animator
import com.example.engine.rendering.FlipMode
import com.example.engine.rendering.Renderer
import com.example.engine.rendering.Sprite
import com.example.engine.scene.Component

/**
 * Plays sprite sheet animations via an Animator controller.
 */
class AnimatedSpriteComponent(
    val animator: Animator = Animator(),
    var tintColor: Int = Color.WHITE,
    var alpha: Float = 1.0f,
    var flip: FlipMode = FlipMode.NONE,
    var width: Float? = null,
    var height: Float? = null
) : Component() {

    private var activeSprite: Sprite? = null

    fun addAnimation(clip: AnimationClip): AnimatedSpriteComponent {
        animator.addClip(clip)
        return this
    }

    fun play(name: String, restart: Boolean = false) {
        animator.play(name, restart)
    }

    override fun onStart() {
        val clip = animator.currentClip
        if (clip != null && activeSprite == null) {
            activeSprite = Sprite(clip.bitmap)
        }
    }

    override fun onUpdate(dt: Float) {
        animator.update(dt)
        val clip = animator.currentClip
        if (clip != null) {
            if (activeSprite == null || activeSprite?.bitmap != clip.bitmap) {
                activeSprite = Sprite(clip.bitmap)
            }
            activeSprite?.let { animator.applyToSprite(it) }
        }
    }

    override fun onRender(renderer: Renderer) {
        val s = activeSprite ?: return
        val tr = transform ?: return

        val w = width ?: s.width
        val h = height ?: s.height
        val origFlip = s.flip
        if (flip != FlipMode.NONE) {
            s.flip = flip
        }

        renderer.drawSprite(
            sprite = s,
            x = tr.worldPosition.x,
            y = tr.worldPosition.y,
            width = w,
            height = h,
            rotation = tr.worldRotation,
            scaleX = tr.worldScale.x,
            scaleY = tr.worldScale.y,
            alpha = alpha
        )

        s.flip = origFlip
    }
}
