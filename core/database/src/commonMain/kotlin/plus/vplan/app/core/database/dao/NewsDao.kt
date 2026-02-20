package plus.vplan.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.DbNews
import plus.vplan.app.core.database.model.database.foreign_key.FKNewsSchool
import plus.vplan.app.core.database.model.embedded.EmbeddedNews

@Dao
interface NewsDao {
    @Upsert
    suspend fun upsert(news: List<DbNews>, schools: List<FKNewsSchool>)

    @Transaction
    @Query("SELECT * FROM news")
    fun getAll(): Flow<List<EmbeddedNews>>

    @Transaction
    @Query("SELECT * FROM news WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedNews?>

    @Query("DELETE FROM news WHERE id IN (:ids)")
    suspend fun delete(ids: List<Int>)
}