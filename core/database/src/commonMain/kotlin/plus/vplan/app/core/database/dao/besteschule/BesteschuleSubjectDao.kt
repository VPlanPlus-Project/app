package plus.vplan.app.core.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleSubject

@Dao
interface BesteschuleSubjectDao {
    @Upsert
    suspend fun upsert(subject: List<DbBesteSchuleSubject>)

    @Query("SELECT * FROM besteschule_subjects")
    fun getAll(): Flow<List<DbBesteSchuleSubject>>

    @Query("SELECT * FROM besteschule_subjects WHERE id = :id")
    fun getById(id: Int): Flow<DbBesteSchuleSubject?>

}