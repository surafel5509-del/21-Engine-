package com.example.studio.engine_bridge

import android.graphics.Paint
import android.graphics.Path
import com.example.engine.rendering.Renderer
import kotlin.math.cos
import kotlin.math.sin

object ShapeDrawer {

    fun drawNodeVisual(
        renderer: Renderer,
        shape: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        colorInt: Int,
        alpha: Float = 1f,
        rotation: Float = 0f
    ) {
        val halfW = width * 0.5f
        val halfH = height * 0.5f

        renderer.pushMatrix()
        renderer.translate(x, y)
        if (rotation != 0f) {
            renderer.rotate(rotation)
        }

        val effectiveColor = if (alpha < 1f) {
            val a = (alpha * 255).toInt().coerceIn(0, 255)
            (colorInt and 0x00FFFFFF) or (a shl 24)
        } else {
            colorInt
        }

        when (shape.lowercase()) {
            "circle" -> {
                renderer.drawCircle(0f, 0f, halfW.coerceAtMost(halfH), effectiveColor, Paint.Style.FILL)
                renderer.drawCircle(0f, 0f, halfW.coerceAtMost(halfH), 0x55FFFFFF, Paint.Style.STROKE, 2f)
            }
            "hero" -> {
                // Knight / Hero Character
                // Body
                renderer.drawRect(-halfW * 0.8f, -halfH * 0.7f, halfW * 1.6f, halfH * 1.4f, effectiveColor, Paint.Style.FILL, cornerRadius = 8f)
                // Head / Visor
                renderer.drawRect(-halfW * 0.5f, -halfH * 0.9f, halfW * 1.0f, halfH * 0.5f, 0xFFE2E8F0.toInt(), Paint.Style.FILL, cornerRadius = 4f)
                // Eye glow
                renderer.drawCircle(halfW * 0.1f, -halfH * 0.65f, halfW * 0.15f, 0xFF00E5FF.toInt(), Paint.Style.FILL)
                // Belt / Accent
                renderer.drawRect(-halfW * 0.8f, 0f, halfW * 1.6f, halfH * 0.2f, 0xFFFFD600.toInt(), Paint.Style.FILL)
                // Feet
                renderer.drawRect(-halfW * 0.7f, halfH * 0.65f, halfW * 0.5f, halfH * 0.35f, 0xFF1E293B.toInt(), Paint.Style.FILL, cornerRadius = 3f)
                renderer.drawRect(halfW * 0.2f, halfH * 0.65f, halfW * 0.5f, halfH * 0.35f, 0xFF1E293B.toInt(), Paint.Style.FILL, cornerRadius = 3f)
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
            }
            "alien" -> {
                // Alien UFO
                renderer.drawCircle(0f, 0f, halfW * 0.9f, effectiveColor, Paint.Style.FILL)
                renderer.drawCircle(0f, -halfH * 0.35f, halfW * 0.5f, 0xFFBBF7D0.toInt(), Paint.Style.FILL)
                // Eyes
                renderer.drawCircle(-halfW * 0.2f, -halfH * 0.35f, halfW * 0.12f, 0xFF0F172A.toInt(), Paint.Style.FILL)
                renderer.drawCircle(halfW * 0.2f, -halfH * 0.35f, halfW * 0.12f, 0xFF0F172A.toInt(), Paint.Style.FILL)
                // Lights
                for (i in -2..2) {
                    renderer.drawCircle(i * halfW * 0.35f, halfH * 0.25f, 3.5f, 0xFFFFD600.toInt(), Paint.Style.FILL)
                }
            }
            "coin" -> {
                // Gold Coin
                renderer.drawCircle(0f, 0f, halfW, 0xFFFFD700.toInt(), Paint.Style.FILL)
                renderer.drawCircle(0f, 0f, halfW * 0.75f, 0xFFF59E0B.toInt(), Paint.Style.FILL)
                renderer.drawCircle(0f, 0f, halfW * 0.4f, 0xFFFFE082.toInt(), Paint.Style.FILL)
            }
            "platform" -> {
                // Platform with top grass accent
                renderer.drawRect(-halfW, -halfH, width, height, effectiveColor, Paint.Style.FILL, cornerRadius = 6f)
                renderer.drawRect(-halfW, -halfH, width, 6f.coerceAtMost(height * 0.3f), 0xFF22C55E.toInt(), Paint.Style.FILL, cornerRadius = 3f)
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
            "gem" -> {
                // Diamond Gem
                renderer.drawCircle(0f, 0f, halfW * 0.85f, effectiveColor, Paint.Style.FILL)
                renderer.drawCircle(0f, 0f, halfW * 0.45f, 0xFFFFFFFF.toInt(), Paint.Style.FILL)
            }
            "heart" -> {
                renderer.drawCircle(-halfW * 0.3f, -halfH * 0.2f, halfW * 0.45f, 0xFFEF4444.toInt(), Paint.Style.FILL)
                renderer.drawCircle(halfW * 0.3f, -halfH * 0.2f, halfW * 0.45f, 0xFFEF4444.toInt(), Paint.Style.FILL)
                renderer.drawRect(-halfW * 0.5f, 0f, width * 0.5f, halfH * 0.6f, 0xFFEF4444.toInt(), Paint.Style.FILL)
            }
            else -> {
                // Default Rounded Box
                renderer.drawRect(-halfW, -halfH, width, height, effectiveColor, Paint.Style.FILL, cornerRadius = 8f)
                renderer.drawRect(-halfW, -halfH, width, height, 0x44FFFFFF, Paint.Style.STROKE, 2f, cornerRadius = 8f)
            }
        }

        renderer.popMatrix()
    }
}
