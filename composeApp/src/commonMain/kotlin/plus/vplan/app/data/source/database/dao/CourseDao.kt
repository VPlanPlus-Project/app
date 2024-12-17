package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.embedded.EmbeddedCourse

@Dao
interface CourseDao {

    @Transaction
    @Query("SELECT * FROM courses WHERE group_id = :groupId")
    fun getByGroup(groupId: Int): Flow<List<EmbeddedCourse>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedCourse?>

    @Upsert
    suspend fun upsert(course: DbCourse)
}