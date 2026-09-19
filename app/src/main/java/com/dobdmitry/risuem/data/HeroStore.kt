package com.dobdmitry.risuem.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Герои, которых добавил взрослый.
 *
 * У каждого две картинки: вырезанное лицо (им становятся наклейки)
 * и контур (им становится раскраска). Плюс короткое имя заглавными.
 */
class HeroStore(private val context: Context) {

    class Hero(val id: String, val name: String, val face: File, val outline: File)

    private val dir: File
        get() = File(context.filesDir, "heroes").apply { mkdirs() }

    fun list(): List<Hero> {
        val names = dir.listFiles { f: File -> f.name.endsWith(".txt") } ?: return emptyList()
        return names.mapNotNull { note ->
            val id = note.nameWithoutExtension
            val face = File(dir, "$id.png")
            val outline = File(dir, "${id}_outline.png")
            if (face.exists() && outline.exists()) {
                Hero(id, note.readText().trim(), face, outline)
            } else {
                null
            }
        }.sortedBy { it.name }
    }

    fun add(name: String, face: Bitmap, outline: Bitmap): Hero {
        val id = "h" + System.currentTimeMillis()
        val faceFile = File(dir, "$id.png")
        val outlineFile = File(dir, "${id}_outline.png")
        faceFile.outputStream().use { face.compress(Bitmap.CompressFormat.PNG, 100, it) }
        outlineFile.outputStream().use { outline.compress(Bitmap.CompressFormat.PNG, 100, it) }
        File(dir, "$id.txt").writeText(name)
        return Hero(id, name, faceFile, outlineFile)
    }

    fun delete(id: String) {
        File(dir, "$id.png").delete()
        File(dir, "${id}_outline.png").delete()
        File(dir, "$id.txt").delete()
    }

    fun loadFace(hero: Hero): Bitmap? = BitmapFactory.decodeFile(hero.face.absolutePath)

    fun loadOutline(hero: Hero): Bitmap? = BitmapFactory.decodeFile(hero.outline.absolutePath)
}
