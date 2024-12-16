package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School

interface SchoolRepository {
    suspend fun fetchAllOnline(): Response<List<OnlineSchool>>
    suspend fun getWithCachingById(id: Int): Response<Flow<School?>>
    suspend fun getById(id: Int): Flow<School?>

    suspend fun getIdFromSp24Id(sp24Id: Int): Response<Int>
}

data class OnlineSchool(
    val id: Int,
    val name: String,
    val sp24Id: Int?
)