package risuem.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoStackTest {

    @Test
    fun `по умолчанию двадцать шагов`() {
        assertEquals(20, UndoStack<String>().limit)
    }

    @Test
    fun `отмена возвращает предыдущее состояние`() {
        val stack = UndoStack<String>()
        stack.push("пусто")
        stack.push("один цвет")
        assertEquals("один цвет", stack.undo())
        assertEquals("пусто", stack.undo())
        assertNull(stack.undo())
    }

    @Test
    fun `состояние возвращается точно, а не копией по ссылке`() {
        val stack = UndoStack<IntArray>()
        val before = intArrayOf(1, 2, 3)
        stack.push(before.copyOf())
        before[1] = 99
        assertArrayEquals(intArrayOf(1, 2, 3), stack.undo())
    }

    @Test
    fun `двадцать шагов помнятся полностью`() {
        val stack = UndoStack<Int>()
        repeat(20) { stack.push(it) }
        assertEquals(20, stack.size)
        for (expected in 19 downTo 0) assertEquals(expected, stack.undo())
        assertFalse(stack.canUndo)
    }

    @Test
    fun `двадцать первый шаг вытесняет самый старый`() {
        val stack = UndoStack<Int>()
        repeat(25) { stack.push(it) }
        assertEquals(20, stack.size)
        assertEquals(24, stack.undo())
        // Первые пять шагов выброшены: самый старый из оставшихся — пятый.
        repeat(19) { stack.undo() }
        assertNull(stack.undo())
    }

    @Test
    fun `очистка убирает всё`() {
        val stack = UndoStack<Int>()
        repeat(5) { stack.push(it) }
        stack.clear()
        assertEquals(0, stack.size)
        assertFalse(stack.canUndo)
    }

    @Test
    fun `подсмотреть можно, не снимая`() {
        val stack = UndoStack<Int>()
        stack.push(7)
        assertEquals(7, stack.peek())
        assertEquals(1, stack.size)
        assertTrue(stack.canUndo)
    }

    @Test
    fun `сто шагов рисования не раздувают память`() {
        val stack = UndoStack<Int>(20)
        repeat(100) { stack.push(it) }
        assertEquals(20, stack.size)
        assertEquals(99, stack.peek())
    }
}
