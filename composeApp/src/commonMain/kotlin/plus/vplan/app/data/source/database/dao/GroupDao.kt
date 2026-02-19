package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.database.model.embedded.EmbeddedGroup
import plus.vplan.app.core.model.AliasProvider
import kotlin.uuid.Uuid

@Dao
interface GroupDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM school_groups WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedGroup>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM school_groups")
    fun getAll(): Flow<List<EmbeddedGroup>>

    @Transaction
    @Query("SELECT * FROM school_groups WHERE id = :id")
    fun findById(id: Uuid): Flow<EmbeddedGroup?>

    @Upsert
    suspend fun upsertGroup(group: DbGroup, aliases: List<DbGroupAlias>)

    @Query("DELETE FROM school_groups WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM groups_aliases LEFT JOIN school_groups ON school_groups.id = groups_aliases.group_id WHERE alias = :value AND alias_type = :provider AND version = :version")
    fun getByAlias(value: String, provider: AliasProvider, version: Int): Flow<EmbeddedGroup?>
}