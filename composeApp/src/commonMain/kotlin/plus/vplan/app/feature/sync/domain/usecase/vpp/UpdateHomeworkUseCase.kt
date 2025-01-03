package plus.vplan.app.feature.sync.domain.usecase.vpp

import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
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

        val groupResponse = homeworkRepository.getByGroup(
            authentication = profile.school.getSchoolApiAccess(),
            groupId = profile.group.id,
            from = null,
            to = null
        )
        if (groupResponse is Response.Error) return groupResponse
        if (groupResponse !is Response.Success) throw IllegalStateException("groupResponse is not successful: $groupResponse")

        val groupCache = groupRepository.getBySchool(profile.school.id)
        val defaultLessonCache = defaultLessonRepository.getBySchool(profile.school.id)
        val homeworkCache = homeworkRepository.getByGroup(profile.group.id)
            .map { flowData -> flowData.filterIsInstance<Homework.CloudHomework>() }

        var cachedGroups = groupCache.latest()
        val cachedDefaultLessons = defaultLessonCache.latest()

        val downloadedHomework = response.data.plus(groupResponse.data).mapNotNull { homeworkResponse ->
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

            Homework.CloudHomework(
                id = homeworkResponse.id,
                createdAt = homeworkResponse.createdAt,
                dueTo = homeworkResponse.dueTo,
                tasks = homeworkResponse.tasks.map { homeworkTaskResponse ->
                    Cacheable.Loaded(Homework.HomeworkTask(
                        id = homeworkTaskResponse.id,
                        content = homeworkTaskResponse.content,
                        homework = Cacheable.Uninitialized(homeworkResponse.id.toString()),
                        isDone = null
                    ))
                },
                defaultLesson = defaultLesson,
                group = group,
                isPublic = homeworkResponse.isPublic,
                createdBy = Cacheable.Uninitialized(homeworkResponse.createdBy.toString())
            )
        }.also { homeworkItems ->
            homeworkItems
                .map { it.createdBy }
                .filterIsInstance<Cacheable.Uninitialized<VppId>>()
                .distinctBy { it.id }
                .forEach {
                    vppIdRepository.getVppIdById(it.id.toInt()).takeUnless { cachedVppId ->
                        cachedVppId is Cacheable.Loaded<*> || cachedVppId is Cacheable.Error<*>
                    }?.last()
                }
            homeworkRepository.upsert(homeworkItems)
        }

        val cachedHomework = homeworkCache.latest()

        cachedHomework.filter { cachedItem -> cachedItem.id !in downloadedHomework.map { it.id } }.let { homeworkToDelete ->
            homeworkRepository.deleteById(homeworkToDelete.map { it.id })
        }

        downloadedHomework.size
        response.data.size
        return Response.Success(Unit)
    }
}