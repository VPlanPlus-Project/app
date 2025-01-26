package plus.vplan.app.ui.common

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.extension
import plus.vplan.app.utils.getBitmapFromBytes
import plus.vplan.app.utils.getDataFromPdf

abstract class AttachedFile {
    abstract val platformFile: PlatformFile
    abstract val name: String
    abstract val bitmap: ImageBitmap?
    abstract val size: Long

    companion object {
        suspend fun fromFile(file: PlatformFile): AttachedFile {
            val bytes = file.readBytes()
            val size = file.getSize() ?: 0L
            val name = file.name
            return when (file.extension) {
                "pdf" -> {
                    val data = getDataFromPdf(bytes)
                    Document(
                        platformFile = file,
                        bitmap = data?.firstPage,
                        size = size,
                        name = name,
                        pages = data?.pages ?: 0
                    )
                }
                "jpg", "jpeg", "png" -> {
                    val bitmap = getBitmapFromBytes(bytes)
                    Image(
                        platformFile = file,
                        bitmap = bitmap,
                        size = size,
                        width = bitmap?.width ?: 0,
                        height = bitmap?.height ?: 0,
                        name = name
                    )
                }
                else -> {
                    val bitmap = getBitmapFromBytes(bytes)
                    Other(
                        platformFile = file,
                        bitmap = bitmap,
                        size = size,
                        name = name
                    )
                }
            }
        }
    }

    data class Document(
        override val platformFile: PlatformFile,
        override val bitmap: ImageBitmap?,
        override val size: Long,
        override val name: String,
        val pages: Int
    ) : AttachedFile() {
        override fun copyBase(platformFile: PlatformFile, bitmap: ImageBitmap?, size: Long, name: String): Document {
            return copy(platformFile = platformFile, bitmap = bitmap, size = size, name = name, pages = pages)
        }
    }

    data class Image(
        override val platformFile: PlatformFile,
        override val bitmap: ImageBitmap?,
        override val size: Long,
        override val name: String,
        val width: Int,
        val height: Int
    ) : AttachedFile() {
        override fun copyBase(platformFile: PlatformFile, bitmap: ImageBitmap?, size: Long, name: String): Image {
            return copy(platformFile = platformFile, bitmap = bitmap, size = size, name = name, width = width, height = height)
        }
    }

    data class Other(
        override val platformFile: PlatformFile,
        override val bitmap: ImageBitmap?,
        override val size: Long,
        override val name: String
    ) : AttachedFile() {
        override fun copyBase(platformFile: PlatformFile, bitmap: ImageBitmap?, size: Long, name: String): Other {
            return copy(platformFile = platformFile, bitmap = bitmap, size = size, name = name)
        }

        constructor(platformFile: PlatformFile) : this(platformFile, null, platformFile.getSize() ?: 0L, platformFile.name)
    }

    abstract fun copyBase(
        platformFile: PlatformFile = this.platformFile,
        bitmap: ImageBitmap? = this.bitmap,
        size: Long = this.size,
        name: String = this.name
    ): AttachedFile
}