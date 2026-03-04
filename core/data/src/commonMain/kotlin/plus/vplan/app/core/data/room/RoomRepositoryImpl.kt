@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import plus.vplan.app.core.database.dao.RoomDao
import plus.vplan.app.core.database.dao.SchoolDao
import plus.vplan.app.core.database.model.database.DbRoom
import plus.vplan.app.core.database.model.database.DbRoomAlias
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

class RoomRepositoryImpl(
    private val roomDao: RoomDao,
    private val schoolDao: SchoolDao,
    private val applicationScope: CoroutineScope,
): RoomRepository {

    private val bySchoolCache = mutableMapOf<Uuid, Flow<List<Room>>>()
    private val allCache: Flow<List<Room>> by lazy {
        roomDao.getAll()
            .map { items -> items.map { it.toModel() } }
            .distinctUntilChanged()
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
    }

    override fun getBySchool(school: School): Flow<List<Room>> {
        return combine(school.aliases.map { alias -> schoolDao.getIdByAlias(alias.value, alias.provider, alias.version) }) { it.firstNotNullOf { it } }
            .flatMapLatest { schoolId ->
                bySchoolCache.getOrPut(schoolId) {
                    roomDao.getBySchool(schoolId)
                        .map { items -> items.map { it.toModel() } }
                        .distinctUntilChanged()
                        .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
                }
            }
    }

    override fun getAll(): Flow<List<Room>> = allCache

    override fun getByIds(identifiers: Set<Alias>): Flow<Room?> {
        if (identifiers.isEmpty()) throw IllegalArgumentException("Identifiers cannot be empty")

        return findLocalIdByIdentifier(identifiers)
            .flatMapLatest { id ->
                id?.let { roomDao.findById(id).map { it?.toModel() }.distinctUntilChanged() } ?: flowOf(null)
            }
    }

    private fun findLocalIdByIdentifier(identifiers: Set<Alias>): Flow<Uuid?> {
        return flow {
            emit(
                identifiers.firstNotNullOfOrNull { alias ->
                    roomDao.getIdByAlias(
                        value = alias.value,
                        provider = alias.provider,
                        version = alias.version
                    )
                }
            )
        }
    }

    override suspend fun save(room: Room): Room {
        val id = findLocalIdByIdentifier(room.aliases.toSet()).first() ?: Uuid.random()
        roomDao.upsertRoom(
            room = DbRoom(
                id = id,
                schoolId = room.school.id,
                name = room.name,
                cachedAt = room.cachedAt
            ),
            aliases = room.aliases.map { DbRoomAlias.fromAlias(it, id) }
        )

        return roomDao.findById(id).first()!!.toModel()
    }
}