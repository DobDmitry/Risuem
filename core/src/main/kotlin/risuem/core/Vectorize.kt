package risuem.core

/**
 * Превращение фотографии в раскраску.
 *
 * Порядок такой:
 *   1) серый цвет вместо цветного;
 *   2) сглаживание — чтобы шум камеры не рассыпался в тысячу точек;
 *   3) разбиение яркости на несколько ступеней (порог/постеризация);
 *   4) поиск связных областей;
 *   5) укрупнение: всё, что мельче [Options.minArea], вливается в соседа —
 *      повторяем, пока каждая область не станет крупной, чтобы четырёхлетний
 *      палец попадал без промаха;
 *   6) границы между областями становятся чёрным контуром.
 */
object Vectorize {

    data class Options(
        /** Сколько ступеней яркости. 3 — контрастно и понятно, 2 — совсем плакатно. */
        val levels: Int = 3,
        /** Радиус сглаживания в пикселях. */
        val blurRadius: Int = 2,
        /** Минимальная область в пикселях. */
        val minArea: Int = 2500,
        /** Толщина контура в пикселях. */
        val outlineWidth: Int = 3,
    )

    /** Разметка: номер области для каждого пикселя. */
    class Labeling(val width: Int, val height: Int, val labels: IntArray, val count: Int) {
        fun areas(): IntArray {
            val out = IntArray(count)
            for (l in labels) if (l >= 0) out[l]++
            return out
        }
    }

    fun toColoring(source: Raster, options: Options = Options()): ColoringPage {
        val outline = toOutline(source, options)
        return ColoringPage.fromOutline(outline, minArea = options.minArea / 4)
    }

    /** Промежуточный результат — чёрный контур на белом. Его же показываем взрослому. */
    fun toOutline(source: Raster, options: Options = Options()): Raster {
        val gray = blur(toGray(source), source.width, source.height, options.blurRadius)
        val levels = posterize(gray, options.levels)
        var labeling = label(levels, source.width, source.height)
        labeling = mergeSmall(labeling, options.minArea)
        return drawBorders(labeling, options.outlineWidth)
    }

    /**
     * Чёрное и белое без полутонов.
     *
     * Кисть на телефоне рисует со сглаживанием, и по краям линии остаются
     * серые пиксели. Для раскраски это вредно: серая кайма превращалась бы
     * в кучу крошечных областей. Поэтому перед разметкой контур приводим
     * к двум цветам.
     */
    fun binarize(source: Raster, threshold: Int = 128): Raster {
        val out = IntArray(source.pixels.size) {
            if (luminance(source.pixels[it]) < threshold) Raster.BLACK else Raster.WHITE
        }
        return Raster(source.width, source.height, out)
    }

    fun toGray(source: Raster): IntArray = IntArray(source.pixels.size) { luminance(source.pixels[it]) }

    /** Простое коробочное размытие в два прохода — быстро и без библиотек. */
    fun blur(gray: IntArray, width: Int, height: Int, radius: Int): IntArray {
        if (radius <= 0) return gray.copyOf()
        val tmp = IntArray(gray.size)
        val out = IntArray(gray.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0
                var n = 0
                for (dx in -radius..radius) {
                    val nx = x + dx
                    if (nx in 0 until width) {
                        sum += gray[y * width + nx]
                        n++
                    }
                }
                tmp[y * width + x] = sum / n
            }
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0
                var n = 0
                for (dy in -radius..radius) {
                    val ny = y + dy
                    if (ny in 0 until height) {
                        sum += tmp[ny * width + x]
                        n++
                    }
                }
                out[y * width + x] = sum / n
            }
        }
        return out
    }

    /** Ступени яркости. Границы считаем по реальному разбросу картинки, а не по 0..255. */
    fun posterize(gray: IntArray, levels: Int): IntArray {
        require(levels >= 2) { "ступеней должно быть хотя бы две" }
        var min = 255
        var max = 0
        for (v in gray) {
            if (v < min) min = v
            if (v > max) max = v
        }
        if (max <= min) return IntArray(gray.size)
        val span = (max - min + 1).toDouble()
        return IntArray(gray.size) {
            val step = ((gray[it] - min) * levels / span).toInt()
            if (step >= levels) levels - 1 else step
        }
    }

    /** Связные области одинаковой ступени, 4-связность. */
    fun label(values: IntArray, width: Int, height: Int): Labeling {
        val labels = IntArray(values.size) { -1 }
        var next = 0
        val stack = IntArrayBuilder()
        for (start in values.indices) {
            if (labels[start] != -1) continue
            val level = values[start]
            val id = next++
            stack.add(start)
            labels[start] = id
            while (stack.size > 0) {
                val i = stack.removeLast()
                val x = i % width
                val y = i / width
                if (x > 0) push(values, labels, stack, i - 1, level, id)
                if (x < width - 1) push(values, labels, stack, i + 1, level, id)
                if (y > 0) push(values, labels, stack, i - width, level, id)
                if (y < height - 1) push(values, labels, stack, i + width, level, id)
            }
        }
        return Labeling(width, height, labels, next)
    }

    private fun push(values: IntArray, labels: IntArray, stack: IntArrayBuilder, i: Int, level: Int, id: Int) {
        if (labels[i] != -1 || values[i] != level) return
        labels[i] = id
        stack.add(i)
    }

    /**
     * Схлопывание мелочи: каждая область меньше [minArea] вливается в соседа,
     * с которым у неё самая длинная общая граница. Повторяем, пока мелочь не кончится.
     */
    fun mergeSmall(labeling: Labeling, minArea: Int): Labeling {
        val width = labeling.width
        val height = labeling.height
        val parent = IntArray(labeling.count) { it }

        fun root(a: Int): Int {
            var x = a
            while (parent[x] != x) {
                parent[x] = parent[parent[x]]
                x = parent[x]
            }
            return x
        }

        val areas = labeling.areas()
        // Длина общей границы каждой пары соседей.
        val border = HashMap<Long, Int>()
        val neighbours = Array(labeling.count) { HashSet<Int>() }
        fun remember(a: Int, b: Int) {
            if (a == b) return
            val lo = minOf(a, b)
            val hi = maxOf(a, b)
            val key = lo.toLong() shl 32 or hi.toLong()
            border[key] = (border[key] ?: 0) + 1
            neighbours[lo].add(hi)
            neighbours[hi].add(lo)
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = y * width + x
                if (x < width - 1) remember(labeling.labels[i], labeling.labels[i + 1])
                if (y < height - 1) remember(labeling.labels[i], labeling.labels[i + width])
            }
        }

        fun borderOf(a: Int, b: Int): Int {
            val lo = minOf(a, b)
            val hi = maxOf(a, b)
            return border[lo.toLong() shl 32 or hi.toLong()] ?: 0
        }

        val alive = (0 until labeling.count).sortedBy { areas[it] }.toMutableList()
        var changed = true
        while (changed) {
            changed = false
            for (id in alive) {
                val r = root(id)
                if (r != id) continue
                if (areas[r] >= minArea) continue
                // самый «родной» сосед: длиннее общая граница, при равенстве — крупнее площадь
                var best = -1
                var bestBorder = -1
                for (n in neighbours[r]) {
                    val nr = root(n)
                    if (nr == r) continue
                    val len = borderOf(r, nr)
                    if (len > bestBorder || (len == bestBorder && best >= 0 && areas[nr] > areas[best])) {
                        best = nr
                        bestBorder = len
                    }
                }
                if (best < 0) continue
                // Вливаем мелкую область в соседа.
                parent[r] = best
                areas[best] += areas[r]
                val merged = neighbours[r].map { root(it) }.filter { it != best }
                neighbours[best].addAll(merged)
                for (m in merged) {
                    neighbours[m].add(best)
                    border[minOf(best, m).toLong() shl 32 or maxOf(best, m).toLong()] =
                        borderOf(best, m) + borderOf(r, m)
                }
                changed = true
            }
        }

        // Перенумеровываем подряд: 0, 1, 2 …
        val rename = HashMap<Int, Int>()
        val out = IntArray(labeling.labels.size)
        for (i in labeling.labels.indices) {
            val r = root(labeling.labels[i])
            out[i] = rename.getOrPut(r) { rename.size }
        }
        return Labeling(width, height, out, rename.size)
    }

    /** Границы между областями — чёрные, всё остальное — белое. */
    fun drawBorders(labeling: Labeling, thickness: Int): Raster {
        val width = labeling.width
        val height = labeling.height
        val raster = Raster(width, height, Raster.WHITE)
        val edge = BooleanArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = y * width + x
                val l = labeling.labels[i]
                val right = x < width - 1 && labeling.labels[i + 1] != l
                val down = y < height - 1 && labeling.labels[i + width] != l
                if (right || down) edge[i] = true
            }
        }
        val half = maxOf(1, thickness) / 2
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!edge[y * width + x]) continue
                for (dy in -half..half) {
                    for (dx in -half..half) {
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until width && ny in 0 until height) {
                            raster[nx, ny] = Raster.BLACK
                        }
                    }
                }
            }
        }
        return raster
    }
}
