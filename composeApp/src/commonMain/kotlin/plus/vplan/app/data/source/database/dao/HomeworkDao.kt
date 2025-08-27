package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.model.database.DbFile
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.data.source.database.model.database.DbProfileHomeworkIndex
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.data.source.database.model.embedded.EmbeddedHomework
import plus.vplan.app.data.source.database.model.embedded.EmbeddedHomeworkTask
import kotlin.uuid.Uuid

@Dao
interface HomeworkDao {

    @Transaction
    suspend fun upsertSingleHomework(
        homework: DbHomework,
        tasks: List<DbHomeworkTask>,
        tasksDoneAccount: List<DbHomeworkTaskDoneAccount>,
        tasksDoneProfile: List<DbHomeworkTaskDoneProfile>,
        fileIds: List<Int>
    ) {
        val existing = getById(homework.id).first()

        val existingTasks = existing?.tasks?.map { it.id }
        if (existingTasks != null) {
            deleteTaskById(existingTasks - tasks.map { it.id })
        }

        val existingFiles = existing?.files?.map { it.fileId }
        if (existingFiles != null) {
            deleteFileHomeworkConnectionsById(existingFiles - fileIds)
        }

        upsertSingleHomework(homework)
        upsertTaskMany(tasks)
        upsertTaskDoneAccountMany(tasksDoneAccount)
        upsertTaskDoneProfileMany(tasksDoneProfile)
        upsertHomeworkFileConnections(fileIds.map { FKHomeworkFile(homework.id, it) })
    }

    @Upsert
    suspend fun upsertSingleHomework(homework: DbHomework)

    @Upsert
    suspend fun upsertMany(homework: List<DbHomework>)

    @Transaction
    @Upsert
    suspend fun upsertTaskMany(homeworkTasks: List<DbHomeworkTask>)

    @Transaction
    @Upsert
    suspend fun upsertFiles(files: List<DbFile>)

    @Upsert
    suspend fun upsertTaskDoneAccountMany(homeworkTaskDoneAccount: List<DbHomeworkTaskDoneAccount>)

    @Upsert
    suspend fun upsertTaskDoneProfileMany(homeworkTaskDoneProfile: List<DbHomeworkTaskDoneProfile>)

    @Upsert
    suspend fun upsertHomeworkFileConnection(fileHomeworkConnection: FKHomeworkFile)

    @Transaction
    @Upsert
    suspend fun upsertHomeworkFileConnections(fileHomeworkConnections: List<FKHomeworkFile>)

    @Query("DELETE FROM fk_homework_file WHERE homework_id = :homeworkId")
    suspend fun deleteFileHomeworkConnections(homeworkId: Int)

    @Query("DELETE FROM fk_homework_file WHERE homework_id IN (:homeworkId)")
    suspend fun deleteFileHomeworkConnections(homeworkId: List<Int>)

    @Query("DELETE FROM fk_homework_file WHERE homework_id = :homeworkId AND file_id = :fileId")
    suspend fun deleteFileHomeworkConnection(homeworkId: Int, fileId: Int)

    @Transaction
    suspend fun upsertMany(
        homework: List<DbHomework>,
        homeworkTask: List<DbHomeworkTask>,
        homeworkTaskDoneAccount: List<DbHomeworkTaskDoneAccount>,
        files: List<DbFile>,
        fileHomeworkConnections: List<FKHomeworkFile>
    ) {
        upsertTaskMany(homeworkTask)
        upsertTaskDoneAccountMany(homeworkTaskDoneAccount)
        upsertFiles(files)
        upsertHomeworkFileConnections(fileHomeworkConnections)
    }

    @Transaction
    @Query("SELECT * FROM homework")
    fun getAll(): Flow<List<EmbeddedHomework>>

    @Transaction
    @Query("SELECT * FROM homework WHERE due_to = :date")
    fun getByDate(date: LocalDate): Flow<List<EmbeddedHomework>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM profile_homework_index LEFT JOIN homework on homework.id = homework_id WHERE profile_id = :profileId")
    fun getByProfile(profileId: Uuid): Flow<List<EmbeddedHomework>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM profile_homework_index LEFT JOIN homework on homework.id = homework_id WHERE profile_id = :profileId AND homework.due_to = :date")
    fun getByProfileAndDate(profileId: Uuid, date: LocalDate): Flow<List<EmbeddedHomework>>

    @Transaction
    @Query("SELECT * FROM homework WHERE id = :id")
    fun getById(id: Int): Flow<EmbeddedHomework?>

    @Transaction
    @Query("DELETE FROM homework WHERE id IN (:ids)")
    suspend fun deleteById(ids: List<Int>)

    @Query("DELETE FROM homework_task WHERE id IN (:ids)")
    suspend fun deleteTaskById(ids: List<Int>)

    @Query("DELETE FROM fk_homework_file WHERE file_id IN (:ids)")
    suspend fun deleteFileHomeworkConnectionsById(ids: List<Int>)

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

    @Query("UPDATE homework SET subject_instance_id = :subjectInstanceId, group_id = :groupId WHERE id = :homeworkId")
    suspend fun updateSubjectInstanceAndGroup(homeworkId: Int, subjectInstanceId: Int?, groupId: Int?)

    @Query("UPDATE homework SET due_to = :dueTo WHERE id = :homeworkId")
    suspend fun updateDueTo(homeworkId: Int, dueTo: LocalDate)

    @Query("UPDATE homework SET is_public = :isPublic WHERE id = :homeworkId")
    suspend fun updateVisibility(homeworkId: Int, isPublic: Boolean)

    @Query("UPDATE homework_task SET content = :content WHERE id = :taskId")
    suspend fun updateTaskContent(taskId: Int, content: String)

    @Query("DELETE FROM profile_homework_index WHERE profile_id = :profileId")
    suspend fun dropHomeworkIndexForProfile(profileId: Uuid)

    @Upsert
    suspend fun upsertHomeworkIndex(indices: List<DbProfileHomeworkIndex>)
}