package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import kotlin.uuid.Uuid

interface SubstitutionPlanRepository {
    suspend fun deleteAllSubstitutionPlans()

    suspend fun upsertLessons(
        schoolId: Uuid,
        date: LocalDate,
        lessons: List<Lesson.SubstitutionPlanLesson>,
        version: Int,
    )
    fun getCurrentVersion(): Flow<Int>

    suspend fun replaceLessonIndex(profileId: Uuid, lessonIds: Set<Uuid>)

    suspend fun getSubstitutionPlanBySchool(schoolId: Uuid, date: LocalDate): Flow<List<Lesson.SubstitutionPlanLesson>>
    suspend fun getForProfile(profile: Profile, date: LocalDate, version: Int?): Flow<List<Lesson.SubstitutionPlanLesson>>
    suspend fun getAll(): Set<Uuid>
    fun getSubstitutionPlanBySchool(schoolId: Uuid, version: Int): Flow<Set<Lesson.SubstitutionPlanLesson>>
    fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?>
}