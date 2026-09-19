package com.dobdmitry.risuem.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Отдать работу наружу — кнопка для взрослого, маленькая и в углу.
 *
 * Разрешений не нужно ни одного: «поделиться» отдаёт файл через FileProvider
 * на один раз, а «сохранить» открывает системный диалог, где взрослый сам
 * выбирает папку. Поэтому приложение работает и на Android 8, где прямая
 * запись в галерею потребовала бы разрешения.
 */
object Export {

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        val uri = cacheUri(context, bitmap)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /** Записать картинку в файл, который взрослый выбрал в системном диалоге. */
    fun writeTo(context: Context, uri: Uri, bitmap: Bitmap): Boolean = try {
        context.contentResolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        } != null
    } catch (e: Exception) {
        false
    }

    /** Куда камера положит снимок. Разрешений не нужно: файл наш собственный. */
    fun cameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "camera.jpg")
        if (!file.exists()) file.createNewFile()
        return FileProvider.getUriForFile(context, context.packageName + ".files", file)
    }

    fun suggestedName(): String = "risuem-" + System.currentTimeMillis() + ".png"

    private fun cacheUri(context: Context, bitmap: Bitmap): Uri {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "risuem.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, context.packageName + ".files", file)
    }
}
