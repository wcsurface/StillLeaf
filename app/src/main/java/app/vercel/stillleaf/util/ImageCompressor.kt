package app.vercel.stillleaf.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    /**
     * Downsamples and compresses an image URI into a JPEG ByteArray <= 1024px max dimension @ 80% quality.
     */
    suspend fun compressUri(context: Context, imageUri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalArgumentException("Cannot open stream for Uri: $imageUri")

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val rotatedBitmap = rotateImageIfRequired(context, imageUri, originalBitmap)
        val scaledBitmap = scaleBitmapToMaxDimension(rotatedBitmap, MAX_DIMENSION)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        outputStream.toByteArray()
    }

    /**
     * Downsamples and compresses a raw Bitmap into a JPEG ByteArray.
     */
    suspend fun compressBitmap(bitmap: Bitmap): ByteArray = withContext(Dispatchers.IO) {
        val scaledBitmap = scaleBitmapToMaxDimension(bitmap, MAX_DIMENSION)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        outputStream.toByteArray()
    }

    private fun scaleBitmapToMaxDimension(src: Bitmap, maxDim: Int): Bitmap {
        val width = src.width
        val height = src.height

        if (width <= maxDim && height <= maxDim) {
            return src
        }

        val scale = maxDim.toFloat() / max(width, height)
        val targetWidth = (width * scale).toInt()
        val targetHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    }

    private fun rotateImageIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
