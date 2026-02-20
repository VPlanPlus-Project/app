package plus.vplan.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.DbFile

@Dao
interface FileDao {

    @Query("SELECT * FROM file WHERE id = :id")
    fun getById(id: Int): Flow<DbFile?>

    @Query("SELECT * FROM file")
    fun getAll(): Flow<List<DbFile>>

    @Upsert
    suspend fun upsert(file: DbFile)

    @Query("UPDATE file SET is_offline_ready = :isOfflineReady WHERE id = :id")
    suspend fun setOfflineReady(id: Int, isOfflineReady: Boolean)

    @Query("UPDATE file SET file_name = :name WHERE id = :id")
    suspend fun updateName(id: Int, name: String)

    @Query("DELETE FROM file WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM file WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Query("SELECT MIN(id) FROM file WHERE id < 0")
    suspend fun getLocalMinId(): Int?

    @Query("DELETE FROM fk_homework_file WHERE file_id = :fileId")
    suspend fun deleteHomeworkFileConnections(fileId: Int)
}