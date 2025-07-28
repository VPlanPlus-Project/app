package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School

interface SchoolRepository: WebEntityRepository<School> {
    suspend fun fetchAllOnline(): Response<List<OnlineSchool>>
    suspend fun getAll(): Flow<List<School>>

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

    suspend fun setIndiwareAccessValidState(school: School, valid: Boolean)

    suspend fun deleteSchool(schoolId: Int)
}

data class OnlineSchool(
    val id: Int,
    val name: String,
    val sp24Id: Int?
)