package plus.vplan.app.data.source.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleCollection

@Dao
interface BesteschuleCollectionDao {
    @Upsert
    suspend fun upsert(items: List<DbBesteSchuleCollection>)

    @Query("SELECT * FROM besteschule_collections")
    fun getAll(): Flow<List<DbBesteSchuleCollection>>

    @Query("SELECT * FROM besteschule_collections WHERE id = :id")
    fun getById(id: Int): Flow<DbBesteSchuleCollection?>

    @Query("SELECT * FROM besteschule_collections WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<DbBesteSchuleCollection>

    @Query("SELECT * FROM besteschule_collections WHERE interval_id = :intervalId")
    fun getByIntervalId(intervalId: Int): Flow<List<DbBesteSchuleCollection>>

    @Query("SELECT * FROM besteschule_collections WHERE teacher_id = :teacherId")
    fun getByTeacherId(teacherId: Int): Flow<List<DbBesteSchuleCollection>>

    @Query("SELECT * FROM besteschule_collections WHERE subject_id = :subjectId")
    fun getBySubjectId(subjectId: Int): Flow<List<DbBesteSchuleCollection>>
}
