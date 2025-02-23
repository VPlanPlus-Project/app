package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterSubjectSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchulverwalterFinalGrade

@Dao
interface FinalGradeDao {
    @Query("SELECT id FROM schulverwalter_final_grade")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_final_grade WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<EmbeddedSchulverwalterFinalGrade?>

    @Upsert
    suspend fun upsert(finalGrades: List<DbSchulverwalterFinalGrade>, subjectCrossovers: List<FKSchulverwalterSubjectSchulverwalterFinalGrade>)

    @Query("DELETE FROM fk_schulverwalter_subject_schulverwalter_final_grade WHERE final_grade_id = :finalGradeId AND subject_id NOT IN (:subjectIds)")
    suspend fun deleteSchulverwalterSubjectSchulverwalterFinalGrade(finalGradeId: Int, subjectIds: List<Int>)
}