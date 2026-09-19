package com.dobdmitry.risuem.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin

/**
 * Все иконки нарисованы кодом.
 *
 * Так в проекте нет ни одной скачанной картинки, иконка не мылится на любом
 * экране и её легко перекрасить. Каждая рисуется в квадрате 0..1 и тянется
 * под размер, который ей дали.
 */
enum class Pic {
    BRUSH, THIN, ERASER, PALETTE, STICKER, TRASH, UNDO, CHECK, HOME,
    SOUND_ON, SOUND_OFF, BUCKET, PENCIL, GRID, HERO, CAMERA, PHOTO, SHARE,
    STAR, HEART, SUN, LOCK, PLUS,
}

@Composable
fun PicIcon(kind: Pic, modifier: Modifier = Modifier, tint: Color = Ink) {
    Canvas(modifier) { drawPic(kind, tint) }
}

fun DrawScope.drawPic(kind: Pic, tint: Color) {
    val s = size.minDimension
    val left = (size.width - s) / 2f
    val top = (size.height - s) / 2f
    fun p(x: Float, y: Float) = Offset(left + s * x, top + s * y)
    val thick = s * 0.11f
    val line = Stroke(width = thick, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun path(build: Path.() -> Unit): Path = Path().apply(build)

    when (kind) {
        Pic.BRUSH -> {
            drawLine(tint, p(0.72f, 0.18f), p(0.40f, 0.52f), thick, StrokeCap.Round)
            drawPath(
                path {
                    moveTo(p(0.44f, 0.46f).x, p(0.44f, 0.46f).y)
                    lineTo(p(0.24f, 0.66f).x, p(0.24f, 0.66f).y)
                    lineTo(p(0.34f, 0.84f).x, p(0.34f, 0.84f).y)
                    lineTo(p(0.56f, 0.60f).x, p(0.56f, 0.60f).y)
                    close()
                },
                tint, style = Fill,
            )
        }
        Pic.THIN -> {
            drawLine(tint, p(0.74f, 0.20f), p(0.42f, 0.54f), s * 0.07f, StrokeCap.Round)
            drawPath(
                path {
                    moveTo(p(0.44f, 0.52f).x, p(0.44f, 0.52f).y)
                    lineTo(p(0.26f, 0.78f).x, p(0.26f, 0.78f).y)
                    lineTo(p(0.36f, 0.82f).x, p(0.36f, 0.82f).y)
                    lineTo(p(0.52f, 0.60f).x, p(0.52f, 0.60f).y)
                    close()
                },
                tint, style = Fill,
            )
        }
        Pic.ERASER -> {
            drawPath(
                path {
                    moveTo(p(0.26f, 0.62f).x, p(0.26f, 0.62f).y)
                    lineTo(p(0.58f, 0.24f).x, p(0.58f, 0.24f).y)
                    lineTo(p(0.82f, 0.46f).x, p(0.82f, 0.46f).y)
                    lineTo(p(0.50f, 0.82f).x, p(0.50f, 0.82f).y)
                    close()
                },
                tint, style = line,
            )
            drawLine(tint, p(0.18f, 0.84f), p(0.56f, 0.84f), thick, StrokeCap.Round)
        }
        Pic.PALETTE -> {
            drawCircle(tint, radius = s * 0.34f, center = p(0.5f, 0.5f), style = line)
            drawCircle(tint, radius = s * 0.07f, center = p(0.36f, 0.36f))
            drawCircle(tint, radius = s * 0.07f, center = p(0.64f, 0.34f))
            drawCircle(tint, radius = s * 0.07f, center = p(0.34f, 0.62f))
            drawCircle(tint, radius = s * 0.07f, center = p(0.62f, 0.64f))
        }
        Pic.STICKER -> {
            drawPath(
                path {
                    moveTo(p(0.20f, 0.20f).x, p(0.20f, 0.20f).y)
                    lineTo(p(0.80f, 0.20f).x, p(0.80f, 0.20f).y)
                    lineTo(p(0.80f, 0.62f).x, p(0.80f, 0.62f).y)
                    lineTo(p(0.62f, 0.80f).x, p(0.62f, 0.80f).y)
                    lineTo(p(0.20f, 0.80f).x, p(0.20f, 0.80f).y)
                    close()
                },
                tint, style = line,
            )
            star(p(0.48f, 0.46f), s * 0.18f, tint)
        }
        Pic.TRASH -> {
            drawPath(
                path {
                    moveTo(p(0.28f, 0.32f).x, p(0.28f, 0.32f).y)
                    lineTo(p(0.72f, 0.32f).x, p(0.72f, 0.32f).y)
                    lineTo(p(0.64f, 0.82f).x, p(0.64f, 0.82f).y)
                    lineTo(p(0.36f, 0.82f).x, p(0.36f, 0.82f).y)
                    close()
                },
                tint, style = line,
            )
            drawLine(tint, p(0.20f, 0.28f), p(0.80f, 0.28f), thick, StrokeCap.Round)
            drawLine(tint, p(0.40f, 0.20f), p(0.60f, 0.20f), thick, StrokeCap.Round)
        }
        Pic.UNDO -> {
            drawPath(
                path {
                    moveTo(p(0.30f, 0.42f).x, p(0.30f, 0.42f).y)
                    cubicTo(
                        p(0.52f, 0.18f).x, p(0.52f, 0.18f).y,
                        p(0.86f, 0.30f).x, p(0.86f, 0.30f).y,
                        p(0.78f, 0.62f).x, p(0.78f, 0.62f).y,
                    )
                    cubicTo(
                        p(0.72f, 0.80f).x, p(0.72f, 0.80f).y,
                        p(0.52f, 0.84f).x, p(0.52f, 0.84f).y,
                        p(0.40f, 0.78f).x, p(0.40f, 0.78f).y,
                    )
                },
                tint, style = line,
            )
            drawPath(
                path {
                    moveTo(p(0.16f, 0.36f).x, p(0.16f, 0.36f).y)
                    lineTo(p(0.34f, 0.24f).x, p(0.34f, 0.24f).y)
                    lineTo(p(0.36f, 0.52f).x, p(0.36f, 0.52f).y)
                    close()
                },
                tint, style = Fill,
            )
        }
        Pic.CHECK -> {
            drawPath(
                path {
                    moveTo(p(0.20f, 0.52f).x, p(0.20f, 0.52f).y)
                    lineTo(p(0.42f, 0.74f).x, p(0.42f, 0.74f).y)
                    lineTo(p(0.82f, 0.26f).x, p(0.82f, 0.26f).y)
                },
                tint, style = Stroke(width = s * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        Pic.HOME -> {
            drawPath(
                path {
                    moveTo(p(0.14f, 0.50f).x, p(0.14f, 0.50f).y)
                    lineTo(p(0.50f, 0.18f).x, p(0.50f, 0.18f).y)
                    lineTo(p(0.86f, 0.50f).x, p(0.86f, 0.50f).y)
                },
                tint, style = line,
            )
            drawPath(
                path {
                    moveTo(p(0.24f, 0.48f).x, p(0.24f, 0.48f).y)
                    lineTo(p(0.24f, 0.82f).x, p(0.24f, 0.82f).y)
                    lineTo(p(0.76f, 0.82f).x, p(0.76f, 0.82f).y)
                    lineTo(p(0.76f, 0.48f).x, p(0.76f, 0.48f).y)
                },
                tint, style = line,
            )
        }
        Pic.SOUND_ON, Pic.SOUND_OFF -> {
            drawPath(
                path {
                    moveTo(p(0.20f, 0.38f).x, p(0.20f, 0.38f).y)
                    lineTo(p(0.34f, 0.38f).x, p(0.34f, 0.38f).y)
                    lineTo(p(0.50f, 0.22f).x, p(0.50f, 0.22f).y)
                    lineTo(p(0.50f, 0.78f).x, p(0.50f, 0.78f).y)
                    lineTo(p(0.34f, 0.62f).x, p(0.34f, 0.62f).y)
                    lineTo(p(0.20f, 0.62f).x, p(0.20f, 0.62f).y)
                    close()
                },
                tint, style = Fill,
            )
            if (kind == Pic.SOUND_ON) {
                drawArc(
                    color = tint,
                    startAngle = -55f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = p(0.40f, 0.26f),
                    size = Size(s * 0.36f, s * 0.48f),
                    style = Stroke(width = s * 0.09f, cap = StrokeCap.Round),
                )
            } else {
                drawLine(tint, p(0.62f, 0.34f), p(0.86f, 0.66f), thick, StrokeCap.Round)
                drawLine(tint, p(0.86f, 0.34f), p(0.62f, 0.66f), thick, StrokeCap.Round)
            }
        }
        Pic.BUCKET -> {
            drawPath(
                path {
                    moveTo(p(0.22f, 0.36f).x, p(0.22f, 0.36f).y)
                    lineTo(p(0.66f, 0.36f).x, p(0.66f, 0.36f).y)
                    lineTo(p(0.58f, 0.80f).x, p(0.58f, 0.80f).y)
                    lineTo(p(0.30f, 0.80f).x, p(0.30f, 0.80f).y)
                    close()
                },
                tint, style = line,
            )
            drawArc(
                color = tint,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = p(0.30f, 0.16f),
                size = Size(s * 0.28f, s * 0.28f),
                style = Stroke(width = s * 0.08f, cap = StrokeCap.Round),
            )
            drawCircle(tint, radius = s * 0.10f, center = p(0.80f, 0.62f))
        }
        Pic.PENCIL -> {
            drawPath(
                path {
                    moveTo(p(0.70f, 0.16f).x, p(0.70f, 0.16f).y)
                    lineTo(p(0.84f, 0.30f).x, p(0.84f, 0.30f).y)
                    lineTo(p(0.36f, 0.78f).x, p(0.36f, 0.78f).y)
                    lineTo(p(0.18f, 0.82f).x, p(0.18f, 0.82f).y)
                    lineTo(p(0.22f, 0.64f).x, p(0.22f, 0.64f).y)
                    close()
                },
                tint, style = line,
            )
        }
        Pic.GRID -> {
            val r = s * 0.06f
            for (gx in 0..1) for (gy in 0..1) {
                drawRoundRect(
                    color = tint,
                    topLeft = p(0.20f + gx * 0.34f, 0.20f + gy * 0.34f),
                    size = Size(s * 0.26f, s * 0.26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                    style = Stroke(width = s * 0.08f),
                )
            }
        }
        Pic.HERO -> {
            drawCircle(tint, radius = s * 0.16f, center = p(0.42f, 0.34f), style = line)
            drawPath(
                path {
                    moveTo(p(0.18f, 0.82f).x, p(0.18f, 0.82f).y)
                    cubicTo(
                        p(0.20f, 0.56f).x, p(0.20f, 0.56f).y,
                        p(0.64f, 0.56f).x, p(0.64f, 0.56f).y,
                        p(0.66f, 0.82f).x, p(0.66f, 0.82f).y,
                    )
                },
                tint, style = line,
            )
            drawLine(tint, p(0.80f, 0.20f), p(0.80f, 0.44f), thick, StrokeCap.Round)
            drawLine(tint, p(0.68f, 0.32f), p(0.92f, 0.32f), thick, StrokeCap.Round)
        }
        Pic.CAMERA -> {
            drawRoundRect(
                color = tint,
                topLeft = p(0.14f, 0.30f),
                size = Size(s * 0.72f, s * 0.48f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.10f, s * 0.10f),
                style = line,
            )
            drawCircle(tint, radius = s * 0.14f, center = p(0.50f, 0.54f), style = line)
            drawLine(tint, p(0.36f, 0.26f), p(0.62f, 0.26f), thick, StrokeCap.Round)
        }
        Pic.PHOTO -> {
            drawRoundRect(
                color = tint,
                topLeft = p(0.14f, 0.22f),
                size = Size(s * 0.72f, s * 0.56f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.08f, s * 0.08f),
                style = line,
            )
            drawPath(
                path {
                    moveTo(p(0.20f, 0.70f).x, p(0.20f, 0.70f).y)
                    lineTo(p(0.40f, 0.46f).x, p(0.40f, 0.46f).y)
                    lineTo(p(0.58f, 0.70f).x, p(0.58f, 0.70f).y)
                    lineTo(p(0.68f, 0.58f).x, p(0.68f, 0.58f).y)
                    lineTo(p(0.80f, 0.70f).x, p(0.80f, 0.70f).y)
                },
                tint, style = line,
            )
            drawCircle(tint, radius = s * 0.06f, center = p(0.66f, 0.36f))
        }
        Pic.SHARE -> {
            drawCircle(tint, radius = s * 0.10f, center = p(0.74f, 0.24f))
            drawCircle(tint, radius = s * 0.10f, center = p(0.26f, 0.50f))
            drawCircle(tint, radius = s * 0.10f, center = p(0.74f, 0.76f))
            drawLine(tint, p(0.34f, 0.46f), p(0.66f, 0.28f), s * 0.07f, StrokeCap.Round)
            drawLine(tint, p(0.34f, 0.56f), p(0.66f, 0.72f), s * 0.07f, StrokeCap.Round)
        }
        Pic.STAR -> star(p(0.5f, 0.5f), s * 0.42f, tint)
        Pic.HEART -> heart(p(0.5f, 0.52f), s * 0.42f, tint)
        Pic.SUN -> {
            drawCircle(tint, radius = s * 0.22f, center = p(0.5f, 0.5f))
            for (i in 0 until 8) {
                val a = i * Math.PI.toFloat() / 4f
                val c = p(0.5f, 0.5f)
                val from = Offset(c.x + kotlin.math.cos(a) * s * 0.30f, c.y + kotlin.math.sin(a) * s * 0.30f)
                val to = Offset(c.x + kotlin.math.cos(a) * s * 0.44f, c.y + kotlin.math.sin(a) * s * 0.44f)
                drawLine(tint, from, to, s * 0.08f, StrokeCap.Round)
            }
        }
        Pic.LOCK -> {
            drawRoundRect(
                color = tint,
                topLeft = p(0.24f, 0.46f),
                size = Size(s * 0.52f, s * 0.38f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.08f, s * 0.08f),
                style = line,
            )
            drawArc(
                color = tint,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = p(0.34f, 0.22f),
                size = Size(s * 0.32f, s * 0.32f),
                style = Stroke(width = s * 0.09f, cap = StrokeCap.Round),
            )
        }
        Pic.PLUS -> {
            drawLine(tint, p(0.50f, 0.18f), p(0.50f, 0.82f), s * 0.14f, StrokeCap.Round)
            drawLine(tint, p(0.18f, 0.50f), p(0.82f, 0.50f), s * 0.14f, StrokeCap.Round)
        }
    }
}

/** Пятиконечная звезда — иконка и наклейка одновременно. */
fun DrawScope.star(center: Offset, radius: Float, color: Color) {
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else radius * 0.45f
        val a = -Math.PI.toFloat() / 2f + i * Math.PI.toFloat() / 5f
        val x = center.x + kotlin.math.cos(a) * r
        val y = center.y + kotlin.math.sin(a) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

/** Сердце. */
fun DrawScope.heart(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val x = center.x
    val y = center.y
    path.moveTo(x, y + radius * 0.75f)
    path.cubicTo(
        x - radius * 1.5f, y - radius * 0.2f,
        x - radius * 0.55f, y - radius * 1.2f,
        x, y - radius * 0.35f,
    )
    path.cubicTo(
        x + radius * 0.55f, y - radius * 1.2f,
        x + radius * 1.5f, y - radius * 0.2f,
        x, y + radius * 0.75f,
    )
    path.close()
    drawPath(path, color, style = Fill)
}
