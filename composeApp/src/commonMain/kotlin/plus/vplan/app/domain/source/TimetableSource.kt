package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.Uuid

class TimetableSource(
    private val timetableRepository: TimetableRepository
) : CacheableItemSource<Lesson>() {
    override fun getAll(configuration: FetchConfiguration<Lesson>): Flow<List<Cacheable<Lesson>>> {
        TODO("Not yet implemented")
    }

    fun getBySchool(schoolId: Int, configuration: FetchConfiguration<Lesson>): Flow<List<Cacheable<Lesson>>> = channelFlow {
        timetableRepository.getTimetableForSchool(schoolId).map { it.map { lesson -> lesson.id } }.collectLatest { timetableLessonIds ->
            combine(timetableLessonIds.map { getById(it, configuration) }) { it }.collectLatest { send(it.toList()) }
        }
    }

    override fun getById(id: String, configuration: FetchConfiguration<Lesson>): Flow<Cacheable<Lesson>> = channelFlow {
        timetableRepository.getById(Uuid.parseHex(id)).collectLatest { timetableCache ->
            if (timetableCache == null) return@collectLatest send(Cacheable.NotExisting(id))
            send(Cacheable.Loaded(timetableCache))
            val timetableLesson = MutableStateFlow(timetableCache)
            launch { timetableLesson.collectLatest { send(Cacheable.Loaded(it)) } }

            if (configuration is Lesson.Fetch) {
                if (configuration.week is Week.Fetch) launch {
                    App.weekSource.getById(timetableCache.week.getItemId(), configuration.week).collectLatest {
                        timetableLesson.value = timetableLesson.value.copy(week = it)
                    }
                }
                // TODO add all other fetch options
            }
        }
    }
}