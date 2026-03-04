package plus.vplan.app.core.data.file

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response
import java.io.File as JavaFile

actual class FileOpener(private val context: Context) {
    
    actual suspend fun openFile(file: File, filePath: String): Response<Unit> {
        return try {
            val javaFile = JavaFile(filePath)
            if (!javaFile.exists()) {
                return Response.Error.Other("File not found at $filePath")
            }
            
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                javaFile
            )
            
            // Set MIME type based on file extension
            when (file.name.substringAfterLast('.', "").lowercase()) {
                "pdf" -> intent.setDataAndType(uri, "application/pdf")
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> intent.setDataAndType(uri, "image/*")
                "doc", "docx" -> intent.setDataAndType(uri, "application/msword")
                "xls", "xlsx" -> intent.setDataAndType(uri, "application/vnd.ms-excel")
                "txt" -> intent.setDataAndType(uri, "text/plain")
                else -> intent.setDataAndType(uri, "*/*")
            }
            
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Response.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.Error.Other(e.message ?: "Failed to open file")
        }
    }
}
