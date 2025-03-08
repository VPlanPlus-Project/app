package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Lesson
import kotlin.uuid.Uuid

interface SubstitutionPlanRepository {
    suspend fun insertNewSubstitutionPlan(schoolId: Int, lessons: List<Lesson.SubstitutionPlanLesson>)
    suspend fun deleteAllSubstitutionPlans()
    suspend fun deleteSubstitutionPlansByVersion(schoolId: Int, version: String)

    fun getSubstitutionPlanBySchool(schoolId: Int, date: LocalDate): Flow<Set<Uuid>>
    fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?>
}