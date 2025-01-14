package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkFile
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.embedded.EmbeddedHomework

@Dao
interface HomeworkDao {

    @Upsert
    suspend fun upsert(homework: DbHomework)

    @Upsert
    suspend fun upsertMany(homework: List<DbHomework>)

    @Transaction
    @Upsert
    suspend fun upsertTaskMany(homeworkTasks: List<DbHomeworkTask>)

    @Transaction
    @Upsert
    suspend fun upsertFiles(files: List<DbHomeworkFile>)

    @Transaction
    suspend fun upsertMany(homework: List<DbHomework>, homeworkTask: List<DbHomeworkTask>, files: List<DbHomeworkFile>) {
        upsertMany(homework)
        upsertTaskMany(homeworkTask)
        upsertFiles(files)
    }

    @Transaction
    @Query("SELECT * FROM homework")
    fun getAll(): Flow<List<EmbeddedHomework>>

    @Transaction
    @Query("SELECT * FROM homework WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedHomework?>

    @Transaction
    @Query("DELETE FROM homework WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Query("SELECT MIN(id) FROM homework")
    fun getMinId(): Flow<Int?>

    @Query("SELECT MIN(id) FROM homework_task")
    fun getMinTaskId(): Flow<Int?>

    @Query("SELECT MIN(id) FROM homework_file")
    fun getMinFileId(): Flow<Int?>

    @Transaction
    @Query("DELETE FROM homework")
    suspend fun deleteAll()
}