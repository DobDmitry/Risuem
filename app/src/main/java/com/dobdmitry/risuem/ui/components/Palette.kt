package com.dobdmitry.risuem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.ui.Ink
import com.dobdmitry.risuem.ui.PaintColors

/**
 * Палитра: двенадцать больших кругов в два ряда.
 *
 * Круг не меньше 56 dp даже на самом узком телефоне — в него легко попасть
 * пальцем четырёхлетнего. Выбранный увеличивается и тихо пульсирует:
 * какой цвет сейчас в руке, видно без единого слова.
 */
@Composable
fun PaletteBar(
    selected: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    val audio = LocalAudio.current
    val pulse = rememberPulse()

    BoxWithConstraints(modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp)) {
        val perRow = 6
        val spacing = 2.dp
        val raw = (maxWidth - spacing * (perRow - 1)) / perRow
        val circle = if (raw > 76.dp) 76.dp else raw

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (row in 0 until 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (column in 0 until perRow) {
                        val index = row * perRow + column
                        if (index >= PaintColors.size) continue
                        val isSelected = index == selected
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(circle)
                                .pointerInput(index) {
                                    detectTapGestures {
                                        audio.play(Sound.BLUP)
                                        onSelect(index)
                                    }
                                },
                        ) {
                            Box(
                                Modifier
                                    .size(circle * if (isSelected) 0.98f else 0.84f)
                                    .scale(if (isSelected) pulse else 1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Ink else Color.Transparent)
                                    .padding(if (isSelected) 4.dp else 0.dp)
                                    .clip(CircleShape)
                                    .background(PaintColors[index]),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Цвет по номеру: одно место, где палитра превращается в краску. */
fun paintColor(index: Int): Color = PaintColors[index.coerceIn(PaintColors.indices)]
