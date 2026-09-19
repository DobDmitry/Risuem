package com.dobdmitry.risuem.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Голос, который проговаривает надписи.
 *
 * Русского голоса на телефоне может не быть — тогда приложение просто молчит,
 * и всё остальное работает как ни в чём не бывало. Ни одного диалога с
 * предложением что-то скачать: ребёнок такое не поймёт, а взрослому не надо.
 */
class Voice(context: Context) {

    private var ready = false
    private var engine: TextToSpeech? = null

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine?.setLanguage(Locale("ru", "RU"))
                ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                engine?.setSpeechRate(0.92f)
            }
        }
    }

    val available: Boolean get() = ready

    /** Сказать. Новая фраза прерывает предыдущую — ребёнок не ждёт очереди. */
    fun say(text: String, enabled: Boolean) {
        if (!enabled || !ready) return
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "risuem")
    }

    fun stop() {
        engine?.stop()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }
}
