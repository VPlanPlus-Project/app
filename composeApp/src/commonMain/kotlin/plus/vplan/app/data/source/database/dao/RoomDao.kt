package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbRoom

@Dao
interface RoomDao {
    @Transaction
    @Query("SELECT * FROM school_rooms WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<DbRoom>>

    @Query("SELECT id FROM school_rooms")
    fun getAll(): Flow<List<Int>>

    @Transaction
    @Query("SELECT * FROM school_rooms WHERE id = :id")
    fun getById(id: Int): Flow<DbRoom?>

    @Query("DELETE FROM school_rooms WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Upsert
    suspend fun upsert(room: DbRoom)
}