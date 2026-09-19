package risuem.core

import kotlin.math.abs
import kotlin.math.hypot

/**
 * Математика линии.
 *
 * Палец даёт редкие точки, особенно на быстром движении — если соединить их
 * отрезками, получится ломаная «проволока». Поэтому:
 *  - точки сглаживаются кривой Катмулла-Рома (линия идёт через все точки, без углов);
 *  - толщина зависит от скорости: медленно — жирно, быстро — тоньше,
 *    и меняется постепенно, так что выходит фломастер, а не проволока.
 */
object StrokeMath {

    data class Point(val x: Float, val y: Float)

    fun distance(a: Point, b: Point): Float = hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()

    /**
     * Сглаживание: между каждой парой соседних точек добавляется [segments] точек
     * по кривой, проходящей через них. Концы линии остаются на месте.
     */
    fun smooth(points: List<Point>, segments: Int = 8): List<Point> {
        if (points.size < 3 || segments < 1) return points
        val out = ArrayList<Point>((points.size - 1) * segments + 1)
        out.add(points.first())
        for (i in 0 until points.size - 1) {
            val p0 = points[if (i == 0) 0 else i - 1]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = points[if (i + 2 > points.size - 1) points.size - 1 else i + 2]
            for (s in 1..segments) {
                out.add(catmullRom(p0, p1, p2, p3, s.toFloat() / segments))
            }
        }
        return out
    }

    fun catmullRom(p0: Point, p1: Point, p2: Point, p3: Point, t: Float): Point {
        val t2 = t * t
        val t3 = t2 * t
        val x = 0.5f * ((2 * p1.x) + (-p0.x + p2.x) * t +
            (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t2 +
            (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t3)
        val y = 0.5f * ((2 * p1.y) + (-p0.y + p2.y) * t +
            (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t2 +
            (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t3)
        return Point(x, y)
    }

    /** Точки не ближе [step] друг к другу: меньше мусора, ровнее линия. */
    fun thin(points: List<Point>, step: Float): List<Point> {
        if (points.isEmpty()) return points
        val out = ArrayList<Point>()
        out.add(points.first())
        for (p in points.drop(1)) {
            if (distance(out.last(), p) >= step) out.add(p)
        }
        if (out.size == 1 || out.last() != points.last()) out.add(points.last())
        return out
    }

    const val FAST_SPEED = 3.0f      // пикселей на миллисекунду — быстрее уже некуда
    const val THIN_FACTOR = 0.55f    // во сколько раз тоньше на полной скорости
    const val THICK_FACTOR = 1.15f   // и насколько жирнее на медленном движении

    /**
     * Толщина для текущей скорости ([speed] — пиксели в миллисекунду).
     * Медленно — линия шире базовой, быстро — уже.
     */
    fun widthFor(speed: Float, base: Float): Float {
        val k = (abs(speed) / FAST_SPEED).coerceIn(0f, 1f)
        val factor = THICK_FACTOR + (THIN_FACTOR - THICK_FACTOR) * k
        return base * factor
    }

    /** Толщина догоняет цель плавно: без скачков на дрожащем пальце. */
    fun ease(current: Float, target: Float, factor: Float = 0.35f): Float =
        current + (target - current) * factor.coerceIn(0f, 1f)

    /** Скорость между двумя точками. Нулевое время не роняет расчёт. */
    fun speed(from: Point, to: Point, millis: Long): Float {
        val dt = if (millis <= 0L) 1L else millis
        return distance(from, to) / dt
    }
}
