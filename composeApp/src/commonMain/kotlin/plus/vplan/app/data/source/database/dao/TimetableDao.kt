package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.data.source.database.model.database.DbProfileTimetableCache
import plus.vplan.app.data.source.database.model.database.DbTimetable
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbTimetableWeekLimitation
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
        profileIndex: List<DbProfileTimetableCache>,
        weekLimitations: List<DbTimetableWeekLimitation>
    ) {
        Logger.d { "Start replacing" }
        val oldLessons = getByTimetable(timetableId).first().map { it.timetableLesson.id }
        Logger.d { "Old lessons: ${oldLessons.size}x" }
        if (oldLessons.isNotEmpty()) deleteTimetableByIds(oldLessons)
        Logger.d { "Deleted old lessons" }
        upsert(lessons, groups, teachers, rooms)
        Logger.d { "Upserted new lessons" }
        upsert(profileIndex)
        Logger.d { "Upserted profile index" }
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
    @Query("SELECT * FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id WHERE school_groups.school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM timetable_lessons WHERE timetable_id = :timetableId")
    fun getByTimetable(timetableId: Uuid): Flow<List<EmbeddedTimetableLesson>>

    @Transaction
    @Query("SELECT DISTINCT timetable_lessons.id FROM timetable_lessons LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id LEFT JOIN timetable_week_limitation ON timetable_week_limitation.timetable_lesson_id = timetable_lessons.id WHERE school_groups.school_id = :schoolId AND timetable_lessons.week_id = :timetableReleaseWeekId AND timetable_lessons.day_of_week = :dayOfWeek AND (timetable_week_limitation.week_id IS NULL OR timetable_week_limitation.week_id = :currentWeekId)")
    fun getBySchool(schoolId: Uuid, timetableReleaseWeekId: String, currentWeekId: String, dayOfWeek: DayOfWeek): Flow<List<Uuid>>

    @Transaction
    @Query("SELECT DISTINCT weeks.id FROM timetable_lessons LEFT JOIN weeks ON weeks.id = timetable_lessons.week_id LEFT JOIN timetable_group_crossover ON timetable_group_crossover.timetable_lesson_id = timetable_lessons.id LEFT JOIN school_groups ON school_groups.id = timetable_group_crossover.group_id WHERE week_index <= :maxWeekIndex AND school_groups.school_id = :schoolId")
    fun getWeekIds(schoolId: Uuid, maxWeekIndex: Int): Flow<List<String>>

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
    @Query("SELECT DISTINCT timetable_lessons.id FROM profile_timetable_cache LEFT JOIN timetable_lessons ON timetable_lessons.id = profile_timetable_cache.timetable_lesson_id LEFT JOIN timetable_week_limitation ON timetable_week_limitation.timetable_lesson_id = timetable_lessons.id WHERE profile_id = :profileId AND timetable_lessons.week_id = :weekId AND timetable_lessons.day_of_week = :dayOfWeek AND (timetable_week_limitation.week_id IS NULL OR timetable_week_limitation.week_id = :currentWeekId)")
    fun getLessonsForProfile(profileId: Uuid, weekId: String, currentWeekId: String, dayOfWeek: DayOfWeek): Flow<List<Uuid>>

    @Query("DELETE FROM profile_timetable_cache WHERE profile_id = :profileId")
    suspend fun dropIndexForProfile(profileId: Uuid)

    @Query("SELECT * FROM timetables WHERE school_id = :schoolId AND week_id = :weekId LIMIT 1")
    fun getTimetableData(schoolId: Uuid, weekId: String): Flow<DbTimetable?>

    @Upsert
    suspend fun upsert(entries: List<DbProfileTimetableCache>)

    @Upsert
    suspend fun upsertWeekLimitations(limitations: List<DbTimetableWeekLimitation>)
}