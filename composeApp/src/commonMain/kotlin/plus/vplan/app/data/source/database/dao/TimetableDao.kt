package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.data.source.database.model.embedded.EmbeddedTimetableLesson
import kotlin.uuid.Uuid

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
    @Query("SELECT timetable_lessons.id FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId AND timetable_lessons.version = :version AND timetable_lessons.week_id = :weekId AND timetable_lessons.day_of_week = :dayOfWeek")
    fun getTimetableLessons(schoolId: Int, version: String, weekId: String, dayOfWeek: DayOfWeek): Flow<List<Uuid>>

    @Transaction
    @Query("SELECT DISTINCT weeks.id FROM timetable_lessons LEFT JOIN weeks ON weeks.id = timetable_lessons.week_id WHERE week_index <= :maxWeekIndex AND timetable_lessons.version = :version")
    suspend fun getWeekIds(version: String, maxWeekIndex: Int): List<String>

    @Transaction
    @Query("SELECT * FROM timetable_lessons WHERE id = :id AND version = :version")
    fun getById(id: String, version: String): Flow<EmbeddedTimetableLesson?>

    @Transaction
    @Query("SELECT * FROM timetable_lessons WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedTimetableLesson?>

    @Transaction
    @Query("DELETE FROM timetable_lessons")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM timetable_lessons WHERE version = :version")
    suspend fun deleteTimetableByVersion(version: String)
}