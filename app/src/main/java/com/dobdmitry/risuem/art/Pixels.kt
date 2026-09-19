package com.dobdmitry.risuem.art

import android.graphics.Bitmap
import risuem.core.Raster

/** Мостик между картинкой Android и чистой математикой модуля core. */

fun Bitmap.toRaster(): Raster {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    return Raster(width, height, pixels)
}

fun Raster.toBitmap(): Bitmap = pixelsToBitmap(pixels, width, height)

fun pixelsToBitmap(pixels: IntArray, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}
