package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.network.isResponseFromBackend
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.utils.sendAll

class TeacherRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : TeacherRepository {
    override fun getBySchool(schoolId: Int): Flow<List<Teacher>> {
        return vppDatabase.teacherDao.getBySchool(schoolId).map { result -> result.map { it.toModel() } }
    }

    override suspend fun getBySchoolWithCaching(school: School): Response<Flow<List<Teacher>>> {
        val flow = getBySchool(school.id)
        if (flow.first().isNotEmpty()) return Response.Success(flow)
        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school/${school.id}/teacher") {
                school.getSchoolApiAccess()?.authentication(this) ?: Response.Error.Other("no auth")
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<List<SchoolItemTeachersResponse>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            data.forEach { teacher ->
                vppDatabase.teacherDao.upsertTeacher(
                    DbTeacher(
                        id = teacher.id,
                        schoolId = school.id,
                        name = teacher.name,
                        cachedAt = Clock.System.now()
                    )
                )
            }
            return Response.Success(getBySchool(school.id))
        }
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Teacher>> {
        val teacherFlow = vppDatabase.teacherDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                sendAll(teacherFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
                if (hadData) return@channelFlow
            }
            send(CacheState.Loading(id.toString()))

            val accessResponse = httpClient.get("${api.url}/api/v2.2/teacher/$id")
            if (accessResponse.status == HttpStatusCode.NotFound && accessResponse.isResponseFromBackend()) {
                vppDatabase.teacherDao.deleteById(listOf(id))
                return@channelFlow send(CacheState.NotExisting(id.toString()))
            }
            if (!accessResponse.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), accessResponse.toErrorResponse<Teacher>()))
            val accessData = ResponseDataWrapper.fromJson<TeacherUnauthenticatedResponse>(accessResponse.bodyAsText())
                ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(accessResponse.bodyAsText())))

            val school = vppDatabase.schoolDao.findById(accessData.schoolId).first()?.toModel()
                .let {
                    if (it is School.IndiwareSchool && !it.credentialsValid) return@channelFlow send(CacheState.Error(id.toString(), Response.Error.Other("no school for teacher $id")))
                    if (it?.getSchoolApiAccess() == null) return@channelFlow send(CacheState.Error(id.toString(), Response.Error.Other("no school for teacher $id")))
                    it.getSchoolApiAccess()!!
                }

            val response = httpClient.get("${api.url}/api/v2.2/teacher/$id") {
                school.authentication(this)
            }
            if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse<Teacher>()))
            val data = ResponseDataWrapper.fromJson<TeacherItemResponse>(response.bodyAsText())
                ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

            vppDatabase.teacherDao.upsertTeacher(
                DbTeacher(
                    id = data.id,
                    schoolId = data.school.id,
                    name = data.name,
                    cachedAt = Clock.System.now()
                )
            )
            return@channelFlow sendAll(getById(id, false))
        }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.teacherDao.getAll()
    }
}

@Serializable
data class SchoolItemTeachersResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)

@Serializable
private data class TeacherUnauthenticatedResponse(
    @SerialName("school_id") val schoolId: Int
)

@Serializable
private data class TeacherItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("school") val school: School
) {
    @Serializable
    data class School(
        @SerialName("id") val id: Int
    )
}