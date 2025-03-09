package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchoolGroup
import plus.vplan.app.data.source.database.model.embedded.EmbeddedGroup

@Dao
interface GroupDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM school_groups LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedGroup>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM school_groups")
    fun getAll(): Flow<List<EmbeddedGroup>>

    @Transaction
    @Query("SELECT * FROM school_groups WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedGroup?>

    @Upsert
    suspend fun upsert(group: DbGroup, fkSchoolGroup: FKSchoolGroup)

    @Query("DELETE FROM school_groups WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)
}