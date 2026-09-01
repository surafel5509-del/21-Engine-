package com.example.engine.core

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.engine.utils.Logger

/**
 * Custom SurfaceView providing hardware-accelerated rendering, safe area inset tracking,
 * and multi-touch event capture for the GameEngine.
 */
class GameSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "GameSurfaceView"
    }

    var engine: GameEngine? = null
        set(value) {
            field = value
            value?.let { eng ->
                eng.viewportManager.updateScreenSize(width.coerceAtLeast(1), height.coerceAtLeast(1))
            }
        }

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()

        // Track Safe Area Insets (notches, bars)
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars())
            engine?.viewportManager?.updateSafeArea(
                left = cutout.left.toFloat(),
                top = cutout.top.toFloat(),
                right = cutout.right.toFloat(),
                bottom = cutout.bottom.toFloat()
            )
            insets
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Logger.d(TAG, "Surface created")
        engine?.surfaceReady = true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Logger.d(TAG, "Surface changed: ${width}x${height}")
        engine?.viewportManager?.updateScreenSize(width, height)
        engine?.surfaceReady = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Logger.d(TAG, "Surface destroyed")
        engine?.surfaceReady = false
    }

    /**
     * Renders a frame using hardware canvas if available.
     */
    fun drawFrame() {
        val eng = engine ?: return
        if (!eng.surfaceReady || !holder.surface.isValid) return

        var canvas: Canvas? = null
        try {
            canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.lockHardwareCanvas()
            } else {
                holder.lockCanvas()
            }

            if (canvas != null) {
                eng.renderer.canvas = canvas
                eng.renderDirect()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error drawing frame: ${e.message}")
        } finally {
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    Logger.e(TAG, "Error unlocking canvas: ${e.message}")
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        engine?.inputManager?.processMotionEvent(event)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        engine?.inputManager?.processKeyEvent(event)
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        engine?.inputManager?.processKeyEvent(event)
        return super.onKeyUp(keyCode, event)
    }
}
