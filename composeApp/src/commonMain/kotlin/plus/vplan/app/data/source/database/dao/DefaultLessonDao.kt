package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbDefaultLessonGroupCrossover
import plus.vplan.app.data.source.database.model.embedded.EmbeddedDefaultLesson

@Dao
interface DefaultLessonDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM default_lesson_group_crossover LEFT JOIN default_lessons ON default_lessons.id = default_lesson_group_crossover.default_lesson_id WHERE group_id = :groupId")
    fun getByGroup(groupId: Int): Flow<List<EmbeddedDefaultLesson>>

    @Transaction
    @Query("SELECT * FROM default_lesson_group_crossover LEFT JOIN default_lessons ON default_lessons.id = default_lesson_group_crossover.default_lesson_id LEFT JOIN school_groups ON default_lesson_group_crossover.group_id = school_groups.id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedDefaultLesson>>

    @Transaction
    @Query("SELECT * FROM default_lessons WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedDefaultLesson?>

    @Upsert
    suspend fun upsert(entity: DbDefaultLesson)

    @Upsert
    suspend fun upsert(entity: DbDefaultLessonGroupCrossover)

    @Transaction
    suspend fun upsert(defaultLessons: List<DbDefaultLesson>, defaultLessonGroupCrossovers: List<DbDefaultLessonGroupCrossover>) {
        defaultLessons.forEach { upsert(it) }
        defaultLessonGroupCrossovers.forEach { upsert(it) }
    }

    @Query("DELETE FROM default_lessons WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM default_lessons WHERE indiware_id = :indiwareId")
    fun getByIndiwareId(indiwareId: String): Flow<EmbeddedDefaultLesson?>

    @Transaction
    suspend fun deleteById(ids: List<Int>) {
        ids.forEach { deleteById(it) }
    }
}