package com.dobdmitry.risuem.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.data.Export
import com.dobdmitry.risuem.data.WorkStore
import com.dobdmitry.risuem.ui.components.SmallIconButton
import com.dobdmitry.risuem.ui.components.SoundToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Мои работы.
 *
 * Тап — открыть и продолжить. Удержание полторы секунды с кольцом — удалить:
 * случайно не выйдет. В углу каждой работы две маленькие кнопки для взрослого:
 * отдать картинку в другое приложение и сохранить файлом.
 */
@Composable
fun WorksScreen(model: RisuemViewModel) {
    val audio = LocalAudio.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var shareTarget by remember { mutableStateOf<WorkStore.Work?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        val work = shareTarget
        if (uri != null && work != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    model.works.loadImage(work.id)?.let { Export.writeTo(context, uri, it) }
                }
                audio.play(Sound.FANFARE)
            }
        }
        shareTarget = null
    }

    LaunchedEffect(Unit) {
        model.refreshWorks()
        audio.say(Words.RABOTY.speech)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SmallIconButton(Pic.HOME) { model.goHome() }
            Spacer(Modifier.weight(1f))
            SoundToggle()
        }
        Spacer(Modifier.size(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(model.workList, key = { it.id }) { work ->
                WorkCard(
                    work = work,
                    load = { model.works.loadImage(work.id)?.asImageBitmap() },
                    onOpen = {
                        audio.play(Sound.BLUP)
                        val next = model.openWork(work)
                        if (next != null) model.go(next)
                    },
                    onDelete = {
                        audio.play(Sound.ERASE)
                        model.deleteWork(work.id)
                    },
                    onShare = {
                        audio.play(Sound.BLUP)
                        model.works.loadImage(work.id)?.let { Export.shareBitmap(context, it) }
                    },
                    onSave = {
                        audio.play(Sound.BLUP)
                        shareTarget = work
                        saveLauncher.launch(Export.suggestedName())
                    },
                )
            }
        }
    }
}

@Composable
private fun WorkCard(
    work: WorkStore.Work,
    load: () -> ImageBitmap?,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
) {
    var image by remember(work.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(work.id) {
        image = withContext(Dispatchers.Default) { load() }
    }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(18.dp))
            .background(PaperDeep)
            .pointerInput(work.id) {
                detectTapGestures(
                    onPress = {
                        val job = scope.launch {
                            progress.animateTo(1f, tween(1500, easing = LinearEasing))
                            onDelete()
                        }
                        tryAwaitRelease()
                        job.cancel()
                        scope.launch { progress.animateTo(0f, tween(150)) }
                    },
                    onTap = { onOpen() },
                )
            },
    ) {
        image?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp),
            )
        }
        // Кольцо удержания поверх работы.
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
            if (progress.value <= 0f) return@Canvas
            val stroke = size.minDimension * 0.05f
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = 360f * progress.value,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        // Маленькие кнопки для взрослого.
        Row(
            Modifier.align(Alignment.TopEnd).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SmallIconButton(Pic.SHARE, size = 34.dp, onClick = onShare)
            SmallIconButton(Pic.PHOTO, size = 34.dp, onClick = onSave)
        }
    }
}
