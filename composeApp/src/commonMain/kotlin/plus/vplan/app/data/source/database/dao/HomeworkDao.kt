package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.embedded.EmbeddedHomework

@Dao
interface HomeworkDao {

    @Upsert
    suspend fun upsert(homework: DbHomework)

    @Upsert
    suspend fun upsert(homework: List<DbHomework>)

    @Transaction
    @Query("SELECT * FROM homework")
    fun getAll(): Flow<List<EmbeddedHomework>>
}