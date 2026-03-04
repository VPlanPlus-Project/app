package plus.vplan.app.core.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleIntervalUser
import plus.vplan.app.core.database.model.embedded.besteschule.EmbeddedBesteschuleInterval

@Dao
interface BesteschuleIntervalDao {
    @Upsert
    suspend fun upsert(items: List<DbBesteSchuleInterval>)

    @Upsert
    suspend fun upsertUserMappings(items: List<DbBesteschuleIntervalUser>)

    @Transaction
    @Query("SELECT * FROM besteschule_intervals")
    fun getAll(): Flow<List<EmbeddedBesteschuleInterval>>

    @Transaction
    @Query("SELECT * FROM besteschule_intervals WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedBesteschuleInterval?>

    @Transaction
    @Query("SELECT * FROM besteschule_intervals LEFT JOIN besteschule_interval_user ON besteschule_intervals.id = besteschule_interval_user.interval_id WHERE besteschule_interval_user.schulverwalter_user_id = :userId")
    fun getIntervalsForUser(userId: Int): Flow<List<EmbeddedBesteschuleInterval>>
}