package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSp24SchoolDetails
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.sp24Service
import plus.vplan.app.utils.sendAll

class SchoolRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : SchoolRepository {
    override suspend fun fetchAllOnline(): Response<List<OnlineSchool>> {
        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school")
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<List<OnlineSchoolResponse>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(data.map { it.toModel() })
        }
    }

    override suspend fun getWithCachingById(id: Int): Response<Flow<School?>> {
        val cached = vppDatabase.schoolDao.findById(id).map { it?.toModel() }
        if (cached.first() != null) return Response.Success(cached)
        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school/$id")
            if (!response.status.isSuccess()) return response.toResponse()
            val data = ResponseDataWrapper.fromJson<SchoolItemResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            vppDatabase.schoolDao.upsertSchool(
                DbSchool(
                    id = id,
                    name = data.name,
                    cachedAt = Clock.System.now()
                )
            )
            return Response.Success(getById(id).filterIsInstance())
        }
    }

    override fun getById(id: Int): Flow<CacheState<School>> = channelFlow {
        vppDatabase.schoolDao.findById(id).collectLatest {
            if (it != null) return@collectLatest send(CacheState.Done(it.toModel()))
            val onlineResponse = getWithCachingById(id)
            if (onlineResponse is Response.Success) return@collectLatest sendAll(onlineResponse.data.map { if (it != null) CacheState.Done(it) else CacheState.NotExisting(id.toString()) })
            if (onlineResponse is Response.Error.OnlineError.NotFound) return@collectLatest send(CacheState.NotExisting(id.toString()))
            if (onlineResponse is Response.Error) return@collectLatest send(CacheState.Error(id.toString(), onlineResponse))
        }
    }

    override suspend fun getAll(): Flow<List<School>> {
        return vppDatabase.schoolDao.getAll().map { results -> results.map { it.toModel() } }
    }

    override suspend fun getIdFromSp24Id(sp24Id: Int): Response<Int> {
        val indiwareSchool = vppDatabase.schoolDao
            .getAll().first()
            .map { it.toModel() }
            .filterIsInstance<School.IndiwareSchool>()
            .firstOrNull { it.sp24Id == sp24Id.toString() }
        if (indiwareSchool != null) return Response.Success(indiwareSchool.id)
        return saveRequest {
            val response = httpClient.get("${sp24Service.url}/school/sp24/$sp24Id")
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<Int>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(data)
        }
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
        vppDatabase.schoolDao.upsertSp24SchoolDetails(
            DbSp24SchoolDetails(
                schoolId = school.id,
                sp24SchoolId = sp24Id.toString(),
                username = username,
                password = password,
                daysPerWeek = daysPerWeek,
                studentsHaveFullAccess = studentsHaveFullAccess,
                downloadMode = downloadMode,
                credentialsValid = true
            )
        )
    }

    override suspend fun updateSp24Access(school: School, username: String, password: String) {
        vppDatabase.schoolDao.updateIndiwareSchoolDetails(school.id, username, password)
    }

    override suspend fun setIndiwareAccessValidState(school: School, valid: Boolean) {
        vppDatabase.schoolDao.setIndiwareAccessValidState(school.id, valid)
    }
}

@Serializable
private data class OnlineSchoolResponse(
    @SerialName("school_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("sp24_id") val sp24Id: Int? = null
) {
    fun toModel(): OnlineSchool {
        return OnlineSchool(
            id = id,
            name = name,
            sp24Id = sp24Id
        )
    }
}

@Serializable
private data class SchoolItemResponse(
    @SerialName("school_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String?,
    @SerialName("coordinates") val coordinates: String?,
    @SerialName("sp24_id") val sp24Id: String?
)