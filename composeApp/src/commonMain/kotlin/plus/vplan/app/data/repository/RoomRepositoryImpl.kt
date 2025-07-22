package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
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
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.network.isResponseFromBackend
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.utils.sendAll

class RoomRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : RoomRepository {
    override fun getBySchool(schoolId: Int): Flow<List<Room>> {
        return vppDatabase.roomDao.getBySchool(schoolId).map { result -> result.map { it.toModel() } }
    }

    override suspend fun getBySchoolWithCaching(school: School, forceReload: Boolean): Response<Flow<List<Room>>> {
        val flow = getBySchool(school.id)
        if (flow.first().isNotEmpty() && !forceReload) return Response.Success(flow)

        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(api).apply {
                        appendPathSegments("api", "v2.2", "school", school.id.toString(), "room")
                    }.build(),
                )
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
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Room>> {
        val roomFlow = vppDatabase.roomDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                sendAll(roomFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
                if (hadData) return@channelFlow
            }
            send(CacheState.Loading(id.toString()))

            safeRequest(onError = { send(CacheState.Error(id.toString(), it)) }) {
                val existing = vppDatabase.roomDao.getById(id).first()
                var schoolApiAccess: SchoolApiAccess? = null
                if (existing != null) {
                    schoolApiAccess = existing.schoolId.let { vppDatabase.schoolDao.findById(it).first()?.toModel()?.getSchoolApiAccess() }
                }

                if (schoolApiAccess == null) {
                    val accessResponse = httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "room", id.toString())
                        }.build())
                    }
                    if (accessResponse.status == HttpStatusCode.NotFound && accessResponse.isResponseFromBackend()) {
                        vppDatabase.roomDao.deleteById(listOf(id))
                        return@channelFlow send(CacheState.NotExisting(id.toString()))
                    }

                    if (!accessResponse.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), accessResponse.toErrorResponse<Room>()))
                    val accessData = ResponseDataWrapper.fromJson<RoomUnauthenticatedResponse>(accessResponse.bodyAsText())
                        ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(accessResponse.bodyAsText())))

                    schoolApiAccess = vppDatabase.schoolDao.findById(accessData.schoolId).first()?.toModel().let {
                        if (it is School.IndiwareSchool && !it.credentialsValid) return@channelFlow send(CacheState.Error(id.toString(), Response.Error.Other("no school for room $id")))
                        it?.getSchoolApiAccess()
                    }
                }
                if (schoolApiAccess == null) {
                    Logger.i { "No school to update room $id" }
                    vppDatabase.roomDao.deleteById(listOf(id))
                    trySend(CacheState.NotExisting(id.toString()))
                    return@channelFlow
                }

                val response = httpClient.get {
                    url(URLBuilder(api).apply {
                        appendPathSegments("api", "v2.2", "room", id.toString())
                    }.build())
                    schoolApiAccess.authentication(this)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse<Room>()))
                val data = ResponseDataWrapper.fromJson<RoomItemResponse>(response.bodyAsText())
                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                vppDatabase.roomDao.upsert(
                    DbRoom(
                        id = data.id,
                        schoolId = data.school.id,
                        name = data.name,
                        cachedAt = Clock.System.now()
                    )
                )

                return@channelFlow sendAll(getById(id, false))
            }
        }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.roomDao.getAll()
    }
}

@Serializable
private data class SchoolItemRoomsResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)

@Serializable
private data class RoomUnauthenticatedResponse(
    @SerialName("school_id") val schoolId: Int
)

@Serializable
private data class RoomItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("school") val school: School
) {
    @Serializable
    data class School(
        @SerialName("id") val id: Int
    )
}