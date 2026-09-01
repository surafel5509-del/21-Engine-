package com.example.engine.assets

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.engine.animation.SpriteSheet
import com.example.engine.rendering.Sprite
import com.example.engine.utils.Logger

/**
 * Asset Manager providing caching, asynchronous loading, and built-in procedural texture generation.
 */
class AssetManager(val context: Context) {
    companion object {
        private const val TAG = "AssetManager"
    }

    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val spriteCache = mutableMapOf<String, Sprite>()
    private val typefaceCache = mutableMapOf<String, Typeface>()

    fun loadBitmapFromDrawable(resId: Int, key: String = "res_$resId"): Bitmap {
        bitmapCache[key]?.let { return it }

        val drawable = ContextCompat.getDrawable(context, resId)
            ?: throw IllegalArgumentException("Resource $resId not found")

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        bitmapCache[key] = bitmap
        return bitmap
    }

    fun loadBitmapFromAssets(assetPath: String): Bitmap {
        bitmapCache[assetPath]?.let { return it }

        val inputStream = context.assets.open(assetPath)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        bitmapCache[assetPath] = bitmap
        return bitmap
    }

    fun getOrCreateSprite(key: String, bitmap: Bitmap): Sprite {
        spriteCache[key]?.let { return it }
        val sprite = Sprite(bitmap)
        spriteCache[key] = sprite
        return sprite
    }

    fun loadTypeface(assetPath: String): Typeface {
        typefaceCache[assetPath]?.let { return it }
        val tf = Typeface.createFromAsset(context.assets, assetPath)
        typefaceCache[assetPath] = tf
        return tf
    }

    // =========================================================================
    // Procedural 2D Texture Generators (Instant standalone game assets)
    // =========================================================================

    /**
     * Generates a 64x64 platform brick texture.
     */
    fun createProceduralTileBitmap(
        width: Int = 64,
        height: Int = 64,
        baseColor: Int = 0xFF37474F.toInt(),
        borderColor: Int = 0xFF263238.toInt(),
        accentColor: Int = 0xFF455A64.toInt()
    ): Bitmap {
        val key = "proc_tile_${width}_${height}_$baseColor"
        bitmapCache[key]?.let { return it }

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Base fill
        paint.color = baseColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Top highlight
        paint.color = accentColor
        canvas.drawRect(0f, 0f, width.toFloat(), 4f, paint)

        // Border
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(1f, 1f, width - 1f, height - 1f, paint)

        // Brick pattern
        paint.strokeWidth = 1.5f
        val halfH = height * 0.5f
        canvas.drawLine(0f, halfH, width.toFloat(), halfH, paint)
        canvas.drawLine(width * 0.5f, 0f, width * 0.5f, halfH, paint)
        canvas.drawLine(width * 0.25f, halfH, width * 0.25f, height.toFloat(), paint)
        canvas.drawLine(width * 0.75f, halfH, width * 0.75f, height.toFloat(), paint)

        bitmapCache[key] = bmp
        return bmp
    }

    /**
     * Generates an animated character sprite sheet with Idle and Walk frames.
     */
    fun createProceduralCharacterSpriteSheet(): SpriteSheet {
        val key = "proc_character_spritesheet"
        val cached = bitmapCache[key]
        val bmp = cached ?: run {
            val frameW = 48
            val frameH = 48
            val cols = 4
            val rows = 2
            val bitmap = Bitmap.createBitmap(frameW * cols, frameH * rows, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            val bodyColor = 0xFF2196F3.toInt()
            val skinColor = 0xFFFFCC80.toInt()
            val eyeColor = 0xFF0D47A1.toInt()
            val hatColor = 0xFFE91E63.toInt()

            // Row 0: Idle animation (4 frames with breathing / eye blink)
            for (f in 0 until 4) {
                val ox = f * frameW
                val oy = 0
                val bob = if (f % 2 == 1) 1f else 0f

                // Body
                paint.color = bodyColor
                canvas.drawRoundRect(ox + 12f, oy + 18f + bob, ox + 36f, oy + 42f, 8f, 8f, paint)

                // Head
                paint.color = skinColor
                canvas.drawCircle(ox + 24f, oy + 16f + bob, 10f, paint)

                // Cap
                paint.color = hatColor
                canvas.drawRoundRect(ox + 13f, oy + 6f + bob, ox + 35f, oy + 14f + bob, 4f, 4f, paint)

                // Eyes
                paint.color = eyeColor
                if (f == 3) {
                    // Blink
                    canvas.drawLine(ox + 26f, oy + 16f + bob, ox + 30f, oy + 16f + bob, paint)
                } else {
                    canvas.drawCircle(ox + 28f, oy + 15f + bob, 2f, paint)
                }

                // Feet
                paint.color = 0xFF37474F.toInt()
                canvas.drawRoundRect(ox + 14f, oy + 40f, ox + 22f, oy + 46f, 3f, 3f, paint)
                canvas.drawRoundRect(ox + 26f, oy + 40f, ox + 34f, oy + 46f, 3f, 3f, paint)
            }

            // Row 1: Walk animation (4 frames with leg stride)
            for (f in 0 until 4) {
                val ox = f * frameW
                val oy = frameH
                val legOffset = when (f) {
                    0 -> -4f
                    1 -> 0f
                    2 -> 4f
                    else -> 0f
                }

                // Body
                paint.color = bodyColor
                canvas.drawRoundRect(ox + 12f, oy + 18f, ox + 36f, oy + 40f, 8f, 8f, paint)

                // Head
                paint.color = skinColor
                canvas.drawCircle(ox + 24f, oy + 16f, 10f, paint)

                // Cap
                paint.color = hatColor
                canvas.drawRoundRect(ox + 13f, oy + 6f, ox + 35f, oy + 14f, 4f, 4f, paint)

                // Eye
                paint.color = eyeColor
                canvas.drawCircle(ox + 28f, oy + 15f, 2f, paint)

                // Feet with walk swing
                paint.color = 0xFF37474F.toInt()
                canvas.drawRoundRect(ox + 14f + legOffset, oy + 40f, ox + 22f + legOffset, oy + 46f, 3f, 3f, paint)
                canvas.drawRoundRect(ox + 26f - legOffset, oy + 40f, ox + 34f - legOffset, oy + 46f, 3f, 3f, paint)
            }

            bitmapCache[key] = bitmap
            bitmap
        }

        return SpriteSheet(bmp, frameWidth = 48, frameHeight = 48)
    }

    /**
     * Generates a spinning coin animation sprite sheet.
     */
    fun createProceduralCoinSpriteSheet(): SpriteSheet {
        val key = "proc_coin_spritesheet"
        val cached = bitmapCache[key]
        val bmp = cached ?: run {
            val frameW = 32
            val frameH = 32
            val cols = 6
            val bitmap = Bitmap.createBitmap(frameW * cols, frameH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            for (f in 0 until cols) {
                val ox = f * frameW
                val scaleX = kotlin.math.cos((f.toFloat() / cols) * Math.PI.toFloat())
                val currentWidth = (frameW * 0.7f) * kotlin.math.abs(scaleX)

                val left = ox + (frameW * 0.5f) - (currentWidth * 0.5f)
                val right = ox + (frameW * 0.5f) + (currentWidth * 0.5f)

                // Coin body
                paint.color = 0xFFFFD700.toInt()
                canvas.drawOval(RectF(left, 6f, right, 26f), paint)

                // Inner rim
                paint.color = 0xFFFFA000.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawOval(RectF(left + 2f, 8f, right - 2f, 24f), paint)
                paint.style = Paint.Style.FILL
            }

            bitmapCache[key] = bitmap
            bitmap
        }

        return SpriteSheet(bmp, frameWidth = 32, frameHeight = 32)
    }

    fun clear() {
        for (bmp in bitmapCache.values) {
            if (!bmp.isRecycled) {
                bmp.recycle()
            }
        }
        bitmapCache.clear()
        spriteCache.clear()
        typefaceCache.clear()
        Logger.d(TAG, "AssetManager cleared and memory freed")
    }
}
