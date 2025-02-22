package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchulverwalterSubject

@Dao
interface SubjectDao {
    @Query("SELECT id FROM schulverwalter_subject")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_subject WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<EmbeddedSchulverwalterSubject?>

    @Upsert
    suspend fun upsert(subjects: List<DbSchulverwalterSubject>)

    @Query("DELETE FROM schulverwalter_subject WHERE id = :id")
    suspend fun deleteById(id: Int)
}