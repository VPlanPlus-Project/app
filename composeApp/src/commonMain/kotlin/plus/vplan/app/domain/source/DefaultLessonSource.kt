package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonSource(
    private val defaultLessonRepository: DefaultLessonRepository
) : CacheableItemSource<DefaultLesson>() {

    override fun getAll(configuration: FetchConfiguration<DefaultLesson>): Flow<List<Cacheable<DefaultLesson>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: FetchConfiguration<DefaultLesson>
    ): Flow<Cacheable<DefaultLesson>> {
        return configuredCache.getOrPut("${id}_$configuration") {
            channelFlow {
                cache.getOrPut(id) { defaultLessonRepository.getById(id).distinctUntilChanged() }.collectLatest { cachedDefaultLesson ->
                    if (cachedDefaultLesson == null) return@collectLatest send(Cacheable.NotExisting(id))
                    send(Cacheable.Loaded(cachedDefaultLesson))
                    val defaultLesson = MutableStateFlow(cachedDefaultLesson)
                    launch { defaultLesson.collectLatest { send(Cacheable.Loaded(it)) } }
                    if (configuration is DefaultLesson.Fetch) {
                        if (configuration.course is Course.Fetch) launch {
                            defaultLesson.value.course?.let { courseId ->
                                App.courseSource.getById(courseId.getItemId(), configuration.course).collectLatest { defaultLesson.value = defaultLesson.value.copy(course = it) }
                            }
                        }
                    }
                }
            }
        }
    }
}