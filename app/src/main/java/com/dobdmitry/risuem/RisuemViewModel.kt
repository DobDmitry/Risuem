package com.dobdmitry.risuem

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dobdmitry.risuem.art.Templates
import com.dobdmitry.risuem.data.HeroStore
import com.dobdmitry.risuem.data.SessionStore
import com.dobdmitry.risuem.data.WorkStore
import com.dobdmitry.risuem.draw.CanvasEngine
import com.dobdmitry.risuem.draw.ColoringCanvas
import com.dobdmitry.risuem.nav.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import risuem.core.ColoringWork
import risuem.core.Document
import risuem.core.DocumentCodec
import risuem.core.Stroke
import risuem.core.UndoStack

/**
 * Состояние всего приложения.
 *
 * Здесь живёт незаконченная работа: и растр рисунка, и цвета раскраски.
 * ViewModel переживает поворот и сворачивание, а при уходе в фон мы ещё и
 * кладём всё на диск — тогда рисунок не теряется даже если систему прижмёт
 * по памяти и приложение выгрузят целиком.
 */
class RisuemViewModel(app: Application) : AndroidViewModel(app) {

    val works = WorkStore(app)
    val heroes = HeroStore(app)
    private val session = SessionStore(app)

    // ── навигация ──────────────────────────────────────────────────────
    var screen by mutableStateOf<Screen>(Screen.Start)
        private set
    private val history = ArrayDeque<Screen>()

    fun go(next: Screen) {
        history.addLast(screen)
        screen = next
    }

    /** Назад. На стартовом экране не делает ничего: выход только удержанием домика. */
    fun back() {
        screen = history.removeLastOrNull() ?: Screen.Start
    }

    fun goHome() {
        history.clear()
        screen = Screen.Start
    }

    // ── цвета ──────────────────────────────────────────────────────────
    var fillColor by mutableIntStateOf(0)
    var brushColor by mutableIntStateOf(0)

    // ── рисование ──────────────────────────────────────────────────────
    var engine by mutableStateOf<CanvasEngine?>(null)
        private set
    var document by mutableStateOf(Document(1, 1))
        private set
    val drawUndo = UndoStack<Document>(20)
    private var drawWorkId: String? = null
    private var restoredImage: Bitmap? = null
    private var pendingWorkNote: String? = null

    /** Холст создаётся, когда экран впервые узнал свой размер. */
    fun prepareCanvas(width: Int, height: Int) {
        val current = engine
        if (current != null && current.width == width && current.height == height) return
        val fresh = CanvasEngine(width, height)
        engine = fresh
        document = Document(width, height)
        drawUndo.clear()

        val note = pendingWorkNote ?: session.loadDrawingNote()
        val image = restoredImage ?: session.loadDrawingImage()
        if (note != null) {
            DocumentCodec.decodeOrNull(note)?.let { document = it.copy(width = width, height = height) }
        }
        if (image != null) {
            fresh.loadFrom(image)
        } else if (note != null) {
            fresh.redraw(document)
        }
        restoredImage = null
        pendingWorkNote = null
    }

    fun addStroke(stroke: Stroke) {
        drawUndo.push(document)
        document = document.plusStroke(stroke)
    }

    fun changeDocument(next: Document, pushUndo: Boolean = true) {
        if (pushUndo) drawUndo.push(document)
        document = next
    }

    /** Отмена: возвращаем предыдущий документ и перерисовываем растр заново. */
    fun undoStroke() {
        val previous = drawUndo.undo() ?: return
        document = previous
        engine?.redraw(previous)
    }

    fun clearDrawing() {
        drawUndo.push(document)
        document = document.cleared()
        engine?.clear()
    }

    fun startNewDrawing() {
        drawWorkId = null
        pendingWorkNote = null
        restoredImage = null
        session.clearDrawing()
        engine?.clear()
        document = document.cleared()
        drawUndo.clear()
    }

    /** Сохранить рисунок в «мои работы». [flat] — картинка вместе с наклейками. */
    fun saveDrawing(flat: Bitmap) {
        val id = drawWorkId ?: works.newId()
        drawWorkId = id
        works.save(id, flat, NOTE_DRAW + "\n" + DocumentCodec.encode(document))
    }

    // ── раскраска ──────────────────────────────────────────────────────
    var coloring by mutableStateOf<ColoringCanvas?>(null)
        private set
    var coloringName by mutableStateOf("")
        private set
    var coloringLoading by mutableStateOf(false)
        private set
    var templateId by mutableStateOf<String?>(null)
        private set
    val colorUndo = UndoStack<IntArray>(20)
    private var coloringWorkId: String? = null

    /** Открыть картинку. Разметка областей считается вне главного потока. */
    fun openTemplate(template: Templates.Template, restoreColors: IntArray? = null, workId: String? = null) {
        // Если приложение закрыли прямо посреди раскраски — цвета вернутся сами,
        // как только ребёнок снова выберет ту же картинку.
        val colors = restoreColors
            ?: unfinishedColoring()?.takeIf { it.first == template.id }?.second
        coloringLoading = true
        coloringName = template.name
        templateId = template.id
        coloringWorkId = workId
        colorUndo.clear()
        viewModelScope.launch {
            val canvas = withContext(Dispatchers.Default) {
                val outline = Templates.outlineBitmap(getApplication<Application>(), template)
                ColoringCanvas(Templates.page(outline))
            }
            if (colors != null && colors.size == canvas.page.colors.size) {
                canvas.page.restore(colors)
                canvas.refresh()
            }
            coloring = canvas
            coloringLoading = false
        }
    }

    fun undoFill() {
        val canvas = coloring ?: return
        val previous = colorUndo.undo() ?: return
        canvas.page.restore(previous)
        canvas.refresh()
    }

    fun clearColoring() {
        val canvas = coloring ?: return
        colorUndo.push(canvas.page.snapshot())
        canvas.page.clear()
        canvas.refresh()
    }

    fun saveColoring(flat: Bitmap) {
        val canvas = coloring ?: return
        val id = coloringWorkId ?: works.newId()
        coloringWorkId = id
        val note = NOTE_COLOR + "\n" +
            DocumentCodec.encodeColoring(ColoringWork(templateId ?: "", canvas.page.snapshot()))
        works.save(id, flat, note)
    }

    fun closeColoring() {
        coloring = null
        templateId = null
        coloringWorkId = null
        colorUndo.clear()
        session.clearColoring()
    }

    // ── мои работы ─────────────────────────────────────────────────────
    var workList by mutableStateOf<List<WorkStore.Work>>(emptyList())
        private set
    var heroList by mutableStateOf<List<HeroStore.Hero>>(emptyList())
        private set

    fun refreshWorks() {
        workList = works.list()
    }

    fun refreshHeroes() {
        heroList = heroes.list()
    }

    fun deleteWork(id: String) {
        works.delete(id)
        refreshWorks()
    }

    /** Открыть сохранённую работу и продолжить её. */
    fun openWork(work: WorkStore.Work): Screen? {
        val note = works.loadNote(work.id) ?: return null
        return when (note.lineSequence().firstOrNull()) {
            NOTE_DRAW -> {
                drawWorkId = work.id
                pendingWorkNote = note.substringAfter('\n')
                restoredImage = works.loadImage(work.id)
                engine = null
                Screen.Draw
            }
            NOTE_COLOR -> {
                val saved = DocumentCodec.decodeColoringOrNull(note.substringAfter('\n')) ?: return null
                val template = Templates.all(heroes.list()).firstOrNull { it.id == saved.templateId }
                    ?: return null
                openTemplate(template, saved.colors, work.id)
                Screen.Coloring
            }
            else -> null
        }
    }

    // ── незаконченное ──────────────────────────────────────────────────

    /** Вызывается при уходе приложения в фон: работа не должна потеряться. */
    fun persist() {
        engine?.let { session.saveDrawing(it.bitmap, DocumentCodec.encode(document)) }
        coloring?.let { canvas ->
            session.saveColoring(
                DocumentCodec.encodeColoring(ColoringWork(templateId ?: "", canvas.page.snapshot()))
            )
        }
    }

    /** Незаконченная раскраска, если она была. Возвращает шаблон и цвета. */
    fun unfinishedColoring(): Pair<String, IntArray>? {
        val note = session.loadColoring() ?: return null
        val work = DocumentCodec.decodeColoringOrNull(note) ?: return null
        if (work.templateId.isEmpty()) return null
        return work.templateId to work.colors
    }

    companion object {
        const val NOTE_DRAW = "DRAW"
        const val NOTE_COLOR = "COLOR"
    }
}
