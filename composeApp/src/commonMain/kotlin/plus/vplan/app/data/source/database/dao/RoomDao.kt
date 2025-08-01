package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomAlias
import plus.vplan.app.data.source.database.model.embedded.EmbeddedRoom
import plus.vplan.app.domain.data.AliasProvider
import kotlin.uuid.Uuid

@Dao
interface RoomDao {
    @Transaction
    @Query("SELECT * FROM school_rooms WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedRoom>>

    @Query("SELECT id FROM school_rooms")
    fun getAll(): Flow<List<Uuid>>

    @Transaction
    @Query("SELECT * FROM school_rooms WHERE id = :id")
    fun findById(id: Uuid): Flow<EmbeddedRoom?>

    @Query("DELETE FROM school_rooms WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Upsert
    suspend fun upsertRoom(room: DbRoom, aliases: List<DbRoomAlias>)

    @Query("SELECT room_id FROM rooms_aliases WHERE alias = :value AND alias_type = :provider AND version = :version")
    suspend fun getIdByAlias(value: String, provider: AliasProvider, version: Int): Uuid?
}