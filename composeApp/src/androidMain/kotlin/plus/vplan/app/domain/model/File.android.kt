package plus.vplan.app.domain.model

import android.content.Intent
import androidx.core.content.FileProvider
import plus.vplan.app.activity

actual fun openFile(file: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    val storedFile = activity.filesDir.resolve("homework_files" + "/" + file.id.toString())

    val newUri = FileProvider.getUriForFile(
        activity,
        activity.packageName + ".fileprovider",
        storedFile
    )
    when (file.name.substringAfterLast(".").lowercase()) {
        "pdf" -> intent.setDataAndType(newUri, "application/pdf")
        "jpg", "jpeg", "png" -> intent.setDataAndType(newUri, "image/*")
    }
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    activity.startActivity(intent)
}