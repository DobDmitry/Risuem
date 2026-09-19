package com.dobdmitry.risuem.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.ui.AddHeroScreen
import com.dobdmitry.risuem.ui.ColoringScreen
import com.dobdmitry.risuem.ui.DrawScreen
import com.dobdmitry.risuem.ui.Paper
import com.dobdmitry.risuem.ui.ParentGateScreen
import com.dobdmitry.risuem.ui.PickScreen
import com.dobdmitry.risuem.ui.StartScreen
import com.dobdmitry.risuem.ui.WorksScreen

/**
 * Дерево экранов.
 *
 * Системная кнопка «назад» никогда не закрывает приложение: она просто
 * возвращает на шаг назад, а на стартовом экране не делает ничего.
 * Выход — только удержание домика полторы секунды.
 */
@Composable
fun AppRoot(model: RisuemViewModel, onExit: () -> Unit) {
    BackHandler(enabled = true) {
        if (model.screen != Screen.Start) model.back()
    }

    LaunchedEffect(Unit) {
        model.refreshHeroes()
        model.refreshWorks()
    }

    Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
        when (model.screen) {
            Screen.Start -> StartScreen(model, onExit)
            Screen.Pick -> PickScreen(model)
            Screen.Coloring -> ColoringScreen(model)
            Screen.Draw -> DrawScreen(model)
            Screen.Works -> WorksScreen(model)
            Screen.Gate -> ParentGateScreen(model)
            Screen.AddHero -> AddHeroScreen(model)
        }
    }
}
