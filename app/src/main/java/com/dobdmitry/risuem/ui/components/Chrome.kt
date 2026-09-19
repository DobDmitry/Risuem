package com.dobdmitry.risuem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.ui.Ink
import com.dobdmitry.risuem.ui.PaperDeep
import com.dobdmitry.risuem.ui.Pic
import com.dobdmitry.risuem.ui.PicIcon
import com.dobdmitry.risuem.ui.Words

/** Маленькая круглая кнопка-иконка: для углов экрана. */
@Composable
fun SmallIconButton(
    pic: Pic,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    tint: Color = Ink,
    background: Color = PaperDeep,
    onClick: () -> Unit,
) {
    val audio = LocalAudio.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(background)
            .pointerInput(Unit) {
                detectTapGestures {
                    audio.play(Sound.BLUP)
                    onClick()
                }
            },
    ) {
        PicIcon(pic, Modifier.size(size * 0.58f), tint)
    }
}

/**
 * Выключатель звука.
 *
 * Иконка честно показывает состояние: перечёркнутый динамик — тихо.
 * Слово «ЗВУК» рядом не нужно нигде, кроме стартового экрана.
 */
@Composable
fun SoundToggle(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val audio = LocalAudio.current
    SmallIconButton(
        pic = if (audio.enabled) Pic.SOUND_ON else Pic.SOUND_OFF,
        modifier = modifier,
        size = size,
    ) {
        val next = !audio.enabled
        audio.setEnabled(next)
        if (next) {
            audio.play(Sound.BLUP)
            audio.say(Words.ZVUK.speech)
        }
    }
}
