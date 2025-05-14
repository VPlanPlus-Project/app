@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface SubstitutionPlanRepository {
    suspend fun insertNewSubstitutionPlan(schoolId: Int, lessons: List<Lesson.SubstitutionPlanLesson>, beforeVersionBump: suspend (newVersion: String) -> Unit = {})
    suspend fun deleteAllSubstitutionPlans()
    suspend fun deleteSubstitutionPlansByVersion(schoolId: Int, version: String)

    /**
     * @param versionString The string of the version key, contains school id and version int, e.g.: <school_id>_<version> ("7_20")
     */
    suspend fun getSubstitutionPlanBySchool(schoolId: Int, date: LocalDate, versionString: String): Set<Uuid>
    suspend fun getForProfile(profile: Profile, date: LocalDate, versionString: String): Set<Uuid>
    fun getSubstitutionPlanBySchool(schoolId: Int, versionString: String?): Flow<Set<Lesson.SubstitutionPlanLesson>>
    fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?>

    suspend fun dropCacheForProfile(profileId: Uuid, version: String? = null)
    suspend fun createCacheForProfile(profileId: Uuid, substitutionLessonIds: List<Uuid>)
}