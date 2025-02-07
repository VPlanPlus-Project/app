package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.data.source.database.model.embedded.EmbeddedAssessment

@Dao
interface AssessmentDao {

    @Upsert
    suspend fun upsert(assessment: DbAssessment)

    @Upsert
    @Transaction
    suspend fun upsert(assessments: List<DbAssessment>, files: List<FKAssessmentFile>)

    @Query("SELECT MIN(id) FROM assessments WHERE id < 0")
    suspend fun getSmallestId(): Int?

    @Query("SELECT * FROM assessments WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedAssessment?>

    @Query("SELECT * FROM assessments")
    fun getAll(): Flow<List<EmbeddedAssessment>>

    @Query("DELETE FROM assessments WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Query("UPDATE assessments SET type = :type WHERE id = :id")
    suspend fun updateType(id: Int, type: Int)

    @Query("UPDATE assessments SET date = :date WHERE id = :id")
    suspend fun updateDate(id: Int, date: LocalDate)

    @Query("UPDATE assessments SET is_public = :isPublic WHERE id = :id")
    suspend fun updateVisibility(id: Int, isPublic: Boolean)

    @Query("UPDATE assessments SET description = :content WHERE id = :id")
    suspend fun updateContent(id: Int, content: String)
}