package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface CourseRepository : AliasedItemRepository<CourseDbDto, Course> {
    fun getByGroup(groupId: Uuid): Flow<List<Course>>
    fun getBySchool(schoolId: Uuid): Flow<List<Course>>

    suspend fun deleteById(id: Uuid)
    suspend fun deleteById(ids: List<Uuid>)
}

data class CourseDbDto(
    val name: String,
    val groups: List<Uuid>,
    val teacher: Uuid?,
    val aliases: List<Alias>
)