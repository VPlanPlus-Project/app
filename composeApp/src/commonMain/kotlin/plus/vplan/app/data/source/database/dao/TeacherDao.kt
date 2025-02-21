package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbTeacher

@Dao
interface TeacherDao {

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE school_id = :schoolId")
    fun getBySchool(schoolId: Int): Flow<List<DbTeacher>>

    @Transaction
    @Query("SELECT * FROM school_teachers WHERE id = :id")
    fun getById(id: Int): Flow<DbTeacher?>

    @Query("SELECT id FROM school_teachers")
    fun getAll(): Flow<List<Int>>

    @Query("DELETE FROM school_teachers WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Upsert
    suspend fun upsertTeacher(teacher: DbTeacher)
}