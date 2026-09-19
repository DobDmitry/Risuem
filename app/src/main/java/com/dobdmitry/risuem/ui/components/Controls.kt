package com.dobdmitry.risuem.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.ui.Ink
import com.dobdmitry.risuem.ui.PaperDeep
import com.dobdmitry.risuem.ui.Pic
import com.dobdmitry.risuem.ui.PicIcon
import com.dobdmitry.risuem.ui.Word
import kotlinx.coroutines.launch

/**
 * Надпись, которую голос проговаривает при появлении.
 *
 * Слово короткое и заглавное, рядом с ним всегда есть иконка — пользоваться
 * приложением можно и совсем не читая.
 */
@Composable
fun SpokenWord(
    word: Word,
    modifier: Modifier = Modifier,
    color: Color = Ink,
    speak: Boolean = true,
) {
    val audio = LocalAudio.current
    LaunchedEffect(word, speak) {
        if (speak) audio.say(word.speech)
    }
    Text(
        text = word.text,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        modifier = modifier,
    )
}

/**
 * Большая кнопка: иконка и под ней слово.
 *
 * Нажатие сопровождается «блупом» и голосом — ребёнок понимает, что попал.
 */
@Composable
fun BigAction(
    pic: Pic,
    word: Word?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    tint: Color = Ink,
    background: Color = PaperDeep,
    speakOnAppear: Boolean = false,
    onClick: () -> Unit,
) {
    val audio = LocalAudio.current
    val press = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .scale(press.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        scope.launch { press.animateTo(0.92f, tween(70)) }
                        tryAwaitRelease()
                        scope.launch { press.animateTo(1f, tween(120)) }
                    },
                    onTap = {
                        audio.play(Sound.BLUP)
                        word?.let { audio.say(it.speech) }
                        onClick()
                    },
                )
            },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size / 4))
                .background(background),
        ) {
            PicIcon(pic, Modifier.size(size * 0.62f), tint)
        }
        if (word != null) {
            SpokenWord(word, Modifier.padding(top = 6.dp), tint, speak = speakOnAppear)
        }
    }
}

/**
 * Кнопка с удержанием: сработает, только если держать палец [holdMillis].
 *
 * Вокруг иконки растёт кольцо — видно, сколько осталось. Так «стереть всё»
 * и «удалить работу» невозможно нажать случайно, а взрослый или подросший
 * ребёнок делает это за секунду.
 */
@Composable
fun HoldAction(
    pic: Pic,
    word: Word?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    holdMillis: Int = 1000,
    tint: Color = Ink,
    ringColor: Color = com.dobdmitry.risuem.ui.Accent,
    background: Color = PaperDeep,
    onHold: () -> Unit,
) {
    val audio = LocalAudio.current
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.pointerInput(holdMillis) {
            detectTapGestures(
                onPress = {
                    audio.play(Sound.BLUP)
                    val job = scope.launch {
                        progress.animateTo(1f, tween(holdMillis, easing = LinearEasing))
                        onHold()
                    }
                    tryAwaitRelease()
                    job.cancel()
                    scope.launch { progress.animateTo(0f, tween(150)) }
                },
            )
        },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size * 0.82f)
                    .clip(RoundedCornerShape(size / 5))
                    .background(background),
            ) {
                PicIcon(pic, Modifier.size(size * 0.50f), tint)
            }
            Canvas(Modifier.fillMaxSize()) {
                if (progress.value <= 0f) return@Canvas
                val stroke = this.size.minDimension * 0.08f
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.value,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(
                        this.size.width - stroke,
                        this.size.height - stroke,
                    ),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        if (word != null) {
            SpokenWord(word, Modifier.padding(top = 6.dp), tint, speak = false)
        }
    }
}

/** Мягкое «дыхание» для выбранного элемента: видно, что выбрано, без слов. */
@Composable
fun rememberPulse(): Float {
    val transition = rememberInfiniteTransition(label = "пульс")
    val value by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "пульс",
    )
    return value
}
