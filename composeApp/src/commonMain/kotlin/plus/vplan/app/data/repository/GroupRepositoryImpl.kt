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
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchoolGroup
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository

class GroupRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : GroupRepository {
    override fun getBySchool(schoolId: Int): Flow<List<Group>> {
        return vppDatabase.groupDao.getBySchool(schoolId)
            .map { result -> result.map { it.toModel() } }
    }

    override suspend fun getBySchoolWithCaching(school: School): Response<Flow<List<Group>>> {
        val flow = getBySchool(school.id)
        if (flow.first().isNotEmpty()) return Response.Success(flow)

        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school/${school.id}/group") {
                school.getSchoolApiAccess().authentication(this)
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data =
                ResponseDataWrapper.fromJson<List<SchoolItemGroupsResponse>>(response.bodyAsText())
                    ?: return Response.Error.ParsingError(response.bodyAsText())
            data.forEach { group ->
                vppDatabase.groupDao.upsert(
                    DbGroup(
                        id = group.id,
                        name = group.name,
                        cachedAt = Clock.System.now()
                    ),
                    FKSchoolGroup(
                        schoolId = school.id,
                        groupId = group.id
                    )
                )
            }
            return Response.Success(getBySchool(school.id))
        }
    }

    override fun getById(id: Int): Flow<CacheState<Group>> {
        return vppDatabase.groupDao.getById(id).map { it?.toModel()?.let { model -> CacheState.Done(model) } ?: CacheState.NotExisting(id.toString()) }
    }

    override suspend fun getByIdWithCaching(id: Int, school: School): Response<Flow<Group?>> {
        val cached = vppDatabase.groupDao.getById(id).map { it?.toModel() }
        if (cached.first() != null) return Response.Success(cached)
        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/group/$id") {
                school.getSchoolApiAccess().authentication(this)
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<SchoolItemGroupsResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())
            vppDatabase.groupDao.upsert(
                DbGroup(
                    id = data.id,
                    name = data.name,
                    cachedAt = Clock.System.now()
                ),
                FKSchoolGroup(
                    schoolId = school.id,
                    groupId = data.id
                )
            )
            return Response.Success(getById(id).filterIsInstance())
        }
    }

}

@Serializable
private data class SchoolItemGroupsResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("users") val users: Long,
)