package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
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
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM courses LEFT JOIN school_groups ON courses.group_id = school_groups.id WHERE school_groups.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedCourse>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedCourse?>

    @Upsert
    suspend fun upsert(course: DbCourse)

    @Transaction
    suspend fun upsert(courses: List<DbCourse>) {
        courses.forEach { upsert(it) }
    }

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun deleteById(ids: List<String>) {
        ids.forEach { deleteById(it) }
    }
}