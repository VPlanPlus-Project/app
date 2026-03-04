package plus.vplan.app.core.data.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import plus.vplan.app.core.model.File
import java.io.File as JavaFile

actual class ThumbnailGenerator(private val filesDir: JavaFile) {
    
    actual suspend fun generateThumbnail(file: File, filePath: String): ImageBitmap? {
        val javaFile = JavaFile(filePath)
        if (!javaFile.exists()) return null
        
        return try {
            when (FileType.fromFileName(file.name)) {
                FileType.PDF -> generatePdfThumbnail(javaFile)
                FileType.IMAGE -> generateImageThumbnail(javaFile)
                else -> null // Generic icons will be handled in UI layer
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun generatePdfThumbnail(file: JavaFile): ImageBitmap? {
        return try {
            val pdfRenderer = PdfRenderer(
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            )
            val page = pdfRenderer.openPage(0)
            
            // Calculate scaling to fit in 256x256
            val scale = minOf(
                THUMBNAIL_SIZE.toFloat() / page.width,
                THUMBNAIL_SIZE.toFloat() / page.height
            )
            val width = (page.width * scale).toInt()
            val height = (page.height * scale).toInt()
            
            val bitmap = createBitmap(width, height)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pdfRenderer.close()
            
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun generateImageThumbnail(file: JavaFile): ImageBitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            
            // Calculate sample size for initial decode
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight)
            
            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize
            
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                ?: return null
            
            // Scale to exact target size
            val scaled = scaleBitmapToTarget(bitmap, THUMBNAIL_SIZE)
            if (scaled != bitmap) {
                bitmap.recycle()
            }
            
            scaled.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val maxDimension = maxOf(width, height)
        
        while (maxDimension / sampleSize > THUMBNAIL_SIZE * 2) {
            sampleSize *= 2
        }
        
        return sampleSize
    }
    
    private fun scaleBitmapToTarget(source: Bitmap, THUMBNAIL_SIZE: Int): Bitmap {
        val scale = minOf(
            THUMBNAIL_SIZE.toFloat() / source.width,
            THUMBNAIL_SIZE.toFloat() / source.height
        )
        
        if (scale >= 1f) return source // Already smaller than target
        
        val width = (source.width * scale).toInt()
        val height = (source.height * scale).toInt()
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
        }
        
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
