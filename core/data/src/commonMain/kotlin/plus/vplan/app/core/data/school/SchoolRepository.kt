package plus.vplan.app.core.data.school

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School

interface SchoolRepository {
    suspend fun getAll(): List<School>
    fun getByIds(identifier: Set<Alias>, forceReload: Boolean = false): Flow<School?>

    fun getById(identifier: Alias, forceReload: Boolean = false): Flow<School?> = getByIds(setOf(identifier), forceReload)

    suspend fun save(school: School.AppSchool): School.AppSchool
}