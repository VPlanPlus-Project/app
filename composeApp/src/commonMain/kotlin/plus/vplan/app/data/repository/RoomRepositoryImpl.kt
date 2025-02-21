package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.RoomRepository

class RoomRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : RoomRepository {
    override fun getBySchool(schoolId: Int): Flow<List<Room>> {
        return vppDatabase.roomDao.getBySchool(schoolId).map { result -> result.map { it.toModel() } }
    }

    override suspend fun getBySchoolWithCaching(school: School): Response<Flow<List<Room>>> {
        val flow = getBySchool(school.id)
        if (flow.first().isNotEmpty()) return Response.Success(flow)

        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school/${school.id}/room") {
                school.getSchoolApiAccess()?.authentication(this) ?: Response.Error.Other("no auth")
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<List<SchoolItemRoomsResponse>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            data.forEach { room ->
                vppDatabase.roomDao.upsert(
                    DbRoom(
                        id = room.id,
                        schoolId = school.id,
                        name = room.name,
                        cachedAt = Clock.System.now()
                    )
                )
            }
            return Response.Success(getBySchool(school.id))
        }
    }

    override fun getById(id: Int): Flow<Room?> {
        return vppDatabase.roomDao.getById(id).map { it?.toModel() }
    }

    override suspend fun getByIdWithCaching(id: Int, school: School): Response<Flow<Room?>> {
        val cached = vppDatabase.roomDao.getById(id).map { it?.toModel() }
        if (cached.first() != null) return Response.Success(cached)

        return saveRequest {
            val response = httpClient.get("${api.url}/api/v2.2/school/${school.id}/room/$id") {
                school.getSchoolApiAccess()?.authentication(this) ?: Response.Error.Other("no auth")
            }
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<SchoolItemRoomsResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())
            vppDatabase.roomDao.upsert(
                DbRoom(
                    id = data.id,
                    schoolId = school.id,
                    name = data.name,
                    cachedAt = Clock.System.now()
                )
            )
            return Response.Success(getById(id))
        }
    }
}

@Serializable
private data class SchoolItemRoomsResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)