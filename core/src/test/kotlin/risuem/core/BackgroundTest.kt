package risuem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundTest {

    private val skin = argb(255, 240, 190, 150)

    @Test
    fun `однотонный фон становится прозрачным`() {
        val r = Raster(20, 20, argb(255, 250, 250, 250))
        r.rect(6, 6, 13, 13, skin)
        val out = Background.removeUniform(r)
        assertEquals(0, alphaOf(out[0, 0]))
        assertEquals(0, alphaOf(out[19, 19]))
        assertEquals(skin, out[10, 10])
    }

    @Test
    fun `герой в середине остаётся целым, даже если он цвета фона`() {
        val wall = argb(255, 250, 250, 250)
        val r = Raster(20, 20, wall)
        r.rect(6, 6, 13, 13, wall) // ровно цвет фона, но отделён рамкой
        r.rect(5, 5, 14, 5, Raster.BLACK)
        r.rect(5, 14, 14, 14, Raster.BLACK)
        r.rect(5, 5, 5, 14, Raster.BLACK)
        r.rect(14, 5, 14, 14, Raster.BLACK)
        val out = Background.removeUniform(r)
        assertEquals(0, alphaOf(out[0, 0]))
        assertEquals(255, alphaOf(out[10, 10]))
    }

    @Test
    fun `лёгкий градиент фона тоже снимается`() {
        val r = Raster(20, 20)
        for (y in 0 until 20) {
            for (x in 0 until 20) {
                r[x, y] = argb(255, 240 + y / 8, 240 + y / 8, 240 + y / 8)
            }
        }
        r.rect(8, 8, 11, 11, skin)
        val out = Background.removeUniform(r)
        assertEquals(0, alphaOf(out[1, 18]))
        assertEquals(skin, out[9, 9])
    }

    @Test
    fun `медианный цвет края не сбивается одной соринкой`() {
        val r = Raster(20, 20, argb(255, 250, 250, 250))
        r[0, 0] = Raster.BLACK
        val edge = Background.medianEdgeColor(r)
        assertTrue(luminance(edge) > 200)
    }
}
