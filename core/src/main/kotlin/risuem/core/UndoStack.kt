package risuem.core

/**
 * Стек отмены на [limit] шагов.
 *
 * Перед каждым изменением кладём сюда снимок предыдущего состояния.
 * [undo] возвращает ровно тот снимок — состояние восстанавливается точно.
 * Когда шагов становится больше лимита, самый старый снимок выбрасывается:
 * память не растёт, а ребёнку 20 шагов назад хватает с запасом.
 */
class UndoStack<T>(val limit: Int = DEFAULT_LIMIT) {

    init {
        require(limit > 0) { "лимит должен быть положительным" }
    }

    private val items = ArrayDeque<T>()

    val size: Int get() = items.size

    val canUndo: Boolean get() = items.isNotEmpty()

    /** Запомнить состояние ДО изменения. */
    fun push(state: T) {
        items.addLast(state)
        while (items.size > limit) items.removeFirst()
    }

    /** Вернуть последнее запомненное состояние и убрать его из стека. */
    fun undo(): T? = items.removeLastOrNull()

    /** Подсмотреть последний снимок, не снимая его. */
    fun peek(): T? = items.lastOrNull()

    fun clear() = items.clear()

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
