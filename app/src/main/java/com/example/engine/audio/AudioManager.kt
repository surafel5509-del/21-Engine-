package com.example.engine.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager as AndroidAudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.engine.math.MathUtils
import com.example.engine.utils.Logger
import kotlin.concurrent.thread
import kotlin.math.sin

/**
 * Complete Audio Manager supporting:
 * 1. Low latency Sound Effects via SoundPool.
 * 2. Background Music via MediaPlayer.
 * 3. Procedural Audio Synthesis via AudioTrack for built-in retro sounds (jump, coin, explosion, laser, click).
 * 4. Master / SFX / Music volume controls and lifecycle management.
 */
class AudioManager(val context: Context) {
    companion object {
        private const val TAG = "AudioManager"
        private const val MAX_SIMULTANEOUS_STREAMS = 8
    }

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>() // resId to soundId
    private var mediaPlayer: MediaPlayer? = null

    var masterVolume: Float = 1.0f
    var sfxVolume: Float = 1.0f
    var musicVolume: Float = 0.8f
    var isMuted: Boolean = false

    private val proceduralSounds = mutableMapOf<String, ByteArray>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_SIMULTANEOUS_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()

        // Pre-generate standard procedural sound effects
        generateProceduralSounds()
    }

    fun loadSound(resId: Int): Int {
        if (soundMap.containsKey(resId)) {
            return soundMap[resId] ?: 0
        }
        val soundId = soundPool.load(context, resId, 1)
        soundMap[resId] = soundId
        return soundId
    }

    fun playSound(resId: Int, volume: Float = 1.0f, pitch: Float = 1.0f): Int {
        if (isMuted) return 0
        val soundId = soundMap[resId] ?: loadSound(resId)
        val finalVol = MathUtils.clamp01(volume * sfxVolume * masterVolume)
        return soundPool.play(soundId, finalVol, finalVol, 1, 0, MathUtils.clamp(pitch, 0.5f, 2.0f))
    }

    fun playMusic(resId: Int, loop: Boolean = true) {
        if (isMuted) return
        stopMusic()

        try {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = loop
                val finalVol = MathUtils.clamp01(musicVolume * masterVolume)
                setVolume(finalVol, finalVol)
                start()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to play music: ${e.message}")
        }
    }

    fun stopMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    fun pauseAll() {
        soundPool.autoPause()
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun resumeAll() {
        if (isMuted) return
        soundPool.autoResume()
        mediaPlayer?.let {
            it.start()
        }
    }

    fun release() {
        stopMusic()
        soundPool.release()
        soundMap.clear()
        proceduralSounds.clear()
    }

    // =========================================================================
    // Procedural Audio Synthesis (Built-in sound effects without external audio files)
    // =========================================================================

    private fun generateProceduralSounds() {
        thread(name = "AudioSynthThread") {
            try {
                proceduralSounds["jump"] = synthesizeTone(startFreq = 220f, endFreq = 660f, durationSeconds = 0.18f, wave = WaveType.SQUARE)
                proceduralSounds["coin"] = synthesizeTwoTone(f1 = 987.77f, f2 = 1318.51f, duration = 0.22f)
                proceduralSounds["explosion"] = synthesizeNoise(duration = 0.35f)
                proceduralSounds["laser"] = synthesizeTone(startFreq = 880f, endFreq = 110f, durationSeconds = 0.15f, wave = WaveType.SAWTOOTH)
                proceduralSounds["click"] = synthesizeTone(startFreq = 440f, endFreq = 220f, durationSeconds = 0.05f, wave = WaveType.SINE)
                proceduralSounds["powerup"] = synthesizeTone(startFreq = 300f, endFreq = 900f, durationSeconds = 0.3f, wave = WaveType.TRIANGLE)
            } catch (e: Exception) {
                Logger.e(TAG, "Procedural audio generation error: ${e.message}")
            }
        }
    }

    fun playSynthSound(name: String, volume: Float = 1.0f) {
        if (isMuted) return
        val pcm = proceduralSounds[name] ?: return
        val finalVol = MathUtils.clamp01(volume * sfxVolume * masterVolume)

        thread(name = "PlaySynth_$name") {
            try {
                val sampleRate = 22050
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.setVolume(finalVol)
                audioTrack.write(pcm, 0, pcm.size)
                audioTrack.play()
                Thread.sleep((pcm.size * 1000L) / sampleRate + 50L)
                audioTrack.release()
            } catch (e: Exception) {
                Logger.e(TAG, "Failed playing synth sound: ${e.message}")
            }
        }
    }

    private enum class WaveType { SINE, SQUARE, SAWTOOTH, TRIANGLE }

    private fun synthesizeTone(startFreq: Float, endFreq: Float, durationSeconds: Float, wave: WaveType): ByteArray {
        val sampleRate = 22050
        val sampleCount = (sampleRate * durationSeconds).toInt()
        val buffer = ByteArray(sampleCount)
        var phase = 0.0

        for (i in 0 until sampleCount) {
            val t = i.toFloat() / sampleCount
            val currentFreq = MathUtils.lerp(startFreq, endFreq, t)
            val deltaPhase = (2.0 * Math.PI * currentFreq) / sampleRate
            phase = (phase + deltaPhase) % (2.0 * Math.PI)

            val rawSample = when (wave) {
                WaveType.SINE -> sin(phase)
                WaveType.SQUARE -> if (phase < Math.PI) 0.8 else -0.8
                WaveType.SAWTOOTH -> (phase / Math.PI) - 1.0
                WaveType.TRIANGLE -> (2.0 / Math.PI) * Math.asin(sin(phase))
            }

            // Envelope: quick attack, linear decay
            val envelope = (1f - t).coerceIn(0f, 1f)
            val byteVal = (128 + (rawSample * envelope * 110.0)).toInt().coerceIn(0, 255)
            buffer[i] = byteVal.toByte()
        }
        return buffer
    }

    private fun synthesizeTwoTone(f1: Float, f2: Float, duration: Float): ByteArray {
        val sampleRate = 22050
        val sampleCount = (sampleRate * duration).toInt()
        val half = sampleCount / 2
        val buffer = ByteArray(sampleCount)
        var phase = 0.0

        for (i in 0 until sampleCount) {
            val freq = if (i < half) f1 else f2
            val deltaPhase = (2.0 * Math.PI * freq) / sampleRate
            phase = (phase + deltaPhase) % (2.0 * Math.PI)

            val raw = if (phase < Math.PI) 0.7 else -0.7
            val envelope = (1f - (i.toFloat() / sampleCount)).coerceIn(0f, 1f)
            val byteVal = (128 + (raw * envelope * 100.0)).toInt().coerceIn(0, 255)
            buffer[i] = byteVal.toByte()
        }
        return buffer
    }

    private fun synthesizeNoise(duration: Float): ByteArray {
        val sampleRate = 22050
        val sampleCount = (sampleRate * duration).toInt()
        val buffer = ByteArray(sampleCount)

        for (i in 0 until sampleCount) {
            val t = i.toFloat() / sampleCount
            val envelope = (1f - t) * (1f - t)
            val noise = (Math.random() * 2.0 - 1.0)
            val byteVal = (128 + (noise * envelope * 120.0)).toInt().coerceIn(0, 255)
            buffer[i] = byteVal.toByte()
        }
        return buffer
    }
}
