@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.utils.combine6

@Immutable
@Stable
sealed class PopulatedHomework {
    abstract val homework: Homework
    abstract val tasks: List<Homework.HomeworkTask>
    abstract val files: List<File>
    abstract val subjectInstance: SubjectInstance?
    abstract val group: Group?
    abstract val createdBy: AppEntity

    data class CloudHomework(
        override val homework: Homework.CloudHomework,
        override val tasks: List<Homework.HomeworkTask>,
        override val files: List<File>,
        override val subjectInstance: SubjectInstance?,
        override val group: Group?,
        override val createdBy: AppEntity.VppId,
        val createdByUser: VppId
    ) : PopulatedHomework()

    data class LocalHomework(
        override val homework: Homework.LocalHomework,
        override val tasks: List<Homework.HomeworkTask>,
        override val files: List<File>,
        override val subjectInstance: SubjectInstance?,
        override val group: Group?,
        override val createdBy: AppEntity.Profile,
        val createdByProfile: Profile.StudentProfile
    ) : PopulatedHomework()
}

class HomeworkPopulator : KoinComponent {
    private val homeworkRepository by inject<HomeworkRepository>()
    private val fileRepository by inject<FileRepository>()
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()
    private val groupRepository by inject<GroupRepository>()
    private val vppIdRepository by inject<VppIdRepository>()
    private val profileRepository by inject<ProfileRepository>()

    fun populateMultiple(
        homework: List<Homework>,
        context: PopulationContext
    ): Flow<List<PopulatedHomework>> {
        if (homework.isEmpty()) return flowOf(emptyList())

        val subjectInstances: Flow<List<SubjectInstance>> = when (context) {
            is PopulationContext.Profile -> subjectInstanceRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> subjectInstanceRepository.getBySchool(context.school)
        }

        val groups: Flow<List<Group>> = when (context) {
            is PopulationContext.Profile -> groupRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> groupRepository.getBySchool(context.school)
        }

        val vppIds: Flow<List<VppId>> = vppIdRepository.getAllLocalIds()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else combine(ids.map { vppIdRepository.getByLocalId(it) }) { it.filterNotNull() }
            }

        val profiles: Flow<List<Profile>> = profileRepository.getAll()

        // Per-homework task/file flows – built once, not rebuilt on every subjectInstances/groups emit
        val homeworkFlows: List<Flow<Pair<List<Homework.HomeworkTask>, List<File>>>> = homework.map { hw ->
            val tasksFlow: Flow<List<Homework.HomeworkTask>> =
                if (hw.taskIds.isEmpty()) flowOf(emptyList())
                else combine(hw.taskIds.map { homeworkRepository.getTaskByLocalId(it) }) { it.filterNotNull() }

            val filesFlow: Flow<List<File>> =
                if (hw.fileIds.isEmpty()) flowOf(emptyList())
                else combine(hw.fileIds.map { fileRepository.getById(it, false) }) { arr ->
                    arr.filterIsInstance<CacheState.Done<File>>().map { it.data }
                }

            combine(tasksFlow, filesFlow) { tasks, files -> tasks to files }
        }

        val homeworkCombined: Flow<List<Pair<List<Homework.HomeworkTask>, List<File>>>> =
            if (homeworkFlows.size == 1) homeworkFlows[0].map { listOf(it) }
            else combine(homeworkFlows) { it.toList() }

        val result: Flow<List<PopulatedHomework>> = combine(
            subjectInstances,
            groups,
            vppIds,
            profiles,
            homeworkCombined,
        ) { subjectInstances, groups, vppIds, profiles, taskFilePairs ->
            homework.mapIndexed { i, hw ->
                val (tasks, files) = taskFilePairs[i]
                when (hw) {
                    is Homework.CloudHomework -> PopulatedHomework.CloudHomework(
                        homework = hw,
                        tasks = tasks,
                        files = files,
                        subjectInstance = hw.subjectInstanceId?.let { sid -> subjectInstances.firstOrNull { it.id == sid } },
                        group = hw.groupId?.let { gid -> groups.firstOrNull { it.id == gid } },
                        createdBy = AppEntity.VppId(hw.createdById),
                        createdByUser = vppIds.first { it.id == hw.createdById }
                    )
                    is Homework.LocalHomework -> PopulatedHomework.LocalHomework(
                        homework = hw,
                        tasks = tasks,
                        files = files,
                        subjectInstance = hw.subjectInstanceId?.let { sid -> subjectInstances.firstOrNull { it.id == sid } },
                        group = hw.groupId?.let { gid -> groups.firstOrNull { it.id == gid } },
                        createdBy = AppEntity.Profile(hw.createdByProfileId),
                        createdByProfile = profiles.first { it.id == hw.createdByProfileId } as Profile.StudentProfile
                    )
                }
            }
        }
        return result.distinctUntilChanged()
    }

    fun populateSingle(homework: Homework): Flow<PopulatedHomework> {
        val subjectInstance = homework.subjectInstanceId
            ?.let { subjectInstanceRepository.getByLocalId(it) }
            ?: flowOf(null)

        val tasks =
            if (homework.taskIds.isEmpty()) flowOf(emptyList())
            else combine(
            homework.taskIds.map { taskId ->
                homeworkRepository.getTaskById(taskId)
                    .filterIsInstance<CacheState.Done<Homework.HomeworkTask>>()
                    .map { it.data }
            }
        ) { it.toList() }

        val files =
            if (homework.fileIds.isEmpty()) flowOf(emptyList())
            else combine(
            homework.fileIds.map { fileId ->
                fileRepository.getById(fileId, false)
                    .filterIsInstance<CacheState.Done<File>>()
                    .map { it.data }
            }
        ) { it.toList() }

        val group = homework.groupId?.let { groupId ->
            groupRepository.getById(
                Alias(
                    provider = AliasProvider.Vpp,
                    value = groupId.toString(),
                    version = 1,
                )
            )
        } ?: flowOf(null)

        val vppId = (homework as? Homework.CloudHomework)?.createdById?.let { vppId ->
            vppIdRepository.getByLocalId(vppId)
        } ?: flowOf(null)

        val profile = (homework as? Homework.LocalHomework)?.createdByProfileId?.let { profileId ->
            profileRepository.getById(profileId).map { it as Profile.StudentProfile }
        } ?: flowOf(null)

        return combine6(
            tasks,
            files,
            subjectInstance,
            group,
            vppId,
            profile,
        ) { tasks, files, subjectInstance, group, vppId, profile ->
            when (homework) {
                is Homework.CloudHomework -> PopulatedHomework.CloudHomework(
                    homework = homework,
                    tasks = tasks,
                    files = files,
                    subjectInstance = subjectInstance,
                    group = group,
                    createdBy = AppEntity.VppId(homework.createdById),
                    createdByUser = vppId!!
                )

                is Homework.LocalHomework -> PopulatedHomework.LocalHomework(
                    homework = homework,
                    tasks = tasks,
                    files = files,
                    subjectInstance = subjectInstance,
                    group = group,
                    createdBy = AppEntity.Profile(homework.createdByProfileId),
                    createdByProfile = profile!!
                )
            }
        }
    }
}

