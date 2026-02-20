package plus.vplan.app.core.model

import kotlin.time.Instant

data class File(
    override val id: Int,
    val name: String,
    val size: Long,
    val isOfflineReady: Boolean,
    val cachedAt: Instant
) : Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
}
