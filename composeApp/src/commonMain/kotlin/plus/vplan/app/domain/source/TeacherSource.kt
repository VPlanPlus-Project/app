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
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.TeacherRepository

class TeacherSource(
    private val teacherRepository: TeacherRepository
) : CacheableItemSource<Teacher>() {
    override fun getAll(configuration: FetchConfiguration<Teacher>): Flow<List<Cacheable<Teacher>>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: String, configuration: FetchConfiguration<Teacher>): Flow<Cacheable<Teacher>> {
        return configuredCache.getOrPut("${id}_$configuration") { channelFlow {
            cache.getOrPut(id) { teacherRepository.getById(id.toInt()).distinctUntilChanged() }.collectLatest { cachedTeacher ->
                if (cachedTeacher !is Cacheable.Loaded) return@collectLatest send(cachedTeacher)
                val teacher = MutableStateFlow(cachedTeacher.value)
                launch { teacher.collectLatest { send(Cacheable.Loaded(it)) } }

                if (configuration is Teacher.Fetch) {
                    if (configuration.school is School.Fetch) launch {
                        App.schoolSource.getById(cachedTeacher.value.school.getItemId(), configuration.school).collectLatest {
                            teacher.value = teacher.value.copy(school = it)
                        }
                    }
                }
            }
        } }
    }
}