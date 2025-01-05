package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.CourseRepository

class CourseSource(
    private val courseRepository: CourseRepository
) : CacheableItemSource<Course>() {
    override fun getAll(configuration: FetchConfiguration<Course>): Flow<List<Cacheable<Course>>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: String, configuration: FetchConfiguration<Course>): Flow<Cacheable<Course>> {
        return configuredCache.getOrPut("${id}_$configuration") { channelFlow {
            cache.getOrPut(id) { courseRepository.getById(id).distinctUntilChanged() }.collectLatest { cachedCourse ->
                if (cachedCourse == null) return@collectLatest send(Cacheable.NotExisting(id))
                val course = MutableStateFlow(cachedCourse)
                launch { course.collectLatest { send(Cacheable.Loaded(it)) } }
                if (configuration is Course.Fetch) {
                    if (configuration.groups is Group.Fetch) launch {
                        combine(cachedCourse.groups.map { App.groupSource.getById(it.getItemId(), configuration.groups) }) { it.toList() }.collectLatest { course.value = course.value.copy(groups = it) }
                    }
                }
            }
        } }
    }
}