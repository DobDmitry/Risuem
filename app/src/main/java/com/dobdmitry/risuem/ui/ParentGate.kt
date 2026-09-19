package com.dobdmitry.risuem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.nav.Screen
import com.dobdmitry.risuem.ui.components.SmallIconButton
import kotlin.random.Random

/**
 * Замок для взрослого.
 *
 * Сюда попадают удержанием иконки полторы секунды, а потом — простой пример
 * на умножение. Четырёхлетний не пройдёт, взрослый справится за три секунды.
 * Цифры нажимаются кнопками: клавиатуры в приложении нет нигде, кроме
 * единственного поля с именем героя.
 */
@Composable
fun ParentGateScreen(model: RisuemViewModel) {
    val audio = LocalAudio.current
    val first = remember { Random.nextInt(6, 10) }
    val second = remember { Random.nextInt(6, 10) }
    var input by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SmallIconButton(Pic.HOME) { model.goHome() }
            Spacer(Modifier.weight(1f))
            PicIcon(Pic.LOCK, Modifier.size(48.dp), Ink)
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "$first × $second",
            style = MaterialTheme.typography.displayLarge,
            color = Ink,
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = if (input.isEmpty()) "—" else input,
            style = MaterialTheme.typography.headlineLarge,
            color = if (wrong) Accent else Ink,
        )

        Spacer(Modifier.weight(1f))

        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
        for (row in rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (digit in row) {
                    KeyButton(digit) {
                        audio.play(Sound.BLUP)
                        wrong = false
                        if (input.length < 3) input += digit
                    }
                }
            }
            Spacer(Modifier.size(10.dp))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallIconButton(Pic.UNDO, size = 72.dp) {
                audio.play(Sound.BLUP)
                input = input.dropLast(1)
            }
            KeyButton("0") {
                audio.play(Sound.BLUP)
                wrong = false
                if (input.length < 3) input += "0"
            }
            SmallIconButton(Pic.CHECK, size = 72.dp, background = Accent) {
                if (input.toIntOrNull() == first * second) {
                    audio.play(Sound.FANFARE)
                    model.refreshHeroes()
                    model.go(Screen.AddHero)
                } else {
                    audio.play(Sound.ERASE)
                    wrong = true
                    input = ""
                }
            }
        }
        Spacer(Modifier.size(12.dp))
    }
}

@Composable
private fun KeyButton(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PaperDeep)
            .pointerInput(label) { detectTapGestures { onClick() } },
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge, color = Ink)
    }
}
