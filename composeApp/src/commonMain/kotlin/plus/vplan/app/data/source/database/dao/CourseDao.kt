package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbCourseIdentifier
import plus.vplan.app.data.source.database.model.embedded.EmbeddedCourse
import kotlin.uuid.Uuid

@Dao
interface CourseDao {

    @Transaction
    @Query("SELECT * FROM courses WHERE group_id = :groupId")
    fun getByGroup(groupId: Uuid): Flow<List<EmbeddedCourse>>

    @Transaction
    @Query("SELECT * FROM courses WHERE entity_id = :id")
    fun getByAppId(id: Uuid): Flow<EmbeddedCourse?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, MIN(courses.entity_id) FROM course_identifiers LEFT JOIN courses ON courses.entity_id = course_identifiers.course_id WHERE value = :id AND origin = 'vpp' GROUP BY courses.entity_id")
    fun findById(id: String): Flow<EmbeddedCourse?>

    @Upsert
    suspend fun upsert(course: DbCourse)

    @Upsert
    suspend fun upsertCourseIdentifier(courseIdentifier: DbCourseIdentifier)
}