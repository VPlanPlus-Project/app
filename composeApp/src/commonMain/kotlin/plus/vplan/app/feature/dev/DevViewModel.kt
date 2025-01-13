package plus.vplan.app.feature.dev

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import kotlin.uuid.Uuid

class DevViewModel(
    private val homeworkRepository: HomeworkRepository,
    private val keyValueRepository: KeyValueRepository
) : ViewModel() {
    var state by mutableStateOf(DevState())
        private set

    init {
        viewModelScope.launch {
            keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().collectLatest {
                App.profileSource.getById(Uuid.parseHex(it))
                    .filterIsInstance<CacheState.Done<Profile>>()
                    .collectLatest { state = state.copy(profile = it.data) }
            }
        }
        viewModelScope.launch {
//            App.homeworkSource.getAll(
//                configuration = Homework.Fetch(
//                    vppId = VppId.Fetch(),
//                    defaultLesson = DefaultLesson.Fetch(
//                        groups = Group.Fetch()
//                    ),
//                    group = Group.Fetch()
//                )
//            ).collect {
//                state = state.copy(homework = it)
//            }
        }
    }

    fun onEvent(event: DevEvent) {
        viewModelScope.launch {
            when (event) {
                DevEvent.Refresh -> {
                    state = state.copy(updateResponse = null)
//                    homeworkRepository.download(
//                        state.profile!!.school.toValueOrNull()!!.getSchoolApiAccess(),
//                        groupId = (state.profile as Profile.StudentProfile).group.toValueOrNull()!!.id,
//                        (state.profile as Profile.StudentProfile).defaultLessons.map { it.key.toValueOrNull()!!.id }).let {
//                        state = state.copy(updateResponse = it)
//                    }

                }

                DevEvent.Clear -> homeworkRepository.clearCache()
            }
        }
    }
}

data class DevState(
    val profile: Profile? = null,
    val homework: List<CacheState<Homework>> = emptyList(),
    val updateResponse: Response.Error? = null
)

sealed class DevEvent {
    data object Refresh : DevEvent()
    data object Clear : DevEvent()
}