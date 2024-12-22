package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Lesson

interface TimetableRepository {
    suspend fun insertNewTimetable(schoolId: Int, lessons: List<Lesson.TimetableLesson>)
    suspend fun deleteAllTimetables()
    suspend fun deleteTimetableByVersion(version: Int)

    fun getTimetableForSchool(schoolId: Int): Flow<List<Lesson.TimetableLesson>>
}