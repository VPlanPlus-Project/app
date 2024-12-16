package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.embedded.EmbeddedTeacher

@Dao
interface TeacherDao {

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<EmbeddedTeacher>>

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedTeacher?>

    @Upsert
    suspend fun upsertTeacher(teacher: DbTeacher)
}