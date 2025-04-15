package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

data class News(
    val id: Int,
    val title: String,
    val content: String,
    val date: Instant,
    val versionFrom: Int?,
    val versionTo: Int?,
    val dateFrom: Instant,
    val dateTo: Instant,
    val schoolIds: List<Int>,
    val author: String,
    val isRead: Boolean
) : Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    val schools by lazy { if (schoolIds.isEmpty()) flowOf(emptyList()) else combine(schoolIds.map { App.schoolSource.getById(it) }) { it.toList() } }
}