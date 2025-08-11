package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomAlias
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.RoomDbDto
import plus.vplan.app.domain.repository.RoomRepository
import kotlin.uuid.Uuid

class RoomRepositoryImpl(
    private val vppDatabase: VppDatabase
) : RoomRepository {
    override fun getBySchool(schoolId: Uuid): Flow<List<Room>> {
        return vppDatabase.roomDao.getBySchool(schoolId).map { result -> result.map { it.toModel() } }
    }

    override suspend fun upsert(item: RoomDbDto): Uuid {
        val roomId = resolveAliasesToLocalId(item.aliases) ?: Uuid.random()
        vppDatabase.roomDao.upsertRoom(
            room = DbRoom(
                id = roomId,
                name = item.name,
                cachedAt = Clock.System.now(),
                schoolId = item.schoolId
            ),
            aliases = item.aliases.map {
                DbRoomAlias.fromAlias(it, roomId)
            }
        )
        return roomId
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.roomDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }

    override fun getByLocalId(id: Uuid): Flow<Room?> {
        return vppDatabase.roomDao.findById(id).map { embeddedRoom ->
            embeddedRoom?.toModel()
        }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.schoolDao.getAll().map { it.map { school -> school.school.id } }
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