@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Homework
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
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
        val subjectInstances = when (context) {
            is PopulationContext.Profile -> subjectInstanceRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> subjectInstanceRepository.getBySchool(context.school.id)
        }

        val groups = when (context) {
            is PopulationContext.Profile -> groupRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> groupRepository.getBySchool(context.school.id)
        }

        val vppIds = vppIdRepository.getAllLocalIds()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else combine(ids.map { vppIdRepository.getByLocalId(it) }) { it.filterNotNull() }
            }

        val profiles = profileRepository.getAll()

        return combine(
            subjectInstances,
            groups,
            vppIds,
            profiles,
        ) { subjectInstances, groups, vppIds, profiles ->
            Quadruple(subjectInstances, groups, vppIds, profiles)
        }.flatMapLatest { (subjectInstances, groups, vppIds, profiles) ->

            combine(
                homework.map { homework ->
                    val tasksFlow =
                        if (homework.taskIds.isEmpty()) flowOf(emptyList())
                        else combine(
                        homework.taskIds.map { homeworkRepository.getTaskByLocalId(it) }
                    ) { it.filterNotNull() }

                    val filesFlow =
                        if (homework.fileIds.isEmpty()) flowOf(emptyList())
                        else combine(
                        homework.fileIds.map { fileRepository.getById(it, false) }
                    ) { it.filterIsInstance<CacheState.Done<File>>() }

                    combine(tasksFlow, filesFlow) { tasks, files ->

                        when (homework) {
                            is Homework.CloudHomework -> PopulatedHomework.CloudHomework(
                                homework = homework,
                                tasks = tasks,
                                files = files.map { it.data },
                                subjectInstance = homework.subjectInstanceId
                                    ?.let { sid -> subjectInstances.firstOrNull { it.id == sid } },
                                group = homework.groupId
                                    ?.let { gid -> groups.firstOrNull { it.id == gid } },
                                createdBy = AppEntity.VppId(homework.createdById),
                                createdByUser = vppIds.first { it.id == homework.createdById }
                            )

                            is Homework.LocalHomework -> PopulatedHomework.LocalHomework(
                                homework = homework,
                                tasks = tasks,
                                files = files.map { it.data },
                                subjectInstance = homework.subjectInstanceId
                                    ?.let { sid -> subjectInstances.firstOrNull { it.id == sid } },
                                group = homework.groupId
                                    ?.let { gid -> groups.firstOrNull { it.id == gid } },
                                createdBy = AppEntity.Profile(homework.createdByProfileId),
                                createdByProfile = profiles.first { it.id == homework.createdByProfileId } as Profile.StudentProfile
                            )
                        }
                    }
                }
            ) { it.toList() }
        }
    }

    fun populateSingle(homework: Homework): Flow<PopulatedHomework> {
        val subjectInstance = homework.subjectInstanceId?.let {
            subjectInstanceRepository.getByLocalId(it)
        } ?: flowOf(null)

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
            groupRepository.getByAlias(
                setOf(
                    Alias(
                        provider = AliasProvider.Vpp,
                        value = groupId.toString(),
                        version = 1
                    )
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

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
