package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {

    @Query("SELECT id FROM schulverwalter_grade")
    fun getAll(): Flow<List<Int>>
}