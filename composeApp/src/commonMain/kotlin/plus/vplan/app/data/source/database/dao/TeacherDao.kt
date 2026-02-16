package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherAlias
import plus.vplan.app.data.source.database.model.embedded.EmbeddedTeacher
import plus.vplan.app.core.model.AliasProvider
import kotlin.uuid.Uuid

@Dao
interface TeacherDao {

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedTeacher>>

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE id = :id")
    fun findById(id: Uuid): Flow<EmbeddedTeacher?>

    @Query("SELECT * FROM school_teachers")
    fun getAll(): Flow<List<EmbeddedTeacher>>

    @Query("DELETE FROM school_teachers WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Uuid>)

    @Upsert
    suspend fun upsertTeacher(teacher: DbTeacher, aliases: List<DbTeacherAlias>)

    @Query("SELECT teacher_id FROM teachers_aliases WHERE alias = :value AND alias_type = :provider AND version = :version")
    suspend fun getIdByAlias(value: String, provider: AliasProvider, version: Int): Uuid?
}