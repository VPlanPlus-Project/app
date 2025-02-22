package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterTeacher

@Dao
interface TeacherDao {

    @Query("SELECT id FROM schulverwalter_teacher")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_teacher WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<DbSchulverwalterTeacher?>

    @Upsert
    suspend fun upsert(teachers: List<DbSchulverwalterTeacher>)
}