package plus.vplan.app.domain.model

import androidx.compose.ui.graphics.ImageBitmap
import plus.vplan.app.domain.cache.Item

data class File(
    val name: String,
    val id: Int,
    val size: Long,
    val isOfflineReady: Boolean,
    val getBitmap: suspend () -> ImageBitmap?
) : Item {
    override fun getEntityId(): String = this.id.toString()

    var preview: ImageBitmap? = null
        private set

    suspend fun getPreview(): ImageBitmap? {
        if (preview != null) return preview
        getBitmap()?.let { preview = it }
        return preview
    }
}