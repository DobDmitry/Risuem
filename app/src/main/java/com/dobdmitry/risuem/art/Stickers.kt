package com.dobdmitry.risuem.art

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.dobdmitry.risuem.R
import com.dobdmitry.risuem.data.HeroStore
import com.dobdmitry.risuem.ui.Names
import risuem.core.Sticker

/**
 * Наклейки: лица героев и простые фигурки.
 *
 * Каждая наклейка знает своё короткое имя — его произносит голос,
 * когда наклейка приземляется на лист.
 */
object Stickers {

    /** Сторона наклейки в точках холста при масштабе 1. */
    const val SIDE = 220f

    class Kind(val id: String, val name: String)

    fun kinds(heroes: List<HeroStore.Hero>): List<Kind> = buildList {
        add(Kind("face_kira", Names.KIRA))
        add(Kind("face_mama", Names.MAMA))
        add(Kind("face_papa", Names.PAPA))
        for (hero in heroes) add(Kind("hero_" + hero.id, hero.name))
        for (figure in Figures.STICKERS) add(Kind(figure.id, figure.name))
    }

    /** Картинка наклейки. Считается один раз и потом живёт в памяти. */
    fun bitmap(context: Context, kind: String, heroes: List<HeroStore.Hero>): Bitmap? = when {
        kind == "face_kira" -> decode(context, R.drawable.face_kira)
        kind == "face_mama" -> decode(context, R.drawable.face_mama)
        kind == "face_papa" -> decode(context, R.drawable.face_papa)
        kind.startsWith("hero_") -> heroes.firstOrNull { "hero_" + it.id == kind }
            ?.let { BitmapFactory.decodeFile(it.face.absolutePath) }
        else -> Figures.byId(kind)?.let { FigureRender.colored(it, 320) }
    }

    fun bitmaps(context: Context, heroes: List<HeroStore.Hero>): Map<String, Bitmap> =
        kinds(heroes).mapNotNull { kind -> bitmap(context, kind.id, heroes)?.let { kind.id to it } }.toMap()

    /**
     * Сплющить рисунок и наклейки в одну картинку — её и сохраняем.
     * Геометрия ровно та же, что на экране: центр, поворот, масштаб.
     */
    fun flatten(base: Bitmap, stickers: List<Sticker>, images: Map<String, Bitmap>): Bitmap {
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        for (sticker in stickers) {
            val image = images[sticker.kind] ?: continue
            val side = SIDE * sticker.scale
            val matrix = Matrix()
            matrix.postScale(side / image.width, side / image.height)
            matrix.postTranslate(-side / 2f, -side / 2f)
            matrix.postRotate(sticker.rotation)
            matrix.postTranslate(sticker.x, sticker.y)
            canvas.drawBitmap(image, matrix, paint)
        }
        return result
    }

    private fun decode(context: Context, resId: Int): Bitmap =
        BitmapFactory.decodeResource(context.resources, resId)
}
