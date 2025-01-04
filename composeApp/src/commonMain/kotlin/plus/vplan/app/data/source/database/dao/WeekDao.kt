package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.data.source.database.model.embedded.EmbeddedWeek

@Dao
interface WeekDao {

    @Upsert
    @Transaction
    suspend fun upsert(weeks: List<DbWeek>)

    @Transaction
    @Query("SELECT * FROM weeks WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedWeek>>

    @Transaction
    @Query("SELECT * FROM weeks WHERE id = :id")
    fun getById(id: String): Flow<EmbeddedWeek?>

    @Transaction
    @Query("DELETE FROM weeks WHERE school_id = :schoolId")
    suspend fun deleteBySchool(schoolId: Int)

    @Transaction
    @Query("DELETE FROM weeks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun deleteById(ids: List<String>) {
        ids.forEach { deleteById(it) }
    }
}