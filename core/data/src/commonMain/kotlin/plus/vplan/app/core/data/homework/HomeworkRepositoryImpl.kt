package plus.vplan.app.core.data.homework

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.database.dao.HomeworkDao
import plus.vplan.app.core.database.model.database.DbHomework
import plus.vplan.app.core.database.model.database.DbHomeworkTask
import plus.vplan.app.core.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.core.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.core.database.model.database.DbProfileHomeworkIndex
import plus.vplan.app.core.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.core.model.getByProvider
import plus.vplan.app.core.utils.Optional
import plus.vplan.app.network.vpp.homework.HomeworkApi
import plus.vplan.app.network.vpp.homework.HomeworkPatchRequest
import plus.vplan.app.network.vpp.homework.HomeworkPostRequest
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import plus.vplan.app.core.model.Optional as ModelOptional

class HomeworkRepositoryImpl(
    private val homeworkDao: HomeworkDao,
    private val homeworkApi: HomeworkApi,
    private val groupRepository: GroupRepository,
    private val vppIdRepository: VppIdRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) : HomeworkRepository {
    override fun getAll(): Flow<List<Homework>> {
        return homeworkDao.getAll()
            .map { items -> items.mapNotNull { it.toModel() } }
            .flowOn(Dispatchers.Default)
    }

    override fun getAllForProfile(profile: Profile): Flow<List<Homework>> {
        return homeworkDao.getByProfile(profile.id)
            .map { items -> items.mapNotNull { it.toModel() } }
            .flowOn(Dispatchers.Default)
    }

    override fun getById(id: Int): Flow<Homework?> {
        return homeworkDao.getById(id)
            .map { it?.toModel() }
            .flowOn(Dispatchers.Default)
    }

    override fun getByGroup(group: Group): Flow<List<Homework>> {
        return getAll()
            .map { homeworks ->
                homeworks.filter { it.group?.id == group.id || it.subjectInstance?.groups?.any { it.id == group.id } == true }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getByDate(date: LocalDate): Flow<List<Homework>> {
        return homeworkDao.getByDate(date)
            .map { items -> items.mapNotNull { it.toModel() } }
            .flowOn(Dispatchers.Default)
    }

    override fun getByProfile(profileId: Uuid, date: LocalDate?): Flow<List<Homework>> {
        return if (date == null) {
            homeworkDao.getByProfile(profileId).map { it.mapNotNull { hw -> hw.toModel() } }
        } else {
            homeworkDao.getByProfileAndDate(profileId, date).map { it.mapNotNull { hw -> hw.toModel() } }
        }.flowOn(Dispatchers.Default)
    }

    override fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return homeworkDao.getTaskById(id)
            .map {
                it?.toModel()?.let { task -> CacheState.Done(task) }
                    ?: CacheState.NotExisting(id.toString())
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun save(homework: Homework) {
        val dbHomework = DbHomework(
            id = homework.id,
            subjectInstanceId = homework.subjectInstance?.id,
            groupId = homework.group?.id,
            createdAt = homework.createdAt,
            dueTo = homework.dueTo,
            createdBy = (homework as? Homework.CloudHomework)?.createdBy?.id,
            createdByProfileId = (homework as? Homework.LocalHomework)?.createdByProfile?.id,
            isPublic = (homework as? Homework.CloudHomework)?.isPublic ?: false,
            cachedAt = homework.cachedAt
        )
        val tasks = homework.tasks.map { task ->
            DbHomeworkTask(
                id = task.id,
                homeworkId = homework.id,
                content = task.content,
                cachedAt = task.cachedAt,
            )
        }
        val tasksDoneAccount = homework.tasks.flatMap { task ->
            task.doneByVppIds.map { vppId -> DbHomeworkTaskDoneAccount(task.id, vppId, true) }
        }
        val tasksDoneProfile = homework.tasks.flatMap { task ->
            task.doneByProfiles.map { profileId -> DbHomeworkTaskDoneProfile(task.id, profileId, true) }
        }

        homeworkDao.upsertSingleHomework(
            homework = dbHomework,
            tasks = tasks,
            tasksDoneAccount = tasksDoneAccount,
            tasksDoneProfile = tasksDoneProfile,
            fileIds = homework.files.map { it.id }
        )
    }

    override suspend fun delete(homework: Homework) {
        if (homework is Homework.CloudHomework) {
            try {
                homeworkApi.deleteHomework(homework.createdBy.asActive()!!, homework.id)
            } catch (e: Exception) {
                // Log error but still delete locally
            }
        }
        homeworkDao.deleteById(listOf(homework.id))
    }

    override suspend fun deleteById(id: Int) {
        homeworkDao.deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<Int>) {
        homeworkDao.deleteById(ids)
    }

    override suspend fun getIdForNewLocalHomework(): Int {
        return (homeworkDao.getMinId().first() ?: 0).coerceAtMost(-1) - 1
    }

    override suspend fun getIdForNewLocalHomeworkTask(): Int {
        return (homeworkDao.getMinTaskId().first() ?: 0).coerceAtMost(-1) - 1
    }

    override suspend fun getIdForNewLocalHomeworkFile(): Int {
        return (homeworkDao.getMinFileId().first() ?: 0).coerceAtMost(-1) - 1
    }

    override suspend fun sync(profile: Profile.StudentProfile) {
        val apiHomeworks = homeworkApi.getHomeworkItems(
            access = profile.vppId?.buildVppSchoolAuthentication() ?: profile.school.buildSp24AppAuthentication(),
            filterGroups = listOfNotNull(profile.group.aliases.firstOrNull()?.toUrlString()),
            filterSubjectInstances = profile
                .subjectInstanceConfiguration
                .filterValues { true }
                .keys
                .mapNotNull { it.aliases.firstOrNull()?.toUrlString() }
        )
        apiHomeworks.forEach { dto ->
            // Resolve group and subject instance IDs from API to local UUIDs via aliases
            val groupId = dto.group?.let { groupEntity ->
                val alias = Alias(AliasProvider.Vpp, groupEntity.id.toString(), 1)
                groupRepository.getByIds(setOf(alias), forceUpdate = false).first()?.id
            }
            
            val subjectInstanceId = dto.subjectInstance?.let { siEntity ->
                val alias = Alias(AliasProvider.Vpp, siEntity.id.toString(), 1)
                subjectInstanceRepository.getByIds(setOf(alias), forceUpdate = false).first()?.id
            }

            vppIdRepository.getById(dto.createdBy.id).first()!!
            
            val homework = DbHomework(
                id = dto.id,
                subjectInstanceId = subjectInstanceId,
                groupId = groupId,
                createdAt = Instant.fromEpochSeconds(dto.createdAt),
                dueTo = LocalDate.parse(dto.dueTo),
                createdBy = dto.createdBy.id,
                createdByProfileId = null,
                isPublic = dto.isPublic,
                cachedAt = Clock.System.now()
            )
            val tasks = dto.tasks.map { taskWrapper ->
                DbHomeworkTask(
                    id = taskWrapper.value.id,
                    homeworkId = dto.id,
                    content = taskWrapper.value.content,
                    cachedAt = Clock.System.now()
                )
            }
            
            val tasksDoneAccount = dto.tasks.mapNotNull { taskWrapper ->
                if (profile.vppId == null) return@mapNotNull null
                taskWrapper.value.done?.let { isDone ->
                    DbHomeworkTaskDoneAccount(
                        taskId = taskWrapper.value.id,
                        vppId = profile.vppId!!.id,
                        isDone = isDone
                    )
                }
            }
            
            homeworkDao.upsertSingleHomework(
                homework = homework,
                tasks = tasks,
                tasksDoneAccount = tasksDoneAccount,
                tasksDoneProfile = emptyList(),
                fileIds = dto.files.map { it.id }
            )
        }
    }

    override suspend fun syncById(authentication: VppSchoolAuthentication, homeworkId: Int, forceReload: Boolean): HomeworkRepository.SyncResult {
        try {
            // Check if we already have fresh data (unless forceReload is true)
            if (!forceReload) {
                val existing = homeworkDao.getById(homeworkId).first()
                if (existing != null) {
                    val age = Clock.System.now() - existing.homework.cachedAt
                    if (age.inWholeMinutes < 5) {
                        return HomeworkRepository.SyncResult.Success // Data is fresh enough
                    }
                }
            }
            
            val dto = homeworkApi.getHomeworkById(authentication, homeworkId)
                ?: return HomeworkRepository.SyncResult.NotExists // Homework not found on server
            
            // Resolve group and subject instance IDs from API to local UUIDs via aliases
            val groupId = dto.group?.let { groupEntity ->
                val alias = Alias(AliasProvider.Vpp, groupEntity.id.toString(), 1)
                try {
                    groupRepository.getByIds(setOf(alias), forceUpdate = false)
                        .first()?.id
                } catch (e: Exception) {
                    return HomeworkRepository.SyncResult.Error(e)
                }
            }
            
            val subjectInstanceId = dto.subjectInstance?.let { siEntity ->
                val alias = Alias(AliasProvider.Vpp, siEntity.id.toString(), 1)
                subjectInstanceRepository.getByIds(setOf(alias), forceUpdate = false).first()?.id
            }

            vppIdRepository.getById(dto.createdBy.id).first()!!
            
            val homework = DbHomework(
                id = dto.id,
                subjectInstanceId = subjectInstanceId,
                groupId = groupId,
                createdAt = Instant.fromEpochSeconds(dto.createdAt),
                dueTo = LocalDate.parse(dto.dueTo),
                createdBy = dto.createdBy.id,
                createdByProfileId = null,
                isPublic = dto.isPublic,
                cachedAt = Clock.System.now()
            )
            val tasks = dto.tasks.map { taskWrapper ->
                DbHomeworkTask(
                    id = taskWrapper.value.id,
                    homeworkId = dto.id,
                    content = taskWrapper.value.content,
                    cachedAt = Clock.System.now()
                )
            }

            val tasksDoneAccount =
                if (authentication is VppSchoolAuthentication.Vpp) dto.tasks.mapNotNull { taskWrapper ->
                    taskWrapper.value.done?.let { isDone ->
                        DbHomeworkTaskDoneAccount(
                            taskId = taskWrapper.value.id,
                            vppId = authentication.vppIdId,
                            isDone = isDone
                        )
                    }
                } else emptyList()
            
            homeworkDao.upsertSingleHomework(
                homework = homework,
                tasks = tasks,
                tasksDoneAccount = tasksDoneAccount,
                tasksDoneProfile = emptyList(),
                fileIds = dto.files.map { it.id }
            )
            return HomeworkRepository.SyncResult.Success
        } catch (e: Exception) {
            return HomeworkRepository.SyncResult.Error(e)
        }
    }

    override suspend fun toggleTaskDone(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Boolean {
        return try {
            val isDone = !task.isDone(profile)
            val activeVppId = profile.vppId?.asActive()
            if (activeVppId != null && task.id > 0) {
                homeworkApi.updateTask(activeVppId, task.homeworkId, task.id, isDone = isDone)
                homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, activeVppId.id, isDone))
            } else {
                homeworkDao.upsertTaskDoneProfile(DbHomeworkTaskDoneProfile(task.id, profile.id, isDone))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addTask(homework: Homework, content: String, profile: Profile.StudentProfile): Response.Error? {
        return try {
            val activeVppId = profile.vppId?.asActive()
            if (homework is Homework.CloudHomework && activeVppId != null) {
                val newId = homeworkApi.addTask(activeVppId, homework.id, content)
                homeworkDao.upsertTaskMany(listOf(DbHomeworkTask(newId, homework.id, content, Clock.System.now())))
            } else {
                val localId = (homeworkDao.getMinTaskId().first() ?: 0).coerceAtMost(-1) - 1
                homeworkDao.upsertTaskMany(listOf(DbHomeworkTask(localId, homework.id, content, Clock.System.now())))
            }
            null
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateTask(task: Homework.HomeworkTask, content: String, profile: Profile.StudentProfile) {
        val activeVppId = profile.vppId?.asActive()
        if (task.id > 0 && activeVppId != null) {
            homeworkApi.updateTask(activeVppId, task.homeworkId, task.id, content = content)
        }
        homeworkDao.updateTaskContent(task.id, content)
    }

    override suspend fun deleteTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error? {
        return try {
            val activeVppId = profile.vppId?.asActive()
            if (task.id > 0 && activeVppId != null) {
                homeworkApi.deleteTask(activeVppId, task.homeworkId, task.id)
            }
            homeworkDao.deleteTaskById(listOf(task.id))
            null
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error? {
        return try {
            val activeVppId = profile.vppId?.asActive()
            if (homework is Homework.CloudHomework && activeVppId != null) {
                homeworkApi.deleteHomework(activeVppId, homework.id)
            }
            homeworkDao.deleteById(listOf(homework.id))
            null
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateHomeworkMetadata(
        homework: Homework,
        dueTo: ModelOptional<LocalDate>,
        subjectInstance: ModelOptional<SubjectInstance?>,
        group: ModelOptional<Group?>,
        isPublic: ModelOptional<Boolean>,
        profile: Profile.StudentProfile
    ) {
        val activeVppId = profile.vppId?.asActive()
        if (homework is Homework.CloudHomework && activeVppId != null) {
            // Only send API request if at least one field is being updated
            if (dueTo.isPresent() || subjectInstance.isPresent() || group.isPresent() || isPublic.isPresent()) {
                homeworkApi.updateHomework(
                    activeVppId,
                    homework.id,
                    HomeworkPatchRequest(
                        subjectInstanceId = if (subjectInstance.isPresent()) 
                            Optional.Defined((subjectInstance as ModelOptional.Present).value?.aliases?.getByProvider(AliasProvider.Vpp)?.value?.toIntOrNull())
                        else Optional.Undefined(),
                        groupId = if (group.isPresent())
                            Optional.Defined((group as ModelOptional.Present).value?.aliases?.getByProvider(AliasProvider.Vpp)?.value?.toIntOrNull())
                        else Optional.Undefined(),
                        dueTo = if (dueTo.isPresent())
                            Optional.Defined((dueTo as ModelOptional.Present).value.toString())
                        else Optional.Undefined(),
                        isPublic = if (isPublic.isPresent())
                            Optional.Defined((isPublic as ModelOptional.Present).value)
                        else Optional.Undefined()
                    )
                )
            }
        }
        
        // Update local database
        if (dueTo is ModelOptional.Present) {
            homeworkDao.updateDueTo(homework.id, dueTo.value)
        }
        if (subjectInstance is ModelOptional.Present || group is ModelOptional.Present) {
            // Only update the fields that were provided
            val subjectInstanceId = when {
                subjectInstance is ModelOptional.Present -> subjectInstance.value?.id
                else -> homework.subjectInstance?.id
            }
            val groupId = when {
                group is ModelOptional.Present -> group.value?.id
                else -> homework.group?.id
            }
            homeworkDao.updateSubjectInstanceAndGroup(homework.id, subjectInstanceId, groupId)
        }
        if (isPublic is ModelOptional.Present) {
            homeworkDao.updateVisibility(homework.id, isPublic.value)
        }
    }

    override suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        groupId: Int?,
        subjectInstanceId: Int?,
        isPublic: Boolean,
        tasks: List<String>,
    ): Response<CreateHomeworkResponse> {
        return try {
            val response = homeworkApi.createHomework(
                vppId,
                HomeworkPostRequest(
                    subjectInstance = subjectInstanceId,
                    groupId = groupId,
                    dueTo = until.toString(),
                    isPublic = isPublic,
                    tasks = tasks
                )
            )
            Response.Success(
                CreateHomeworkResponse(
                    id = response.id,
                    taskIds = tasks.mapIndexed { index, task ->
                        task to response.tasks[index].id
                    }.toMap()
                )
            )
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun linkHomeworkFile(vppId: VppId.Active?, homeworkId: Int, fileId: Int): Response<Unit> {
        return try {
            if (homeworkId > 0 && vppId != null) {
                homeworkApi.linkFile(vppId, homeworkId, fileId)
            }
            homeworkDao.upsertHomeworkFileConnection(FKHomeworkFile(homeworkId, fileId))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun unlinkHomeworkFile(vppId: VppId.Active?, homeworkId: Int, fileId: Int): Response<Unit> {
        return try {
            if (homeworkId > 0 && vppId != null) {
                homeworkApi.unlinkFile(vppId, homeworkId, fileId)
            }
            homeworkDao.deleteFileHomeworkConnection(homeworkId, fileId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache() {
        homeworkDao.deleteCache()
    }

    override suspend fun dropIndexForProfile(profileId: Uuid) {
        homeworkDao.dropHomeworkIndexForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, homeworkIds: Collection<Int>) {
        homeworkDao.upsertHomeworkIndex(
            homeworkIds.map { DbProfileHomeworkIndex(it, profileId) }
        )
    }
    
    private fun VppId.asActive(): VppId.Active? = this as? VppId.Active
}
