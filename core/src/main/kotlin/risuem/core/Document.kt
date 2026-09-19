package risuem.core

/** Чем рисуем. */
enum class Tool { BRUSH, THIN, ERASER }

/** Точка штриха вместе с толщиной в этом месте. */
data class StrokePoint(val x: Float, val y: Float, val width: Float)

/** Один штрих: от касания до отрыва пальца. */
data class Stroke(
    val tool: Tool,
    val color: Int,
    val points: List<StrokePoint>,
)

/** Наклейка: лицо героя или фигурка. */
data class Sticker(
    val kind: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
)

/**
 * Рисунок целиком: слой штрихов и слой наклеек поверх.
 *
 * Модель неизменяемая — так отмена шага сводится к возврату предыдущего
 * документа, и ошибиться негде.
 */
data class Document(
    val width: Int,
    val height: Int,
    val background: Int = Raster.WHITE,
    val strokes: List<Stroke> = emptyList(),
    val stickers: List<Sticker> = emptyList(),
) {
    val isEmpty: Boolean get() = strokes.isEmpty() && stickers.isEmpty()

    fun plusStroke(stroke: Stroke): Document = copy(strokes = strokes + stroke)

    fun plusSticker(sticker: Sticker): Document = copy(stickers = stickers + sticker)

    fun withSticker(index: Int, sticker: Sticker): Document {
        if (index !in stickers.indices) return this
        val list = stickers.toMutableList()
        list[index] = sticker
        return copy(stickers = list)
    }

    fun withoutSticker(index: Int): Document {
        if (index !in stickers.indices) return this
        val list = stickers.toMutableList()
        list.removeAt(index)
        return copy(stickers = list)
    }

    fun cleared(): Document = copy(strokes = emptyList(), stickers = emptyList())
}

/** Готовая раскраска: какой шаблон и каким цветом закрашена каждая область. */
data class ColoringWork(
    val templateId: String,
    val colors: IntArray,
) {
    override fun equals(other: Any?): Boolean =
        other is ColoringWork && templateId == other.templateId && colors.contentEquals(other.colors)

    override fun hashCode(): Int = 31 * templateId.hashCode() + colors.contentHashCode()
}
