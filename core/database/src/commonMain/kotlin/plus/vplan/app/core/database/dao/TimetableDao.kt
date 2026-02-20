package plus.vplan.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.core.database.model.database.DbProfileTimetableCache
import plus.vplan.app.core.database.model.database.DbTimetable
import plus.vplan.app.core.database.model.database.DbTimetableLesson
import plus.vplan.app.core.database.model.database.DbTimetableWeekLimitation
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.core.database.model.embedded.EmbeddedTimetableLesson
import kotlin.uuid.Uuid

@Dao
interface TimetableDao {

    @Upsert
    suspend fun upsert(timetable: DbTimetableLesson)

    @Upsert
    suspend fun upsert(timetable: DbTimetable)

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
    suspend fun replaceForTimetable(
        timetableId: Uuid,
        lessons: List<DbTimetableLesson>,
        groups: List<DbTimetableGroupCrossover>,
        teachers: List<DbTimetableTeacherCrossover>,
        rooms: List<DbTimetableRoomCrossover>,
        weekLimitations: List<DbTimetableWeekLimitation>
    ) {
        Logger.d { "Start replacing" }
        val oldLessons = getByTimetable(timetableId).first().map { it.timetableLesson.id }
        Logger.d { "Old lessons: ${oldLessons.size}x" }
        if (oldLessons.isNotEmpty()) deleteTimetableByIds(oldLessons)
        Logger.d { "Deleted old lessons" }
        upsert(lessons, groups, teachers, rooms)
        Logger.d { "Upserted new lessons" }
        upsertWeekLimitations(weekLimitations)
        Logger.d { "Upserted week limitations" }
    }

    @Transaction
    suspend fun replaceIndex(index: List<DbProfileTimetableCache>) {
        index.map { it.profileId }.distinct().forEach { dropIndexForProfile(it) }
        upsert(index)
    }

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id WHERE school_groups.school_id = :schoolId AND version = :version")
    fun getBySchool(schoolId: Uuid, version: Int): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM timetable_lessons WHERE timetable_id = :timetableId")
    fun getByTimetable(timetableId: Uuid): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("""
SELECT timetable_lessons.id
FROM timetable_lessons
         LEFT JOIN timetable_week_limitation ON timetable_lessons.id = timetable_week_limitation.timetable_lesson_id
         LEFT JOIN main.weeks w ON w.id = timetable_week_limitation.week_id
WHERE timetable_lessons.timetable_id = (SELECT timetables.id
                                        FROM timetables
                                                 LEFT JOIN weeks w ON w.id = timetables.week_id
                                        WHERE timetables.data_state = 'Yes'
                                          AND week_index <= :currentWeekIndex
                                          AND w.school_id = :schoolId
                                        ORDER BY week_index DESC
                                        LIMIT 1)
  AND (w.week_index = :currentWeekIndex OR w.week_index IS NULL)
  AND day_of_week = :dayOfWeek;
    """)
    fun getBySchool(schoolId: Uuid, currentWeekIndex: Int, dayOfWeek: DayOfWeek): Flow<List<Uuid>>

    @Transaction
    @Query("SELECT * FROM timetable_lessons WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedTimetableLesson?>

    @Transaction
    @Query("DELETE FROM timetable_lessons")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM timetable_lessons WHERE id IN (:ids)")
    suspend fun deleteTimetableByIdsUnsafe(ids: List<Uuid>)

    @Transaction
    suspend fun deleteTimetableByIds(ids: List<Uuid>) {
        ids.chunked(20).forEach { chunk ->
            deleteTimetableByIdsUnsafe(chunk)
        }
    }

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
WITH profile_school AS (SELECT school_id
                        FROM profiles
                        WHERE id = :profileId
                        LIMIT 1),
     latest_timetable AS (SELECT t.id
                          FROM timetables t
                                   JOIN weeks w_tt ON w_tt.id = t.week_id
                                   JOIN profile_school ps ON ps.school_id = w_tt.school_id
                          WHERE t.data_state = 'Yes'
                            AND w_tt.week_index <= :currentWeekIndex
                          ORDER BY w_tt.week_index DESC
                          LIMIT 1)
SELECT tl.id
FROM profile_timetable_cache AS ptc
         JOIN timetable_lessons AS tl
              ON tl.id = ptc.timetable_lesson_id
         JOIN profiles AS p
              ON p.id = ptc.profile_id
         LEFT JOIN timetable_week_limitation AS twl
                   ON tl.id = twl.timetable_lesson_id
         LEFT JOIN main.weeks AS w_main
                   ON w_main.id = twl.week_id
         JOIN latest_timetable lt
              ON tl.timetable_id = lt.id
WHERE (w_main.week_index = :currentWeekIndex OR w_main.week_index IS NULL)
  AND tl.day_of_week = :dayOfWeek AND p.id = :profileId
    """)
    fun getLessonsForProfile(profileId: Uuid, currentWeekIndex: Int, dayOfWeek: DayOfWeek): Flow<List<Uuid>>

    @Query("DELETE FROM profile_timetable_cache WHERE profile_id = :profileId")
    suspend fun dropIndexForProfile(profileId: Uuid)

    @Query("SELECT * FROM timetables WHERE school_id = :schoolId AND week_id = :weekId LIMIT 1")
    fun getTimetableData(schoolId: Uuid, weekId: String): Flow<DbTimetable?>

    @Upsert
    suspend fun upsert(entries: List<DbProfileTimetableCache>)

    @Upsert
    suspend fun upsertWeekLimitations(limitations: List<DbTimetableWeekLimitation>)

    @Query("SELECT MAX(version) FROM timetable_lessons")
    fun getCurrentVersion(): Flow<Int?>
}