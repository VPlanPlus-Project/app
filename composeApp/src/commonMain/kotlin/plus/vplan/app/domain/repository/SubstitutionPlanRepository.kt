package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import kotlin.uuid.Uuid

interface SubstitutionPlanRepository {
    suspend fun insertNewSubstitutionPlan(schoolId: Int, lessons: List<Lesson.SubstitutionPlanLesson>)
    suspend fun deleteAllSubstitutionPlans()
    suspend fun deleteSubstitutionPlansByVersion(schoolId: Int, version: String)

    fun getSubstitutionPlanBySchool(schoolId: Int, date: LocalDate): Flow<Set<Uuid>>
    fun getSubstitutionPlanBySchool(schoolId: Int): Flow<Set<Lesson.SubstitutionPlanLesson>>
    fun getForProfile(profile: Profile, date: LocalDate): Flow<Set<Uuid>>
    fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?>

    suspend fun dropCacheForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, substitutionLessonIds: List<Uuid>)
}