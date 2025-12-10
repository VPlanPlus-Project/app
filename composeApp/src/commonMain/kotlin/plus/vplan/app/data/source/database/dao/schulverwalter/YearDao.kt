package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterYear
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchulverwalterYear

@Dao
interface YearDao {
    @Query("SELECT id FROM schulverwalter_year")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_year WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<EmbeddedSchulverwalterYear?>

    @Upsert
    suspend fun upsert(
        years: List<DbSchulverwalterYear>,
        intervalsCrossovers: List<FKSchulverwalterYearSchulverwalterInterval>,
        intervals: List<DbSchulverwalterInterval>
    )

    @Query("DELETE FROM fk_schulverwalter_year_schulverwalter_interval WHERE year_id = :yearId AND interval_id NOT IN (:intervalIds)")
    suspend fun deleteSchulverwalterYearSchulverwalterInterval(yearId: Int, intervalIds: List<Int>)

    @Query("DELETE FROM fk_schulverwalter_year_schulverwalter_interval WHERE interval_id IN (SELECT interval_id FROM fk_schulverwalter_year_schulverwalter_interval LEFT JOIN schulverwalter_year ON schulverwalter_year.id = fk_schulverwalter_year_schulverwalter_interval.interval_id WHERE schulverwalter_year.user_for_request = :schulverwalterUserId AND fk_schulverwalter_year_schulverwalter_interval.interval_id NOT IN (:downloadedIntervalIds))")
    suspend fun deleteSchulverwalterIntervalsForUserThatAreNotInList(schulverwalterUserId: Int, downloadedIntervalIds: Set<Int>)
}