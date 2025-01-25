package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbAssessment

@Dao
interface AssessmentDao {

    @Upsert
    suspend fun upsert(assessment: DbAssessment)
}