package com.dobdmitry.risuem.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.dobdmitry.risuem.R

/** Какие звуки бывают. Все сгенерированы скриптом tools/make_sounds.py. */
enum class Sound { BLUP, SPLASH, ERASE, POP, FANFARE, WOW }

/**
 * Короткие звуки через SoundPool: он держит их распакованными в памяти,
 * поэтому «блуп» звучит мгновенно и не мешает рисовать.
 */
class Sfx(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids: Map<Sound, Int> = mapOf(
        Sound.BLUP to pool.load(context, R.raw.blup, 1),
        Sound.SPLASH to pool.load(context, R.raw.splash, 1),
        Sound.ERASE to pool.load(context, R.raw.erase, 1),
        Sound.POP to pool.load(context, R.raw.pop, 1),
        Sound.FANFARE to pool.load(context, R.raw.fanfare, 1),
        Sound.WOW to pool.load(context, R.raw.wow, 1),
    )

    /** Последний «блуп» чуть меняет высоту: пятидесятое касание подряд не бесит. */
    private var step = 0

    fun play(sound: Sound, enabled: Boolean) {
        if (!enabled) return
        val id = ids[sound] ?: return
        val rate = when (sound) {
            Sound.BLUP -> {
                step = (step + 1) % 4
                0.95f + step * 0.035f
            }
            else -> 1f
        }
        pool.play(id, 0.75f, 0.75f, 1, 0, rate)
    }

    fun release() = pool.release()
}
