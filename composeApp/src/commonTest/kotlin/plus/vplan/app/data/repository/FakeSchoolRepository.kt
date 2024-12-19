package plus.vplan.app.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.domain.repository.SchoolRepository

class FakeSchoolRepository(
    private val simulateNetworkOutage: Boolean = false,
    private val simulateOnlineSchools: List<OnlineSchool> = emptyList()
) : SchoolRepository {

    override suspend fun fetchAllOnline(): Response<List<OnlineSchool>> {
        if (simulateNetworkOutage) return Response.Error.OnlineError.ConnectionError
        delay(200)
        return Response.Success(simulateOnlineSchools)
    }

    override suspend fun getWithCachingById(id: Int): Response<Flow<School?>> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: Int): Flow<School?> {
        TODO("Not yet implemented")
    }

    override suspend fun getIdFromSp24Id(sp24Id: Int): Response<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun setSp24Info(
        school: School,
        sp24Id: Int,
        username: String,
        password: String,
        daysPerWeek: Int,
        studentsHaveFullAccess: Boolean,
        downloadMode: School.IndiwareSchool.SchoolDownloadMode
    ) {
        TODO("Not yet implemented")
    }
}