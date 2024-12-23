package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.embedded.EmbeddedDefaultLesson

@Dao
interface DefaultLessonDao {

    @Transaction
    @Query("SELECT * FROM default_lessons WHERE group_id = :groupId")
    fun getByGroup(groupId: Int): Flow<List<EmbeddedDefaultLesson>>

    @Transaction
    @Query("SELECT default_lessons.id, default_lessons.subject, default_lessons.teacher_id, default_lessons.group_id, default_lessons.group_id, default_lessons.course_id FROM default_lessons LEFT JOIN school_groups ON default_lessons.group_id = school_groups.id WHERE school_groups.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedDefaultLesson>>

    @Transaction
    @Query("SELECT * FROM default_lessons WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedDefaultLesson?>

    @Upsert
    suspend fun upsert(entity: DbDefaultLesson)

    @Transaction
    suspend fun upsert(defaultLessons: List<DbDefaultLesson>) {
        defaultLessons.forEach { upsert(it) }
    }

    @Query("DELETE FROM default_lessons WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun deleteById(ids: List<String>) {
        ids.forEach { deleteById(it) }
    }
}