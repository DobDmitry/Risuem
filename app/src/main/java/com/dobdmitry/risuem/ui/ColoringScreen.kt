package com.dobdmitry.risuem.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.draw.ColoringCanvas
import com.dobdmitry.risuem.ui.components.BigAction
import com.dobdmitry.risuem.ui.components.Confetti
import com.dobdmitry.risuem.ui.components.HoldAction
import com.dobdmitry.risuem.ui.components.PaletteBar
import com.dobdmitry.risuem.ui.components.SmallIconButton
import com.dobdmitry.risuem.ui.components.SoundToggle
import com.dobdmitry.risuem.ui.components.paintColor
import com.dobdmitry.risuem.ui.components.rememberPulse
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Раскраска.
 *
 * Тап по области — она заливается выбранным цветом, краска растекается от
 * пальца за четверть секунды. Внизу палитра, над ней три кнопки:
 * НАЗАД (отменить последнюю заливку), ЕЩЁ РАЗ (стереть всё, с удержанием)
 * и ГОТОВО (сохранить). Когда закрашено всё — конфетти и «Как красиво!».
 */
@Composable
fun ColoringScreen(model: RisuemViewModel) {
    val audio = LocalAudio.current
    val scope = rememberCoroutineScope()
    val canvas = model.coloring
    val reveal = remember(canvas) { Animatable(0f) }
    var celebrate by remember(canvas) { mutableStateOf(false) }

    LaunchedEffect(canvas) {
        if (canvas != null) audio.say(Words.PICK_COLOR)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SmallIconButton(Pic.HOME) {
                    model.closeColoring()
                    model.goHome()
                }
                Spacer(Modifier.weight(1f))
                SoundToggle()
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
            ) {
                if (canvas == null) {
                    // Пока считаются области — спокойная пульсирующая иконка, без слов.
                    PicIcon(
                        Pic.PALETTE,
                        Modifier.size(96.dp).scale(rememberPulse()),
                        Ink,
                    )
                } else {
                    ColoringSurface(
                        canvas = canvas,
                        reveal = reveal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(canvas.page.width.toFloat() / canvas.page.height)
                            .clip(RoundedCornerShape(16.dp))
                            .background(androidx.compose.ui.graphics.Color.White),
                        onTap = { pageX, pageY, screenX, screenY ->
                            val region = canvas.page.regionAt(pageX, pageY)
                            if (region >= 0) {
                                val snapshot = canvas.page.snapshot()
                                val color = paintColor(model.fillColor).toArgb()
                                if (canvas.fill(region, color, screenX, screenY)) {
                                    model.colorUndo.push(snapshot)
                                    audio.play(Sound.SPLASH)
                                    scope.launch {
                                        reveal.snapTo(0f)
                                        reveal.animateTo(1f, tween(250, easing = FastOutSlowInEasing))
                                        canvas.settle()
                                    }
                                    if (canvas.page.isComplete()) {
                                        celebrate = true
                                        audio.play(Sound.WOW)
                                        audio.say(Words.PRAISE)
                                    }
                                }
                            } else {
                                audio.play(Sound.BLUP)
                            }
                        },
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BigAction(pic = Pic.UNDO, word = Words.NAZAD, size = 76.dp) {
                    model.undoFill()
                    celebrate = false
                }
                HoldAction(
                    pic = Pic.TRASH,
                    word = Words.ESHCHYO,
                    size = 76.dp,
                    holdMillis = 1000,
                ) {
                    model.clearColoring()
                    celebrate = false
                    audio.say(Words.ESHCHYO.speech)
                }
                BigAction(pic = Pic.CHECK, word = Words.GOTOVO, size = 76.dp) {
                    val ready = canvas ?: return@BigAction
                    model.saveColoring(ready.snapshot())
                    model.refreshWorks()
                    audio.play(Sound.FANFARE)
                    audio.say(Words.SAVED)
                }
            }

            PaletteBar(selected = model.fillColor) { model.fillColor = it }
        }

        Confetti(running = celebrate, modifier = Modifier.fillMaxSize())
    }
}

/**
 * Сама картинка.
 *
 * Показываем растр «до заливки», а поверх — «после», но только внутри
 * растущего круга от места касания. Поэтому краска именно растекается,
 * и при этом ни один пиксель не пересчитывается на каждом кадре.
 */
@Composable
private fun ColoringSurface(
    canvas: ColoringCanvas,
    reveal: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier,
    onTap: (pageX: Int, pageY: Int, screenX: Float, screenY: Float) -> Unit,
) {
    val before = remember(canvas) { canvas.before.asImageBitmap() }
    val after = remember(canvas) { canvas.after.asImageBitmap() }
    val page = canvas.page

    Canvas(
        modifier.pointerInput(canvas) {
            detectTapGestures { offset ->
                val x = (offset.x / size.width * page.width).toInt()
                val y = (offset.y / size.height * page.height).toInt()
                onTap(x, y, offset.x, offset.y)
            }
        },
    ) {
        canvas.version // подписка на изменения растра
        val grow = reveal.value // читаем прямо в отрисовке: кадр анимации без перекомпоновки
        val whole = IntSize(size.width.toInt(), size.height.toInt())
        val source = IntSize(page.width, page.height)
        drawImage(
            image = before,
            srcOffset = IntOffset.Zero,
            srcSize = source,
            dstOffset = IntOffset.Zero,
            dstSize = whole,
        )
        if (grow > 0f) {
            val center = Offset(canvas.splashX, canvas.splashY)
            val maxRadius = hypot(size.width.toDouble(), size.height.toDouble()).toFloat()
            val radius = maxRadius * grow
            val circle = Path().apply {
                addOval(
                    Rect(
                        center.x - radius,
                        center.y - radius,
                        center.x + radius,
                        center.y + radius,
                    )
                )
            }
            clipPath(circle) {
                drawImage(
                    image = after,
                    srcOffset = IntOffset.Zero,
                    srcSize = source,
                    dstOffset = IntOffset.Zero,
                    dstSize = whole,
                )
            }
        }
    }
}
