package plus.vplan.app.core.data.room

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School

interface RoomRepository {
    fun getBySchool(school: School): Flow<List<Room>>
    fun getAll(): Flow<List<Room>>

    fun getById(identifier: Alias) =
        getByIds(setOf(identifier))

    fun getByIds(identifiers: Set<Alias>): Flow<Room?>

    suspend fun save(room: Room)
}