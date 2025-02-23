package plus.vplan.app.feature.grades.page.view.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import kotlinx.coroutines.Job
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetCurrentIntervalUseCase

class GradesViewModel(
    private val getCurrentIntervalUseCase: GetCurrentIntervalUseCase,
    private val calculateAverageUseCase: CalculateAverageUseCase
) : ViewModel() {
    var state by mutableStateOf(GradesState())
        private set

    private var updateFullAverageJob: Job? = null
    private val updateAverageForSubjectJobs = mutableMapOf<Int, Job>()
    private val updateAverageForCategoryJobs = mutableMapOf<Int, Job>()

    fun init(vppIdId: Int) {
        viewModelScope.launch {
            state = GradesState()
            App.vppIdSource.getById(vppIdId).filterIsInstance<CacheState.Done<VppId>>().map { it.data }.collectLatest { vppId ->
                val interval = getCurrentIntervalUseCase()
                state = state.copy(vppId = vppId, currentInterval = interval)
                if (interval == null ||vppId !is VppId.Active) return@collectLatest

                val grades = App.gradeSource.getAll().map { it.filterIsInstance<CacheState.Done<Grade>>().map { gradeState -> gradeState.data } }.first()
                grades.groupBy { it.subjectId }.map { (subjectId, gradesForSubject) ->
                    val subject = App.subjectSource.getById(subjectId).getFirstValue()!!
                    val calculationRule = subject.finalGrade?.first()?.calculationRule
                    Subject(
                        id = subjectId,
                        average = null,
                        name = subject.name,
                        categories = gradesForSubject.groupBy { it.collection.getFirstValue()!!.type }.map { (type, gradesForType) ->
                            val weight = if (calculationRule != null) {
                                val regex = Regex("""([\d.]+)\s*\*\s*\(\(($type)_sum\)/\(($type)_count\)\)""")
                                regex.find(calculationRule)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                            } else 1.0

                            Subject.Category(
                                id = (subjectId.toString() + type).hashCode(),
                                name = type,
                                average = null,
                                count = gradesForType.size,
                                weight = weight,
                                grades = gradesForType
                                    .associateWith {
                                        if (it.value == null) null
                                        else it.isSelectedForFinalGrade
                                    }
                                    .toList()
                                    .sortedBy { it.first.givenAt }
                                    .toMap()
                            )
                        }.sortedBy { it.name }
                    )
                }
                    .sortedBy { it.name }
                    .let {
                        state = state.copy(subjects = it)
                        updateFullAverage()

                        state.subjects.forEach { subject ->
                            updateAverageForSubject(subject.id)
                        }
                    }
            }
        }
    }

    private fun updateFullAverage() {
        updateFullAverageJob?.cancel()
        updateFullAverageJob = viewModelScope.launch {
            val interval = getCurrentIntervalUseCase() ?: return@launch
            val grades = state.subjects.flatMap { subject -> subject.categories.map { category -> category.grades.filterValues { it == true }.keys.toList() } }.flatten()
            state = state.copy(fullAverage = null)
            val average = calculateAverageUseCase(grades, interval)
            state = state.copy(fullAverage = average)
        }
    }

    private fun updateAverageForSubject(subjectId: Int) {
        updateAverageForSubjectJobs[subjectId]?.cancel()
        updateAverageForSubjectJobs[subjectId] = viewModelScope.launch {
            val interval = getCurrentIntervalUseCase() ?: return@launch
            val subject = state.subjects.find { it.id == subjectId } ?: return@launch
            val grades = subject.categories.flatMap { category -> category.grades.filterValues { it == true }.keys.toList() }
            state = state.copy(subjects = state.subjects.map { currentSubjcet ->
                if (currentSubjcet.id == subjectId) currentSubjcet.copy(average = null)
                else currentSubjcet
            })
            val average = calculateAverageUseCase(grades, interval)
            state = state.copy(subjects = state.subjects.map { currentSubject ->
                if (currentSubject.id == subjectId) currentSubject.copy(average = average)
                else currentSubject
            })

            subject.categories.forEach { category ->
                updateAverageForCategory(category.id)
            }
        }
    }

    private fun updateAverageForCategory(categoryId: Int) {
        updateAverageForCategoryJobs[categoryId]?.cancel()
        updateAverageForCategoryJobs[categoryId] = viewModelScope.launch {
            val interval = getCurrentIntervalUseCase() ?: return@launch
            val category = state.subjects.flatMap { subject -> subject.categories }.find { it.id == categoryId } ?: return@launch
            val grades = category.grades.filterValues { it == true }.keys.toList()
            state = state.copy(subjects = state.subjects.map { currentSubject ->
                currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                    if (currentCategory.id == categoryId) currentCategory.copy(average = null)
                    else currentCategory
                })
            })
            val average = calculateAverageUseCase(grades, interval)
            state = state.copy(subjects = state.subjects.map { currentSubject ->
                currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                    if (currentCategory.id == categoryId) currentCategory.copy(average = average)
                    else currentCategory
                })
            })
        }
    }

    fun onEvent(event: GradeDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is GradeDetailEvent.ToggleConsiderForFinalGrade -> {
                    state = state.copy(subjects = state.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            currentCategory.copy(grades = currentCategory.grades.toList().associate { (grade, isSelectedForFinalGrade) ->
                                if (grade.id == event.grade.id) grade to !(isSelectedForFinalGrade ?: true)
                                else grade to isSelectedForFinalGrade
                            })
                        })
                    })
                    updateAverageForSubject(event.grade.subjectId)
                    updateFullAverage()
                }
            }
        }
    }
}

data class GradesState(
    val fullAverage: Double? = null,
    val currentInterval: Interval? = null,
    val vppId: VppId? = null,
    val subjects: List<Subject> = emptyList()
)

data class Subject(
    val id: Int,
    val average: Double?,
    val name: String,
    val categories: List<Category>
) {
    data class Category(
        val id: Int,
        val name: String,
        val average: Double?,
        val count: Int,
        val weight: Double,
        val grades: Map<Grade, Boolean?>
    )
}

sealed class GradeDetailEvent {
    data class ToggleConsiderForFinalGrade(val grade: Grade) : GradeDetailEvent()
}