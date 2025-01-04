package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonSource(
    private val defaultLessonRepository: DefaultLessonRepository
) : CacheableItemSource<DefaultLesson> {
    override fun getAll(configuration: CacheableItemSource.FetchConfiguration<DefaultLesson>): Flow<List<Cacheable<DefaultLesson>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: CacheableItemSource.FetchConfiguration<DefaultLesson>
    ): Flow<Cacheable<DefaultLesson>> = channelFlow {
        defaultLessonRepository.getById(id).collect { cachedDefaultLesson ->
            if (cachedDefaultLesson == null) return@collect send(Cacheable.NotExisting(id))
            send(Cacheable.Loaded(cachedDefaultLesson))
            val defaultLesson = MutableStateFlow(cachedDefaultLesson)
            launch { defaultLesson.collectLatest { send(Cacheable.Loaded(it)) } }
            if (configuration is DefaultLesson.Fetch) {
                if (configuration.course is Course.Fetch) launch {
                    defaultLesson.value.course?.let { courseId ->

                    }
                }
            }
        }
    }
}