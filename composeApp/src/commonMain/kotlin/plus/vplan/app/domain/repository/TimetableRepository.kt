@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TimetableRepository {
    suspend fun insertNewTimetable(schoolId: Int, lessons: List<Lesson.TimetableLesson>)
    suspend fun deleteAllTimetables()
    suspend fun deleteTimetableByVersion(schoolId: Int, version: Int)

    suspend fun getTimetableForSchool(schoolId: Int, versionString: String): List<Lesson.TimetableLesson>
    fun getById(id: Uuid): Flow<Lesson.TimetableLesson?>
    suspend fun getForSchool(schoolId: Int, weekIndex: Int, dayOfWeek: DayOfWeek, version: String): Set<Uuid>
    suspend fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek, version: String): Set<Uuid>

    suspend fun dropCacheForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, timetableLessonIds: List<Uuid>)
}