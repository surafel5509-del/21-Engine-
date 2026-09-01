package com.example.studio.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class CustomAsset(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var type: String = "IMAGE", // IMAGE, SPRITESHEET, AUDIO
    var localPath: String? = null,
    var width: Int = 64,
    var height: Int = 64,
    var frameCols: Int = 1,
    var frameRows: Int = 1,
    var frameFps: Float = 8f
)

/**
 * Manages user-uploaded images, custom textures, and built-in procedural sprite sheets.
 */
object CustomAssetManager {

    private val cachedBitmaps = mutableMapOf<String, Bitmap>()
    val assetList = mutableListOf<CustomAsset>()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        // Generate and register built-in high-res sprite sheets
        registerBuiltInSprites()
    }

    fun getAsset(id: String?): CustomAsset? {
        if (id == null) return null
        return assetList.find { it.id == id }
    }

    fun getBitmap(key: String?): Bitmap? {
        if (key == null) return null
        return cachedBitmaps[key]
    }

    fun registerBitmap(key: String, bitmap: Bitmap) {
        cachedBitmaps[key] = bitmap
    }

    /**
     * Imports an image from Android Photo Picker (Uri) into the app's local assets directory.
     */
    fun importImageUri(context: Context, uri: Uri, name: String = "Custom_Sprite"): CustomAsset? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream.close()

            val assetId = "custom_${UUID.randomUUID().toString().take(8)}"
            val fileName = "$assetId.png"
            val file = File(context.filesDir, fileName)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            cachedBitmaps[assetId] = bitmap

            val asset = CustomAsset(
                id = assetId,
                name = name,
                type = "IMAGE",
                localPath = file.absolutePath,
                width = bitmap.width,
                height = bitmap.height
            )
            assetList.add(asset)
            return asset
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun registerBuiltInSprites() {
        // 1. Knight Hero Spritesheet (4 frames: Idle, Run1, Run2, Jump)
        val heroSheet = createHeroSpriteSheet()
        val heroAsset = CustomAsset(
            id = "spritesheet_hero",
            name = "Hero Knight (4 Frames)",
            type = "SPRITESHEET",
            width = 256,
            height = 64,
            frameCols = 4,
            frameRows = 1,
            frameFps = 8f
        )
        cachedBitmaps[heroAsset.id] = heroSheet
        assetList.add(heroAsset)

        // 2. Slime Monster Spritesheet (4 frames)
        val slimeSheet = createSlimeSpriteSheet()
        val slimeAsset = CustomAsset(
            id = "spritesheet_slime",
            name = "Green Slime (4 Frames)",
            type = "SPRITESHEET",
            width = 256,
            height = 64,
            frameCols = 4,
            frameRows = 1,
            frameFps = 6f
        )
        cachedBitmaps[slimeAsset.id] = slimeSheet
        assetList.add(slimeAsset)

        // 3. Spinning Coin Spritesheet (4 frames)
        val coinSheet = createCoinSpriteSheet()
        val coinAsset = CustomAsset(
            id = "spritesheet_coin",
            name = "Spinning Coin (4 Frames)",
            type = "SPRITESHEET",
            width = 256,
            height = 64,
            frameCols = 4,
            frameRows = 1,
            frameFps = 10f
        )
        cachedBitmaps[coinAsset.id] = coinSheet
        assetList.add(coinAsset)

        // 4. Space Fighter Ship
        val shipSheet = createSpaceshipSprite()
        val shipAsset = CustomAsset(
            id = "sprite_ship",
            name = "Cyber Starfighter",
            type = "IMAGE",
            width = 64,
            height = 64,
            frameCols = 1,
            frameRows = 1
        )
        cachedBitmaps[shipAsset.id] = shipSheet
        assetList.add(shipAsset)

        // 5. Dungeon Tile Block
        val tileBmp = createTileBlock()
        val tileAsset = CustomAsset(
            id = "sprite_tile_grass",
            name = "Grass Platform Tile",
            type = "IMAGE",
            width = 64,
            height = 64,
            frameCols = 1,
            frameRows = 1
        )
        cachedBitmaps[tileAsset.id] = tileBmp
        assetList.add(tileAsset)
    }

    private fun createHeroSpriteSheet(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        for (frame in 0..3) {
            val ox = frame * 64f + 32f
            val oy = 32f
            val bobY = if (frame % 2 == 1) -2f else 0f

            // Body Armor (Cyan Blue)
            p.color = 0xFF0284C7.toInt()
            canvas.drawRoundRect(ox - 16f, oy - 16f + bobY, ox + 16f, oy + 14f + bobY, 6f, 6f, p)

            // Helmet (Silver Slate)
            p.color = 0xFFE2E8F0.toInt()
            canvas.drawRoundRect(ox - 14f, oy - 26f + bobY, ox + 14f, oy - 12f + bobY, 4f, 4f, p)

            // Glowing Visor
            p.color = 0xFF00E5FF.toInt()
            canvas.drawRect(ox - 8f, oy - 22f + bobY, ox + 12f, oy - 16f + bobY, p)

            // Gold Belt
            p.color = 0xFFFFD600.toInt()
            canvas.drawRect(ox - 16f, oy + bobY, ox + 16f, oy + 4f + bobY, p)

            // Legs/Feet with running swing
            p.color = 0xFF1E293B.toInt()
            when (frame) {
                0 -> { // Idle
                    canvas.drawRect(ox - 12f, oy + 14f, ox - 2f, oy + 26f, p)
                    canvas.drawRect(ox + 2f, oy + 14f, ox + 12f, oy + 26f, p)
                }
                1 -> { // Run frame 1
                    canvas.drawRect(ox - 16f, oy + 12f, ox - 6f, oy + 24f, p)
                    canvas.drawRect(ox + 4f, oy + 16f, ox + 16f, oy + 28f, p)
                }
                2 -> { // Run frame 2
                    canvas.drawRect(ox - 10f, oy + 14f, ox, oy + 26f, p)
                    canvas.drawRect(ox + 2f, oy + 14f, ox + 12f, oy + 26f, p)
                }
                3 -> { // Jump pose
                    canvas.drawRect(ox - 14f, oy + 10f, ox - 4f, oy + 20f, p)
                    canvas.drawRect(ox + 4f, oy + 10f, ox + 14f, oy + 20f, p)
                }
            }
        }
        return bmp
    }

    private fun createSlimeSpriteSheet(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        for (frame in 0..3) {
            val ox = frame * 64f + 32f
            val oy = 36f
            val squishX = when (frame) {
                0 -> 18f
                1 -> 22f
                2 -> 16f
                else -> 20f
            }
            val squishY = when (frame) {
                0 -> 16f
                1 -> 12f
                2 -> 20f
                else -> 14f
            }

            // Green Body
            p.color = 0xFF22C55E.toInt()
            canvas.drawOval(ox - squishX, oy - squishY, ox + squishX, oy + squishY, p)

            // Inner Glow
            p.color = 0xFF86EFAC.toInt()
            canvas.drawOval(ox - squishX * 0.6f, oy - squishY * 0.7f, ox + squishX * 0.6f, oy - squishY * 0.2f, p)

            // Eyes
            p.color = Color.BLACK
            canvas.drawCircle(ox - 6f, oy - 2f, 3.5f, p)
            canvas.drawCircle(ox + 6f, oy - 2f, 3.5f, p)

            p.color = Color.WHITE
            canvas.drawCircle(ox - 5f, oy - 3f, 1.5f, p)
            canvas.drawCircle(ox + 7f, oy - 3f, 1.5f, p)
        }
        return bmp
    }

    private fun createCoinSpriteSheet(): Bitmap {
        val bmp = Bitmap.createBitmap(256, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        for (frame in 0..3) {
            val ox = frame * 64f + 32f
            val oy = 32f
            val widthScale = when (frame) {
                0 -> 1.0f
                1 -> 0.65f
                2 -> 0.2f
                else -> 0.65f
            }

            p.color = 0xFFFFD700.toInt()
            canvas.drawOval(ox - 20f * widthScale, oy - 20f, ox + 20f * widthScale, oy + 20f, p)

            p.color = 0xFFF59E0B.toInt()
            canvas.drawOval(ox - 14f * widthScale, oy - 14f, ox + 14f * widthScale, oy + 14f, p)

            p.color = 0xFFFFFBEB.toInt()
            canvas.drawOval(ox - 8f * widthScale, oy - 8f, ox + 4f * widthScale, oy - 2f, p)
        }
        return bmp
    }

    private fun createSpaceshipSprite(): Bitmap {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        val path = Path().apply {
            moveTo(32f, 8f)
            lineTo(56f, 52f)
            lineTo(32f, 44f)
            lineTo(8f, 52f)
            close()
        }
        p.color = 0xFF0284C7.toInt()
        canvas.drawPath(path, p)

        // Wings
        p.color = 0xFF38BDF8.toInt()
        canvas.drawCircle(32f, 26f, 8f, p)

        // Thruster flame
        p.color = 0xFFFF4500.toInt()
        canvas.drawOval(26f, 44f, 38f, 58f, p)
        p.color = 0xFFFFD700.toInt()
        canvas.drawOval(29f, 44f, 35f, 52f, p)

        return bmp
    }

    private fun createTileBlock(): Bitmap {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Dirt Base
        p.color = 0xFF78350F.toInt()
        canvas.drawRect(0f, 0f, 64f, 64f, p)

        // Brick Texture Lines
        p.color = 0xFF451A03.toInt()
        p.strokeWidth = 2f
        p.style = Paint.Style.STROKE
        canvas.drawLine(0f, 32f, 64f, 32f, p)
        canvas.drawLine(32f, 0f, 32f, 32f, p)
        canvas.drawLine(16f, 32f, 16f, 64f, p)
        canvas.drawLine(48f, 32f, 48f, 64f, p)

        // Grass Top
        p.style = Paint.Style.FILL
        p.color = 0xFF22C55E.toInt()
        canvas.drawRect(0f, 0f, 64f, 12f, p)

        // Grass tufts
        p.color = 0xFF16A34A.toInt()
        for (i in 0..7) {
            val gx = i * 8f + 4f
            canvas.drawCircle(gx, 12f, 3f, p)
        }

        return bmp
    }
}
