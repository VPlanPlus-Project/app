package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class GetHomeworkForProfileUseCase(
    private val homeworkRepository: HomeworkRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profile: Profile.StudentProfile): Flow<List<Homework>> {
        val group = profile.group
        val enabledSubjectInstances = profile.subjectInstanceConfiguration.filterValues { it }.keys
        return homeworkRepository.getByGroup(group).map { it.map { homework -> homework.id } }
            .map { combine(it.map { id -> App.homeworkSource.getById(id) }) { items -> items.toList() } }
            .map { it.map { homeworkState -> homeworkState.filterIsInstance<CacheState.Done<Homework>>().map { item -> item.data } } }
            .flattenMerge()
            .onEach {
                it.onEach { homework ->
                    homework.group?.getFirstValue()
                    homework.subjectInstance?.getFirstValue()
                    when (homework) {
                        is Homework.CloudHomework -> homework.getCreatedBy()
                        is Homework.LocalHomework -> homework.getCreatedByProfile()
                    }
                }
            }
            .map {
                it.filter { homework -> homework.subjectInstanceId == null || homework.subjectInstanceId in enabledSubjectInstances }.sortedByDescending { homework -> homework.createdAt }
            }
    }
}