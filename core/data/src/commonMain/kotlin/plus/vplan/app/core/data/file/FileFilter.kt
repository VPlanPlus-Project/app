package plus.vplan.app.core.data.file

enum class FileType {
    IMAGE,
    DOCUMENT,
    PDF,
    OTHER;

    companion object {
        fun fromMimeType(mimeType: String?): FileType {
            return when {
                mimeType == null -> OTHER
                mimeType.startsWith("image/") -> IMAGE
                mimeType == "application/pdf" -> PDF
                mimeType.startsWith("application/") -> DOCUMENT
                else -> OTHER
            }
        }

        fun fromFileName(fileName: String): FileType {
            return when (fileName.substringAfterLast('.', "").lowercase()) {
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> IMAGE
                "pdf" -> PDF
                "doc", "docx", "txt", "rtf", "odt" -> DOCUMENT
                else -> OTHER
            }
        }
    }
}

data class FileFilter(
    val types: Set<FileType> = emptySet(),
    val maxSize: Long? = null
)
