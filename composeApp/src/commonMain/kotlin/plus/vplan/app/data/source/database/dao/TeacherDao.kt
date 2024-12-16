package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherIdentifier
import plus.vplan.app.data.source.database.model.embedded.EmbeddedTeacher
import kotlin.uuid.Uuid

@Dao
interface TeacherDao {

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Uuid): Flow<List<EmbeddedTeacher>>

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE entity_id = :id")
    fun getByAppId(id: Uuid): Flow<EmbeddedTeacher?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, MIN(school_teachers.entity_id) FROM school_teacher_identifiers LEFT JOIN school_teachers ON school_teachers.entity_id = school_teacher_identifiers.teacher_id WHERE value = :id AND origin = 'vpp' GROUP BY school_teachers.entity_id")
    fun findById(id: String): Flow<EmbeddedTeacher?>

    @Upsert
    suspend fun upsertTeacher(teacher: DbTeacher)

    @Upsert
    suspend fun upsertTeacherIdentifier(teacherIdentifier: DbTeacherIdentifier)
}