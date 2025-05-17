package plus.vplan.app.data.source.database.dao.schulverwalter

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterGrade
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.embedded.EmbeddedSchulverwalterGrade

@Dao
interface GradeDao {

    @Query("SELECT id FROM schulverwalter_grade")
    fun getAll(): Flow<List<Int>>

    @Query("SELECT * FROM schulverwalter_grade")
    fun getAllRaw(): Flow<List<DbSchulverwalterGrade>>

    @Upsert
    suspend fun upsert(
        grades: List<DbSchulverwalterGrade>,
        collectionsCrossovers: List<FKSchulverwalterGradeSchulverwalterCollection>,
        subjectsCrossovers: List<FKSchulverwalterGradeSchulverwalterSubject>,
        teachersCrossovers: List<FKSchulverwalterGradeSchulverwalterTeacher>
    )

    @Query("SELECT * FROM schulverwalter_grade WHERE id = :id")
    @Transaction
    fun getById(id: Int): Flow<EmbeddedSchulverwalterGrade?>

    @Query("DELETE FROM fk_schulverwalter_grade_schulverwalter_collection WHERE grade_id = :gradeId AND collection_id NOT IN (:collectionIds)")
    suspend fun deleteSchulverwalterGradeSchulverwalterCollection(gradeId: Int, collectionIds: List<Int>)

    @Query("DELETE FROM fk_schulverwalter_grade_schulverwalter_subject WHERE grade_id = :gradeId AND subject_id NOT IN (:subjectIds)")
    suspend fun deleteSchulverwalterGradeSchulverwalterSubject(gradeId: Int, subjectIds: List<Int>)

    @Query("DELETE FROM fk_schulverwalter_grade_schulverwalter_teacher WHERE grade_id = :gradeId AND teacher_id NOT IN (:teacherIds)")
    suspend fun deleteSchulverwalterGradeSchulverwalterTeacher(gradeId: Int, teacherIds: List<Int>)

    @Query("UPDATE schulverwalter_grade SET is_selected_for_final_grade = :useForFinalGrade WHERE id = :gradeId")
    suspend fun setConsiderForFinalGrade(gradeId: Int, useForFinalGrade: Boolean)

    @Query("DELETE FROM schulverwalter_grade WHERE vpp_id = :vppId")
    suspend fun deleteByVppId(vppId: Int)
}