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
    @Query("SELECT * FROM default_lessons WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedDefaultLesson?>

    @Upsert
    suspend fun upsert(entity: DbDefaultLesson)
}