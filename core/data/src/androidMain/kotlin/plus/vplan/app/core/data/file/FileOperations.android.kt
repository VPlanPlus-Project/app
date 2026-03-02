package plus.vplan.app.core.data.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

actual suspend fun writeFilePlatform(path: String, content: ByteArray) {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeBytes(content)
}

actual suspend fun deleteFilePlatform(path: String) {
    val file = File(path)
    if (file.exists()) {
        file.delete()
    }
}

actual suspend fun fileExistsPlatform(path: String): Boolean {
    return File(path).exists()
}

actual suspend fun loadThumbnailPlatform(path: String): ImageBitmap? {
    return try {
        val file = File(path)
        if (!file.exists()) return null
        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

actual suspend fun saveThumbnailPlatform(thumbnail: ImageBitmap, path: String) {
    val file = File(path)
    file.parentFile?.mkdirs()
    
    FileOutputStream(file).use { out ->
        thumbnail.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
}
