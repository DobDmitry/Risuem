package risuem.core

/**
 * Заливка области по касанию — сердце раскраски.
 *
 * Алгоритм построчный (scanline): вместо рекурсии по каждому пикселю
 * обрабатываем целые горизонтальные отрезки. На картинке 1500x1500
 * это сотые доли секунды и никакого переполнения стека.
 *
 * Два правила, без которых заливка «протекает» у ребёнка на глазах:
 *  1) допуск по цвету (tolerance) — сглаженные края контура чуть серые,
 *     и без допуска вокруг линии остаётся белая бахрома;
 *  2) порог контура (barrier) — тёмный пиксель остаётся стеной, даже если
 *     допуск большой. Иначе краска вытекает наружу через мягкую границу.
 *
 * Тап по самому контуру не делает ничего: линия рисунка не закрашивается.
 * Перекрасить уже закрашенное всё равно можно — раскраска работает по
 * заранее размеченным областям ([ColoringPage]), а не заливкой заново.
 */
object FloodFill {

    /** Что получилось: сколько пикселей закрашено и в какой прямоугольник уложилось. */
    data class Result(
        val filled: Int,
        val minX: Int,
        val minY: Int,
        val maxX: Int,
        val maxY: Int,
    ) {
        val isEmpty: Boolean get() = filled == 0

        companion object {
            val EMPTY = Result(0, 0, 0, -1, -1)
        }
    }

    const val DEFAULT_TOLERANCE = 32
    const val DEFAULT_BARRIER = 110

    /**
     * Заливает область, в которую попал палец, цветом [newColor].
     * Меняет [raster] на месте и возвращает описание закрашенного.
     */
    fun fill(
        raster: Raster,
        startX: Int,
        startY: Int,
        newColor: Int,
        tolerance: Int = DEFAULT_TOLERANCE,
        barrier: Int = DEFAULT_BARRIER,
    ): Result {
        if (!raster.inside(startX, startY)) return Result.EMPTY
        if (raster[startX, startY] == newColor) return Result.EMPTY

        var filled = 0
        var minX = raster.width
        var minY = raster.height
        var maxX = -1
        var maxY = -1

        val pixels = collect(raster, startX, startY, tolerance, barrier)
        for (p in pixels) {
            raster.pixels[p] = newColor
            filled++
            val x = p % raster.width
            val y = p / raster.width
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }
        if (filled == 0) return Result.EMPTY
        return Result(filled, minX, minY, maxX, maxY)
    }

    /**
     * Собирает индексы пикселей области, ничего не закрашивая.
     * На этом же обходе держится разметка областей раскраски.
     */
    fun collect(
        raster: Raster,
        startX: Int,
        startY: Int,
        tolerance: Int = DEFAULT_TOLERANCE,
        barrier: Int = DEFAULT_BARRIER,
    ): IntArray {
        val out = IntArrayBuilder()
        if (!raster.inside(startX, startY)) return out.toArray()

        val width = raster.width
        val height = raster.height
        val target = raster[startX, startY]
        // Тап по самой линии ничего не красит: контур — это рисунок, а не область.
        if (luminance(target) < barrier) return out.toArray()
        val useBarrier = true

        val seen = BooleanArray(width * height)
        val stack = IntArrayBuilder()
        stack.add(startY * width + startX)

        while (stack.size > 0) {
            val p = stack.removeLast()
            if (seen[p]) continue
            val y = p / width
            val x = p % width
            if (!matches(raster, x, y, target, tolerance, barrier, useBarrier)) continue

            var left = x
            while (left - 1 >= 0 && !seen[y * width + left - 1] &&
                matches(raster, left - 1, y, target, tolerance, barrier, useBarrier)
            ) left--
            var right = x
            while (right + 1 < width && !seen[y * width + right + 1] &&
                matches(raster, right + 1, y, target, tolerance, barrier, useBarrier)
            ) right++

            var i = left
            while (i <= right) {
                seen[y * width + i] = true
                out.add(y * width + i)
                i++
            }

            if (y > 0) pushRuns(raster, seen, stack, left, right, y - 1, target, tolerance, barrier, useBarrier)
            if (y < height - 1) pushRuns(raster, seen, stack, left, right, y + 1, target, tolerance, barrier, useBarrier)
        }
        return out.toArray()
    }

    private fun matches(
        raster: Raster,
        x: Int,
        y: Int,
        target: Int,
        tolerance: Int,
        barrier: Int,
        useBarrier: Boolean,
    ): Boolean {
        val c = raster[x, y]
        if (useBarrier && luminance(c) < barrier) return false
        return colorDistance(c, target) <= tolerance
    }

    /** Складывает в стек по одной точке на каждый подходящий отрезок соседней строки. */
    private fun pushRuns(
        raster: Raster,
        seen: BooleanArray,
        stack: IntArrayBuilder,
        from: Int,
        to: Int,
        y: Int,
        target: Int,
        tolerance: Int,
        barrier: Int,
        useBarrier: Boolean,
    ) {
        val width = raster.width
        var x = from
        while (x <= to) {
            if (!seen[y * width + x] && matches(raster, x, y, target, tolerance, barrier, useBarrier)) {
                stack.add(y * width + x)
                while (x <= to && !seen[y * width + x] &&
                    matches(raster, x, y, target, tolerance, barrier, useBarrier)
                ) x++
            } else {
                x++
            }
        }
    }
}

/** Растущий массив int без боксинга — заменяет ArrayList<Int> в горячем коде заливки. */
class IntArrayBuilder(initial: Int = 64) {
    private var data = IntArray(initial)
    var size: Int = 0
        private set

    fun add(value: Int) {
        if (size == data.size) data = data.copyOf(data.size * 2)
        data[size++] = value
    }

    fun removeLast(): Int = data[--size]

    operator fun get(i: Int): Int = data[i]

    fun toArray(): IntArray = data.copyOf(size)
}
