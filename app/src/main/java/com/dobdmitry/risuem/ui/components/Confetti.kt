package com.dobdmitry.risuem.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.dobdmitry.risuem.ui.PaintColors
import kotlin.math.sin
import kotlin.random.Random

/**
 * Конфетти — награда за закрашенную картинку.
 *
 * Считается один раз: бумажки заранее получают свой путь, а кадр только
 * подставляет время. Поэтому праздник не мешает рисовать дальше.
 */
private class Fleck(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val swing: Float,
    val size: Float,
    val spin: Float,
    val color: Color,
)

@Composable
fun Confetti(
    running: Boolean,
    modifier: Modifier = Modifier,
    count: Int = 70,
) {
    val flecks = remember(count) {
        val random = Random(7)
        List(count) {
            Fleck(
                x = random.nextFloat(),
                delay = random.nextFloat() * 0.35f,
                speed = 0.75f + random.nextFloat() * 0.6f,
                swing = 0.02f + random.nextFloat() * 0.05f,
                size = 0.018f + random.nextFloat() * 0.022f,
                spin = 2f + random.nextFloat() * 6f,
                color = PaintColors[random.nextInt(PaintColors.size)],
            )
        }
    }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(running) {
        if (running) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(2600, easing = LinearEasing))
        } else {
            progress.snapTo(0f)
        }
    }

    if (progress.value <= 0f) return

    Canvas(modifier) {
        val t = progress.value
        for (fleck in flecks) {
            val local = ((t - fleck.delay) * fleck.speed).coerceAtMost(1.4f)
            if (local <= 0f) continue
            val y = local * size.height * 1.25f - size.height * 0.15f
            if (y > size.height) continue
            val x = fleck.x * size.width + sin(local * fleck.spin) * size.width * fleck.swing
            val side = size.minDimension * fleck.size
            val squash = 0.4f + 0.6f * kotlin.math.abs(sin(local * fleck.spin))
            drawRect(
                color = fleck.color,
                topLeft = Offset(x, y),
                size = Size(side, side * squash),
            )
        }
    }
}
