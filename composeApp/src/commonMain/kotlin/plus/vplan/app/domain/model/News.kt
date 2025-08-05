package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import kotlin.uuid.Uuid

data class News(
    override val id: Int,
    val title: String,
    val content: String,
    val date: Instant,
    val versionFrom: Int?,
    val versionTo: Int?,
    val dateFrom: Instant,
    val dateTo: Instant,
    val schoolIds: List<Uuid>,
    val author: String,
    val isRead: Boolean
) : Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()

    val schools by lazy { if (schoolIds.isEmpty()) flowOf(emptyList()) else combine(schoolIds.map { App.schoolSource.getById(it) }) { it.toList() } }
}