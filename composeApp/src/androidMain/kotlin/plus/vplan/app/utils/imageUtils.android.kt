package plus.vplan.app.utils

import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import java.io.File


actual fun getBitmapFromPdf(bytes: ByteArray): ImageBitmap? {
    val file = File.createTempFile("temp", ".pdf")
    file.writeBytes(bytes)
    val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
    val page = pdfRenderer.openPage(0)
    val bitmap = createBitmap(page.width, page.height)
    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    return bitmap.asImageBitmap()
}

actual fun getBitmapFromBytes(bytes: ByteArray?): ImageBitmap? {
    return if (bytes != null) {
        BitmapFactory.decodeByteArray(bytes,0,bytes.size)?.asImageBitmap()
    } else {
        null
    }
}