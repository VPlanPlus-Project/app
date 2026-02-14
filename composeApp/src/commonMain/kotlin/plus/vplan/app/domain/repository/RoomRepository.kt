package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface RoomRepository: AliasedItemRepository<RoomDbDto, Room> {
    fun getBySchool(schoolId: Uuid): Flow<List<Room>>
}

data class RoomDbDto(
    val name: String,
    val schoolId: Uuid,
    val aliases: List<Alias>
)