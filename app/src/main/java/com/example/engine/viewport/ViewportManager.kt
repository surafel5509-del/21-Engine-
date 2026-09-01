package com.example.engine.viewport

import com.example.engine.camera.Camera2D
import com.example.engine.math.MathUtils
import com.example.engine.math.Rect2D
import com.example.engine.math.Vector2
import com.example.engine.utils.Logger
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * ViewportManager is the engine's core resolution and screen-adaptation subsystem.
 *
 * It manages:
 * 1. Logical resolution (e.g. 1280x720) that the game developer designs against.
 * 2. Physical screen resolution (pixels) of any phone, tablet, foldable, or resizable window.
 * 3. Scaling factors ($scaleX, scaleY$) and viewport offset ($offsetX, offsetY$) for letterboxing/pillarboxing.
 * 4. Safe area insets (notches, navigation bars).
 * 5. Accurate bidirectional coordinate transforms:
 *    - Screen (pixels) <-> Logical (game design coords) <-> World (camera-transformed coords) <-> UI (responsive anchor space).
 */
class ViewportManager(
    var logicalWidth: Float = 1280f,
    var logicalHeight: Float = 720f,
    var scaleMode: ScaleMode = ScaleMode.FIT_LETTERBOX
) {
    companion object {
        private const val TAG = "ViewportManager"
    }

    /** Physical screen dimensions in real device pixels */
    var screenWidth: Int = 1280
        private set
    var screenHeight: Int = 720
        private set

    /** Viewport rectangle on physical screen */
    val viewportRect = Rect2D()

    /** Scale factors from logical game space to physical screen pixels */
    var scaleX: Float = 1f
        private set
    var scaleY: Float = 1f
        private set

    /** Viewport offset (letterbox/pillarbox bars) in physical pixels */
    var offsetX: Float = 0f
        private set
    var offsetY: Float = 0f
        private set

    /** Physical safe area insets in pixels */
    val physicalSafeArea = SafeAreaInsets()

    /** Logical safe area insets transformed into game coordinates */
    val logicalSafeArea = SafeAreaInsets()

    /** Listeners triggered whenever the viewport or window size changes */
    private val onResizeListeners = mutableListOf<(width: Int, height: Int) -> Unit>()

    init {
        recalculateViewport()
    }

    fun addResizeListener(listener: (width: Int, height: Int) -> Unit) {
        if (!onResizeListeners.contains(listener)) {
            onResizeListeners.add(listener)
        }
    }

    fun removeResizeListener(listener: (width: Int, height: Int) -> Unit) {
        onResizeListeners.remove(listener)
    }

    fun setLogicalResolution(width: Float, height: Float) {
        if (logicalWidth != width || logicalHeight != height) {
            logicalWidth = max(1f, width)
            logicalHeight = max(1f, height)
            recalculateViewport()
        }
    }

    fun updateScreenSize(newScreenWidth: Int, newScreenHeight: Int) {
        if (newScreenWidth <= 0 || newScreenHeight <= 0) return
        if (screenWidth != newScreenWidth || screenHeight != newScreenHeight) {
            screenWidth = newScreenWidth
            screenHeight = newScreenHeight
            recalculateViewport()
            for (listener in onResizeListeners) {
                listener(newScreenWidth, newScreenHeight)
            }
        }
    }

    fun updateSafeArea(left: Float, top: Float, right: Float, bottom: Float) {
        physicalSafeArea.set(left, top, right, bottom)
        recalculateLogicalSafeArea()
    }

    /**
     * Recalculates viewport scaling, offsets, and letterbox boundaries based on the current scale mode.
     */
    fun recalculateViewport() {
        val pw = screenWidth.toFloat()
        val ph = screenHeight.toFloat()

        if (pw <= 0f || ph <= 0f || logicalWidth <= 0f || logicalHeight <= 0f) return

        when (scaleMode) {
            ScaleMode.FIT_LETTERBOX -> {
                val scale = min(pw / logicalWidth, ph / logicalHeight)
                scaleX = scale
                scaleY = scale
                val vpWidth = logicalWidth * scale
                val vpHeight = logicalHeight * scale
                offsetX = (pw - vpWidth) * 0.5f
                offsetY = (ph - vpHeight) * 0.5f
                viewportRect.set(offsetX, offsetY, vpWidth, vpHeight)
            }
            ScaleMode.FILL_CROP -> {
                val scale = max(pw / logicalWidth, ph / logicalHeight)
                scaleX = scale
                scaleY = scale
                val vpWidth = logicalWidth * scale
                val vpHeight = logicalHeight * scale
                offsetX = (pw - vpWidth) * 0.5f
                offsetY = (ph - vpHeight) * 0.5f
                viewportRect.set(offsetX, offsetY, vpWidth, vpHeight)
            }
            ScaleMode.STRETCH -> {
                scaleX = pw / logicalWidth
                scaleY = ph / logicalHeight
                offsetX = 0f
                offsetY = 0f
                viewportRect.set(0f, 0f, pw, ph)
            }
            ScaleMode.EXTEND_VIEWPORT -> {
                val scale = min(pw / logicalWidth, ph / logicalHeight)
                scaleX = scale
                scaleY = scale
                offsetX = 0f
                offsetY = 0f
                viewportRect.set(0f, 0f, pw, ph)
            }
            ScaleMode.INTEGER_SCALE -> {
                val maxIntegerScale = max(1f, floor(min(pw / logicalWidth, ph / logicalHeight)))
                scaleX = maxIntegerScale
                scaleY = maxIntegerScale
                val vpWidth = logicalWidth * maxIntegerScale
                val vpHeight = logicalHeight * maxIntegerScale
                offsetX = (pw - vpWidth) * 0.5f
                offsetY = (ph - vpHeight) * 0.5f
                viewportRect.set(offsetX, offsetY, vpWidth, vpHeight)
            }
        }

        recalculateLogicalSafeArea()
        Logger.d(TAG, "Viewport recalculated: Screen=${screenWidth}x${screenHeight}, Logical=${logicalWidth}x${logicalHeight}, Scale=($scaleX, $scaleY), Offset=($offsetX, $offsetY)")
    }

    private fun recalculateLogicalSafeArea() {
        if (scaleX > 0f && scaleY > 0f) {
            val safeLeftPx = max(0f, physicalSafeArea.left - offsetX)
            val safeTopPx = max(0f, physicalSafeArea.top - offsetY)
            val safeRightPx = max(0f, physicalSafeArea.right - (screenWidth - (offsetX + viewportRect.width)))
            val safeBottomPx = max(0f, physicalSafeArea.bottom - (screenHeight - (offsetY + viewportRect.height)))

            logicalSafeArea.set(
                safeLeftPx / scaleX,
                safeTopPx / scaleY,
                safeRightPx / scaleX,
                safeBottomPx / scaleY
            )
        }
    }

    // =========================================================================
    // Coordinate Transformation Functions
    // =========================================================================

    /**
     * Converts a physical screen touch coordinate (in pixels) to logical game coordinates.
     */
    fun screenToLogical(screenX: Float, screenY: Float, outVec: Vector2): Vector2 {
        if (scaleX <= 0f || scaleY <= 0f) {
            outVec.set(0f, 0f)
            return outVec
        }
        val lx = (screenX - offsetX) / scaleX
        val ly = (screenY - offsetY) / scaleY
        outVec.set(lx, ly)
        return outVec
    }

    /**
     * Converts a logical game coordinate into physical screen pixel coordinates.
     */
    fun logicalToScreen(logicalX: Float, logicalY: Float, outVec: Vector2): Vector2 {
        val sx = logicalX * scaleX + offsetX
        val sy = logicalY * scaleY + offsetY
        outVec.set(sx, sy)
        return outVec
    }

    /**
     * Converts a logical game coordinate to world coordinates given a Camera2D.
     */
    fun logicalToWorld(camera: Camera2D, logicalX: Float, logicalY: Float, outVec: Vector2): Vector2 {
        return camera.logicalToWorld(logicalX, logicalY, outVec)
    }

    /**
     * Converts a world coordinate to logical game coordinates given a Camera2D.
     */
    fun worldToLogical(camera: Camera2D, worldX: Float, worldY: Float, outVec: Vector2): Vector2 {
        return camera.worldToLogical(worldX, worldY, outVec)
    }

    /**
     * Converts physical screen touch directly to world coordinates given a Camera2D.
     */
    fun screenToWorld(camera: Camera2D, screenX: Float, screenY: Float, outVec: Vector2): Vector2 {
        screenToLogical(screenX, screenY, outVec)
        return camera.logicalToWorld(outVec.x, outVec.y, outVec)
    }

    /**
     * Converts world coordinates directly to physical screen pixel coordinates given a Camera2D.
     */
    fun worldToScreen(camera: Camera2D, worldX: Float, worldY: Float, outVec: Vector2): Vector2 {
        camera.worldToLogical(worldX, worldY, outVec)
        return logicalToScreen(outVec.x, outVec.y, outVec)
    }

    /**
     * Checks if a physical screen point falls within the active game viewport.
     */
    fun isInsideViewport(screenX: Float, screenY: Float): Boolean {
        return viewportRect.contains(screenX, screenY)
    }
}
