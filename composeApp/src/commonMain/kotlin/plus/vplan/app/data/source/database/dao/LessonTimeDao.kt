package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.embedded.EmbeddedLessonTime

@Dao
interface LessonTimeDao {

    @Transaction
    @Query("SELECT * FROM lesson_times WHERE group_id = :groupId")
    fun getByGroup(groupId: Int): Flow<List<EmbeddedLessonTime>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM lesson_times LEFT JOIN school_groups ON lesson_times.group_id = school_groups.id WHERE school_groups.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedLessonTime>>

    @Transaction
    @Query("SELECT * FROM lesson_times WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedLessonTime?>

    @Upsert
    suspend fun upsert(lessonTime: DbLessonTime)

    @Transaction
    suspend fun upsert(lessonTimes: List<DbLessonTime>) {
        lessonTimes.forEach { upsert(it) }
    }

    @Query("DELETE FROM lesson_times WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun deleteById(ids: List<String>) {
        ids.forEach { deleteById(it) }
    }
}