package com.dobdmitry.risuem.art

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import com.dobdmitry.risuem.R
import com.dobdmitry.risuem.data.HeroStore
import com.dobdmitry.risuem.ui.Names
import risuem.core.ColoringPage
import risuem.core.Vectorize
import java.io.File

/**
 * Картинки для раскрашивания.
 *
 * Бывают трёх сортов: нарисованные фигуры, лица героев из фотографий
 * и герои, которых добавил взрослый. Все приводятся к одному виду —
 * чёрный контур на белом листе 900x1200 — и дальше живут одинаково.
 */
object Templates {

    const val PAGE_WIDTH = 900
    const val PAGE_HEIGHT = 1200

    /**
     * Минимальная закрашиваемая область на странице.
     *
     * Страница шириной 900 точек показывается примерно на 360 dp экрана,
     * то есть 60 dp — это около 150 точек. Всё, что мельче, отдаём рисунку:
     * пусть лучше деталь будет частью контура, чем ребёнок промахивается.
     */
    const val MIN_REGION = 12_000

    sealed class Source {
        data class Drawn(val figure: Figures.Figure) : Source()
        data class Face(val resId: Int) : Source()
        data class Added(val outline: File, val face: File) : Source()
    }

    class Template(val id: String, val name: String, val source: Source)

    /** Готовые картинки: сначала люди, потом звери, потом простые вещи. */
    fun builtIn(): List<Template> = buildList {
        add(Template("face_kira", Names.KIRA, Source.Face(R.drawable.face_kira)))
        add(Template("face_mama", Names.MAMA, Source.Face(R.drawable.face_mama)))
        add(Template("face_papa", Names.PAPA, Source.Face(R.drawable.face_papa)))
        for (figure in Figures.TEMPLATES) {
            add(Template(figure.id, figure.name, Source.Drawn(figure)))
        }
    }

    /** Готовые картинки плюс герои, которых добавил взрослый. */
    fun all(heroes: List<HeroStore.Hero>): List<Template> =
        heroes.map { Template("hero_" + it.id, it.name, Source.Added(it.outline, it.face)) } + builtIn()

    /** Контур страницы. Тяжёлая работа — фото считается не в главном потоке. */
    fun outlineBitmap(context: Context, template: Template): Bitmap = when (val source = template.source) {
        is Source.Drawn -> FigureRender.outline(source.figure, PAGE_WIDTH, PAGE_HEIGHT)
        is Source.Face -> photoToOutline(decodeResource(context, source.resId))
        is Source.Added -> BitmapFactory.decodeFile(source.outline.absolutePath)
            ?: FigureRender.outline(Figures.HOUSE, PAGE_WIDTH, PAGE_HEIGHT)
    }


    /** Маленькая цветная картинка для сетки выбора: её видно с полуметра. */
    fun thumbnail(context: Context, template: Template): Bitmap = when (val source = template.source) {
        is Source.Drawn -> FigureRender.colored(source.figure, 320)
        is Source.Face -> decodeResource(context, source.resId)
        is Source.Added -> BitmapFactory.decodeFile(source.face.absolutePath)
            ?: FigureRender.colored(Figures.HOUSE, 320)
    }

    /** Разметка областей: по ней работает заливка одним касанием. */
    fun page(outline: Bitmap): ColoringPage {
        val raster = Vectorize.binarize(outline.toRaster())
        return ColoringPage.fromOutline(raster, minArea = MIN_REGION)
    }

    /**
     * Фотография → раскраска.
     *
     * Считаем на уменьшенной копии: так вдесятеро быстрее и мелкие детали
     * сами собой пропадают. Потом увеличиваем контур обратно без сглаживания,
     * чтобы линия осталась чёрной, а лист — белым.
     */
    fun photoToOutline(photo: Bitmap): Bitmap {
        val small = fitOnWhite(photo, PAGE_WIDTH / 2, PAGE_HEIGHT / 2)
        val outline = Vectorize.toOutline(
            small.toRaster(),
            Vectorize.Options(
                levels = 3,
                blurRadius = 2,
                minArea = MIN_REGION / 4,
                outlineWidth = 3,
            ),
        ).toBitmap()
        val big = Bitmap.createScaledBitmap(outline, PAGE_WIDTH, PAGE_HEIGHT, false)
        return Vectorize.binarize(big.toRaster()).toBitmap()
    }

    /** Вписать фотографию в белый лист целиком, ничего не обрезая. */
    fun fitOnWhite(source: Bitmap, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val margin = 0.06f
        val scale = minOf(
            width * (1 - margin * 2) / source.width,
            height * (1 - margin * 2) / source.height,
        )
        val w = (source.width * scale).toInt().coerceAtLeast(1)
        val h = (source.height * scale).toInt().coerceAtLeast(1)
        val left = (width - w) / 2
        val top = (height - h) / 2
        canvas.drawBitmap(source, null, Rect(left, top, left + w, top + h), null)
        return bitmap
    }

    private fun decodeResource(context: Context, resId: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        return BitmapFactory.decodeResource(context.resources, resId, options)
    }
}
