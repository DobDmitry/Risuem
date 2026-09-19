package com.dobdmitry.risuem.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobdmitry.risuem.RisuemViewModel
import com.dobdmitry.risuem.art.Templates
import com.dobdmitry.risuem.art.toBitmap
import com.dobdmitry.risuem.art.toRaster
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sound
import com.dobdmitry.risuem.data.Export
import com.dobdmitry.risuem.data.Photos
import com.dobdmitry.risuem.ui.components.BigAction
import com.dobdmitry.risuem.ui.components.SmallIconButton
import com.dobdmitry.risuem.ui.components.rememberPulse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import risuem.core.Background

private enum class Step { CHOOSE, WORKING, PREVIEW, NAME }

/**
 * Новый герой — раздел для взрослого.
 *
 * Фото берётся системным Photo Picker или камерой: ни одного разрешения
 * в манифесте для этого не нужно. Дальше приложение само убирает однотонный
 * фон и превращает снимок в раскраску с крупными областями, показывает,
 * что вышло, и даёт переснять. Имя — единственное поле ввода во всём
 * приложении: заглавными, не длиннее семи букв.
 */
@Composable
fun AddHeroScreen(model: RisuemViewModel) {
    val audio = LocalAudio.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(Step.CHOOSE) }
    var face by remember { mutableStateOf<Bitmap?>(null) }
    var outline by remember { mutableStateOf<Bitmap?>(null) }
    var name by remember { mutableStateOf("") }
    val cameraUri = remember { Export.cameraUri(context) }

    fun process(uri: Uri) {
        step = Step.WORKING
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                val photo = Photos.loadScaled(context, uri) ?: return@withContext null
                val cut = Background.removeUniform(photo.toRaster()).toBitmap()
                cut to Templates.photoToOutline(cut)
            }
            if (result == null) {
                step = Step.CHOOSE
            } else {
                face = result.first
                outline = result.second
                step = Step.PREVIEW
            }
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) process(uri) }

    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok) process(cameraUri) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SmallIconButton(Pic.HOME) { model.goHome() }
            Spacer(Modifier.weight(1f))
            PicIcon(Pic.HERO, Modifier.size(48.dp), Ink)
        }

        Spacer(Modifier.weight(1f))

        when (step) {
            Step.CHOOSE -> Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BigAction(pic = Pic.PHOTO, word = Words.FOTO, size = 120.dp) {
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                BigAction(pic = Pic.CAMERA, word = Words.SNYAT, size = 120.dp) {
                    takePhoto.launch(cameraUri)
                }
            }

            Step.WORKING -> PicIcon(
                Pic.PALETTE,
                Modifier.size(110.dp).scale(rememberPulse()),
                Ink,
            )

            Step.PREVIEW -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Preview(face, Modifier.weight(1f))
                    Preview(outline, Modifier.weight(1f))
                }
                Spacer(Modifier.size(20.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    BigAction(pic = Pic.CAMERA, word = Words.ESHCHYO, size = 92.dp) {
                        step = Step.CHOOSE
                    }
                    BigAction(pic = Pic.CHECK, word = Words.DALSHE, size = 92.dp) {
                        step = Step.NAME
                    }
                }
            }

            Step.NAME -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Preview(face, Modifier.size(180.dp))
                Spacer(Modifier.size(16.dp))
                Text(Words.IMYA.text, style = MaterialTheme.typography.bodyLarge, color = Ink)
                OutlinedTextField(
                    value = name,
                    onValueChange = { typed ->
                        // Заглавными и не длиннее семи букв — таково правило имён.
                        name = typed.uppercase().filter { it.isLetter() }.take(7)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                    ),
                    modifier = Modifier.padding(12.dp),
                )
                Spacer(Modifier.size(12.dp))
                BigAction(pic = Pic.CHECK, word = Words.GOTOVO, size = 92.dp) {
                    val hero = face
                    val page = outline
                    if (hero != null && page != null && name.isNotEmpty()) {
                        model.heroes.add(name, hero, page)
                        model.refreshHeroes()
                        audio.play(Sound.FANFARE)
                        audio.say(name)
                        model.goHome()
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun Preview(bitmap: Bitmap?, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(6.dp),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
