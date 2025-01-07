package plus.vplan.app.utils

import androidx.compose.ui.graphics.ImageBitmap

expect fun getBitmapFromBytes(bytes: ByteArray?): ImageBitmap?
expect fun getDataFromPdf(bytes: ByteArray): PdfData?

data class PdfData(
    val pages: Int,
    val firstPage: ImageBitmap?
)