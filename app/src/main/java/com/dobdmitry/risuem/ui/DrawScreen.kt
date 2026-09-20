package com.dobdmitry.risuem.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.art.Stickers
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.ui.components.BigAction
import com.dobdmitry.risuem.ui.components.HoldAction
import com.dobdmitry.risuem.ui.components.PaletteBar
import com.dobdmitry.risuem.ui.components.SmallIconButton
import com.dobdmitry.risuem.ui.components.SoundToggle
import com.dobdmitry.risuem.ui.components.paintColor
import risuem.core.Sticker
import risuem.core.Tool

/** Какая полка открыта под панелью инструментов. */
private enum class Panel { NONE, COLORS, STICKERS }

/** Толщина линий в точках холста: жирная кисть, тонкая кисть, ластик. */
private const val WIDTH_BRUSH = 46f
private const val WIDTH_THIN = 16f
private const val WIDTH_ERASER = 90f

/**
 * Чистый лист.
 *
 * Рисование идёт прямо в растр: на каждое движение пальца дорисовывается
 * только новый кусочек линии. Наклейки живут отдельным слоем сверху —
 * их можно двигать, растягивать двумя пальцами и выбрасывать в корзину.
 *
 * OptIn нужен ради `historical`: система отдаёт промежуточные точки касания
 * между кадрами, и только с ними быстрый росчерк остаётся гладким. API
 * помечено экспериментальным, но без него линия рвётся на резких движениях.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawScreen(model: RisuemViewModel) {
    val audio = LocalAudio.current
    val context = LocalContext.current
    val density = LocalDensity.current

    var tool by remember { mutableStateOf(Tool.BRUSH) }
    var panel by remember { mutableStateOf(Panel.NONE) }
    var trashBounds by remember { mutableStateOf(Rect.Zero) }
    var canvasOrigin by remember { mutableStateOf(Offset.Zero) }

    val stickerImages = remember(model.heroList) { Stickers.bitmaps(context, model.heroList) }
    // Обёртки для Compose считаем один раз: каждый кадр их пересоздавать незачем.
    val stickerPictures = remember(stickerImages) { stickerImages.mapValues { it.value.asImageBitmap() } }
    val stickerKinds = remember(model.heroList) { Stickers.kinds(model.heroList) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallIconButton(Pic.HOME) { model.goHome() }
            Spacer(Modifier.weight(1f))
            BigAction(pic = Pic.UNDO, word = Words.NAZAD, size = 64.dp) { model.undoStroke() }
            Spacer(Modifier.size(12.dp))
            BigAction(pic = Pic.CHECK, word = Words.GOTOVO, size = 64.dp) {
                val engine = model.engine ?: return@BigAction
                val flat = Stickers.flatten(engine.snapshot(), model.document.stickers, stickerImages)
                model.saveDrawing(flat)
                model.refreshWorks()
                audio.play(Sound.FANFARE)
                audio.say(Words.SAVED)
            }
            Spacer(Modifier.weight(1f))
            SoundToggle()
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .onGloballyPositioned { canvasOrigin = it.boundsInWindow().topLeft },
        ) {
            val widthPx = with(density) { maxWidth.roundToPx() }
            val heightPx = with(density) { maxHeight.roundToPx() }
            LaunchedEffect(widthPx, heightPx) {
                if (widthPx > 0 && heightPx > 0) model.prepareCanvas(widthPx, heightPx)
            }

            val engine = model.engine
            if (engine != null && engine.width == widthPx && engine.height == heightPx) {
                val paper = remember(engine) { engine.bitmap.asImageBitmap() }
                val baseWidth = when (tool) {
                    Tool.BRUSH -> WIDTH_BRUSH
                    Tool.THIN -> WIDTH_THIN
                    Tool.ERASER -> WIDTH_ERASER
                }
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(engine, tool, model.brushColor) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                engine.beginStroke(
                                    tool,
                                    paintColor(model.brushColor).toArgb(),
                                    baseWidth,
                                    down.position.x,
                                    down.position.y,
                                    down.uptimeMillis,
                                )
                                audio.play(if (tool == Tool.ERASER) Sound.ERASE else Sound.BLUP)
                                down.consume()
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    // Промежуточные точки от системы: линия не рвётся
                                    // даже на самом быстром движении пальца.
                                    for (past in change.historical) {
                                        engine.extendStroke(past.position.x, past.position.y, past.uptimeMillis)
                                    }
                                    engine.extendStroke(change.position.x, change.position.y, change.uptimeMillis)
                                    change.consume()
                                    if (!change.pressed) break
                                }
                                engine.endStroke()?.let { model.addStroke(it) }
                            }
                        },
                ) {
                    engine.version // подписка на изменения растра
                    drawImage(
                        image = paper,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(engine.width, engine.height),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                    )
                }

                // Слой наклеек поверх рисунка.
                model.document.stickers.forEachIndexed { index, sticker ->
                    val image = stickerPictures[sticker.kind] ?: return@forEachIndexed
                    StickerView(
                        sticker = sticker,
                        image = image,
                        onChange = { next ->
                            model.changeDocument(model.document.withSticker(index, next), pushUndo = false)
                        },
                        onEnd = { finished ->
                            val here = canvasOrigin + Offset(finished.x, finished.y)
                            if (trashBounds.contains(here)) {
                                model.changeDocument(model.document.withoutSticker(index))
                                audio.play(Sound.ERASE)
                            }
                        },
                    )
                }
            }
        }

        // Панель инструментов.
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolButton(Pic.BRUSH, tool == Tool.BRUSH && panel == Panel.NONE) {
                tool = Tool.BRUSH
                panel = Panel.NONE
            }
            ToolButton(Pic.THIN, tool == Tool.THIN && panel == Panel.NONE) {
                tool = Tool.THIN
                panel = Panel.NONE
            }
            ToolButton(Pic.ERASER, tool == Tool.ERASER) {
                tool = Tool.ERASER
                panel = Panel.NONE
            }
            ToolButton(Pic.PALETTE, panel == Panel.COLORS) {
                panel = if (panel == Panel.COLORS) Panel.NONE else Panel.COLORS
                if (tool == Tool.ERASER) tool = Tool.BRUSH
            }
            ToolButton(Pic.STICKER, panel == Panel.STICKERS) {
                panel = if (panel == Panel.STICKERS) Panel.NONE else Panel.STICKERS
            }
            Box(Modifier.onGloballyPositioned { trashBounds = it.boundsInWindow() }) {
                HoldAction(pic = Pic.TRASH, word = null, size = 64.dp, holdMillis = 1000) {
                    model.clearDrawing()
                    audio.play(Sound.ERASE)
                }
            }
        }

        when (panel) {
            Panel.COLORS -> PaletteBar(selected = model.brushColor) { model.brushColor = it }
            Panel.STICKERS -> LazyRow(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(stickerKinds, key = { it.id }) { kind ->
                    val image = stickerPictures[kind.id]
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .pointerInput(kind.id) {
                                    detectTapGestures {
                                        val engine = model.engine ?: return@detectTapGestures
                                        model.changeDocument(
                                            model.document.plusSticker(
                                                Sticker(
                                                    kind = kind.id,
                                                    x = engine.width / 2f,
                                                    y = engine.height / 2f,
                                                )
                                            )
                                        )
                                        audio.play(Sound.POP)
                                        audio.say(kind.name)
                                    }
                                },
                        )
                    }
                }
            }
            Panel.NONE -> Unit
        }
    }
}

@Composable
private fun ToolButton(pic: Pic, selected: Boolean, onClick: () -> Unit) {
    SmallIconButton(
        pic = pic,
        size = 64.dp,
        background = if (selected) Accent else PaperDeep,
        tint = if (selected) Color.White else Ink,
        onClick = onClick,
    )
}

/**
 * Одна наклейка.
 *
 * Появляется с отскоком, двигается пальцем, масштабируется и поворачивается
 * двумя. Если отпустить её над корзиной — исчезает.
 */
@Composable
private fun StickerView(
    sticker: Sticker,
    image: androidx.compose.ui.graphics.ImageBitmap,
    onChange: (Sticker) -> Unit,
    onEnd: (Sticker) -> Unit,
) {
    val density = LocalDensity.current
    val appear = remember { Animatable(0.25f) }
    LaunchedEffect(Unit) {
        appear.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        )
    }
    val sideDp = with(density) { Stickers.SIDE.toDp() }
    // Жест живёт дольше одной перекомпоновки — ему нужна всегда свежая наклейка.
    val latest by rememberUpdatedState(sticker)
    var current = sticker

    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier
            .offset {
                IntOffset(
                    (sticker.x - Stickers.SIDE / 2f).toInt(),
                    (sticker.y - Stickers.SIDE / 2f).toInt(),
                )
            }
            .size(sideDp)
            .graphicsLayer(
                scaleX = sticker.scale * appear.value,
                scaleY = sticker.scale * appear.value,
                rotationZ = sticker.rotation,
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    current = latest
                    while (true) {
                        val event = awaitPointerEvent()
                        val pan = event.calculatePan()
                        val zoom = event.calculateZoom()
                        val rotation = event.calculateRotation()
                        if (pan != Offset.Zero || zoom != 1f || rotation != 0f) {
                            current = current.copy(
                                x = current.x + pan.x,
                                y = current.y + pan.y,
                                scale = (current.scale * zoom).coerceIn(0.4f, 3f),
                                rotation = current.rotation + rotation,
                            )
                            onChange(current)
                        }
                        event.changes.forEach { it.consume() }
                        if (event.changes.none { it.pressed }) break
                    }
                    onEnd(current)
                }
            },
    )
}
