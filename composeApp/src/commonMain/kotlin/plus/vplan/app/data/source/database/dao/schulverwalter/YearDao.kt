package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterYear

@Dao
interface YearDao {
    @Query("SELECT id FROM schulverwalter_year")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_year WHERE id = :id")
    fun getById(id: Int): Flow<DbSchulverwalterYear?>

    @Upsert
    suspend fun upsert(years: List<DbSchulverwalterYear>)
}