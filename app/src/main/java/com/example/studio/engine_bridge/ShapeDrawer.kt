package com.example.studio.engine_bridge

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import com.example.engine.math.Rect2D
import com.example.engine.rendering.Renderer
import com.example.studio.model.GameNode
import com.example.studio.storage.CustomAssetManager
import kotlin.math.cos
import kotlin.math.sin

object ShapeDrawer {

    private val tempSrcRect = Rect2D()
    private val tempDstRect = Rect2D()

    fun drawNode(
        renderer: Renderer,
        node: GameNode,
        elapsedTime: Float = 0f
    ) {
        val halfW = (node.width * node.scaleX) * 0.5f
        val halfH = (node.height * node.scaleY) * 0.5f

        renderer.pushMatrix()
        renderer.translate(node.posX, node.posY)
        if (node.rotation != 0f) {
            renderer.rotate(node.rotation)
        }

        val effectiveColor = if (node.alpha < 1f) {
            val a = (node.alpha * 255).toInt().coerceIn(0, 255)
            (node.color.toInt() and 0x00FFFFFF) or (a shl 24)
        } else {
            node.color.toInt()
        }

        // 1. Check if custom asset or spritesheet is attached
        val customBmp = CustomAssetManager.getBitmap(node.customTextureKey)
        if (customBmp != null) {
            drawCustomTexture(
                renderer = renderer,
                bitmap = customBmp,
                node = node,
                elapsedTime = elapsedTime,
                halfW = halfW,
                halfH = halfH
            )
            renderer.popMatrix()
            return
        }

        // 2. Procedural Built-In Shapes & Characters
        drawProceduralShape(
            renderer = renderer,
            shape = node.spriteShape,
            halfW = halfW,
            halfH = halfH,
            width = node.width * node.scaleX,
            height = node.height * node.scaleY,
            effectiveColor = effectiveColor,
            elapsedTime = elapsedTime
        )

        renderer.popMatrix()
    }

    private fun drawCustomTexture(
        renderer: Renderer,
        bitmap: Bitmap,
        node: GameNode,
        elapsedTime: Float,
        halfW: Float,
        halfH: Float
    ) {
        val cols = node.animationCols.coerceAtLeast(1)
        val rows = node.animationRows.coerceAtLeast(1)
        val totalFrames = cols * rows

        val frameWidth = bitmap.width.toFloat() / cols
        val frameHeight = bitmap.height.toFloat() / rows

        val currentFrame = if (node.isAnimated && totalFrames > 1) {
            val frameIndex = (elapsedTime * node.animationFps).toInt() % totalFrames
            frameIndex
        } else 0

        val col = currentFrame % cols
        val row = currentFrame / cols
        val srcX = col * frameWidth
        val srcY = row * frameHeight

        tempSrcRect.set(srcX, srcY, frameWidth, frameHeight)
        tempDstRect.set(-halfW, -halfH, halfW * 2f, halfH * 2f)

        renderer.drawBitmap(
            bitmap = bitmap,
            srcRect = tempSrcRect,
            dstRect = tempDstRect,
            alpha = node.alpha,
            rotation = 0f
        )
    }

    private fun drawProceduralShape(
        renderer: Renderer,
        shape: String,
        halfW: Float,
        halfH: Float,
        width: Float,
        height: Float,
        effectiveColor: Int,
        elapsedTime: Float
    ) {
        val animFrame = (elapsedTime * 6f).toInt() % 4
        val bob = sin(elapsedTime * 8f) * 3f

        when (shape.lowercase()) {
            "circle" -> {
                renderer.drawCircle(0f, 0f, halfW.coerceAtMost(halfH), effectiveColor, Paint.Style.FILL)
                renderer.drawCircle(0f, 0f, halfW.coerceAtMost(halfH), 0x55FFFFFF, Paint.Style.STROKE, 2f)
            }
            "hero" -> {
                // Knight / Hero Character with animated walk bob
                renderer.drawRect(-halfW * 0.8f, -halfH * 0.7f + bob, halfW * 1.6f, halfH * 1.4f, effectiveColor, Paint.Style.FILL, cornerRadius = 8f)
                // Helmet / Visor
                renderer.drawRect(-halfW * 0.5f, -halfH * 0.9f + bob, halfW * 1.0f, halfH * 0.5f, 0xFFE2E8F0.toInt(), Paint.Style.FILL, cornerRadius = 4f)
                // Glowing Cyan Visor Eye
                renderer.drawCircle(halfW * 0.15f, -halfH * 0.65f + bob, halfW * 0.15f, 0xFF00E5FF.toInt(), Paint.Style.FILL)
                // Gold Belt
                renderer.drawRect(-halfW * 0.8f, 0f + bob, halfW * 1.6f, halfH * 0.2f, 0xFFFFD600.toInt(), Paint.Style.FILL)
                // Feet / Boots
                val footSwing = sin(elapsedTime * 10f) * 4f
                renderer.drawRect(-halfW * 0.7f, halfH * 0.65f + footSwing, halfW * 0.5f, halfH * 0.35f, 0xFF1E293B.toInt(), Paint.Style.FILL, cornerRadius = 3f)
                renderer.drawRect(halfW * 0.2f, halfH * 0.65f - footSwing, halfW * 0.5f, halfH * 0.35f, 0xFF1E293B.toInt(), Paint.Style.FILL, cornerRadius = 3f)
            }
            "slime" -> {
                // Green Slime Enemy
                val squish = sin(elapsedTime * 8f) * 0.15f
                val sw = halfW * (1f + squish)
                val sh = halfH * (1f - squish)
                renderer.drawCircle(0f, halfH * 0.2f, sw.coerceAtMost(sh), 0xFF22C55E.toInt(), Paint.Style.FILL)
                // Slime Eyes
                renderer.drawCircle(-halfW * 0.25f, 0f, 4f, 0xFF000000.toInt(), Paint.Style.FILL)
                renderer.drawCircle(halfW * 0.25f, 0f, 4f, 0xFF000000.toInt(), Paint.Style.FILL)
                renderer.drawCircle(-halfW * 0.22f, -1.5f, 1.5f, 0xFFFFFFFF.toInt(), Paint.Style.FILL)
                renderer.drawCircle(halfW * 0.28f, -1.5f, 1.5f, 0xFFFFFFFF.toInt(), Paint.Style.FILL)
            }
            "spaceship" -> {
                // Delta Starship
                renderer.drawCircle(0f, 0f, halfW * 0.7f, effectiveColor, Paint.Style.FILL)
                // Cockpit
                renderer.drawCircle(0f, -halfH * 0.2f, halfW * 0.35f, 0xFF38BDF8.toInt(), Paint.Style.FILL)
                // Wings
                renderer.drawRect(-halfW, 0f, halfW * 2f, halfH * 0.3f, 0xFF64748B.toInt(), Paint.Style.FILL, cornerRadius = 4f)
                // Cannons
                renderer.drawRect(-halfW * 0.9f, -halfH * 0.4f, halfW * 0.25f, halfH * 0.6f, 0xFFFF3D00.toInt(), Paint.Style.FILL)
                renderer.drawRect(halfW * 0.65f, -halfH * 0.4f, halfW * 0.25f, halfH * 0.6f, 0xFFFF3D00.toInt(), Paint.Style.FILL)
                // Thruster glow
                renderer.drawCircle(0f, halfH * 0.6f, 6f + sin(elapsedTime * 20f) * 2f, 0xFFFF4500.toInt(), Paint.Style.FILL)
            }
            "alien" -> {
                // Alien UFO with rotating lights
                renderer.drawCircle(0f, 0f, halfW * 0.9f, effectiveColor, Paint.Style.FILL)
                renderer.drawCircle(0f, -halfH * 0.35f, halfW * 0.5f, 0xFFBBF7D0.toInt(), Paint.Style.FILL)
                // Eyes
                renderer.drawCircle(-halfW * 0.2f, -halfH * 0.35f, halfW * 0.12f, 0xFF0F172A.toInt(), Paint.Style.FILL)
                renderer.drawCircle(halfW * 0.2f, -halfH * 0.35f, halfW * 0.12f, 0xFF0F172A.toInt(), Paint.Style.FILL)
                // Lights
                for (i in -2..2) {
                    val lightCol = if ((i + animFrame) % 2 == 0) 0xFFFFD600.toInt() else 0xFF38BDF8.toInt()
                    renderer.drawCircle(i * halfW * 0.35f, halfH * 0.25f, 3.5f, lightCol, Paint.Style.FILL)
                }
            }
            "coin" -> {
                // Animated Gold Coin
                val coinW = halfW * (0.3f + 0.7f * (0.5f + 0.5f * cos(elapsedTime * 6f)))
                renderer.drawRect(-coinW, -halfH, coinW * 2f, height, 0xFFFFD700.toInt(), Paint.Style.FILL, cornerRadius = 8f)
                renderer.drawRect(-coinW * 0.7f, -halfH * 0.7f, coinW * 1.4f, height * 0.7f, 0xFFF59E0B.toInt(), Paint.Style.FILL, cornerRadius = 6f)
            }
            "platform" -> {
                // Ground platform with top grass
                renderer.drawRect(-halfW, -halfH, width, height, effectiveColor, Paint.Style.FILL, cornerRadius = 6f)
                renderer.drawRect(-halfW, -halfH, width, 8f.coerceAtMost(height * 0.3f), 0xFF22C55E.toInt(), Paint.Style.FILL, cornerRadius = 4f)
            }
            "spike" -> {
                // Danger Hazard Spike
                renderer.drawRect(-halfW, 0f, width, halfH, 0xFF7F1D1D.toInt(), Paint.Style.FILL)
                val spikeCount = (width / 24f).toInt().coerceAtLeast(1)
                val sw = width / spikeCount
                for (i in 0 until spikeCount) {
                    val sx = -halfW + i * sw + sw * 0.5f
                    renderer.drawCircle(sx, -halfH * 0.2f, sw * 0.45f, 0xFFEF4444.toInt(), Paint.Style.FILL)
                }
            }
            "trophy", "flag" -> {
                // Goal Victory Flag
                renderer.drawRect(-halfW * 0.8f, -halfH, 6f, height * 1.8f, 0xFFE2E8F0.toInt(), Paint.Style.FILL)
                val flagW = halfW * 1.4f
                val wave = sin(elapsedTime * 6f) * 3f
                renderer.drawRect(-halfW * 0.8f + 6f, -halfH + wave, flagW, halfH * 0.8f, 0xFFFFD700.toInt(), Paint.Style.FILL, cornerRadius = 3f)
                renderer.drawCircle(-halfW * 0.8f + 3f, -halfH, 6f, 0xFFFFD700.toInt(), Paint.Style.FILL)
            }
            "gem" -> {
                renderer.drawCircle(0f, 0f, halfW * 0.85f, effectiveColor, Paint.Style.FILL)
                renderer.drawCircle(0f, 0f, halfW * 0.45f, 0xFFFFFFFF.toInt(), Paint.Style.FILL)
            }
            "heart" -> {
                val pBob = sin(elapsedTime * 6f) * 2f
                renderer.drawCircle(-halfW * 0.3f, -halfH * 0.2f + pBob, halfW * 0.45f, 0xFFEF4444.toInt(), Paint.Style.FILL)
                renderer.drawCircle(halfW * 0.3f, -halfH * 0.2f + pBob, halfW * 0.45f, 0xFFEF4444.toInt(), Paint.Style.FILL)
                renderer.drawRect(-halfW * 0.5f, 0f + pBob, width * 0.5f, halfH * 0.6f, 0xFFEF4444.toInt(), Paint.Style.FILL)
            }
            else -> {
                // Default Rounded Box
                renderer.drawRect(-halfW, -halfH, width, height, effectiveColor, Paint.Style.FILL, cornerRadius = 8f)
                renderer.drawRect(-halfW, -halfH, width, height, 0x44FFFFFF, Paint.Style.STROKE, 2f, cornerRadius = 8f)
            }
        }
    }
}
