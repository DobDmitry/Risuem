package risuem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FloodFillTest {

    private val red = argb(255, 230, 70, 60)
    private val blue = argb(255, 60, 110, 230)

    /** Белый квадрат с чёрной рамкой внутри: классическая раскраска. */
    private fun boxed(): Raster {
        val r = Raster(40, 40, Raster.WHITE)
        // рамка-контур по кругу картинки
        r.rect(10, 10, 29, 10, Raster.BLACK)
        r.rect(10, 29, 29, 29, Raster.BLACK)
        r.rect(10, 10, 10, 29, Raster.BLACK)
        r.rect(29, 10, 29, 29, Raster.BLACK)
        return r
    }

    @Test
    fun `заливает всю область целиком`() {
        val r = boxed()
        val result = FloodFill.fill(r, 20, 20, red)
        assertEquals(18 * 18, result.filled)
        assertEquals(red, r[11, 11])
        assertEquals(red, r[28, 28])
    }

    @Test
    fun `не протекает через контур`() {
        val r = boxed()
        FloodFill.fill(r, 20, 20, red)
        assertEquals(Raster.WHITE, r[5, 5])
        assertEquals(Raster.WHITE, r[35, 20])
        assertEquals(Raster.BLACK, r[10, 20])
    }

    @Test
    fun `заливает фон вокруг фигуры, доходя до краёв картинки`() {
        val r = boxed()
        val result = FloodFill.fill(r, 0, 0, blue)
        assertEquals(blue, r[0, 0])
        assertEquals(blue, r[39, 39])
        assertEquals(blue, r[39, 0])
        assertEquals(Raster.WHITE, r[20, 20])
        assertEquals(40 * 40 - 20 * 20, result.filled)
    }

    @Test
    fun `сглаженная граница не пропускает краску наружу`() {
        val r = Raster(30, 30, Raster.WHITE)
        // Контур со сглаживанием: чёрное ядро и серые полутона по бокам.
        // Линия делит картинку насквозь — обойти её негде.
        for (y in 0..29) {
            r[14, y] = argb(255, 150, 150, 150)
            r[15, y] = Raster.BLACK
            r[16, y] = argb(255, 150, 150, 150)
        }
        FloodFill.fill(r, 5, 15, red)
        assertEquals(red, r[13, 15])
        assertEquals(Raster.WHITE, r[20, 15])
        assertTrue("серый полутон остаётся границей", luminance(r[14, 15]) < 200)
    }

    @Test
    fun `по контуру заливка не запускается`() {
        val r = boxed()
        val result = FloodFill.fill(r, 10, 20, red)
        assertTrue(result.isEmpty)
        assertEquals(Raster.BLACK, r[10, 20])
    }

    @Test
    fun `закрашенную область можно перекрасить`() {
        val r = boxed()
        FloodFill.fill(r, 20, 20, red)
        val again = FloodFill.fill(r, 20, 20, blue)
        assertEquals(18 * 18, again.filled)
        assertEquals(blue, r[20, 20])
        assertEquals(Raster.BLACK, r[10, 10])
    }

    @Test
    fun `повторная заливка тем же цветом ничего не делает`() {
        val r = boxed()
        FloodFill.fill(r, 20, 20, red)
        assertTrue(FloodFill.fill(r, 20, 20, red).isEmpty)
    }

    @Test
    fun `рамка результата совпадает с областью`() {
        val r = boxed()
        val result = FloodFill.fill(r, 20, 20, red)
        assertEquals(11, result.minX)
        assertEquals(11, result.minY)
        assertEquals(28, result.maxX)
        assertEquals(28, result.maxY)
    }

    @Test
    fun `касание за пределами картинки безопасно`() {
        val r = boxed()
        assertTrue(FloodFill.fill(r, -1, 5, red).isEmpty)
        assertTrue(FloodFill.fill(r, 100, 5, red).isEmpty)
    }

    @Test
    fun `область в один пиксель заливается`() {
        val r = Raster(5, 5, Raster.BLACK)
        r[2, 2] = Raster.WHITE
        val result = FloodFill.fill(r, 2, 2, red)
        assertEquals(1, result.filled)
        assertFalse(result.isEmpty)
    }

    @Test
    fun `узкий коридор проходится целиком`() {
        val r = Raster(60, 9, Raster.BLACK)
        r.rect(0, 4, 59, 4, Raster.WHITE)
        val result = FloodFill.fill(r, 0, 4, red)
        assertEquals(60, result.filled)
        assertEquals(red, r[59, 4])
    }

    @Test
    fun `большая картинка заливается без переполнения стека`() {
        val r = Raster(700, 700, Raster.WHITE)
        val result = FloodFill.fill(r, 350, 350, red)
        assertEquals(700 * 700, result.filled)
    }
}
