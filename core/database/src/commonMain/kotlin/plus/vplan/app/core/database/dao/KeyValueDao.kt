package plus.vplan.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyValueDao {
    @Query("SELECT value FROM key_value WHERE id = :key")
    fun get(key: String): Flow<String?>

    @Query("INSERT OR REPLACE INTO key_value (id, value) VALUES (:key, :value)")
    suspend fun set(key: String, value: String)

    @Query("DELETE FROM key_value WHERE id = :key")
    suspend fun delete(key: String)
}