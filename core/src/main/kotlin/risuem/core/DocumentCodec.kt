package risuem.core

/**
 * Сохранение работы в текст и обратно.
 *
 * Формат нарочно простой и построчный: его читает человек, он не ломается
 * от смены версии библиотек и не тянет ни одной зависимости.
 *
 *   RISUEM 1
 *   SIZE 1080 1600
 *   BG -1
 *   S BRUSH -65536 3
 *   P 10.0 20.0 12.0 11.0 21.0 12.5 ...
 *   K star 100.0 200.0 1.5 0.25
 */
object DocumentCodec {

    const val HEADER = "RISUEM"
    const val VERSION = 1

    fun encode(document: Document): String = buildString {
        append(HEADER).append(' ').append(VERSION).append('\n')
        append("SIZE ").append(document.width).append(' ').append(document.height).append('\n')
        append("BG ").append(document.background).append('\n')
        for (stroke in document.strokes) {
            append("S ").append(stroke.tool.name).append(' ').append(stroke.color).append('\n')
            append("P")
            for (p in stroke.points) {
                append(' ').append(p.x).append(' ').append(p.y).append(' ').append(p.width)
            }
            append('\n')
        }
        for (s in document.stickers) {
            append("K ").append(s.kind).append(' ').append(s.x).append(' ').append(s.y)
                .append(' ').append(s.scale).append(' ').append(s.rotation).append('\n')
        }
    }

    fun decode(text: String): Document {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        require(lines.isNotEmpty()) { "пустая запись" }
        val head = lines[0].split(' ')
        require(head.size == 2 && head[0] == HEADER) { "это не работа «РИСУЕМ»" }
        require(head[1].toInt() <= VERSION) { "запись новее приложения" }

        var width = 0
        var height = 0
        var background = Raster.WHITE
        val strokes = ArrayList<Stroke>()
        val stickers = ArrayList<Sticker>()
        var pendingTool: Tool? = null
        var pendingColor = 0

        for (line in lines.drop(1)) {
            val parts = line.split(' ')
            when (parts[0]) {
                "SIZE" -> {
                    require(parts.size == 3) { "битая строка SIZE" }
                    width = parts[1].toInt()
                    height = parts[2].toInt()
                }
                "BG" -> background = parts[1].toInt()
                "S" -> {
                    require(parts.size == 3) { "битая строка штриха" }
                    pendingTool = Tool.valueOf(parts[1])
                    pendingColor = parts[2].toInt()
                }
                "P" -> {
                    val tool = requireNotNull(pendingTool) { "точки без штриха" }
                    val numbers = parts.drop(1)
                    require(numbers.size % 3 == 0) { "точки идут тройками" }
                    val points = ArrayList<StrokePoint>(numbers.size / 3)
                    var i = 0
                    while (i < numbers.size) {
                        points.add(
                            StrokePoint(
                                numbers[i].toFloat(),
                                numbers[i + 1].toFloat(),
                                numbers[i + 2].toFloat(),
                            )
                        )
                        i += 3
                    }
                    strokes.add(Stroke(tool, pendingColor, points))
                    pendingTool = null
                }
                "K" -> {
                    require(parts.size == 6) { "битая наклейка" }
                    stickers.add(
                        Sticker(
                            parts[1],
                            parts[2].toFloat(),
                            parts[3].toFloat(),
                            parts[4].toFloat(),
                            parts[5].toFloat(),
                        )
                    )
                }
                else -> Unit // незнакомые строки молча пропускаем: старые записи не ломаются
            }
        }
        require(width > 0 && height > 0) { "нет размеров рисунка" }
        return Document(width, height, background, strokes, stickers)
    }

    fun decodeOrNull(text: String): Document? = try {
        decode(text)
    } catch (e: Exception) {
        null
    }

    fun encodeColoring(work: ColoringWork): String = buildString {
        append(HEADER).append(' ').append(VERSION).append('\n')
        append("TEMPLATE ").append(work.templateId).append('\n')
        append("COLORS")
        for (c in work.colors) append(' ').append(c)
        append('\n')
    }

    fun decodeColoring(text: String): ColoringWork {
        var template: String? = null
        var colors = IntArray(0)
        for (line in text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }) {
            val parts = line.split(' ')
            when (parts[0]) {
                "TEMPLATE" -> template = parts.getOrNull(1)
                "COLORS" -> colors = IntArray(parts.size - 1) { parts[it + 1].toInt() }
            }
        }
        return ColoringWork(requireNotNull(template) { "не указан шаблон" }, colors)
    }

    fun decodeColoringOrNull(text: String): ColoringWork? = try {
        decodeColoring(text)
    } catch (e: Exception) {
        null
    }
}
