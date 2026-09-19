package risuem.core

/**
 * Раскраска: чёрный контур на белом, внутри — заранее размеченные области.
 *
 * Области считаются один раз при загрузке картинки ([fromOutline]), поэтому
 * касание пальцем стоит ровно один поиск номера области, а не заливку заново.
 * Так раскраска не тормозит даже на большой картинке, а «закрашено всё?»
 * проверяется мгновенно.
 */
class ColoringPage(
    val width: Int,
    val height: Int,
    private val outline: IntArray,
    private val labels: IntArray,
    val regions: List<Region>,
) {

    /** Одна область: её пиксели и рамка, чтобы знать, куда ставить «блуп» и конфетти. */
    class Region(
        val id: Int,
        val pixels: IntArray,
        val minX: Int,
        val minY: Int,
        val maxX: Int,
        val maxY: Int,
    ) {
        val area: Int get() = pixels.size
        val centerX: Int get() = (minX + maxX) / 2
        val centerY: Int get() = (minY + maxY) / 2
        val boxWidth: Int get() = maxX - minX + 1
        val boxHeight: Int get() = maxY - minY + 1
    }

    /** Цвет каждой области, 0 — ещё не закрашена. */
    val colors: IntArray = IntArray(regions.size)

    val regionCount: Int get() = regions.size

    /** Номер области под пальцем или -1, если попали в контур. */
    fun regionAt(x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= width || y >= height) return NO_REGION
        return labels[y * width + x]
    }

    fun colorOf(region: Int): Int =
        if (region in colors.indices) colors[region] else 0

    /** Закрасить область. Возвращает false, если цвет тот же или области нет. */
    fun fill(region: Int, color: Int): Boolean {
        if (region !in colors.indices) return false
        if (colors[region] == color) return false
        colors[region] = color
        return true
    }

    fun snapshot(): IntArray = colors.copyOf()

    fun restore(state: IntArray) {
        require(state.size == colors.size) { "снимок не от этой картинки" }
        state.copyInto(colors)
    }

    fun clear() = colors.fill(0)

    val filledCount: Int get() = colors.count { it != 0 }

    fun isComplete(): Boolean = regions.isNotEmpty() && colors.all { it != 0 }

    /** Готовая картинка: цвета областей, сверху исходный контур. */
    fun render(): IntArray {
        val out = IntArray(width * height)
        for (i in out.indices) {
            val label = labels[i]
            val color = if (label >= 0) colors[label] else 0
            out[i] = if (color != 0) color else outline[i]
        }
        return out
    }

    /** Контур без раскраски — для превью в списке картинок. */
    fun outlineCopy(): IntArray = outline.copyOf()

    companion object {
        const val NO_REGION = -1

        /**
         * Размечает области внутри готового контура.
         *
         * [minArea] — минимальный размер области в пикселях: всё, что мельче,
         * считается деталью рисунка и отдаётся контуру, чтобы пальцем
         * не приходилось целиться.
         */
        fun fromOutline(
            raster: Raster,
            minArea: Int = 0,
            tolerance: Int = FloodFill.DEFAULT_TOLERANCE,
            barrier: Int = FloodFill.DEFAULT_BARRIER,
        ): ColoringPage {
            val width = raster.width
            val height = raster.height
            val labels = IntArray(width * height) { NO_REGION }
            val regions = ArrayList<Region>()
            val work = raster.copy()

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val i = y * width + x
                    if (labels[i] != NO_REGION) continue
                    if (luminance(work.pixels[i]) < barrier) continue

                    val pixels = FloodFill.collect(work, x, y, tolerance, barrier)
                    if (pixels.isEmpty()) continue
                    if (pixels.size < minArea) {
                        // Мелочь не делаем областью: пусть останется рисунком.
                        for (p in pixels) labels[p] = NO_REGION
                        for (p in pixels) work.pixels[p] = Raster.BLACK
                        continue
                    }

                    val id = regions.size
                    var minX = width
                    var minY = height
                    var maxX = 0
                    var maxY = 0
                    for (p in pixels) {
                        labels[p] = id
                        val px = p % width
                        val py = p / width
                        if (px < minX) minX = px
                        if (px > maxX) maxX = px
                        if (py < minY) minY = py
                        if (py > maxY) maxY = py
                    }
                    regions.add(Region(id, pixels, minX, minY, maxX, maxY))
                }
            }
            // Отдаём подправленный контур: мелочь, которую мы отбросили, уже стала рисунком.
            return ColoringPage(width, height, work.pixels.copyOf(), labels, regions)
        }
    }
}
