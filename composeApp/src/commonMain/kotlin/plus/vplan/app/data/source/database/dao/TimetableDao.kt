package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.data.source.database.model.embedded.EmbeddedTimetableLesson

@Dao
interface TimetableDao {

    @Upsert
    suspend fun upsert(timetable: DbTimetableLesson)

    @Upsert
    suspend fun upsert(crossover: DbTimetableGroupCrossover)

    @Upsert
    suspend fun upsert(crossover: DbTimetableTeacherCrossover)

    @Upsert
    suspend fun upsert(crossover: DbTimetableRoomCrossover)

    @Transaction
    suspend fun upsert(
        lessons: List<DbTimetableLesson>,
        groupCrossovers: List<DbTimetableGroupCrossover>,
        teacherCrossovers: List<DbTimetableTeacherCrossover>,
        roomCrossovers: List<DbTimetableRoomCrossover>,
    ) {
        lessons.forEach { upsert(it) }
        groupCrossovers.forEach { upsert(it) }
        teacherCrossovers.forEach { upsert(it) }
        roomCrossovers.forEach { upsert(it) }
    }

    @Transaction
    @Query("SELECT * FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId AND timetable_lessons.version = :version")
    fun getTimetableLessons(schoolId: Int, version: String): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("SELECT * FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId AND timetable_lessons.version = :version AND timetable_lessons.week_id IN (:allowedWeekIds)")
    fun getTimetableLessons(schoolId: Int, version: String, allowedWeekIds: List<String>): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("SELECT * FROM timetable_lessons WHERE id = :id AND version = :version")
    fun getById(id: String, version: String): Flow<EmbeddedTimetableLesson?>

    @Transaction
    @Query("SELECT * FROM timetable_lessons WHERE id = :id")
    fun getById(id: String): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("DELETE FROM timetable_lessons")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM timetable_lessons WHERE version = :version")
    suspend fun deleteTimetableByVersion(version: String)
}