package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupIdentifier
import plus.vplan.app.data.source.database.model.embedded.EmbeddedGroup
import kotlin.uuid.Uuid

@Dao
interface GroupDao {

    @Transaction
    @Query("SELECT * FROM school_groups WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedGroup>>

    @Transaction
    @Query("SELECT * FROM school_groups WHERE entity_id = :id")
    fun getByAppId(id: Uuid): Flow<EmbeddedGroup?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, MIN(school_groups.entity_id) FROM school_group_identifiers LEFT JOIN school_groups ON school_group_identifiers.group_id = school_groups.entity_id WHERE school_group_identifiers.value = :id GROUP BY school_groups.entity_id")
    fun findById(id: String): Flow<EmbeddedGroup?>

    @Upsert
    suspend fun upsert(group: DbGroup)

    @Upsert
    suspend fun upsertGroupIdentifier(groupIdentifier: DbGroupIdentifier)
}