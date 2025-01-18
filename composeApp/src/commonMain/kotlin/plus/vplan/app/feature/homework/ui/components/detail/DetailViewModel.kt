package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

class DetailViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) : ViewModel() {
    var state by mutableStateOf(DetailState())
        private set

    fun init(homeworkId: Int) {
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                App.homeworkSource.getById(homeworkId)
            ) { profile, homeworkData ->
                if (homeworkData !is CacheState.Done || profile !is Profile.StudentProfile) return@combine null
                val homework = homeworkData.data
                homework.getDefaultLessonItem()
                homework.getGroupItem()
                homework.getTaskItems()
                homework.taskItems!!.onEach { it.setIsDone(profile) }
                if (homework is Homework.CloudHomework) homework.getCreatedBy()
                state.copy(homework = homework)
            }.filterNotNull().collectLatest { state = it }
        }
    }
}

data class DetailState(
    val homework: Homework? = null
)