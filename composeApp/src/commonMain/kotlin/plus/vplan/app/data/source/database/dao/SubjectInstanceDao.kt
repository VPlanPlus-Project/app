package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSubjectInstance

@Dao
interface SubjectInstanceDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM fk_subject_instance_group LEFT JOIN subject_instance ON subject_instance.id = fk_subject_instance_group.subject_instance_id WHERE group_id = :groupId")
    fun getByGroup(groupId: Int): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @Query("SELECT * FROM subject_instance")
    fun getAll(): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM fk_subject_instance_group LEFT JOIN subject_instance ON subject_instance.id = fk_subject_instance_group.subject_instance_id LEFT JOIN school_groups ON fk_subject_instance_group.group_id = school_groups.id LEFT JOIN fk_school_group ON fk_school_group.group_id = school_groups.id WHERE fk_school_group.school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @Query("SELECT * FROM subject_instance WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedSubjectInstance?>

    @Upsert
    suspend fun upsert(entity: DbSubjectInstance)

    @Upsert
    suspend fun upsert(entity: FKSubjectInstanceGroup)

    @Transaction
    suspend fun upsert(subjectInstances: List<DbSubjectInstance>, subjectInstanceGroupCrossovers: List<FKSubjectInstanceGroup>) {
        subjectInstances.forEach { upsert(it) }
        subjectInstanceGroupCrossovers.forEach { upsert(it) }
    }

    @Query("DELETE FROM subject_instance WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM subject_instance WHERE indiware_id = :indiwareId")
    fun getByIndiwareId(indiwareId: String): Flow<EmbeddedSubjectInstance?>

    @Transaction
    suspend fun deleteById(ids: List<Int>) {
        ids.forEach { deleteById(it) }
    }
}