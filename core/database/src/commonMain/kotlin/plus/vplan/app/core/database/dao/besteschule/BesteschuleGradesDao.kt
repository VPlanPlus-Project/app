package plus.vplan.app.core.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleGrade
import plus.vplan.app.core.database.model.embedded.besteschule.EmbeddedBesteSchuleGrade

@Dao
interface BesteschuleGradesDao {

    @Upsert
    suspend fun upsert(items: List<DbBesteSchuleGrade>)

    @Query("SELECT * FROM besteschule_grades WHERE schulverwalter_user_id = :schulverwalterUserId")
    fun getAllForUser(schulverwalterUserId: Int): Flow<List<EmbeddedBesteSchuleGrade>>

    @Query("SELECT * FROM besteschule_grades")
    fun getAll(): Flow<List<EmbeddedBesteSchuleGrade>>

    @Query("SELECT * FROM besteschule_grades WHERE id = :gradeId")
    fun getById(gradeId: Int): Flow<EmbeddedBesteSchuleGrade?>

    @Query("DELETE FROM besteschule_grades WHERE schulverwalter_user_id = :userId")
    suspend fun clearCacheForUser(userId: Int)
}