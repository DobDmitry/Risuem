package risuem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import risuem.core.StrokeMath.Point

class StrokeMathTest {

    @Test
    fun `сглаженная линия проходит через исходные точки`() {
        val points = listOf(Point(0f, 0f), Point(10f, 10f), Point(20f, 0f), Point(30f, 10f))
        val smooth = StrokeMath.smooth(points, 4)
        assertTrue(smooth.size > points.size)
        assertTrue(smooth.contains(points.first()))
        for (p in points) {
            assertTrue("точка $p потерялась", smooth.any {
                kotlin.math.abs(it.x - p.x) < 0.01f && kotlin.math.abs(it.y - p.y) < 0.01f
            })
        }
    }

    @Test
    fun `на быстром движении нет разрывов`() {
        // две точки далеко друг от друга — сглаживание заполняет промежуток
        val points = listOf(Point(0f, 0f), Point(5f, 0f), Point(200f, 0f), Point(205f, 0f))
        val smooth = StrokeMath.smooth(points, 16)
        var maxGap = 0f
        for (i in 0 until smooth.size - 1) {
            maxGap = maxOf(maxGap, StrokeMath.distance(smooth[i], smooth[i + 1]))
        }
        assertTrue("разрыв $maxGap слишком большой", maxGap < 20f)
    }

    @Test
    fun `двух точек хватает, ничего не ломается`() {
        val points = listOf(Point(1f, 1f), Point(2f, 2f))
        assertEquals(points, StrokeMath.smooth(points))
        assertEquals(emptyList<Point>(), StrokeMath.smooth(emptyList()))
    }

    @Test
    fun `медленное движение даёт линию жирнее быстрого`() {
        val slow = StrokeMath.widthFor(0.05f, 20f)
        val fast = StrokeMath.widthFor(3f, 20f)
        assertTrue(slow > fast)
        assertTrue(slow <= 20f * StrokeMath.THICK_FACTOR + 0.01f)
        assertTrue(fast >= 20f * StrokeMath.THIN_FACTOR - 0.01f)
    }

    @Test
    fun `толщина не улетает на запредельной скорости`() {
        val width = StrokeMath.widthFor(1000f, 10f)
        assertEquals(10f * StrokeMath.THIN_FACTOR, width, 0.001f)
        assertTrue(width > 0f)
    }

    @Test
    fun `толщина меняется плавно`() {
        var w = 10f
        repeat(10) { w = StrokeMath.ease(w, 20f) }
        assertTrue(w > 15f)
        assertTrue(w < 20f)
        assertEquals(20f, StrokeMath.ease(10f, 20f, 1f), 0.001f)
    }

    @Test
    fun `скорость считается и при нулевом времени`() {
        val s = StrokeMath.speed(Point(0f, 0f), Point(3f, 4f), 0)
        assertEquals(5f, s, 0.001f)
        assertEquals(1f, StrokeMath.speed(Point(0f, 0f), Point(0f, 10f), 10), 0.001f)
    }

    @Test
    fun `лишние точки выбрасываются, концы остаются`() {
        val points = listOf(Point(0f, 0f), Point(0.2f, 0f), Point(0.4f, 0f), Point(10f, 0f))
        val thinned = StrokeMath.thin(points, 1f)
        assertEquals(Point(0f, 0f), thinned.first())
        assertEquals(Point(10f, 0f), thinned.last())
        assertTrue(thinned.size < points.size)
    }
}
