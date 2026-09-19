package com.dobdmitry.risuem.art

import android.graphics.Path
import android.graphics.RectF
import com.dobdmitry.risuem.ui.Names

/**
 * Все рисованные фигуры приложения.
 *
 * Фигура описана один раз, а используется дважды: чёрным контуром —
 * как раскраска, и в цвете — как наклейка. Поэтому звери в двух режимах
 * одинаковые, и добавить нового зверя — это добавить сюда один список частей.
 *
 * Координаты внутри квадрата 300x300; при рисовании фигура растягивается
 * под нужный размер. Части нарочно крупные: самая маленькая закрашиваемая
 * область всё равно останется больше 60x60 dp на экране телефона.
 */
object Figures {

    const val SIZE = 300f

    /**
     * Часть фигуры.
     * [filled] — мелкая деталь (глаз, нос): она всегда чёрная и в раскраске
     * остаётся рисунком, а не областью, в которую надо попадать пальцем.
     */
    class Part(val color: Int, val filled: Boolean = false, val build: Path.() -> Unit)

    class Figure(val id: String, val name: String, val parts: List<Part>)

    private fun oval(x0: Float, y0: Float, x1: Float, y1: Float): Path.() -> Unit = {
        addOval(RectF(x0, y0, x1, y1), Path.Direction.CW)
    }

    private fun box(x0: Float, y0: Float, x1: Float, y1: Float, round: Float = 0f): Path.() -> Unit = {
        if (round > 0f) {
            addRoundRect(RectF(x0, y0, x1, y1), round, round, Path.Direction.CW)
        } else {
            addRect(RectF(x0, y0, x1, y1), Path.Direction.CW)
        }
    }

    private fun poly(vararg points: Float): Path.() -> Unit = {
        moveTo(points[0], points[1])
        var i = 2
        while (i < points.size) {
            lineTo(points[i], points[i + 1])
            i += 2
        }
        close()
    }

    private const val RED = 0xFFE8503A.toInt()
    private const val ORANGE = 0xFFF07A2E.toInt()
    private const val YELLOW = 0xFFF5C024.toInt()
    private const val GREEN = 0xFF4B9E5F.toInt()
    private const val LIGHT_GREEN = 0xFF9CC14A.toInt()
    private const val BLUE = 0xFF4C9BD6.toInt()
    private const val DEEP_BLUE = 0xFF3E63B0.toInt()
    private const val PINK = 0xFFE877A6.toInt()
    private const val BROWN = 0xFF8B5E3C.toInt()
    private const val LIGHT_BROWN = 0xFFC08A55.toInt()
    private const val GREY = 0xFF9AA3A8.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val BLACK = 0xFF3B3027.toInt()

    // ───────────────────────── простые картинки ─────────────────────────

    val HOUSE = Figure("domik", Names.DOMIK, listOf(
        Part(RED, build = poly(20f, 145f, 150f, 35f, 280f, 145f)),
        Part(YELLOW, build = box(50f, 145f, 250f, 275f)),
        Part(BROWN, build = box(100f, 190f, 160f, 275f)),
        Part(BLUE, build = box(175f, 175f, 240f, 240f)),
    ))

    val CAR = Figure("mashina", Names.MASHINA, listOf(
        Part(BLUE, build = box(20f, 150f, 280f, 220f, 24f)),
        Part(LIGHT_GREEN, build = box(80f, 85f, 205f, 150f, 18f)),
        Part(BLACK, filled = true, build = oval(60f, 195f, 130f, 265f)),
        Part(BLACK, filled = true, build = oval(175f, 195f, 245f, 265f)),
        Part(GREY, build = oval(75f, 210f, 115f, 250f)),
        Part(GREY, build = oval(190f, 210f, 230f, 250f)),
    ))

    val SUN = Figure("solnce", Names.SOLNCE, listOf(
        Part(YELLOW, build = {
            for (i in 0 until 8) {
                val a = i * Math.PI / 4.0
                val nx = 150f + Math.cos(a).toFloat() * 140f
                val ny = 150f + Math.sin(a).toFloat() * 140f
                val lx = 150f + Math.cos(a + 0.35).toFloat() * 95f
                val ly = 150f + Math.sin(a + 0.35).toFloat() * 95f
                val rx = 150f + Math.cos(a - 0.35).toFloat() * 95f
                val ry = 150f + Math.sin(a - 0.35).toFloat() * 95f
                moveTo(lx, ly)
                lineTo(nx, ny)
                lineTo(rx, ry)
                close()
            }
        }),
        Part(ORANGE, build = oval(55f, 55f, 245f, 245f)),
    ))

    val FLOWER = Figure("cvetok", Names.CVETOK, listOf(
        Part(GREEN, build = box(120f, 150f, 180f, 285f, 20f)),
        Part(LIGHT_GREEN, build = oval(20f, 190f, 125f, 250f)),
        Part(LIGHT_GREEN, build = oval(175f, 190f, 280f, 250f)),
        Part(PINK, build = oval(45f, 20f, 145f, 120f)),
        Part(PINK, build = oval(155f, 20f, 255f, 120f)),
        Part(PINK, build = oval(45f, 85f, 145f, 185f)),
        Part(PINK, build = oval(155f, 85f, 255f, 185f)),
        Part(YELLOW, build = oval(110f, 65f, 190f, 145f)),
    ))

    val FISH = Figure("rybka", Names.RYBKA, listOf(
        Part(ORANGE, build = oval(20f, 80f, 215f, 235f)),
        Part(RED, build = poly(200f, 158f, 285f, 90f, 285f, 225f)),
        Part(YELLOW, build = oval(75f, 55f, 165f, 110f)),
        Part(BLACK, filled = true, build = oval(55f, 125f, 85f, 155f)),
    ))

    val BUTTERFLY = Figure("babochka", Names.BABOCHKA, listOf(
        Part(BROWN, build = box(125f, 60f, 175f, 255f, 24f)),
        Part(PINK, build = oval(10f, 45f, 130f, 150f)),
        Part(PINK, build = oval(170f, 45f, 290f, 150f)),
        Part(ORANGE, build = oval(20f, 145f, 130f, 255f)),
        Part(ORANGE, build = oval(170f, 145f, 280f, 255f)),
        Part(BLACK, filled = true, build = poly(133f, 70f, 92f, 12f, 104f, 6f, 145f, 66f)),
        Part(BLACK, filled = true, build = poly(167f, 70f, 208f, 12f, 196f, 6f, 155f, 66f)),
    ))

    val TREE = Figure("yolka", Names.YOLKA, listOf(
        Part(BROWN, build = box(120f, 235f, 180f, 295f)),
        Part(GREEN, build = poly(45f, 240f, 150f, 130f, 255f, 240f)),
        Part(GREEN, build = poly(60f, 155f, 150f, 55f, 240f, 155f)),
        Part(YELLOW, build = {
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) 34f else 15f
                val a = -Math.PI / 2.0 + i * Math.PI / 5.0
                val x = 150f + Math.cos(a).toFloat() * r
                val y = 42f + Math.sin(a).toFloat() * r
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }),
    ))

    val CAKE = Figure("tort", Names.TORT, listOf(
        Part(WHITE, build = oval(15f, 250f, 285f, 295f)),
        Part(PINK, build = box(45f, 165f, 255f, 255f, 12f)),
        Part(YELLOW, build = box(70f, 95f, 230f, 165f, 12f)),
        Part(BLACK, filled = true, build = box(112f, 40f, 128f, 95f)),
        Part(BLACK, filled = true, build = box(172f, 40f, 188f, 95f)),
        Part(ORANGE, build = oval(95f, 5f, 145f, 45f)),
        Part(ORANGE, build = oval(155f, 5f, 205f, 45f)),
    ))

    // ───────────────────────────── звери ─────────────────────────────

    val DUCK = Figure("krya", Names.KRYA, listOf(
        Part(YELLOW, build = oval(30f, 130f, 230f, 270f)),
        Part(YELLOW, build = oval(150f, 40f, 270f, 160f)),
        Part(ORANGE, build = poly(255f, 85f, 300f, 105f, 255f, 130f)),
        Part(ORANGE, build = oval(70f, 165f, 180f, 240f)),
        Part(BLACK, filled = true, build = oval(195f, 75f, 225f, 105f)),
    ))

    val HEDGEHOG = Figure("yozhik", Names.YOZHIK, listOf(
        Part(BROWN, build = {
            moveTo(30f, 230f)
            var i = 0
            while (i < 7) {
                val x0 = 30f + i * 32f
                lineTo(x0 + 16f, 90f + (i % 2) * 24f)
                lineTo(x0 + 32f, 230f)
                i++
            }
            close()
        }),
        Part(LIGHT_BROWN, build = oval(150f, 160f, 290f, 270f)),
        Part(LIGHT_BROWN, build = oval(30f, 190f, 170f, 275f)),
        Part(BLACK, filled = true, build = oval(255f, 195f, 285f, 225f)),
        Part(BLACK, filled = true, build = oval(215f, 185f, 240f, 210f)),
    ))

    val FOX = Figure("lisa", Names.LISA, listOf(
        Part(ORANGE, build = oval(45f, 45f, 255f, 235f)),
        Part(ORANGE, build = poly(50f, 90f, 35f, 5f, 120f, 45f)),
        Part(ORANGE, build = poly(250f, 90f, 265f, 5f, 180f, 45f)),
        Part(WHITE, build = oval(95f, 150f, 205f, 250f)),
        Part(BLACK, filled = true, build = oval(100f, 110f, 130f, 140f)),
        Part(BLACK, filled = true, build = oval(170f, 110f, 200f, 140f)),
        Part(BLACK, filled = true, build = oval(133f, 180f, 167f, 210f)),
    ))

    val BEAR = Figure("mishka", Names.MISHKA, listOf(
        Part(BROWN, build = oval(40f, 60f, 260f, 270f)),
        Part(BROWN, build = oval(25f, 25f, 110f, 110f)),
        Part(BROWN, build = oval(190f, 25f, 275f, 110f)),
        Part(LIGHT_BROWN, build = oval(100f, 165f, 200f, 250f)),
        Part(BLACK, filled = true, build = oval(95f, 115f, 125f, 145f)),
        Part(BLACK, filled = true, build = oval(175f, 115f, 205f, 145f)),
        Part(BLACK, filled = true, build = oval(133f, 185f, 167f, 212f)),
    ))

    val WOLF = Figure("volk", Names.VOLK, listOf(
        Part(GREY, build = oval(45f, 55f, 255f, 240f)),
        Part(GREY, build = poly(60f, 80f, 55f, 0f, 130f, 45f)),
        Part(GREY, build = poly(240f, 80f, 245f, 0f, 170f, 45f)),
        Part(WHITE, build = oval(105f, 160f, 195f, 265f)),
        Part(BLACK, filled = true, build = oval(100f, 110f, 128f, 138f)),
        Part(BLACK, filled = true, build = oval(172f, 110f, 200f, 138f)),
        Part(BLACK, filled = true, build = oval(135f, 195f, 165f, 220f)),
    ))

    val LION = Figure("lev", Names.LEV, listOf(
        Part(ORANGE, build = {
            for (i in 0 until 12) {
                val a = i * Math.PI / 6.0
                val nx = 150f + Math.cos(a).toFloat() * 145f
                val ny = 150f + Math.sin(a).toFloat() * 145f
                val lx = 150f + Math.cos(a + 0.28).toFloat() * 100f
                val ly = 150f + Math.sin(a + 0.28).toFloat() * 100f
                val rx = 150f + Math.cos(a - 0.28).toFloat() * 100f
                val ry = 150f + Math.sin(a - 0.28).toFloat() * 100f
                moveTo(lx, ly)
                lineTo(nx, ny)
                lineTo(rx, ry)
                close()
            }
        }),
        Part(YELLOW, build = oval(60f, 60f, 240f, 240f)),
        Part(LIGHT_BROWN, build = oval(110f, 155f, 190f, 225f)),
        Part(BLACK, filled = true, build = oval(105f, 110f, 133f, 138f)),
        Part(BLACK, filled = true, build = oval(167f, 110f, 195f, 138f)),
        Part(BLACK, filled = true, build = oval(135f, 165f, 165f, 190f)),
    ))

    // ───────────────────────── фигурки-наклейки ─────────────────────────

    val STAR = Figure("zvezda", Names.ZVEZDA, listOf(
        Part(YELLOW, build = {
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) 145f else 62f
                val a = -Math.PI / 2.0 + i * Math.PI / 5.0
                val x = 150f + Math.cos(a).toFloat() * r
                val y = 150f + Math.sin(a).toFloat() * r
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }),
    ))

    val HEART = Figure("serdce", Names.SERDCE, listOf(
        Part(RED, build = {
            moveTo(150f, 275f)
            cubicTo(-30f, 150f, 55f, 15f, 150f, 105f)
            cubicTo(245f, 15f, 330f, 150f, 150f, 275f)
            close()
        }),
    ))

    val SUN_STICKER = Figure("solnyshko", Names.SOLNCE, SUN.parts)

    /** Картинки для раскрашивания: простые вещи и звери. */
    val TEMPLATES: List<Figure> = listOf(
        HOUSE, CAR, SUN, FLOWER, FISH, BUTTERFLY, TREE, CAKE,
        DUCK, HEDGEHOG, FOX, BEAR, WOLF, LION,
    )

    /** Фигурки, которые можно лепить на рисунок. */
    val STICKERS: List<Figure> = listOf(
        STAR, HEART, SUN_STICKER, DUCK, HEDGEHOG, FOX, BEAR, WOLF, LION,
    )

    fun byId(id: String): Figure? =
        (TEMPLATES + STICKERS).firstOrNull { it.id == id }
}
