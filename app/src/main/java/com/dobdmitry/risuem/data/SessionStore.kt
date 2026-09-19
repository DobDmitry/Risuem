package com.dobdmitry.risuem.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Незаконченная работа.
 *
 * Ребёнок сворачивает приложение как угодно и когда угодно. Поэтому при уходе
 * с экрана мы сразу кладём растр рисунка и запись документа на диск, а при
 * возвращении поднимаем обратно — рисунок не пропадает никогда.
 */
class SessionStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "session").apply { mkdirs() }

    private val drawImage get() = File(dir, "draw.png")
    private val drawNote get() = File(dir, "draw.txt")
    private val colorNote get() = File(dir, "color.txt")

    fun saveDrawing(bitmap: Bitmap?, note: String) {
        if (bitmap != null) {
            drawImage.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        drawNote.writeText(note)
    }

    fun loadDrawingImage(): Bitmap? =
        if (drawImage.exists()) BitmapFactory.decodeFile(drawImage.absolutePath) else null

    fun loadDrawingNote(): String? = drawNote.takeIf { it.exists() }?.readText()

    fun clearDrawing() {
        drawImage.delete()
        drawNote.delete()
    }

    fun saveColoring(note: String) = colorNote.writeText(note)

    fun loadColoring(): String? = colorNote.takeIf { it.exists() }?.readText()

    fun clearColoring() {
        colorNote.delete()
    }
}
