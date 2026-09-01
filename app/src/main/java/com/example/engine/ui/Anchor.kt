package com.example.engine.ui

import com.example.engine.math.Vector2
import com.example.engine.viewport.SafeAreaInsets

enum class Anchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT
}

/**
 * Responsive layout position that stays anchored across small phones, large phones,
 * tablets, foldables, portrait, landscape, and resizable window configurations.
 */
data class LayoutPosition(
    var anchor: Anchor = Anchor.TOP_LEFT,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var percentX: Float = 0f, // 0.0 to 1.0
    var percentY: Float = 0f, // 0.0 to 1.0
    var useSafeArea: Boolean = true
) {
    fun calculate(
        containerWidth: Float,
        containerHeight: Float,
        safeArea: SafeAreaInsets,
        elementWidth: Float,
        elementHeight: Float,
        outPos: Vector2
    ): Vector2 {
        val safeLeft = if (useSafeArea) safeArea.left else 0f
        val safeTop = if (useSafeArea) safeArea.top else 0f
        val safeRight = if (useSafeArea) containerWidth - safeArea.right else containerWidth
        val safeBottom = if (useSafeArea) containerHeight - safeArea.bottom else containerHeight

        val safeWidth = safeRight - safeLeft
        val safeHeight = safeBottom - safeTop

        var baseX = safeLeft
        var baseY = safeTop

        when (anchor) {
            Anchor.TOP_LEFT -> {
                baseX = safeLeft
                baseY = safeTop
            }
            Anchor.TOP_CENTER -> {
                baseX = safeLeft + safeWidth * 0.5f - elementWidth * 0.5f
                baseY = safeTop
            }
            Anchor.TOP_RIGHT -> {
                baseX = safeRight - elementWidth
                baseY = safeTop
            }
            Anchor.CENTER_LEFT -> {
                baseX = safeLeft
                baseY = safeTop + safeHeight * 0.5f - elementHeight * 0.5f
            }
            Anchor.CENTER -> {
                baseX = safeLeft + safeWidth * 0.5f - elementWidth * 0.5f
                baseY = safeTop + safeHeight * 0.5f - elementHeight * 0.5f
            }
            Anchor.CENTER_RIGHT -> {
                baseX = safeRight - elementWidth
                baseY = safeTop + safeHeight * 0.5f - elementHeight * 0.5f
            }
            Anchor.BOTTOM_LEFT -> {
                baseX = safeLeft
                baseY = safeBottom - elementHeight
            }
            Anchor.BOTTOM_CENTER -> {
                baseX = safeLeft + safeWidth * 0.5f - elementWidth * 0.5f
                baseY = safeBottom - elementHeight
            }
            Anchor.BOTTOM_RIGHT -> {
                baseX = safeRight - elementWidth
                baseY = safeBottom - elementHeight
            }
        }

        outPos.x = baseX + offsetX + (safeWidth * percentX)
        outPos.y = baseY + offsetY + (safeHeight * percentY)
        return outPos
    }
}
