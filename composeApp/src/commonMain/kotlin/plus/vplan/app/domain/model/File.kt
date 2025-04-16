package plus.vplan.app.domain.model

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

expect fun openFile(file: File)

data class File(
    val name: String,
    val id: Int,
    val size: Long,
    val isOfflineReady: Boolean,
    val getBitmap: suspend () -> ImageBitmap?,
    val cachedAt: Instant
) : Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    var preview: ImageBitmap? = null
        private set

    suspend fun getPreview(): ImageBitmap? {
        if (preview != null) return preview
        getBitmap()?.let { preview = it }
        return preview
    }
}