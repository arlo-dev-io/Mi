package com.example.animelibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private const val MAX_POSTER_SIDE = 1000

/**
 * Copies a picked gallery image into the app's own storage (shrunk, rotated upright)
 * and returns a file:// link for it, or null if anything fails.
 * The copy keeps working even if the original photo is moved or deleted.
 */
suspend fun savePoster(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    try {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_POSTER_SIDE) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return@withContext null

        val rotation = resolver.openInputStream(uri)?.use { input ->
            when (
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        val upright = if (rotation != 0f) {
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height,
                Matrix().apply { postRotate(rotation) }, true
            )
        } else {
            bitmap
        }

        val dir = File(context.filesDir, "posters").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { upright.compress(Bitmap.CompressFormat.JPEG, 90, it) }

        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        null
    }
}

/** Deletes a poster file we saved earlier. Web links and unknown values are ignored. */
fun deletePosterFile(posterUrl: String) {
    if (!posterUrl.startsWith("file://")) return
    try {
        Uri.parse(posterUrl).path?.let { File(it).delete() }
    } catch (e: Exception) {
        // nothing to clean up
    }
}
