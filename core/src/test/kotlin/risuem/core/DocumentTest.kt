package risuem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentTest {

    private fun sample(): Document = Document(
        width = 1080,
        height = 1600,
        strokes = listOf(
            Stroke(Tool.BRUSH, argb(255, 230, 70, 60), listOf(
                StrokePoint(10f, 20f, 12f),
                StrokePoint(11.5f, 21.5f, 13.25f),
            )),
            Stroke(Tool.ERASER, 0, listOf(StrokePoint(100f, 200f, 40f))),
        ),
        stickers = listOf(
            Sticker("kira", 300f, 400f, 1.5f, 0.25f),
            Sticker("star", 50f, 60f),
        ),
    )

    @Test
    fun `работа сохраняется и читается обратно без потерь`() {
        val text = DocumentCodec.encode(sample())
        val back = DocumentCodec.decode(text)
        assertEquals(sample(), back)
    }

    @Test
    fun `пустой рисунок тоже сохраняется`() {
        val empty = Document(800, 800)
        assertTrue(empty.isEmpty)
        assertEquals(empty, DocumentCodec.decode(DocumentCodec.encode(empty)))
    }

    @Test
    fun `битая запись не роняет приложение`() {
        assertNull(DocumentCodec.decodeOrNull("совсем не то"))
        assertNull(DocumentCodec.decodeOrNull(""))
        assertNull(DocumentCodec.decodeOrNull("RISUEM 1\nSIZE лапша\n"))
        assertNotNull(DocumentCodec.decodeOrNull(DocumentCodec.encode(sample())))
    }

    @Test
    fun `незнакомые строки из будущих версий пропускаются`() {
        val text = DocumentCodec.encode(sample()) + "ЧТОТОНОВОЕ 1 2 3\n"
        assertEquals(sample(), DocumentCodec.decode(text))
    }

    @Test
    fun `штрих добавляется, документ остаётся прежним`() {
        val doc = Document(100, 100)
        val stroke = Stroke(Tool.THIN, Raster.BLACK, listOf(StrokePoint(1f, 1f, 4f)))
        val next = doc.plusStroke(stroke)
        assertEquals(0, doc.strokes.size)
        assertEquals(1, next.strokes.size)
    }

    @Test
    fun `наклейку можно двигать и удалять`() {
        val doc = Document(100, 100).plusSticker(Sticker("star", 10f, 10f))
        val moved = doc.withSticker(0, doc.stickers[0].copy(x = 50f, scale = 2f))
        assertEquals(50f, moved.stickers[0].x, 0.001f)
        assertEquals(2f, moved.stickers[0].scale, 0.001f)
        assertEquals(0, moved.withoutSticker(0).stickers.size)
        assertEquals(1, moved.withoutSticker(7).stickers.size)
    }

    @Test
    fun `очистка убирает и штрихи, и наклейки`() {
        assertTrue(sample().cleared().isEmpty)
    }

    @Test
    fun `отмена штриха через стек возвращает точный документ`() {
        val undo = UndoStack<Document>()
        var doc = Document(100, 100)
        undo.push(doc)
        doc = doc.plusStroke(Stroke(Tool.BRUSH, Raster.BLACK, listOf(StrokePoint(1f, 1f, 5f))))
        undo.push(doc)
        doc = doc.plusSticker(Sticker("star", 5f, 5f))
        doc = undo.undo()!!
        assertEquals(1, doc.strokes.size)
        assertEquals(0, doc.stickers.size)
    }

    @Test
    fun `раскраска сохраняется и читается обратно`() {
        val work = ColoringWork("domik", intArrayOf(0, argb(255, 1, 2, 3), -1))
        val back = DocumentCodec.decodeColoring(DocumentCodec.encodeColoring(work))
        assertEquals(work, back)
        assertNull(DocumentCodec.decodeColoringOrNull("мусор"))
    }
}
