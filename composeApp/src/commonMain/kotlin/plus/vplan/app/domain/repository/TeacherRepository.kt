package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface TeacherRepository: AliasedItemRepository<TeacherDbDto, Teacher> {
    fun getBySchool(schoolId: Uuid): Flow<List<Teacher>>
}

data class TeacherDbDto(
    val schoolId: Uuid,
    val name: String,
    val aliases: List<Alias>
)