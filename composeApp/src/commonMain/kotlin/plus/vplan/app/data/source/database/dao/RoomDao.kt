package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomIdentifier
import plus.vplan.app.data.source.database.model.embedded.EmbeddedRoom
import kotlin.uuid.Uuid

@Dao
interface RoomDao {
    @Transaction
    @Query("SELECT * FROM school_rooms WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedRoom>>

    @Transaction
    @Query("SELECT * FROM school_rooms WHERE entity_id = :id")
    fun getByAppId(id: Uuid): Flow<EmbeddedRoom?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, MIN(school_rooms.entity_id) FROM room_identifiers LEFT JOIN school_rooms ON room_identifiers.room_id = school_rooms.entity_id WHERE value = :id AND origin = 'vpp' GROUP BY school_rooms.entity_id")
    fun findById(id: String): Flow<EmbeddedRoom?>

    @Upsert
    suspend fun upsert(room: DbRoom)

    @Upsert
    suspend fun upsertRoomIdentifier(roomIdentifier: DbRoomIdentifier)
}