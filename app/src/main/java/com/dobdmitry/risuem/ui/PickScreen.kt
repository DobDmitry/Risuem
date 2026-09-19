package com.dobdmitry.risuem.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.art.Templates
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.nav.Screen
import com.dobdmitry.risuem.ui.components.SmallIconButton
import com.dobdmitry.risuem.ui.components.SpokenWord
import com.dobdmitry.risuem.ui.components.SoundToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Выбор картинки для раскрашивания.
 *
 * Крупные цветные превью в два столбца, под каждым — короткое имя.
 * Сначала герои, которых добавил взрослый, потом Кира, мама и папа,
 * потом звери и простые картинки.
 */
@Composable
fun PickScreen(model: RisuemViewModel) {
    val audio = LocalAudio.current
    val templates = remember(model.heroList) { Templates.all(model.heroList) }

    LaunchedEffect(Unit) { audio.say(Words.PICK_PICTURE) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SmallIconButton(Pic.HOME) { model.goHome() }
            Spacer(Modifier.weight(1f))
            SoundToggle()
        }
        Spacer(Modifier.padding(4.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateCard(template) {
                    audio.play(Sound.BLUP)
                    audio.say(template.name)
                    model.openTemplate(template)
                    model.go(Screen.Coloring)
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(template: Templates.Template, onClick: () -> Unit) {
    val thumb = rememberThumbnail(template)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pointerInput(template.id) { detectTapGestures { onClick() } },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(PaperDeep),
        ) {
            if (thumb != null) {
                Image(
                    bitmap = thumb,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                )
            }
        }
        SpokenWord(Word(template.name), Modifier.padding(top = 4.dp), speak = false)
    }
}

/** Превью считается вне главного потока: сетка листается плавно. */
@Composable
private fun rememberThumbnail(template: Templates.Template): ImageBitmap? {
    val context = LocalContext.current
    var image by remember(template.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(template.id) {
        image = withContext(Dispatchers.Default) {
            Templates.thumbnail(context, template).asImageBitmap()
        }
    }
    return image
}
