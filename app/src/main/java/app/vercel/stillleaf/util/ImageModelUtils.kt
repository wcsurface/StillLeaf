package app.vercel.stillleaf.util

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun rememberImageModel(photoUrl: String): Any {
    val context = LocalContext.current
    return remember(photoUrl) {
        when {
            photoUrl.isBlank() -> ""
            photoUrl.startsWith("file://") -> {
                val filePath = photoUrl.removePrefix("file://")
                val file = File(filePath)
                if (file.exists()) file else photoUrl
            }
            photoUrl.startsWith("data:image/jpeg;base64,") || photoUrl.startsWith("data:image/png;base64,") -> {
                try {
                    val base64Data = photoUrl.substringAfter("base64,")
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: photoUrl
                } catch (_: Exception) {
                    photoUrl
                }
            }
            else -> photoUrl
        }
    }
}
