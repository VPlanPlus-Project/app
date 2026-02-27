package plus.vplan.app.network.vpp.news

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School
import kotlin.time.Instant

interface NewsApi {
    suspend fun getBySchool(school: School.AppSchool): List<NewsDto>
}

data class NewsDto(
    val id: Int,
    val title: String,
    val content: String,
    val date: Instant,
    val versionFrom: Int?,
    val versionTo: Int?,
    val dateFrom: Instant,
    val dateTo: Instant,
    val schools: Set<Alias>,
    val author: String,
    val isRead: Boolean
)