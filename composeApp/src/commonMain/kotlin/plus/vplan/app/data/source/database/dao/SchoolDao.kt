package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSchoolAlias
import plus.vplan.app.data.source.database.model.database.DbSchoolIndiwareAccess
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchool
import plus.vplan.app.domain.data.AliasProvider
import kotlin.uuid.Uuid

@Dao
interface SchoolDao {

    @Transaction
    @Query("SELECT * FROM schools")
    fun getAll(): Flow<List<EmbeddedSchool>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM schools WHERE id = :id")
    fun findById(id: Uuid): Flow<EmbeddedSchool?>

    @Upsert
    suspend fun upsertSchool(school: DbSchool, aliases: List<DbSchoolAlias>)

    @Upsert
    suspend fun upsertSp24SchoolDetails(details: DbSchoolIndiwareAccess)

    @Query("UPDATE school_indiware_access SET credentials_valid = :valid WHERE school_id = :schoolId")
    suspend fun setIndiwareAccessValidState(schoolId: Uuid, valid: Boolean)

    @Query("DELETE FROM schools WHERE id = :schoolId")
    suspend fun deleteById(schoolId: Uuid)

    @Query("SELECT school_id FROM schools_aliases WHERE alias = :value AND alias_type = :provider AND version = :version")
    suspend fun getIdByAlias(value: String, provider: AliasProvider, version: Int): Uuid?
}