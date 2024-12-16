package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.embedded.EmbeddedRoom

@Dao
interface RoomDao {
    @Transaction
    @Query("SELECT * FROM school_rooms WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedRoom>>

    @Transaction
    @Query("SELECT * FROM school_rooms WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedRoom?>

    @Upsert
    suspend fun upsert(room: DbRoom)
}