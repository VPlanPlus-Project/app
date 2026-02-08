package plus.vplan.app.data.source.database.dao.besteschule

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleGrade

@Dao
interface BesteschuleGradesDao {

    @Upsert
    suspend fun upsert(items: List<DbBesteSchuleGrade>)

    @Query("SELECT * FROM besteschule_grades WHERE schulverwalter_user_id = :schulverwalterUserId")
    fun getAllForUser(schulverwalterUserId: Int): Flow<List<DbBesteSchuleGrade>>

    @Query("SELECT * FROM besteschule_grades")
    fun getAll(): Flow<List<DbBesteSchuleGrade>>

    @Query("SELECT * FROM besteschule_grades WHERE id = :gradeId")
    fun getById(gradeId: Int): Flow<DbBesteSchuleGrade?>

    @Query("SELECT * FROM besteschule_grades WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<DbBesteSchuleGrade>

    @Query("DELETE FROM besteschule_grades WHERE schulverwalter_user_id = :userId")
    suspend fun clearCacheForUser(userId: Int)

    @Query("""
        SELECT g.* FROM besteschule_grades g
        INNER JOIN besteschule_collections c ON g.collection_id = c.id
        WHERE c.interval_id = :intervalId
    """)
    fun getByIntervalId(intervalId: Int): Flow<List<DbBesteSchuleGrade>>

    @Query("""
        SELECT g.* FROM besteschule_grades g
        INNER JOIN besteschule_collections c ON g.collection_id = c.id
        WHERE c.teacher_id = :teacherId
    """)
    fun getByTeacherId(teacherId: Int): Flow<List<DbBesteSchuleGrade>>

    @Query("""
        SELECT g.* FROM besteschule_grades g
        INNER JOIN besteschule_collections c ON g.collection_id = c.id
        WHERE c.subject_id = :subjectId
    """)
    fun getBySubjectId(subjectId: Int): Flow<List<DbBesteSchuleGrade>>

    @Query("SELECT * FROM besteschule_grades WHERE collection_id = :collectionId")
    fun getByCollectionId(collectionId: Int): Flow<List<DbBesteSchuleGrade>>

    @Query("SELECT * FROM besteschule_grades WHERE given_at = :givenAt")
    fun getByGivenAt(givenAt: String): Flow<List<DbBesteSchuleGrade>>
}