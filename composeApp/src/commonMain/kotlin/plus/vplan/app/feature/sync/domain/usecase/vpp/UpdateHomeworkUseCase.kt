package plus.vplan.app.feature.sync.domain.usecase.vpp

import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.utils.latest

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val groupRepository: GroupRepository,
    private val defaultLessonRepository: DefaultLessonRepository,
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(profile: Profile.StudentProfile): Response<Unit> {
        val response = homeworkRepository.getByDefaultLesson(
            authentication = profile.school.getSchoolApiAccess(),
            defaultLessonIds = profile.defaultLessons.filterValues { it }.keys.map { it.id },
            from = null,
            to = null
        )
        if (response is Response.Error) return response
        if (response !is Response.Success) throw IllegalStateException("response is not successful: $response")

        val groupCache = groupRepository.getBySchool(profile.school.id)
        val defaultLessonCache = defaultLessonRepository.getBySchool(profile.school.id)
        val vppIdCache = vppIdRepository.getVppIds()
        val homeworkCache = homeworkRepository.getByGroup(profile.group.id)
            .map { flowData -> flowData.filterIsInstance<Homework.CloudHomework>() }

        var cachedGroups = groupCache.latest()
        val cachedDefaultLessons = defaultLessonCache.latest()
        var cachedVppIds = vppIdCache.latest()

        val downloadedHomework = response.data.mapNotNull { homeworkResponse ->
            val group = homeworkResponse.group?.let updateGroup@{ groupId ->
                cachedGroups.firstOrNull { it.id == homeworkResponse.group } ?:
                groupRepository.getByIdWithCaching(groupId, profile.school).let { groupUpdateResponse ->
                    if (groupUpdateResponse !is Response.Success) return@mapNotNull null
                    cachedGroups = groupCache.latest()
                    cachedGroups.firstOrNull { it.id == groupId } ?: return@mapNotNull null
                }
            }
            val defaultLesson = homeworkResponse.defaultLesson?.let getDefaultLesson@{ defaultLessonId ->
                cachedDefaultLessons.firstOrNull { it.id == defaultLessonId } ?: return@mapNotNull null
            }
            val createdBy = cachedVppIds.firstOrNull { it.id == homeworkResponse.createdBy } ?: run {
                vppIdRepository.getVppIdByIdWithCaching(profile.school.getSchoolApiAccess(), homeworkResponse.createdBy).let { vppIdResponse ->
                    if (vppIdResponse !is Response.Success) return@mapNotNull null
                    cachedVppIds = vppIdCache.latest()
                    cachedVppIds.firstOrNull { it.id == homeworkResponse.createdBy } ?: return@mapNotNull null
                }
            }

            Homework.CloudHomework(
                id = homeworkResponse.id,
                createdAt = homeworkResponse.createdAt,
                dueTo = homeworkResponse.dueTo,
                tasks = homeworkResponse.tasks.map { homeworkTaskResponse ->
                    Homework.HomeworkTask(
                        id = homeworkTaskResponse.id,
                        content = homeworkTaskResponse.content,
                        isDone = null
                    )
                },
                defaultLesson = defaultLesson,
                group = group,
                isPublic = homeworkResponse.isPublic,
                createdBy = createdBy
            )
        }.also { homeworkRepository.upsert(it) }

        val cachedHomework = homeworkCache.latest()

        cachedHomework.filter { it.id !in downloadedHomework.map { it.id } }.let { homeworkToDelete ->
            homeworkRepository.deleteById(homeworkToDelete.map { it.id })
        }

        downloadedHomework.size
        response.data.size
        return Response.Success(Unit)
    }
}