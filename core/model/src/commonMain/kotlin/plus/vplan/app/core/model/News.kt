package plus.vplan.app.core.model

import kotlin.time.Instant

data class News(
    override val id: Int,
    val title: String,
    val content: String,
    val date: Instant,
    val versionFrom: Int?,
    val versionTo: Int?,
    val dateFrom: Instant,
    val dateTo: Instant,
    val schoolIds: School.Ids,
    val author: String,
    val isRead: Boolean
) : Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
}