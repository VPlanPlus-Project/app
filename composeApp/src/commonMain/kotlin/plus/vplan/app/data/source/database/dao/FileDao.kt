package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbFile

@Dao
interface FileDao {

    @Query("SELECT * FROM file WHERE id = :id")
    fun getById(id: Int): Flow<DbFile?>

    @Upsert
    suspend fun upsert(file: DbFile)

    @Query("UPDATE file SET is_offline_ready = :isOfflineReady WHERE id = :id")
    suspend fun setOfflineReady(id: Int, isOfflineReady: Boolean)
}