package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbAssessment

@Dao
interface AssessmentDao {

    @Upsert
    suspend fun upsert(assessment: DbAssessment)

    @Upsert
    @Transaction
    suspend fun upsert(assessments: List<DbAssessment>)

    @Query("SELECT MIN(id) FROM assessments WHERE id < 0")
    suspend fun getSmallestId(): Int?
}