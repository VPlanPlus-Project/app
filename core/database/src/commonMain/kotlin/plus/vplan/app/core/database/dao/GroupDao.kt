package plus.vplan.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbGroupAlias
import plus.vplan.app.core.database.model.embedded.EmbeddedGroup
import plus.vplan.app.core.model.AliasProvider
import kotlin.uuid.Uuid

@Dao
interface GroupDao {

    @Upsert
    suspend fun upsert(alias: DbGroupAlias)

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
    suspend fun upsert(group: DbGroup, aliases: List<DbGroupAlias>)

    @Query("DELETE FROM school_groups WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT group_id FROM groups_aliases WHERE alias = :value AND alias_type = :provider AND version = :version")
    fun getIdByAlias(value: String, provider: AliasProvider, version: Int): Flow<Uuid?>
}