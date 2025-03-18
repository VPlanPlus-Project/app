package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Item

data class News(
    val id: Int,
    val title: String,
    val content: String,
    val date: Instant,
    val versionFrom: Int?,
    val versionTo: Int?,
    val dateFrom: Instant?,
    val dateTo: Instant?,
    val schoolIds: List<Int>,
    val author: String
) : Item {
    override fun getEntityId(): String = this.id.toString()
}