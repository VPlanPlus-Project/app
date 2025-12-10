package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterIntervalUser
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchulverwalterInterval

@Dao
interface IntervalDao {

    @Query("SELECT id FROM schulverwalter_interval")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_interval WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<EmbeddedSchulverwalterInterval?>

    @Upsert
    suspend fun upsert(intervals: List<DbSchulverwalterInterval>, intervalYearCrossovers: List<FKSchulverwalterYearSchulverwalterInterval>)

    @Query("DELETE FROM fk_schulverwalter_year_schulverwalter_interval WHERE interval_id = :intervalId AND year_id NOT IN (:yearIds)")
    suspend fun deleteSchulverwalterYearSchulverwalterInterval(intervalId: Int, yearIds: List<Int>)

    @Transaction
    suspend fun updateIntervalUserConnections(schulverwalterUserId: Int, intervalIds: Set<Int>) {
        deleteIntervalConnectionsForUser(schulverwalterUserId)
        upsert(intervalIds.map { DbSchulverwalterIntervalUser(it, schulverwalterUserId) })
    }

    @Query("DELETE FROM schulverwalter_interval_user WHERE schulverwalter_user_id = :schulverwalterUserId")
    suspend fun deleteIntervalConnectionsForUser(schulverwalterUserId: Int)

    @Upsert
    suspend fun upsert(items: List<DbSchulverwalterIntervalUser>)
}