package plus.vplan.app.data.source.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleSubject

@Dao
interface BesteschuleSubjectDao {
    @Upsert
    suspend fun upsert(subject: List<DbBesteschuleSubject>)

    @Query("SELECT * FROM besteschule_subjects")
    fun getAll(): Flow<List<DbBesteschuleSubject>>

    @Query("SELECT * FROM besteschule_subjects WHERE id = :id")
    fun getById(id: Int): Flow<DbBesteschuleSubject?>

}