@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TimetableRepository {
    suspend fun deleteAllTimetables()

    suspend fun upsertLessons(
        schoolId: Int,
        lessons: List<Lesson.TimetableLesson>,
        profiles: List<Profile.StudentProfile>
    )

    suspend fun getTimetableForSchool(schoolId: Int): Flow<List<Lesson.TimetableLesson>>
    fun getById(id: Uuid): Flow<Lesson.TimetableLesson?>
    suspend fun getForSchool(schoolId: Int, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>>
    suspend fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>>

    suspend fun replaceLessonIndex(profileId: Uuid, lessonIds: Set<Uuid>)
}