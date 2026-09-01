package com.example.engine.components

import android.graphics.Color
import com.example.engine.rendering.FlipMode
import com.example.engine.rendering.Renderer
import com.example.engine.rendering.Sprite
import com.example.engine.scene.Component

/**
 * Renders a static 2D Sprite with offsets, tinting, rotation, and alpha.
 */
class SpriteRendererComponent(
    var sprite: Sprite? = null,
    var tintColor: Int = Color.WHITE,
    var alpha: Float = 1.0f,
    var flip: FlipMode = FlipMode.NONE,
    var width: Float? = null,
    var height: Float? = null
) : Component() {

    override fun onRender(renderer: Renderer) {
        val s = sprite ?: return
        val tr = transform ?: return

        val w = width ?: s.width
        val h = height ?: s.height
        val originalFlip = s.flip
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

        s.flip = originalFlip
    }
}
