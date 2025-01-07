package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSubstitutionPlanLesson

@Dao
interface SubstitutionPlanDao {

    @Upsert
    suspend fun upsert(substitutionPlanLesson: DbSubstitutionPlanLesson)

    @Upsert
    suspend fun upsert(substitutionPlanGroupCrossover: DbSubstitutionPlanGroupCrossover)

    @Upsert
    suspend fun upsert(substitutionPlanTeacherCrossover: DbSubstitutionPlanTeacherCrossover)

    @Upsert
    suspend fun upsert(substitutionPlanRoomCrossover: DbSubstitutionPlanRoomCrossover)

    @Transaction
    suspend fun upsert(
        lessons: List<DbSubstitutionPlanLesson>,
        groups: List<DbSubstitutionPlanGroupCrossover>,
        teachers: List<DbSubstitutionPlanTeacherCrossover>,
        rooms: List<DbSubstitutionPlanRoomCrossover>
    ) {
        lessons.forEach { upsert(it) }
        groups.forEach { upsert(it) }
        teachers.forEach { upsert(it) }
        rooms.forEach { upsert(it) }
    }
    
    @Transaction
    @Query("DELETE FROM substitution_plan_lesson")
    suspend fun deleteAll()
    
    @Transaction
    @Query("DELETE FROM substitution_plan_lesson WHERE version = :version")
    suspend fun deleteSubstitutionPlanByVersion(version: String)

    @Transaction
    @Query("SELECT * FROM substitution_plan_lesson LEFT JOIN substitution_plan_group_crossover ON substitution_plan_group_crossover.substitution_plan_lesson_id = substitution_plan_lesson.id LEFT JOIN school_groups ON school_groups.id = substitution_plan_group_crossover.group_id LEFT JOIN day ON day.id = day_id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId AND substitution_plan_lesson.version = :version AND day.date = :date")
    fun getTimetableLessons(schoolId: Int, version: String, date: LocalDate): Flow<List<EmbeddedSubstitutionPlanLesson>>
}