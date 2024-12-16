package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.embedded.EmbeddedGroup

@Dao
interface GroupDao {

    @Transaction
    @Query("SELECT * FROM school_groups WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedGroup>>

    @Transaction
    @Query("SELECT * FROM school_groups WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedGroup?>

    @Upsert
    suspend fun upsert(group: DbGroup)
}