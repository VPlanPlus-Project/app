package plus.vplan.app.data.source.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleYear
import plus.vplan.app.data.source.database.model.embedded.besteschule.EmbeddedBesteSchuleYear

@Dao
interface BesteschuleYearDao {

    @Upsert
    suspend fun upsert(items: List<DbBesteschuleYear>)

    @Query("SELECT * FROM besteschule_year")
    fun getAll(): Flow<List<EmbeddedBesteSchuleYear>>

    @Query("SELECT * FROM besteschule_year WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedBesteSchuleYear?>
}