package plus.vplan.app.domain.repository

import plus.vplan.app.domain.data.Response

interface SchoolRepository {
    suspend fun fetchAllOnline(): Response<List<OnlineSchool>>
}

data class OnlineSchool(
    val id: Int,
    val name: String,
    val sp24Id: Int?
)