package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.data.source.database.model.database.DbProfileTimetableCache
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
    suspend fun replaceForSchool(
        schoolId: Int,
        lessons: List<DbTimetableLesson>,
        groups: List<DbTimetableGroupCrossover>,
        teachers: List<DbTimetableTeacherCrossover>,
        rooms: List<DbTimetableRoomCrossover>,
        profileIndex: List<DbProfileTimetableCache>
    ) {
        val oldLessons = getBySchool(schoolId).first().map { it.timetableLesson.id }
        deleteTimetableByIds(oldLessons)
        upsert(lessons, groups, teachers, rooms)
        upsert(profileIndex)
    }

    @Transaction
    suspend fun replaceIndex(index: List<DbProfileTimetableCache>) {
        index.map { it.profileId }.distinct().forEach { dropIndexForProfile(it) }
        upsert(index)
    }

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("SELECT timetable_lessons.id FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId AND timetable_lessons.week_id = :weekId AND timetable_lessons.day_of_week = :dayOfWeek")
    fun getBySchool(schoolId: Int, weekId: String, dayOfWeek: DayOfWeek): Flow<List<Uuid>>

    @Transaction
    @Query("SELECT DISTINCT weeks.id FROM timetable_lessons LEFT JOIN weeks ON weeks.id = timetable_lessons.week_id WHERE week_index <= :maxWeekIndex")
    suspend fun getWeekIds(maxWeekIndex: Int): List<String>

    @Transaction
    @Query("SELECT * FROM timetable_lessons WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedTimetableLesson?>

    @Transaction
    @Query("DELETE FROM timetable_lessons")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM timetable_lessons WHERE id IN (:ids)")
    suspend fun deleteTimetableByIds(ids: List<Uuid>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM profile_timetable_cache LEFT JOIN timetable_lessons ON timetable_lessons.id = profile_timetable_cache.timetable_lesson_id WHERE profile_id = :profileId AND timetable_lessons.week_id = :weekId AND timetable_lessons.day_of_week = :dayOfWeek")
    fun getLessonsForProfile(profileId: Uuid, weekId: String, dayOfWeek: DayOfWeek): Flow<List<DbProfileTimetableCache>>

    @Query("DELETE FROM profile_timetable_cache WHERE profile_id = :profileId")
    suspend fun dropIndexForProfile(profileId: Uuid)

    @Upsert
    suspend fun upsert(entries: List<DbProfileTimetableCache>)
}