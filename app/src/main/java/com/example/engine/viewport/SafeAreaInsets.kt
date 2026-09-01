package com.example.engine.viewport

/**
 * Screen safe area insets (notches, display cutouts, rounded corners, navigation and status bars).
 */
data class SafeAreaInsets(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) {
    fun set(l: Float, t: Float, r: Float, b: Float): SafeAreaInsets {
        left = l
        top = t
        right = r
        bottom = b
        return this
    }

    fun isZero(): Boolean = left == 0f && top == 0f && right == 0f && bottom == 0f
}
