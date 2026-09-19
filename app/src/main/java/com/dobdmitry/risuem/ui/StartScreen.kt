package com.dobdmitry.risuem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.nav.Screen
import com.dobdmitry.risuem.ui.components.BigAction
import com.dobdmitry.risuem.ui.components.HoldAction
import com.dobdmitry.risuem.ui.components.SoundToggle

/**
 * Стартовый экран: две большие иконки — красим и рисуем.
 *
 * Внизу домик: удержание полторы секунды закрывает приложение.
 * Слева вверху замок для взрослого — туда же удержанием.
 */
@Composable
fun StartScreen(model: RisuemViewModel, onExit: () -> Unit) {
    val audio = LocalAudio.current

    LaunchedEffect(Unit) {
        audio.say(Words.RISUEM.speech)
        model.refreshWorks()
        model.refreshHeroes()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Вход для взрослого: удержание полторы секунды, потом пример.
            HoldAction(
                pic = Pic.LOCK,
                word = null,
                size = 56.dp,
                holdMillis = 1500,
                onHold = { model.go(Screen.Gate) },
            )
            Spacer(Modifier.weight(1f))
            SoundToggle()
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = Words.RISUEM.text,
            style = MaterialTheme.typography.displayLarge,
            color = Ink,
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BigAction(
                pic = Pic.BUCKET,
                word = Words.KRASIM,
                size = 140.dp,
                onClick = { model.go(Screen.Pick) },
            )
            BigAction(
                pic = Pic.PENCIL,
                word = Words.RISUEM,
                size = 140.dp,
                onClick = {
                    model.startNewDrawing()
                    model.go(Screen.Draw)
                },
            )
        }

        Spacer(Modifier.height(28.dp))

        BigAction(
            pic = Pic.GRID,
            word = Words.RABOTY,
            size = 110.dp,
            onClick = {
                model.refreshWorks()
                model.go(Screen.Works)
            },
        )

        Spacer(Modifier.weight(1f))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            // Выход: только удержанием, с кольцом прогресса.
            HoldAction(
                pic = Pic.HOME,
                word = null,
                size = 84.dp,
                holdMillis = 1500,
                onHold = onExit,
            )
        }
    }
}
