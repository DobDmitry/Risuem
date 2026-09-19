package risuem.core

/**
 * Картинка в памяти: массив пикселей ARGB и размеры.
 *
 * Ни одной зависимости от Android — всё считается здесь, а Compose только
 * показывает готовый результат. Поэтому каждую функцию можно проверить тестом.
 */
class Raster(val width: Int, val height: Int, val pixels: IntArray) {

    init {
        require(width > 0 && height > 0) { "размеры должны быть положительными" }
        require(pixels.size == width * height) { "массив пикселей не совпадает с размерами" }
    }

    constructor(width: Int, height: Int, fill: Int = WHITE) :
        this(width, height, IntArray(width * height) { fill })

    fun index(x: Int, y: Int): Int = y * width + x

    fun inside(x: Int, y: Int): Boolean = x >= 0 && y >= 0 && x < width && y < height

    operator fun get(x: Int, y: Int): Int = pixels[y * width + x]

    operator fun set(x: Int, y: Int, color: Int) {
        pixels[y * width + x] = color
    }

    fun copy(): Raster = Raster(width, height, pixels.copyOf())

    fun fillAll(color: Int) {
        pixels.fill(color)
    }

    /** Прямоугольник — им рисуются тестовые картинки и крупные области шаблонов. */
    fun rect(x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        for (y in maxOf(0, y0)..minOf(height - 1, y1)) {
            for (x in maxOf(0, x0)..minOf(width - 1, x1)) {
                pixels[y * width + x] = color
            }
        }
    }

    companion object {
        const val WHITE: Int = -0x1          // 0xFFFFFFFF
        const val BLACK: Int = -0x1000000    // 0xFF000000
        const val TRANSPARENT: Int = 0
    }
}

fun argb(a: Int, r: Int, g: Int, b: Int): Int =
    ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

fun alphaOf(color: Int): Int = (color ushr 24) and 0xFF
fun redOf(color: Int): Int = (color ushr 16) and 0xFF
fun greenOf(color: Int): Int = (color ushr 8) and 0xFF
fun blueOf(color: Int): Int = color and 0xFF

/** Яркость 0..255. Прозрачный пиксель считаем светлым: это «пусто», а не линия. */
fun luminance(color: Int): Int {
    if (alphaOf(color) < 32) return 255
    return (redOf(color) * 299 + greenOf(color) * 587 + blueOf(color) * 114) / 1000
}

/** Расстояние между цветами — максимум по каналам. Дёшево и понятно. */
fun colorDistance(a: Int, b: Int): Int {
    val dr = kotlin.math.abs(redOf(a) - redOf(b))
    val dg = kotlin.math.abs(greenOf(a) - greenOf(b))
    val db = kotlin.math.abs(blueOf(a) - blueOf(b))
    val da = kotlin.math.abs(alphaOf(a) - alphaOf(b))
    return maxOf(dr, dg, db, da)
}
