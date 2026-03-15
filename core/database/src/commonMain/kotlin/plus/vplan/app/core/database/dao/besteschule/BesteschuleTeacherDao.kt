package plus.vplan.app.core.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleTeacher

@Dao
interface BesteschuleTeacherDao {
    @Upsert
    suspend fun upsert(items: List<DbBesteSchuleTeacher>)

    @Query("SELECT * FROM besteschule_teacher")
    fun getAll(): Flow<List<DbBesteSchuleTeacher>>

    @Query("SELECT * FROM besteschule_teacher WHERE id = :teacherId")
    fun getTeacher(teacherId: Int): Flow<DbBesteSchuleTeacher?>

}