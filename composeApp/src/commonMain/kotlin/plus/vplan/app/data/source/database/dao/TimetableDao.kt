package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroup
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoom
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacher
import plus.vplan.app.data.source.database.model.embedded.EmbeddedTimetableLesson

@Dao
interface TimetableDao {

    @Upsert
    suspend fun upsert(timetable: DbTimetableLesson)

    @Upsert
    suspend fun upsert(crossover: DbTimetableGroup)

    @Upsert
    suspend fun upsert(crossover: DbTimetableTeacher)

    @Upsert
    suspend fun upsert(crossover: DbTimetableRoom)

    @Transaction
    suspend fun upsert(
        lessons: List<DbTimetableLesson>,
        groupCrossovers: List<DbTimetableGroup>,
        teacherCrossovers: List<DbTimetableTeacher>,
        roomCrossovers: List<DbTimetableRoom>,
    ) {
        lessons.forEach { upsert(it) }
        groupCrossovers.forEach { upsert(it) }
        teacherCrossovers.forEach { upsert(it) }
        roomCrossovers.forEach { upsert(it) }
    }

    @Transaction
    @Query("SELECT * FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id WHERE school_groups.school_id = :schoolId AND timetable_lessons.version = :version")
    fun getTimetableLessons(schoolId: Int, version: Int): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("DELETE FROM timetable_lessons")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM timetable_lessons WHERE version = :version")
    suspend fun deleteTimetableByVersion(version: Int)
}