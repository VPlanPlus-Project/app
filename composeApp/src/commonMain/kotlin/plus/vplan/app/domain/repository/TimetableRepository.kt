package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.domain.model.Lesson
import kotlin.uuid.Uuid

interface TimetableRepository {
    suspend fun insertNewTimetable(schoolId: Int, lessons: List<Lesson.TimetableLesson>)
    suspend fun deleteAllTimetables()
    suspend fun deleteTimetableByVersion(schoolId: Int, version: Int)

    fun getTimetableForSchool(schoolId: Int): Flow<List<Lesson.TimetableLesson>>
    fun getById(id: Uuid): Flow<Lesson.TimetableLesson?>
    fun getForSchool(schoolId: Int, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>>
}