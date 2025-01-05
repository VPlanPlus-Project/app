package plus.vplan.app.feature.dev

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase

class DevViewModel(
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val homeworkRepository: HomeworkRepository,
    private val teacherRepository: TeacherRepository,
    private val keyValueRepository: KeyValueRepository
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            val c = Profile.Fetch(studentProfile = Profile.StudentProfile.Fetch(group = Group.Fetch(school = School.Fetch())))
            keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().collectLatest {
                App.profileSource.getById(it, c)
                    .filterIsInstance<Cacheable.Loaded<Profile>>()
                    .filter { it.isConfigSatisfied(c, false) }
                    .collectLatest { state = state.copy(profile = it.value) }
            }
        }
        viewModelScope.launch {
            App.homeworkSource.getById(
                id = 208.toString(),
                configuration = Homework.Fetch(
                    vppId = VppId.Fetch()
                )
            ).collect {
                state = state.copy(homework = listOf(it))
            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> teacherRepository.getBySchoolWithCaching(state.profile!!.school.toValueOrNull()!!)
                DevEvent.Clear -> homeworkRepository.clearCache()
            }
        }
    }
}

data class DevState(
    val profile: Profile? = null,
    val reloadResponse: Response<Unit>? = null,
    val homework: List<Cacheable<Homework>> = emptyList(),
)

sealed class DevEvent {
    data object Refresh : DevEvent()
    data object Clear : DevEvent()
}