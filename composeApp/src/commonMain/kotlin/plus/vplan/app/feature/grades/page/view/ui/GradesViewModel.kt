package plus.vplan.app.feature.grades.page.view.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.CalculatorGrade
import plus.vplan.app.feature.grades.domain.usecase.GetCurrentIntervalUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase

class GradesViewModel(
    private val getCurrentIntervalUseCase: GetCurrentIntervalUseCase,
    private val calculateAverageUseCase: CalculateAverageUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val lockUseCase: LockGradesUseCase
) : ViewModel() {
    var state by mutableStateOf(GradesState())
        private set

    private var updateFullAverageJob: Job? = null
    private val updateAverageForSubjectJobs = mutableMapOf<Int, Job>()
    private val updateAverageForCategoryJobs = mutableMapOf<Int, Job>()

    fun init(vppIdId: Int) {
        viewModelScope.launch {
            state = GradesState()
            getGradeLockStateUseCase().collectLatest { areGradesLocked ->
                state = state.copy(gradeLockState = areGradesLocked)
                if (!areGradesLocked.canAccess) return@collectLatest
                App.vppIdSource.getById(vppIdId).filterIsInstance<CacheState.Done<VppId>>().map { it.data }.collectLatest collectLatestGrades@{ vppId ->
                    val interval = getCurrentIntervalUseCase()
                    state = state.copy(vppId = vppId, currentInterval = interval)
                    if (interval == null ||vppId !is VppId.Active) return@collectLatestGrades

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
                                        .toMap(),
                                    calculatorGrades = state.subjects.find { it.id == subjectId }?.categories?.find { it.name == type }?.calculatorGrades ?: emptyList()
                                )
                            }.sortedBy { it.name }
                        )
                    }
                        .sortedBy { it.name }
                        .let {
                            state = state.copy(subjects = it)
                            updateFullAverage(true)
                        }
                }
            }
        }
    }

    private fun updateFullAverage(updateSubjects: Boolean) {
        updateFullAverageJob?.cancel()
        updateFullAverageJob = viewModelScope.launch {
            val interval = getCurrentIntervalUseCase() ?: return@launch
            val grades = state.subjects.flatMap { subject -> subject.categories.map { category -> category.grades.filterValues { it == true }.keys.toList() } }.flatten()
            state = state.copy(fullAverage = null)
            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades = state.subjects.flatMap { subject ->
                    subject.categories.flatMap { category ->
                        category.calculatorGrades.map { calculatorGradeValue ->
                            CalculatorGrade.CustomGrade(
                                grade = calculatorGradeValue,
                                subject = state.allGrades.first { grade -> grade.subjectId == subject.id }.subject.getFirstValue()!!,
                                type = category.name
                            )
                        }
                    }
                }
            )
            state = state.copy(fullAverage = average)
        }
        if (updateSubjects) state.subjects.forEach { subject -> updateAverageForSubject(subject.id) }
    }

    private fun updateAverageForSubject(subjectId: Int) {
        updateAverageForSubjectJobs[subjectId]?.cancel()
        updateAverageForSubjectJobs[subjectId] = viewModelScope.launch {
            val interval = getCurrentIntervalUseCase() ?: return@launch
            val subject = state.subjects.find { it.id == subjectId } ?: return@launch
            val grades = subject.categories.flatMap { category -> category.grades.filterValues { it == true }.keys.toList() }
            state = state.copy(subjects = state.subjects.map { currentSubject ->
                if (currentSubject.id == subjectId) currentSubject.copy(average = null)
                else currentSubject
            })
            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades = subject.categories.flatMap { category ->
                    category.calculatorGrades.map { calculatorGradeValue ->
                        CalculatorGrade.CustomGrade(
                            grade = calculatorGradeValue,
                            subject = state.allGrades.first { grade -> grade.subjectId == subject.id }.subject.getFirstValue()!!,
                            type = category.name
                        )
                    }
                }
            )
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
            val subject = state.subjects.first { it.categories.any { category -> category.id == categoryId } }
            val category = state.subjects.flatMap { s -> s.categories }.find { it.id == categoryId } ?: return@launch
            val grades = category.grades.filterValues { it == true }.keys.toList()
            state = state.copy(subjects = state.subjects.map { currentSubject ->
                currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                    if (currentCategory.id == categoryId) currentCategory.copy(average = null)
                    else currentCategory
                })
            })
            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades =
                    category.calculatorGrades.map { calculatorGradeValue ->
                        CalculatorGrade.CustomGrade(
                            grade = calculatorGradeValue,
                            subject = state.allGrades.first { grade -> grade.subjectId == subject.id }.subject.getFirstValue()!!,
                            type = category.name
                        )
                    }
            )
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
                    updateFullAverage(false)
                }
                is GradeDetailEvent.ToggleEditMode -> state = state.copy(isInEditMode = !state.isInEditMode)
                is GradeDetailEvent.AddGrade -> {
                    state = state.copy(subjects = state.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            if (currentCategory.id == event.categoryId) currentCategory.copy(calculatorGrades = currentCategory.calculatorGrades + event.grade)
                            else currentCategory
                        })
                    })
                    updateFullAverage(false)
                    updateAverageForSubject(state.subjects.first { it.categories.any { category -> category.id == event.categoryId } }.id)
                }
                is GradeDetailEvent.RemoveGrade -> {
                    state = state.copy(subjects = state.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            if (currentCategory.id == event.categoryId) currentCategory.copy(calculatorGrades = currentCategory.calculatorGrades - event.grade)
                            else currentCategory
                        })
                    })
                    updateFullAverage(false)
                    updateAverageForSubject(state.subjects.first { it.categories.any { category -> category.id == event.categoryId } }.id)
                }
                is GradeDetailEvent.ResetAdditionalGrades -> {
                    state = state.copy(subjects = state.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            currentCategory.copy(calculatorGrades = emptyList())
                        })
                    })
                    updateFullAverage(true)
                }
                is GradeDetailEvent.Refresh -> {
                    state = state.copy(isUpdating = true)
                    syncGradesUseCase(true)
                    state = state.copy(isUpdating = false)
                }
                is GradeDetailEvent.RequestGradeUnlock -> requestGradeUnlockUseCase()
                is GradeDetailEvent.RequestGradeLock -> lockUseCase()
            }
        }
    }
}

data class GradesState(
    val fullAverage: Double? = null,
    val currentInterval: Interval? = null,
    val vppId: VppId? = null,
    val isInEditMode: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val isUpdating: Boolean = false,
    val gradeLockState: GradeLockState? = null,
) {
    val allGrades: List<Grade>
        get() = subjects.flatMap { subject -> subject.categories.flatMap { category -> category.grades.filterValues { it == true }.keys } }
}

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
        val grades: Map<Grade, Boolean?>,
        val calculatorGrades: List<Int>
    )
}

sealed class GradeDetailEvent {
    data class ToggleConsiderForFinalGrade(val grade: Grade) : GradeDetailEvent()
    data object ToggleEditMode : GradeDetailEvent()

    data class AddGrade(val categoryId: Int, val grade: Int) : GradeDetailEvent()
    data class RemoveGrade(val categoryId: Int, val grade: Int) : GradeDetailEvent()

    data object Refresh: GradeDetailEvent()

    data object ResetAdditionalGrades : GradeDetailEvent()

    data object RequestGradeUnlock: GradeDetailEvent()
    data object RequestGradeLock: GradeDetailEvent()
}