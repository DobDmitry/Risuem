package risuem.core

/**
 * Снятие однотонного фона с фотографии.
 *
 * Заливкой от краёв: то, что похоже на цвет по краям кадра и связано с краем,
 * становится прозрачным. Герой в середине остаётся целым, даже если он
 * такого же цвета, как стена за ним, — он просто не связан с краем.
 */
object Background {

    const val DEFAULT_TOLERANCE = 48

    /** Возвращает копию с прозрачным фоном. Исходник не трогаем. */
    fun removeUniform(source: Raster, tolerance: Int = DEFAULT_TOLERANCE): Raster {
        val out = source.copy()
        val width = out.width
        val height = out.height
        val edge = medianEdgeColor(source)
        val seen = BooleanArray(width * height)
        val stack = IntArrayBuilder()

        fun consider(x: Int, y: Int) {
            val i = y * width + x
            if (seen[i]) return
            if (colorDistance(source.pixels[i], edge) > tolerance) return
            seen[i] = true
            stack.add(i)
        }

        for (x in 0 until width) {
            consider(x, 0)
            consider(x, height - 1)
        }
        for (y in 0 until height) {
            consider(0, y)
            consider(width - 1, y)
        }

        while (stack.size > 0) {
            val i = stack.removeLast()
            out.pixels[i] = Raster.TRANSPARENT
            val x = i % width
            val y = i / width
            if (x > 0) consider(x - 1, y)
            if (x < width - 1) consider(x + 1, y)
            if (y > 0) consider(x, y - 1)
            if (y < height - 1) consider(x, y + 1)
        }
        return out
    }

    /** Медианный цвет рамки кадра: устойчив к случайной соринке в углу. */
    fun medianEdgeColor(source: Raster): Int {
        val reds = ArrayList<Int>()
        val greens = ArrayList<Int>()
        val blues = ArrayList<Int>()
        fun take(x: Int, y: Int) {
            val c = source[x, y]
            reds.add(redOf(c))
            greens.add(greenOf(c))
            blues.add(blueOf(c))
        }
        for (x in 0 until source.width) {
            take(x, 0)
            take(x, source.height - 1)
        }
        for (y in 0 until source.height) {
            take(0, y)
            take(source.width - 1, y)
        }
        reds.sort(); greens.sort(); blues.sort()
        val m = reds.size / 2
        return argb(255, reds[m], greens[m], blues[m])
    }
}
