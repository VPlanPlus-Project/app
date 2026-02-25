package plus.vplan.app.core.data.school

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School

interface SchoolRepository {
    suspend fun getAll(): List<School>
    fun getByIds(identifier: Set<Alias>): Flow<School?>

    fun getById(identifier: Alias): Flow<School?> = getByIds(setOf(identifier))

    suspend fun save(school: School.AppSchool)
}