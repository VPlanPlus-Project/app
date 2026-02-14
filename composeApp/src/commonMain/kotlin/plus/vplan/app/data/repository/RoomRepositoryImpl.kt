@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomAlias
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Room
import plus.vplan.app.domain.repository.RoomDbDto
import plus.vplan.app.domain.repository.RoomRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
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
