package com.dobdmitry.risuem.nav

/** Экраны приложения. Их немного нарочно: ребёнку негде потеряться. */
sealed interface Screen {
    /** Стартовый экран: две большие иконки. */
    data object Start : Screen

    /** Выбор картинки для раскрашивания. */
    data object Pick : Screen

    /** Раскраска. */
    data object Coloring : Screen

    /** Чистый лист. */
    data object Draw : Screen

    /** Мои работы. */
    data object Works : Screen

    /** Замок для взрослого: пример на умножение. */
    data object Gate : Screen

    /** Добавление нового героя. */
    data object AddHero : Screen
}
