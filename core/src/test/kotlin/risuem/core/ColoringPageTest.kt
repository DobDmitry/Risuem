package risuem.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColoringPageTest {

    private val red = argb(255, 230, 70, 60)
    private val blue = argb(255, 60, 110, 230)

    /** Картинка «домик»: две крупные области, разделённые чёрной линией. */
    private fun twoRooms(): Raster {
        val r = Raster(40, 20, Raster.WHITE)
        r.rect(20, 0, 20, 19, Raster.BLACK)
        return r
    }

    @Test
    fun `области находятся по контуру`() {
        val page = ColoringPage.fromOutline(twoRooms())
        assertEquals(2, page.regionCount)
        assertEquals(20 * 20, page.regions[0].area)
        assertEquals(19 * 20, page.regions[1].area)
    }

    @Test
    fun `касание попадает в нужную область`() {
        val page = ColoringPage.fromOutline(twoRooms())
        assertEquals(0, page.regionAt(5, 5))
        assertEquals(1, page.regionAt(30, 5))
        assertEquals(ColoringPage.NO_REGION, page.regionAt(20, 5))
        assertEquals(ColoringPage.NO_REGION, page.regionAt(100, 5))
    }

    @Test
    fun `заливка красит область и видна в готовой картинке`() {
        val page = ColoringPage.fromOutline(twoRooms())
        assertTrue(page.fill(page.regionAt(5, 5), red))
        val image = page.render()
        assertEquals(red, image[5 * 40 + 5])
        assertEquals(Raster.WHITE, image[5 * 40 + 30])
        assertEquals(Raster.BLACK, image[5 * 40 + 20])
    }

    @Test
    fun `тот же цвет второй раз не считается шагом`() {
        val page = ColoringPage.fromOutline(twoRooms())
        assertTrue(page.fill(0, red))
        assertFalse(page.fill(0, red))
        assertTrue(page.fill(0, blue))
    }

    @Test
    fun `снимок и откат возвращают точное состояние`() {
        val page = ColoringPage.fromOutline(twoRooms())
        page.fill(0, red)
        val snapshot = page.snapshot()
        page.fill(1, blue)
        assertEquals(2, page.filledCount)
        page.restore(snapshot)
        assertEquals(1, page.filledCount)
        assertEquals(red, page.colorOf(0))
        assertEquals(0, page.colorOf(1))
        assertArrayEquals(snapshot, page.snapshot())
    }

    @Test
    fun `готово, когда закрашены все области`() {
        val page = ColoringPage.fromOutline(twoRooms())
        assertFalse(page.isComplete())
        page.fill(0, red)
        assertFalse(page.isComplete())
        page.fill(1, blue)
        assertTrue(page.isComplete())
    }

    @Test
    fun `очистка снимает все цвета`() {
        val page = ColoringPage.fromOutline(twoRooms())
        page.fill(0, red)
        page.fill(1, blue)
        page.clear()
        assertEquals(0, page.filledCount)
        assertFalse(page.isComplete())
    }

    @Test
    fun `мелкие детали не становятся областями`() {
        val r = Raster(40, 20, Raster.WHITE)
        r.rect(20, 0, 20, 19, Raster.BLACK)
        // крошечная белая точка внутри чёрной кляксы — в неё пальцем не попасть
        r.rect(2, 2, 6, 6, Raster.BLACK)
        r[4, 4] = Raster.WHITE
        val page = ColoringPage.fromOutline(r, minArea = 100)
        assertEquals(2, page.regionCount)
        assertEquals(ColoringPage.NO_REGION, page.regionAt(4, 4))
    }

    @Test
    fun `отмена через стек возвращает раскраску шаг назад`() {
        val page = ColoringPage.fromOutline(twoRooms())
        val undo = UndoStack<IntArray>()
        undo.push(page.snapshot())
        page.fill(0, red)
        undo.push(page.snapshot())
        page.fill(1, blue)
        page.restore(undo.undo()!!)
        assertEquals(red, page.colorOf(0))
        assertEquals(0, page.colorOf(1))
    }
}
