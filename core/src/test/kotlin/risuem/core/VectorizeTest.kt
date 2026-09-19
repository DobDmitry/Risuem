package risuem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VectorizeTest {

    private fun gray(v: Int) = argb(255, v, v, v)

    /** Фото-заменитель: три широкие полосы разной яркости и одна крошечная точка. */
    private fun stripes(): Raster {
        val r = Raster(120, 120, gray(240))
        r.rect(0, 0, 39, 119, gray(30))
        r.rect(40, 0, 79, 119, gray(130))
        r.rect(80, 0, 119, 119, gray(230))
        r.rect(58, 58, 61, 61, gray(30)) // мелочь: 4x4 пикселя
        return r
    }

    @Test
    fun `на тестовой картинке получается ожидаемое число областей`() {
        val outline = Vectorize.toOutline(stripes(), Vectorize.Options(minArea = 800, blurRadius = 1))
        val page = ColoringPage.fromOutline(outline, minArea = 200)
        assertEquals(3, page.regionCount)
    }

    @Test
    fun `мелкие области схлопываются`() {
        val labeling = Vectorize.label(
            Vectorize.posterize(Vectorize.toGray(stripes()), 3),
            120,
            120,
        )
        assertTrue("до укрупнения мелочь видна отдельной областью", labeling.count >= 4)
        val merged = Vectorize.mergeSmall(labeling, 800)
        assertEquals(3, merged.count)
        for (area in merged.areas()) assertTrue("осталась мелкая область: $area", area >= 800)
    }

    @Test
    fun `каждая область крупнее пальца`() {
        val page = Vectorize.toColoring(stripes(), Vectorize.Options(minArea = 2000, blurRadius = 1))
        assertTrue(page.regionCount in 1..4)
        for (region in page.regions) {
            assertTrue("узкая область ${region.boxWidth}x${region.boxHeight}", region.boxWidth >= 20)
            assertTrue("низкая область ${region.boxWidth}x${region.boxHeight}", region.boxHeight >= 20)
        }
    }

    @Test
    fun `постеризация раскладывает яркость по ступеням`() {
        val levels = Vectorize.posterize(intArrayOf(0, 60, 120, 200, 255), 3)
        assertEquals(0, levels[0])
        assertEquals(2, levels[4])
        assertTrue(levels[2] in 1..2)
    }

    @Test
    fun `размытие сглаживает одинокую точку`() {
        val gray = IntArray(25) { 255 }
        gray[12] = 0
        val blurred = Vectorize.blur(gray, 5, 5, 1)
        assertTrue("точка размылась", blurred[12] > 100)
        assertTrue("соседи потемнели", blurred[11] < 255)
    }

    @Test
    fun `разметка находит связные куски`() {
        val values = intArrayOf(
            0, 0, 1, 1,
            0, 0, 1, 1,
            2, 2, 2, 2,
        )
        val labeling = Vectorize.label(values, 4, 3)
        assertEquals(3, labeling.count)
        assertEquals(labeling.labels[0], labeling.labels[5])
        assertTrue(labeling.labels[0] != labeling.labels[2])
    }

    @Test
    fun `контур рисуется по границам областей`() {
        val labeling = Vectorize.Labeling(
            4, 2,
            intArrayOf(
                0, 0, 1, 1,
                0, 0, 1, 1,
            ),
            2,
        )
        val raster = Vectorize.drawBorders(labeling, 1)
        assertEquals(Raster.BLACK, raster[1, 0])
        assertEquals(Raster.WHITE, raster[0, 0])
    }

    @Test
    fun `бинаризация убирает серую кайму сглаживания`() {
        val r = Raster(3, 1)
        r[0, 0] = Raster.BLACK
        r[1, 0] = argb(255, 150, 150, 150)
        r[2, 0] = Raster.WHITE
        val out = Vectorize.binarize(r)
        assertEquals(Raster.BLACK, out[0, 0])
        assertEquals(Raster.WHITE, out[1, 0])
        assertEquals(Raster.WHITE, out[2, 0])
        val dark = Vectorize.binarize(r, threshold = 200)
        assertEquals(Raster.BLACK, dark[1, 0])
    }

    @Test
    fun `однотонная картинка не разваливается`() {
        val flat = Raster(30, 30, gray(200))
        val page = Vectorize.toColoring(flat, Vectorize.Options(minArea = 100))
        assertEquals(1, page.regionCount)
    }
}
