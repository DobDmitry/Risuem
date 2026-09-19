package com.dobdmitry.risuem.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Мои работы: картинки лежат в личной папке приложения.
 *
 * Личная папка — значит, что их не видит никто снаружи, они переживают
 * перезапуск телефона и исчезают только вместе с приложением. Рядом с каждой
 * картинкой лежит текстовая запись, из которой работу можно открыть и
 * продолжить: штрихи и наклейки для рисунка, цвета областей для раскраски.
 */
class WorkStore(private val context: Context) {

    class Work(val id: String, val image: File, val note: File, val time: Long)

    private val dir: File
        get() = File(context.filesDir, "works").apply { mkdirs() }

    fun list(): List<Work> {
        val files = dir.listFiles { f: File -> f.name.endsWith(".png") } ?: return emptyList()
        return files
            .map { Work(it.nameWithoutExtension, it, noteFile(it.nameWithoutExtension), it.lastModified()) }
            .sortedByDescending { it.time }
    }

    fun newId(): String = "w" + System.currentTimeMillis()

    /** Сохранить работу. Если [id] уже есть — перезаписываем: ребёнок продолжил ту же. */
    fun save(id: String, bitmap: Bitmap, note: String): Work {
        val image = File(dir, "$id.png")
        image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        noteFile(id).writeText(note)
        return Work(id, image, noteFile(id), image.lastModified())
    }

    fun loadImage(id: String): Bitmap? {
        val file = File(dir, "$id.png")
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun loadNote(id: String): String? = noteFile(id).takeIf { it.exists() }?.readText()

    fun delete(id: String) {
        File(dir, "$id.png").delete()
        noteFile(id).delete()
    }

    private fun noteFile(id: String) = File(dir, "$id.txt")
}
