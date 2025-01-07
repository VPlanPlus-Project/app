package plus.vplan.app.domain.source

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
): CacheableItemSource<Homework>() {
    @OptIn(FlowPreview::class)
    override fun getAll(configuration: FetchConfiguration<Homework>): Flow<List<Cacheable<Homework>>> = channelFlow {
        var previousWasEmpty = true
        homeworkRepository.getAll().collectLatest { homeworkEmission ->
            if (homeworkEmission.isEmpty()) {
                previousWasEmpty = true
                send(emptyList())
                return@collectLatest
            }
            val debounce = if (previousWasEmpty) 0L else 200L
            previousWasEmpty = false
            combine(homeworkEmission.map { getById(it.getItemId(), configuration) }) { it.toList() }.debounce(debounce).collectLatest { send(it) }
        }
    }

    override fun getById(
        id: String,
        configuration: FetchConfiguration<Homework>
    ): Flow<Cacheable<Homework>> {
        return configuredCache.getOrPut("${id}_$configuration") { channelFlow {
            cache.getOrPut(id) { homeworkRepository.getById(id.toInt()).distinctUntilChanged() }.collectLatest { cacheableHomework ->
                if (cacheableHomework !is Cacheable.Loaded) {
                    send(cacheableHomework)
                    return@collectLatest
                }
                val homework = MutableStateFlow(cacheableHomework)
                launch { homework.collectLatest { send(it) } }

                if (configuration is FetchConfiguration.Ignore) return@collectLatest
                if (cacheableHomework.value is Homework.CloudHomework && configuration is Homework.Fetch) {
                    if (configuration.vppId is FetchConfiguration.Fetch) { launch {
                        App.vppIdSource.getById(cacheableHomework.value.createdBy.getItemId(), configuration.vppId).collect { cacheableVppId ->
                            homework.value = homework.value.copy((homework.value.value as Homework.CloudHomework).copy(createdBy = cacheableVppId))
                        }
                    } }
                }
                if (configuration is Homework.Fetch) {
                    if (configuration.group is Group.Fetch && cacheableHomework.value.group != null) { launch {
                        App.groupSource.getById(cacheableHomework.value.group!!.getItemId(), configuration.group).collectLatest {
                            homework.value = homework.value.copy(homework.value.value.copyBase(group = it))
                        }
                    } }

                    if (configuration.defaultLesson is DefaultLesson.Fetch && cacheableHomework.value.defaultLesson != null) { launch {
                        App.defaultLessonSource.getById(cacheableHomework.value.defaultLesson!!.getItemId(), configuration.defaultLesson).collectLatest {
                            homework.value = homework.value.copy(homework.value.value.copyBase(defaultLesson = it))
                        }
                    } }
                }
            }
        } }
    }
}