package com.dobdmitry.risuem.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import risuem.core.Document
import risuem.core.Stroke
import risuem.core.StrokeMath
import risuem.core.StrokePoint
import risuem.core.Tool

/**
 * Холст рисования.
 *
 * Главное правило скорости: штрихи копятся в растре. Палец двигается —
 * мы дорисовываем в тот же Bitmap только новый кусочек линии, а не
 * перерисовываем всё заново. Поэтому и после сотни штрихов рисование
 * остаётся мгновенным.
 *
 * Полная перерисовка бывает ровно в двух случаях: отмена шага и открытие
 * сохранённой работы — там она стоит один кадр и никому не мешает.
 */
class CanvasEngine(val width: Int, val height: Int) {

    val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        eraseColor(Color.WHITE)
    }

    private val canvas = Canvas(bitmap)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /** Счётчик кадров: по нему Compose понимает, что растр изменился. */
    var version by mutableIntStateOf(0)
        private set

    private var tool = Tool.BRUSH
    private var color = Color.BLACK
    private var baseWidth = 24f
    private var currentWidth = 24f
    private var lastX = 0f
    private var lastY = 0f
    private var lastTime = 0L
    private val points = ArrayList<StrokePoint>()

    val isDrawing: Boolean get() = points.isNotEmpty()

    fun beginStroke(tool: Tool, color: Int, baseWidth: Float, x: Float, y: Float, time: Long) {
        this.tool = tool
        this.color = color
        this.baseWidth = baseWidth
        currentWidth = baseWidth
        lastX = x
        lastY = y
        lastTime = time
        points.clear()
        points.add(StrokePoint(x, y, currentWidth))
        paint.color = if (tool == Tool.ERASER) Color.WHITE else color
        paint.strokeWidth = currentWidth
        // Точка от одиночного касания: ткнул — и кружок уже есть.
        canvas.drawPoint(x, y, Paint(paint).apply { strokeCap = Paint.Cap.ROUND })
        version++
    }

    /**
     * Продолжение штриха. Линия идёт мягкой дугой через середины отрезков —
     * на быстром движении не остаётся ни углов, ни разрывов.
     */
    fun extendStroke(x: Float, y: Float, time: Long) {
        if (points.isEmpty()) return
        val speed = StrokeMath.speed(
            StrokeMath.Point(lastX, lastY),
            StrokeMath.Point(x, y),
            time - lastTime,
        )
        val target = StrokeMath.widthFor(speed, baseWidth)
        currentWidth = StrokeMath.ease(currentWidth, target)

        val midX = (lastX + x) / 2f
        val midY = (lastY + y) / 2f
        val segment = Path().apply {
            val previous = points.last()
            moveTo((previous.x + lastX) / 2f, (previous.y + lastY) / 2f)
            quadTo(lastX, lastY, midX, midY)
        }
        paint.strokeWidth = currentWidth
        canvas.drawPath(segment, paint)

        points.add(StrokePoint(x, y, currentWidth))
        lastX = x
        lastY = y
        lastTime = time
        version++
    }

    /** Закончить штрих и отдать его документу — для отмены и сохранения. */
    fun endStroke(): Stroke? {
        if (points.isEmpty()) return null
        val stroke = Stroke(tool, if (tool == Tool.ERASER) Color.WHITE else color, points.toList())
        points.clear()
        return stroke
    }

    fun clear() {
        bitmap.eraseColor(Color.WHITE)
        points.clear()
        version++
    }

    /** Перерисовать всё заново — после отмены шага или при открытии работы. */
    fun redraw(document: Document) {
        bitmap.eraseColor(Color.WHITE)
        for (stroke in document.strokes) drawWhole(stroke)
        version++
    }

    fun loadFrom(source: Bitmap) {
        bitmap.eraseColor(Color.WHITE)
        val scaled = if (source.width == width && source.height == height) {
            source
        } else {
            Bitmap.createScaledBitmap(source, width, height, true)
        }
        canvas.drawBitmap(scaled, 0f, 0f, null)
        version++
    }

    /** Копия растра — её сохраняют в работы и отдают наружу. */
    fun snapshot(): Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

    private fun drawWhole(stroke: Stroke) {
        if (stroke.points.isEmpty()) return
        paint.color = if (stroke.tool == Tool.ERASER) Color.WHITE else stroke.color
        if (stroke.points.size == 1) {
            val only = stroke.points.first()
            paint.strokeWidth = only.width
            canvas.drawPoint(only.x, only.y, paint)
            return
        }
        for (i in 1 until stroke.points.size) {
            val from = stroke.points[i - 1]
            val to = stroke.points[i]
            paint.strokeWidth = (from.width + to.width) / 2f
            val segment = Path().apply {
                val prev = stroke.points[if (i >= 2) i - 2 else i - 1]
                moveTo((prev.x + from.x) / 2f, (prev.y + from.y) / 2f)
                quadTo(from.x, from.y, (from.x + to.x) / 2f, (from.y + to.y) / 2f)
            }
            canvas.drawPath(segment, paint)
        }
    }
}
