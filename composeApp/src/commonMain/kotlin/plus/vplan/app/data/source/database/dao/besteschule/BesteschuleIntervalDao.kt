package plus.vplan.app.data.source.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleIntervalUser
import plus.vplan.app.data.source.database.model.embedded.besteschule.EmbeddedBesteschuleInterval

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
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM besteschule_intervals LEFT JOIN besteschule_interval_user ON besteschule_intervals.id = besteschule_interval_user.interval_id WHERE besteschule_interval_user.schulverwalter_user_id = :userId")
    fun getIntervalsForUser(userId: Int): Flow<List<EmbeddedBesteschuleInterval>>

    @Transaction
    @Query("SELECT * FROM besteschule_intervals WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<EmbeddedBesteschuleInterval>

    @Transaction
    @Query("SELECT * FROM besteschule_intervals WHERE year_id = :yearId")
    fun getByYearId(yearId: Int): Flow<List<EmbeddedBesteschuleInterval>>

    @Transaction
    @Query("SELECT * FROM besteschule_intervals WHERE included_interval_id = :includedIntervalId")
    fun getByIncludedIntervalId(includedIntervalId: Int): Flow<List<EmbeddedBesteschuleInterval>>
}