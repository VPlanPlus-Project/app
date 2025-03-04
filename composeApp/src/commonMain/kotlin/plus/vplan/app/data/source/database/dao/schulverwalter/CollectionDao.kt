package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchulverwalterCollection

@Dao
interface CollectionDao {
    @Query("SELECT id FROM schulverwalter_collection")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_collection WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<EmbeddedSchulverwalterCollection?>

    @Upsert
    suspend fun upsert(
        collections: List<DbSchulverwalterCollection>,
        intervalsCrossovers: List<FKSchulverwalterCollectionSchulverwalterInterval>,
        subjectsCrossovers: List<FKSchulverwalterCollectionSchulverwalterSubject>,
    )

    @Query("DELETE FROM fk_schulverwalter_collection_schulverwalter_interval WHERE collection_id = :collectionId AND interval_id NOT IN (:intervalIds)")
    suspend fun deleteSchulverwalterCollectionSchulverwalterInterval(collectionId: Int, intervalIds: List<Int>)

    @Query("DELETE FROM fk_schulverwalter_collection_schulverwalter_subject WHERE collection_id = :collectionId AND subject_id NOT IN (:subjectIds)")
    suspend fun deleteSchulverwalterCollectionSchulverwalterSubject(collectionId: Int, subjectIds: List<Int>)
}