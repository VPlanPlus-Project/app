package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.data.source.database.model.embedded.EmbeddedVppId

@Dao
interface VppIdDao {

    @Upsert
    suspend fun upsert(vppId: DbVppId)

    @Upsert
    suspend fun upsert(vppIdSchulverwalterConnection: DbVppIdSchulverwalter)

    @Upsert
    suspend fun upsert(vppIdAccess: DbVppIdAccess)

    @Upsert
    suspend fun upsert(vppIdGroup: DbVppIdGroupCrossover)

    @Transaction
    suspend fun upsert(
        vppId: DbVppId,
        vppIdAccess: DbVppIdAccess,
        vppIdSchulverwalter: DbVppIdSchulverwalter?,
        groupCrossovers: List<DbVppIdGroupCrossover>
    ) {
        upsert(vppId)
        upsert(vppIdAccess)
        vppIdSchulverwalter?.let { upsert(it) }
        groupCrossovers.forEach { upsert(it) }
    }

    @Transaction
    suspend fun upsert(
        vppId: DbVppId,
        groupCrossovers: List<DbVppIdGroupCrossover>
    ) {
        upsert(vppId)
        groupCrossovers.forEach { upsert(it) }
    }

    @Transaction
    @Query("SELECT * FROM vpp_id WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedVppId?>

    @Query("DELETE FROM vpp_id WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Transaction
    @Query("SELECT * FROM vpp_id")
    fun getAll(): Flow<List<EmbeddedVppId>>

    @Query("DELETE FROM vpp_id_access WHERE vpp_id = :vppId")
    suspend fun deleteAccessToken(vppId: Int)

    @Query("DELETE FROM vpp_id_schulverwalter WHERE vpp_id = :vppId")
    suspend fun deleteSchulverwalterAccessToken(vppId: Int)
}