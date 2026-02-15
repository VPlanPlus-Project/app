package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSubjectInstance
import kotlin.uuid.Uuid

@Dao
interface SubjectInstanceDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM subject_instance LEFT JOIN fk_subject_instance_group ON subject_instance.id = fk_subject_instance_group.subject_instance_id WHERE group_id = :groupId")
    fun getByGroup(groupId: Uuid): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM subject_instance WHERE subject_instance.teacher_id = :teacherId")
    fun getByTeacher(teacherId: Uuid): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @Query("SELECT * FROM subject_instance")
    fun getAll(): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM fk_subject_instance_group LEFT JOIN subject_instance ON subject_instance.id = fk_subject_instance_group.subject_instance_id LEFT JOIN school_groups ON fk_subject_instance_group.group_id = school_groups.id WHERE school_groups.school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedSubjectInstance>>

    @Transaction
    @Query("SELECT * FROM subject_instance WHERE id = :id")
    fun findById(id: Uuid): Flow<EmbeddedSubjectInstance?>

    @Upsert
    suspend fun upsertSubjectInstance(entity: DbSubjectInstance, groups: List<FKSubjectInstanceGroup>, aliases: List<DbSubjectInstanceAlias>)

    @Query("DELETE FROM subject_instance WHERE id = :id")
    suspend fun deleteById(id: Uuid)

    @Transaction
    suspend fun deleteById(ids: List<Uuid>) {
        ids.forEach { deleteById(it) }
    }

    @Query("SELECT subject_instance_id FROM subject_instances_aliases WHERE alias = :value AND alias_type = :provider AND version = :version")
    suspend fun getIdByAlias(value: String, provider: AliasProvider, version: Int): Uuid?
}