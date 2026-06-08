package com.wings.picexchange.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.wings.picexchange.domain.ImageRef
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Imports user images (gallery or camera) into app-internal storage, downscaled, and returns a
 * typed "file:" [ImageRef] string. Keeping images app-private (filesDir) keeps the app fully offline
 * and avoids revocable content:// permissions.
 */
@Singleton
class ImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    data class CameraTarget(val file: File, val uri: Uri)

    private val imagesDir: File get() = File(context.filesDir, "card_images").apply { mkdirs() }
    private val cameraDir: File get() = File(context.filesDir, "camera").apply { mkdirs() }

    /** Copies + downscales the gallery image at [source] into internal storage. */
    suspend fun importFromUri(source: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeDownsampled(source) ?: error("Could not decode image")
            val ref = writeToImages(bitmap)
            bitmap.recycle()
            ref
        }
    }

    /** A FileProvider target the camera can write a full-resolution capture into. */
    fun createCameraTarget(): CameraTarget {
        val file = File(cameraDir, "capture_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return CameraTarget(file, uri)
    }

    /** Downscales a camera capture into internal storage and removes the full-res temp file. */
    suspend fun finalizeCameraCapture(temp: File): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeDownsampled(Uri.fromFile(temp)) ?: error("Could not decode photo")
            val ref = writeToImages(bitmap)
            bitmap.recycle()
            temp.delete()
            ref
        }
    }

    /** Deletes the backing file for a "file:" ref; no-op for built-in drawables. */
    fun deleteIfLocal(imageRef: String) {
        (ImageRef.parse(imageRef) as? ImageRef.LocalFile)?.let { runCatching { File(it.path).delete() } }
    }

    private fun writeToImages(bitmap: Bitmap): String {
        val target = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
        FileOutputStream(target).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }
        return ImageRef.LocalFile(target.absolutePath).encode()
    }

    private fun decodeDownsampled(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val longest = max(bounds.outWidth, bounds.outHeight)
        if (longest <= 0) return null

        var sample = 1
        while (longest / sample > MAX_DIM * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        val scale = MAX_DIM.toFloat() / max(decoded.width, decoded.height)
        return if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).roundToInt(),
                (decoded.height * scale).roundToInt(),
                true,
            ).also { if (it != decoded) decoded.recycle() }
        } else {
            decoded
        }
    }

    private companion object {
        const val MAX_DIM = 1024
    }
}
