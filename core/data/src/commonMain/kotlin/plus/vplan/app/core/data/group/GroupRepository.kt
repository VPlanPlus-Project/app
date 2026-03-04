package plus.vplan.app.core.data.group

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

interface GroupRepository {
    fun getBySchool(school: School): Flow<List<Group>>
    fun getAll(): Flow<List<Group>>

    fun getById(identifier: Alias, forceUpdate: Boolean = false) =
        getByIds(setOf(identifier), forceUpdate)

    fun getByIds(identifiers: Set<Alias>, forceUpdate: Boolean = false): Flow<Group?>

    suspend fun save(group: Group)
}