package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSp24SchoolDetails
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchool
import kotlin.uuid.Uuid

@Dao
interface SchoolDao {

    @Transaction
    @Query("SELECT * FROM schools")
    fun getAll(): Flow<List<EmbeddedSchool>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM schools WHERE id = :id")
    fun findById(id: Int): Flow<EmbeddedSchool?>

    @Upsert
    suspend fun upsertSchool(school: DbSchool)

    @Upsert
    suspend fun upsertSp24SchoolDetails(details: DbSp24SchoolDetails)

    @Query("UPDATE schools_sp24details SET username = :username, password = :password WHERE school_id = :schoolId")
    suspend fun updateIndiwareSchoolDetails(schoolId: Int, username: String, password: String)

    @Query("DELETE FROM schools_sp24details WHERE school_id = :schoolId AND username = :username")
    suspend fun deleteIndiwareSchoolDetails(schoolId: Uuid, username: String)
}