package plus.vplan.app.domain.model

import androidx.compose.runtime.Stable
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import kotlin.time.Instant

@Stable
data class News(
    override val id: Int,
    val title: String,
    val content: String,
    val date: Instant,
    val versionFrom: Int?,
    val versionTo: Int?,
    val dateFrom: Instant,
    val dateTo: Instant,
    val schools: List<School>,
    val author: String,
    val isRead: Boolean
) : Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
}