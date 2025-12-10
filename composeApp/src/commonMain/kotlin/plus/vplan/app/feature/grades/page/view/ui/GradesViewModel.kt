package plus.vplan.app.feature.grades.page.view.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
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
    private val lockUseCase: LockGradesUseCase,
    private val vppIdRepository: VppIdRepository
) : ViewModel() {
    private val gradeState = MutableStateFlow(GradesState())
    val state = gradeState.asStateFlow()

    private var updateFullAverageJob: Job? = null
    private val updateAverageForSubjectJobs = mutableMapOf<Int, Job>()
    private val updateAverageForCategoryJobs = mutableMapOf<Int, Job>()

    private var grades = listOf<Grade>()

    private suspend fun setGrades() {
        grades
            .filter { it.collection.getFirstValueOld()!!.intervalId in listOfNotNull(gradeState.value.selectedInterval?.id, gradeState.value.selectedInterval?.includedIntervalId) }
            .groupBy { it.subjectId }.map { (subjectId, gradesForSubject) ->
                val subject = App.subjectSource.getById(subjectId).getFirstValueOld()!!
                val calculationRule = subject.finalGrade?.first()?.calculationRule
                Subject(
                    id = subjectId,
                    average = null,
                    name = subject.name,
                    categories = gradesForSubject.groupBy { it.collection.getFirstValueOld()!!.type }.map { (type, gradesForType) ->
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
                            calculatorGrades = gradeState.value.subjects.find { it.id == subjectId }?.categories?.find { it.name == type }?.calculatorGrades ?: emptyList()
                        )
                    }.sortedBy { it.name }
            )
        }
            .sortedBy { it.name }
            .let {
                gradeState.value = gradeState.value.copy(subjects = it)
                updateFullAverage(true)
            }
    }

    private var mainJob: Job? = null
    fun init(vppIdId: Int) {
        mainJob?.cancel()
        gradeState.value = GradesState()
        mainJob = viewModelScope.launch {
            val activeJobs = mutableListOf<Job>()
            vppIdRepository.getById(vppIdId, ResponsePreference.Fast)
                .filterIsInstance<CacheState.Done<VppId.Active>>()
                .map { it.data }
                .filter { it.schulverwalterConnection != null }
                .distinctUntilChangedBy { it.id.hashCode() + it.schulverwalterConnection.hashCode() }
                .onEach { vppIdActive ->
                    activeJobs.forEach { it.cancelAndJoin() }
                    activeJobs.clear()
                    gradeState.value = gradeState.value.copy(vppId = vppIdActive)
                }
                .collectLatest { vppId ->
                    launch {
                        App.intervalSource.getForUser(vppId.schulverwalterConnection!!.userId)
                            .collectLatest { gradeState.value = gradeState.value.copy(intervals = it) }
                    }.let(activeJobs::add)

                    launch {
                        getGradeLockStateUseCase().collectLatest { areGradesLocked ->
                            gradeState.update { it.copy(gradeLockState = areGradesLocked) }
                            if (!areGradesLocked.canAccess) return@collectLatest

                            val interval = getCurrentIntervalUseCase(vppId.schulverwalterConnection!!.userId)
                                ?: return@collectLatest

                            gradeState.update { it.copy(vppId = vppId, currentInterval = interval) }

                            gradeState.update {
                                it.copy(
                                    intervals = gradeState.value.intervals,
                                    selectedInterval = interval
                                )
                            }

                            grades = App.gradeSource.getAll().map { it.filterIsInstance<CacheState.Done<Grade>>().map { gradeState -> gradeState.data } }.first()

                            setGrades()
                        }
                    }.let(activeJobs::add)
                }
        }
    }

    private fun updateFullAverage(updateSubjects: Boolean) {
        updateFullAverageJob?.cancel()
        updateFullAverageJob = viewModelScope.launch {
            val interval = gradeState.value.selectedInterval ?: return@launch
            val grades = gradeState.value.subjects.flatMap { subject -> subject.categories.map { category -> category.grades.filterValues { it == true }.keys.toList() } }.flatten()
            gradeState.update { it.copy(fullAverage = null) }
            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades = gradeState.value.subjects.flatMap { subject ->
                    subject.categories.flatMap { category ->
                        category.calculatorGrades.map { calculatorGradeValue ->
                            CalculatorGrade.CustomGrade(
                                grade = calculatorGradeValue,
                                subject = gradeState.value.allGrades.first { grade -> grade.subjectId == subject.id }.subject.getFirstValueOld()!!,
                                type = category.name
                            )
                        }
                    }
                }
            )
            gradeState.update { it.copy(fullAverage = average) }
        }
        if (updateSubjects) gradeState.value.subjects.forEach { subject -> updateAverageForSubject(subject.id) }
    }

    private fun updateAverageForSubject(subjectId: Int) {
        updateAverageForSubjectJobs[subjectId]?.cancel()
        updateAverageForSubjectJobs[subjectId] = viewModelScope.launch {
            val interval = gradeState.value.selectedInterval ?: return@launch
            val subject = gradeState.value.subjects.find { it.id == subjectId } ?: return@launch
            val grades = subject.categories.flatMap { category -> category.grades.filterValues { it == true }.keys.toList() }

            run {
                val subjects = gradeState.value.subjects.map { currentSubject ->
                    if (currentSubject.id == subjectId) currentSubject.copy(average = null)
                    else currentSubject
                }
                gradeState.update { it.copy(subjects = subjects) }
            }

            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades = subject.categories.flatMap { category ->
                    category.calculatorGrades.map { calculatorGradeValue ->
                        CalculatorGrade.CustomGrade(
                            grade = calculatorGradeValue,
                            subject = gradeState.value.allGrades.first { grade -> grade.subjectId == subject.id }.subject.getFirstValueOld()!!,
                            type = category.name
                        )
                    }
                }
            )
            run {
                val subjects = gradeState.value.subjects.map { currentSubject ->
                    if (currentSubject.id == subjectId) currentSubject.copy(average = average)
                    else currentSubject
                }
                gradeState.update { it.copy(subjects = subjects) }
            }

            subject.categories.forEach { category ->
                updateAverageForCategory(category.id)
            }
        }
    }

    private fun updateAverageForCategory(categoryId: Int) {
        updateAverageForCategoryJobs[categoryId]?.cancel()
        updateAverageForCategoryJobs[categoryId] = viewModelScope.launch {
            val interval = gradeState.value.selectedInterval ?: return@launch
            val subject = gradeState.value.subjects.first { it.categories.any { category -> category.id == categoryId } }
            val category = gradeState.value.subjects.flatMap { s -> s.categories }.find { it.id == categoryId } ?: return@launch
            val grades = category.grades.filterValues { it == true }.keys.toList()

            run {
                val subjects = gradeState.value.subjects.map { currentSubject ->
                    currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                        if (currentCategory.id == categoryId) currentCategory.copy(average = null)
                        else currentCategory
                    })
                }
                gradeState.update { it.copy(subjects = subjects) }
            }

            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades =
                    category.calculatorGrades.map { calculatorGradeValue ->
                        CalculatorGrade.CustomGrade(
                            grade = calculatorGradeValue,
                            subject = gradeState.value.allGrades.first { grade -> grade.subjectId == subject.id }.subject.getFirstValueOld()!!,
                            type = category.name
                        )
                    }
            )

            run {
                val subjects = gradeState.value.subjects.map { currentSubject ->
                    currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                        if (currentCategory.id == categoryId) currentCategory.copy(average = average)
                        else currentCategory
                    })
                }
                gradeState.update { it.copy(subjects = subjects) }
            }
        }
    }

    fun onEvent(event: GradeDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is GradeDetailEvent.ToggleConsiderForFinalGrade -> {
                    run {
                        val subjects = gradeState.value.subjects.map { currentSubject ->
                            currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                                currentCategory.copy(grades = currentCategory.grades.toList().associate { (grade, isSelectedForFinalGrade) ->
                                    if (grade.id == event.grade.id) grade to !(isSelectedForFinalGrade ?: true)
                                    else grade to isSelectedForFinalGrade
                                })
                            })
                        }
                        gradeState.update { it.copy(subjects = subjects) }
                    }
                    updateAverageForSubject(event.grade.subjectId)
                    updateFullAverage(false)
                }
                is GradeDetailEvent.ToggleEditMode -> {
                    gradeState.update { it.copy(isInEditMode = !gradeState.value.isInEditMode) }
                }
                is GradeDetailEvent.AddGrade -> {
                    val subjects = gradeState.value.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            if (currentCategory.id == event.categoryId) currentCategory.copy(calculatorGrades = currentCategory.calculatorGrades + event.grade)
                            else currentCategory
                        })
                    }
                    gradeState.update { it.copy(subjects = subjects) }
                    updateFullAverage(false)
                    updateAverageForSubject(gradeState.value.subjects.first { it.categories.any { category -> category.id == event.categoryId } }.id)
                }
                is GradeDetailEvent.RemoveGrade -> {
                    val subjects = gradeState.value.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            if (currentCategory.id == event.categoryId) currentCategory.copy(calculatorGrades = currentCategory.calculatorGrades - event.grade)
                            else currentCategory
                        })
                    }
                    gradeState.update { it.copy(subjects = subjects) }
                    updateFullAverage(false)
                    updateAverageForSubject(gradeState.value.subjects.first { it.categories.any { category -> category.id == event.categoryId } }.id)
                }
                is GradeDetailEvent.ResetAdditionalGrades -> {
                    gradeState.value = gradeState.value.copy(subjects = gradeState.value.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            currentCategory.copy(calculatorGrades = emptyList())
                        })
                    })
                    updateFullAverage(true)
                }
                is GradeDetailEvent.Refresh -> {
                    gradeState.update { it.copy(isUpdating = true) }
                    try {
                        syncGradesUseCase(true)
                    } finally {
                        gradeState.update { it.copy(isUpdating = false) }
                    }
                }
                is GradeDetailEvent.RequestGradeUnlock -> requestGradeUnlockUseCase()
                is GradeDetailEvent.RequestGradeLock -> lockUseCase()
                is GradeDetailEvent.SelectInterval -> {
                    gradeState.update { it.copy(selectedInterval = event.interval) }
                    setGrades()
                }
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
    val intervals: List<Interval> = emptyList(),
    val selectedInterval: Interval? = null
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

    data class SelectInterval(val interval: Interval): GradeDetailEvent()
}