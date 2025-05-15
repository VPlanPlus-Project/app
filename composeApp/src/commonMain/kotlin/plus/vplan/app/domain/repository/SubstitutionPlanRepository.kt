@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface SubstitutionPlanRepository {
    suspend fun deleteAllSubstitutionPlans()

    suspend fun upsertLessons(
        schoolId: Int,
        date: LocalDate,
        lessons: List<Lesson.SubstitutionPlanLesson>,
        profiles: List<Profile.StudentProfile>
    )

    suspend fun getSubstitutionPlanBySchool(schoolId: Int, date: LocalDate): Flow<Set<Uuid>>
    suspend fun getForProfile(profile: Profile, date: LocalDate): Flow<Set<Uuid>>
    suspend fun getAll(): Set<Uuid>
    fun getSubstitutionPlanBySchool(schoolId: Int): Flow<Set<Lesson.SubstitutionPlanLesson>>
    fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?>
}