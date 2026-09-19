package com.dobdmitry.risuem.art

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path

/**
 * Рисование фигуры: один и тот же набор частей превращается
 * либо в чёрный контур для раскраски, либо в цветную наклейку.
 */
object FigureRender {

    private fun paintFill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }

    private fun paintStroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        this.color = color
    }

    /** Матрица «вписать квадрат 300x300 в прямоугольник с полями». */
    private fun fit(width: Int, height: Int, margin: Float): Matrix {
        val scale = minOf(width, height) * (1f - margin * 2f) / Figures.SIZE
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(
            (width - Figures.SIZE * scale) / 2f,
            (height - Figures.SIZE * scale) / 2f,
        )
        return matrix
    }

    /** Чёрный контур на белом — готовая раскраска. */
    fun outline(figure: Figures.Figure, width: Int, height: Int, lineWidth: Float = 12f): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val matrix = fit(width, height, 0.08f)
        val stroke = paintStroke(Color.BLACK, lineWidth)
        val fill = paintFill(Color.BLACK)
        for (part in figure.parts) {
            val path = Path().apply(part.build)
            path.transform(matrix)
            if (part.filled) canvas.drawPath(path, fill) else canvas.drawPath(path, stroke)
        }
        return bitmap
    }

    /** Цветная фигурка на прозрачном фоне — наклейка. */
    fun colored(figure: Figures.Figure, side: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val matrix = fit(side, side, 0.04f)
        val stroke = paintStroke(0xFF3B3027.toInt(), side * 0.022f)
        for (part in figure.parts) {
            val path = Path().apply(part.build)
            path.transform(matrix)
            canvas.drawPath(path, paintFill(part.color))
            if (!part.filled) canvas.drawPath(path, stroke)
        }
        return bitmap
    }
}
