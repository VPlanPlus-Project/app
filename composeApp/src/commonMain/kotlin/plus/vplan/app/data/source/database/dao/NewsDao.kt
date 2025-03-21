package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbNews
import plus.vplan.app.data.source.database.model.database.foreign_key.FKNewsSchool
import plus.vplan.app.data.source.database.model.embedded.EmbeddedNews

@Dao
interface NewsDao {
    @Upsert
    suspend fun upsert(news: List<DbNews>, schools: List<FKNewsSchool>)

    @Query("SELECT * FROM news")
    fun getAll(): Flow<List<EmbeddedNews>>

    @Query("DELETE FROM news WHERE id IN (:ids)")
    suspend fun delete(ids: List<Int>)
}