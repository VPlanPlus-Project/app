package plus.vplan.app.utils

import androidx.compose.ui.graphics.ImageBitmap

expect fun getBitmapFromBytes(bytes: ByteArray?): ImageBitmap?
expect fun getBitmapFromPdf(bytes: ByteArray): ImageBitmap?