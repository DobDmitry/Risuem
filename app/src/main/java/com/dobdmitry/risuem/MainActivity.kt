package com.dobdmitry.risuem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dobdmitry.risuem.audio.AppAudio
import com.dobdmitry.risuem.audio.LocalAudio
import com.dobdmitry.risuem.audio.Sfx
import com.dobdmitry.risuem.audio.Voice
import com.dobdmitry.risuem.nav.AppRoot
import com.dobdmitry.risuem.ui.RisuemTheme

/**
 * Единственный экран приложения: всё остальное — Compose внутри него.
 *
 * Поворот заблокирован в манифесте, разрешений нет ни одного,
 * выйти можно только удержанием домика — кнопка «назад» приложение не закрывает.
 */
class MainActivity : ComponentActivity() {

    private val model: RisuemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val audio = remember { AppAudio(Sfx(context), Voice(context)) }
            DisposableEffect(Unit) {
                onDispose { audio.release() }
            }
            RisuemTheme {
                CompositionLocalProvider(LocalAudio provides audio) {
                    AppRoot(model) { finish() }
                }
            }
        }
    }

    /** Уходим в фон — сразу кладём незаконченную работу на диск. */
    override fun onStop() {
        super.onStop()
        model.persist()
    }
}
