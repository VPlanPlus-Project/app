package plus.vplan.app.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

actual fun getDataFromPdf(bytes: ByteArray): PdfData? = null
actual fun getBitmapFromBytes(bytes: ByteArray?): ImageBitmap? {
    return if (bytes != null) {
        Bitmap.makeFromImage(Image.makeFromEncoded(bytes)).asComposeImageBitmap()
    } else {
        null
    }
}