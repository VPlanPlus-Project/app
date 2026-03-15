package plus.vplan.app.core.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.core.database.model.embedded.besteschule.EmbeddedBesteSchuleCollection

@Dao
interface BesteschuleCollectionDao {
    @Upsert
    suspend fun upsert(items: List<DbBesteSchuleCollection>)

    @Query("SELECT * FROM besteschule_collections")
    fun getAll(): Flow<List<EmbeddedBesteSchuleCollection>>

    @Query("SELECT * FROM besteschule_collections WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedBesteSchuleCollection?>
}
