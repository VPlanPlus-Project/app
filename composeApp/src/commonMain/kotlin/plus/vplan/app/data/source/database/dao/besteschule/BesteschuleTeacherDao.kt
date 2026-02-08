package plus.vplan.app.data.source.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleTeacher

@Dao
interface BesteschuleTeacherDao {
    @Upsert
    suspend fun upsert(items: List<DbBesteschuleTeacher>)

    @Query("SELECT * FROM besteschule_teacher")
    fun getAll(): Flow<List<DbBesteschuleTeacher>>

    @Query("SELECT * FROM besteschule_teacher WHERE id = :teacherId")
    fun getTeacher(teacherId: Int): Flow<DbBesteschuleTeacher?>

    @Query("SELECT * FROM besteschule_teacher WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<DbBesteschuleTeacher>

}