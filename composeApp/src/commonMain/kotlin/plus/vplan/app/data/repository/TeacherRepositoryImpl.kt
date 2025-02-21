package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository

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

    override fun getById(teacherId: Int): Flow<CacheState<Teacher>> {
        return vppDatabase.teacherDao.getById(teacherId).map { it?.toModel()?.let { model -> CacheState.Done(model) } ?: CacheState.NotExisting(teacherId.toString()) }
    }

    override suspend fun getByIdWithCaching(id: Int, school: School): Response<Flow<Teacher?>> {
        val cached = vppDatabase.teacherDao.getById(id).map { it?.toModel() }
        if (cached.first() != null) return Response.Success(cached)
        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school/${school.id}/teacher/$id") {
                school.getSchoolApiAccess()?.authentication(this) ?: Response.Error.Other("no auth")
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<SchoolItemTeachersResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            vppDatabase.teacherDao.upsertTeacher(
                DbTeacher(
                    id = data.id,
                    schoolId = school.id,
                    name = data.name,
                    cachedAt = Clock.System.now()
                )
            )
            return Response.Success(getById(id).filterIsInstance())
        }
    }
}

@Serializable
data class SchoolItemTeachersResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)