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
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.CalculatorGrade
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import plus.vplan.app.utils.now

class GradesViewModel(
    private val calculateAverageUseCase: CalculateAverageUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val lockUseCase: LockGradesUseCase,
    private val vppIdRepository: VppIdRepository
) : ViewModel(), KoinComponent {
    private val gradeState = MutableStateFlow(GradesState())
    val state = gradeState.asStateFlow()

    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()

    private var updateFullAverageJob: Job? = null
    private val updateAverageForSubjectJobs = mutableMapOf<Int, Job>()
    private val updateAverageForCategoryJobs = mutableMapOf<Int, Job>()

    private var grades = listOf<BesteSchuleGrade>()

    private suspend fun setGrades() {
        grades
            .filter { grade ->
                grade.collection.first()!!.intervalId in listOfNotNull(
                    gradeState.value.selectedInterval?.id,
                    gradeState.value.selectedInterval?.includedIntervalId
                )
            }
            .groupBy { grade -> grade.collection.first()!!.subjectId }
            .map { (subjectId, gradesForSubject) ->
                val subject = besteSchuleSubjectsRepository.getSubjectFromCache(subjectId).first()!!
                val calculationRule = null // fixme subject.finalGrade?.first()?.calculationRule
                Subject(
                    id = subjectId,
                    average = null,
                    name = subject.fullName,
                    categories = gradesForSubject.groupBy { grade -> grade.collection.first()!!.type }
                        .map { (type, gradesForType) ->
                            val weight = if (calculationRule != null) { // fixme
                                val regex =
                                    Regex("""([\d.]+)\s*\*\s*\(\(($type)_sum\)/\(($type)_count\)\)""")
                                regex.find(calculationRule)?.groupValues?.get(1)?.toDoubleOrNull()
                                    ?: 1.0
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
                                calculatorGrades = gradeState.value.subjects.find { it.id == subjectId }?.categories?.find { it.name == type }?.calculatorGrades
                                    ?: emptyList()
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
                        besteSchuleIntervalsRepository.getIntervals(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleAccessToken = vppId.schulverwalterConnection!!.accessToken,
                            contextBesteschuleUserId = vppId.schulverwalterConnection.userId
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleInterval>>>()
                            .map { it.data.sortedByDescending { interval -> interval.from } }
                            .collectLatest { intervals ->
                                val currentInterval =
                                    intervals.firstOrNull { LocalDate.now() in it.from..it.to }
                                val isFirstEmissionForThisUser = state.value.intervals.isEmpty()
                                val selectedInterval =
                                    if (isFirstEmissionForThisUser) currentInterval
                                    else state.value.selectedInterval

                                gradeState.value = gradeState.value.copy(
                                    intervals = intervals,
                                    currentInterval = currentInterval,
                                    selectedInterval = selectedInterval
                                )

                                if (isFirstEmissionForThisUser) setGrades()
                            }
                    }.let(activeJobs::add)

                    launch {
                        getGradeLockStateUseCase().collectLatest { areGradesLocked ->
                            gradeState.update { it.copy(gradeLockState = areGradesLocked) }
                            if (!areGradesLocked.canAccess) return@collectLatest

                            besteSchuleGradesRepository.getGrades(
                                responsePreference = ResponsePreference.Fast,
                                contextBesteschuleUserId = vppId.schulverwalterConnection!!.userId,
                                contextBesteschuleAccessToken = vppId.schulverwalterConnection.accessToken
                            )
                                .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                                .map { it.data }
                                .collectLatest {
                                    grades = it
                                    setGrades()
                                }
                        }
                    }.let(activeJobs::add)
                }
        }
    }

    private fun updateFullAverage(updateSubjects: Boolean) {
        updateFullAverageJob?.cancel()
        updateFullAverageJob = viewModelScope.launch {
            val interval = gradeState.value.selectedInterval ?: return@launch
            val grades =
                gradeState.value.subjects.flatMap { subject -> subject.categories.map { category -> category.grades.filterValues { it == true }.keys.toList() } }
                    .flatten()
            gradeState.update { it.copy(fullAverage = null) }
            val average = calculateAverageUseCase(
                grades = grades,
                interval = interval,
                additionalGrades = gradeState.value.subjects.flatMap { subject ->
                    subject.categories.flatMap { category ->
                        category.calculatorGrades.map { calculatorGradeValue ->
                            CalculatorGrade.CustomGrade(
                                grade = calculatorGradeValue,
                                subject = gradeState.value.allGrades.first { grade -> grade.collection.first()!!.subjectId == subject.id }.collection.first()!!.subject.first()!!,
                                type = category.name
                            )
                        }
                    }
                }
            )
            gradeState.update { it.copy(fullAverage = average) }
        }
        if (updateSubjects) gradeState.value.subjects.forEach { subject ->
            updateAverageForSubject(
                subject.id
            )
        }
    }

    private fun updateAverageForSubject(subjectId: Int) {
        updateAverageForSubjectJobs[subjectId]?.cancel()
        updateAverageForSubjectJobs[subjectId] = viewModelScope.launch {
            val interval = gradeState.value.selectedInterval ?: return@launch
            val subject = gradeState.value.subjects.find { it.id == subjectId } ?: return@launch
            val grades =
                subject.categories.flatMap { category -> category.grades.filterValues { it == true }.keys.toList() }

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
                            subject = gradeState.value.allGrades.first { grade -> grade.collection.first()!!.subjectId == subject.id }.collection.first()!!.subject.first()!!,
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
            val subject =
                gradeState.value.subjects.first { it.categories.any { category -> category.id == categoryId } }
            val category =
                gradeState.value.subjects.flatMap { s -> s.categories }.find { it.id == categoryId }
                    ?: return@launch
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
                            subject = gradeState.value.allGrades.first { grade -> grade.collection.first()!!.subjectId == subject.id }.collection.first()!!.subject.first()!!,
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
                                currentCategory.copy(
                                    grades = currentCategory.grades.toList()
                                        .associate { (grade, isSelectedForFinalGrade) ->
                                            if (grade.id == event.grade.id) grade to !(isSelectedForFinalGrade
                                                ?: true)
                                            else grade to isSelectedForFinalGrade
                                        })
                            })
                        }
                        gradeState.update { it.copy(subjects = subjects) }
                    }
                    updateAverageForSubject(event.grade.collection.first()!!.subjectId)
                    updateFullAverage(false)
                }

                is GradeDetailEvent.ToggleEditMode -> {
                    gradeState.update { it.copy(isInEditMode = !gradeState.value.isInEditMode) }
                }

                is GradeDetailEvent.AddGrade -> {
                    val subjects = gradeState.value.subjects.map { currentSubject ->
                        currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                            if (currentCategory.id == event.categoryId) currentCategory.copy(
                                calculatorGrades = currentCategory.calculatorGrades + event.grade
                            )
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
                            if (currentCategory.id == event.categoryId) currentCategory.copy(
                                calculatorGrades = currentCategory.calculatorGrades - event.grade
                            )
                            else currentCategory
                        })
                    }
                    gradeState.update { it.copy(subjects = subjects) }
                    updateFullAverage(false)
                    updateAverageForSubject(gradeState.value.subjects.first { it.categories.any { category -> category.id == event.categoryId } }.id)
                }

                is GradeDetailEvent.ResetAdditionalGrades -> {
                    gradeState.value =
                        gradeState.value.copy(subjects = gradeState.value.subjects.map { currentSubject ->
                            currentSubject.copy(categories = currentSubject.categories.map { currentCategory ->
                                currentCategory.copy(calculatorGrades = emptyList())
                            })
                        })
                    updateFullAverage(true)
                }

                is GradeDetailEvent.Refresh -> {
                    gradeState.update { it.copy(isUpdating = true) }
                    try {
                        val selectedYearId = state.value.selectedInterval?.yearId
                        syncGradesUseCase(
                            allowNotifications = true,
                            yearId = selectedYearId
                        )
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
    val currentInterval: BesteSchuleInterval? = null,
    val vppId: VppId? = null,
    val isInEditMode: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val isUpdating: Boolean = false,
    val gradeLockState: GradeLockState? = null,
    val intervals: List<BesteSchuleInterval> = emptyList(),
    val selectedInterval: BesteSchuleInterval? = null
) {
    val allGrades: List<BesteSchuleGrade>
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
        val grades: Map<BesteSchuleGrade, Boolean?>,
        val calculatorGrades: List<Int>
    )
}

sealed class GradeDetailEvent {
    data class ToggleConsiderForFinalGrade(val grade: BesteSchuleGrade) : GradeDetailEvent()
    data object ToggleEditMode : GradeDetailEvent()

    data class AddGrade(val categoryId: Int, val grade: Int) : GradeDetailEvent()
    data class RemoveGrade(val categoryId: Int, val grade: Int) : GradeDetailEvent()

    data object Refresh : GradeDetailEvent()

    data object ResetAdditionalGrades : GradeDetailEvent()

    data object RequestGradeUnlock : GradeDetailEvent()
    data object RequestGradeLock : GradeDetailEvent()

    data class SelectInterval(val interval: BesteSchuleInterval) : GradeDetailEvent()
}