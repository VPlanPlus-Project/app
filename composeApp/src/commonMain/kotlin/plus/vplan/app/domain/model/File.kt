package plus.vplan.app.domain.model

import androidx.compose.ui.graphics.ImageBitmap
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.Item
import kotlin.time.Instant

expect fun openFile(file: File)

data class File(
    override val id: Int,
    val name: String,
    val size: Long,
    val isOfflineReady: Boolean,
    val getBitmap: suspend () -> ImageBitmap?,
    val cachedAt: Instant
) : Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()

    var preview: ImageBitmap? = null
        private set

    suspend fun getPreview(): ImageBitmap? {
        if (preview != null) return preview
        getBitmap()?.let { preview = it }
        return preview
    }
}
