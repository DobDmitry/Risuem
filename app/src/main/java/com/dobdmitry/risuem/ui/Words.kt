package com.dobdmitry.risuem.ui

/**
 * Все надписи приложения — в одном месте, рядом с тем, что произносит голос.
 *
 * Правила, которые здесь нельзя нарушать:
 *  - не длиннее семи букв: читает ребёнок четырёх лет;
 *  - только заглавные;
 *  - буква «ё» пишется именно «ё»;
 *  - у каждой надписи есть иконка рядом — слово только помогает.
 *
 * [speech] — то, что скажет голос: там можно и длиннее, слушать легче, чем читать.
 */
data class Word(val text: String, val speech: String = text)

object Words {
    val RISUEM = Word("РИСУЕМ", "Рисуем")
    val KRASIM = Word("КРАСИМ", "Красим")
    val RABOTY = Word("РАБОТЫ", "Мои работы")
    val GOTOVO = Word("ГОТОВО", "Готово")
    val NAZAD = Word("НАЗАД", "Назад")
    val ESHCHYO = Word("ЕЩЁ РАЗ", "Ещё раз")
    val CVETA = Word("ЦВЕТА", "Цвета")
    val ZVUK = Word("ЗВУК", "Звук")
    val DOMOJ = Word("ДОМОЙ", "Домой")
    val GEROJ = Word("ГЕРОЙ", "Новый герой")
    val FOTO = Word("ФОТО", "Выбрать фото")
    val SNYAT = Word("СНЯТЬ", "Снять камерой")
    val IMYA = Word("ИМЯ", "Имя героя")
    val DALSHE = Word("ДАЛЬШЕ", "Дальше")
    val UDALIT = Word("УДАЛИТЬ", "Удалить")

    /** Фразы голоса — их не читают, их слушают. */
    const val PRAISE = "Как красиво!"
    const val SAVED = "Сохранил"
    const val PICK_PICTURE = "Выбери картинку"
    const val PICK_COLOR = "Выбери цвет"
    const val PARENT_QUESTION = "Это для взрослого"
}

/** Имена картинок и наклеек: тоже короткие и заглавными. */
object Names {
    const val KIRA = "КИРА"
    const val MAMA = "МАМА"
    const val PAPA = "ПАПА"
    const val KRYA = "КРЯ"
    const val YOZHIK = "ЁЖИК"
    const val LISA = "ЛИСА"
    const val MISHKA = "МИШКА"
    const val VOLK = "ВОЛК"
    const val LEV = "ЛЕВ"
    const val DOMIK = "ДОМИК"
    const val MASHINA = "МАШИНА"
    const val SOLNCE = "СОЛНЦЕ"
    const val CVETOK = "ЦВЕТОК"
    const val RYBKA = "РЫБКА"
    const val BABOCHKA = "БАБОЧКА"
    const val YOLKA = "ЁЛКА"
    const val TORT = "ТОРТ"
    const val ZVEZDA = "ЗВЕЗДА"
    const val SERDCE = "СЕРДЦЕ"
}
