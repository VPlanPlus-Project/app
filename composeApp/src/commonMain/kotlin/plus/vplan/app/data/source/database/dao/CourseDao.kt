package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.data.source.database.model.embedded.EmbeddedCourse
import kotlin.uuid.Uuid

@Dao
interface CourseDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM course_group_crossover LEFT JOIN courses ON courses.id = course_group_crossover.course_id WHERE group_id = :groupId")
    fun getByGroup(groupId: Uuid): Flow<List<EmbeddedCourse>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * from courses")
    fun getAll(): Flow<List<EmbeddedCourse>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM course_group_crossover LEFT JOIN courses ON courses.id = course_group_crossover.course_id LEFT JOIN school_groups ON course_group_crossover.group_id = school_groups.id WHERE school_groups.school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedCourse>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedCourse?>

    @Transaction
    @Query("SELECT * FROM courses WHERE indiware_id = :indiwareId")
    fun getByIndiwareId(indiwareId: String): Flow<EmbeddedCourse?>

    @Upsert
    suspend fun upsert(course: DbCourse)

    @Upsert
    suspend fun upsert(course: DbCourseGroupCrossover)

    @Transaction
    suspend fun upsert(courses: List<DbCourse>, courseGroupCrossovers: List<DbCourseGroupCrossover>) {
        courses.forEach { upsert(it) }
        courseGroupCrossovers.forEach { upsert(it) }
    }

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Transaction
    suspend fun deleteById(ids: List<Int>) {
        ids.forEach { deleteById(it) }
    }
}