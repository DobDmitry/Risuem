package com.dobdmitry.risuem.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * Загрузка фотографии из системного выбора или с камеры.
 *
 * Снимок с современного телефона — это десятки мегапикселей; для раскраски
 * столько не нужно и памяти жалко. Поэтому сразу уменьшаем до разумного.
 */
object Photos {

    fun loadScaled(context: Context, uri: Uri, maxSide: Int = 1200): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    } catch (e: Exception) {
        null
    }
}
