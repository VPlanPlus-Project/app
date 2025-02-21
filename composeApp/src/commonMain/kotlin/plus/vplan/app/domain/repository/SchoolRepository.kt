package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School

interface SchoolRepository {
    suspend fun fetchAllOnline(): Response<List<OnlineSchool>>
    suspend fun getWithCachingById(id: Int): Response<Flow<School?>>
    fun getById(id: Int): Flow<CacheState<School>>
    suspend fun getAll(): Flow<List<School>>

    suspend fun getIdFromSp24Id(sp24Id: Int): Response<Int>

    suspend fun setSp24Info(
        school: School,
        sp24Id: Int,
        username: String,
        password: String,
        daysPerWeek: Int,
        studentsHaveFullAccess: Boolean,
        downloadMode: School.IndiwareSchool.SchoolDownloadMode
    )

    suspend fun updateSp24Access(
        school: School,
        username: String,
        password: String
    )
}

data class OnlineSchool(
    val id: Int,
    val name: String,
    val sp24Id: Int?
)