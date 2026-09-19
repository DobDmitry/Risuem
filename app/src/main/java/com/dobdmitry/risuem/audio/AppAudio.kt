package com.dobdmitry.risuem.audio

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Звук и голос вместе с выключателем.
 *
 * Выключатель один на всё приложение: кнопка «ЗВУК» в углу гасит и звуки,
 * и голос — иногда нужно поиграть тихо.
 */
class AppAudio(val sfx: Sfx, val voice: Voice) {

    // Состояние Compose: иконка «ЗВУК» сама перерисуется при переключении.
    var enabled: Boolean by mutableStateOf(true)
        private set

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) voice.stop()
    }

    fun play(sound: Sound) = sfx.play(sound, enabled)

    fun say(text: String) = voice.say(text, enabled)

    fun release() {
        sfx.release()
        voice.release()
    }
}

/** Достать звук из любого места дерева, не таская его параметром. */
val LocalAudio = compositionLocalOf<AppAudio> { error("звук не подключён") }
