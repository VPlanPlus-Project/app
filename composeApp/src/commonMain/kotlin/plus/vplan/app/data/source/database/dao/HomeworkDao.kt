package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.model.database.DbFile
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.data.source.database.model.embedded.EmbeddedHomework
import plus.vplan.app.data.source.database.model.embedded.EmbeddedHomeworkTask

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
    suspend fun upsertFiles(files: List<DbFile>)

    @Transaction
    @Upsert
    suspend fun upsertTaskDoneAccountMany(homeworkTaskDoneAccount: List<DbHomeworkTaskDoneAccount>)

    @Transaction
    @Upsert
    suspend fun upsertFileHomeworkConnections(fileHomeworkConnections: List<FKHomeworkFile>)

    @Query("DELETE FROM fk_homework_file WHERE homework_id IN (:homeworkId)")
    suspend fun deleteFileHomeworkConnections(homeworkId: List<Int>)

    @Transaction
    suspend fun upsertMany(
        homework: List<DbHomework>,
        homeworkTask: List<DbHomeworkTask>,
        homeworkTaskDoneAccount: List<DbHomeworkTaskDoneAccount>,
        files: List<DbFile>,
        fileHomeworkConnections: List<FKHomeworkFile>
    ) {
        upsertMany(homework)
        upsertTaskMany(homeworkTask)
        upsertTaskDoneAccountMany(homeworkTaskDoneAccount)
        upsertFiles(files)
        upsertFileHomeworkConnections(fileHomeworkConnections)
    }

    @Transaction
    @Query("SELECT * FROM homework")
    fun getAll(): Flow<List<EmbeddedHomework>>

    @Transaction
    @Query("SELECT * FROM homework WHERE due_to = :date")
    fun getByDate(date: LocalDate): Flow<List<EmbeddedHomework>>

    @Transaction
    @Query("SELECT * FROM homework WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedHomework?>

    @Transaction
    @Query("DELETE FROM homework WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Transaction
    @Query("DELETE FROM homework_task WHERE id IN (:ids)")
    suspend fun deleteTaskById(ids: List<Int>)

    @Query("SELECT MIN(id) FROM homework")
    fun getMinId(): Flow<Int?>

    @Query("SELECT MIN(id) FROM homework_task")
    fun getMinTaskId(): Flow<Int?>

    @Query("SELECT MIN(id) FROM file")
    fun getMinFileId(): Flow<Int?>

    @Transaction
    @Query("SELECT * FROM homework_task WHERE id = :id")
    fun getTaskById(id: Int): Flow<EmbeddedHomeworkTask?>

    @Transaction
    @Query("DELETE FROM homework")
    suspend fun deleteAll()

    @Transaction
    @Query("DELETE FROM homework WHERE id > 0")
    suspend fun deleteCache()

    @Upsert
    suspend fun upsertTaskDoneAccount(taskDoneAccount: DbHomeworkTaskDoneAccount)

    @Upsert
    suspend fun upsertTaskDoneProfile(taskDoneProfile: DbHomeworkTaskDoneProfile)

    @Query("UPDATE homework SET default_lesson_id = :defaultLessonId, group_id = :groupId WHERE id = :homeworkId")
    suspend fun updateDefaultLessonAndGroup(homeworkId: Int, defaultLessonId: Int?, groupId: Int?)

    @Query("UPDATE homework SET due_to = :dueTo WHERE id = :homeworkId")
    suspend fun updateDueTo(homeworkId: Int, dueTo: LocalDate)

    @Query("UPDATE homework SET is_public = :isPublic WHERE id = :homeworkId")
    suspend fun updateVisibility(homeworkId: Int, isPublic: Boolean)

    @Query("UPDATE homework_task SET content = :content WHERE id = :taskId")
    suspend fun updateTaskContent(taskId: Int, content: String)
}