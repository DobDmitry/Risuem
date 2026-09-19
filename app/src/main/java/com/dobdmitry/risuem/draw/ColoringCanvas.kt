package com.dobdmitry.risuem.draw

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.dobdmitry.risuem.art.pixelsToBitmap
import risuem.core.ColoringPage

/**
 * Раскраска на экране.
 *
 * Держим два растра: «до» и «после» последней заливки. Краска растекается
 * по кругу от места касания — экран просто показывает «после» внутри
 * растущего круга, поверх «до». Так анимация ничего не считает по пикселям
 * и идёт ровно, сколько бы областей ни было закрашено.
 */
class ColoringCanvas(val page: ColoringPage) {

    private val pixels: IntArray = page.render()

    val after: Bitmap = pixelsToBitmap(pixels, page.width, page.height)
    val before: Bitmap = pixelsToBitmap(pixels, page.width, page.height)

    var version by mutableIntStateOf(0)
        private set

    /** Куда ткнул палец в прошлый раз — из этой точки растекается краска. */
    var splashX: Float = 0f
        private set
    var splashY: Float = 0f
        private set

    private var pendingRegion: Int = -1
    private var pendingColor: Int = 0

    /** Закрасить область. Возвращает false, если там уже этот цвет или это контур. */
    fun fill(region: Int, color: Int, atX: Float, atY: Float): Boolean {
        if (!page.fill(region, color)) return false
        settle()
        paint(after, region, color)
        pendingRegion = region
        pendingColor = color
        splashX = atX
        splashY = atY
        version++
        return true
    }

    /** Закончить анимацию: «до» догоняет «после». */
    fun settle() {
        val region = pendingRegion
        if (region < 0) return
        paint(before, region, pendingColor)
        pendingRegion = -1
    }

    /** Полная перерисовка — после отмены шага или очистки. */
    fun refresh() {
        settle()
        val fresh = page.render()
        fresh.copyInto(pixels)
        after.setPixels(pixels, 0, page.width, 0, 0, page.width, page.height)
        before.setPixels(pixels, 0, page.width, 0, 0, page.width, page.height)
        version++
    }

    fun snapshot(): Bitmap = after.copy(Bitmap.Config.ARGB_8888, false)

    /** Красим только пиксели области и только её кусок растра — это быстро. */
    private fun paint(target: Bitmap, regionId: Int, color: Int) {
        val region = page.regions.getOrNull(regionId) ?: return
        for (p in region.pixels) pixels[p] = color
        val width = region.maxX - region.minX + 1
        val height = region.maxY - region.minY + 1
        target.setPixels(
            pixels,
            region.minY * page.width + region.minX,
            page.width,
            region.minX,
            region.minY,
            width,
            height,
        )
    }
}
