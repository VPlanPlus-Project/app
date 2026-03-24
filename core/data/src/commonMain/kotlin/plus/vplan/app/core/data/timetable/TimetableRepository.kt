package plus.vplan.app.core.data.timetable

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Timetable
import kotlin.uuid.Uuid

interface TimetableRepository {
    suspend fun deleteAllTimetables()

    suspend fun upsertLessons(
        timetableId: Uuid,
        lessons: List<Lesson.TimetableLesson>,
        profileMapping: Map<Profile, List<Lesson.TimetableLesson>>
    )

    fun getTimetableForSchool(schoolId: Uuid): Flow<List<Lesson.TimetableLesson>>
    fun getById(id: Uuid): Flow<Lesson.TimetableLesson?>
    fun getForSchool(schoolId: Uuid, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Lesson.TimetableLesson>>
    fun getForProfile(profile: Profile): Flow<Set<Lesson.TimetableLesson>>
    fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Lesson.TimetableLesson>>

    suspend fun upsertTimetable(timetable: Timetable)
    suspend fun getTimetableData(schoolId: Uuid, weekId: String): Flow<Timetable?>
    fun getTimetables(school: School.AppSchool): Flow<List<Timetable>>
}
