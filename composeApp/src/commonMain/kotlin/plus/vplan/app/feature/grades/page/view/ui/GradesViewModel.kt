package plus.vplan.app.feature.grades.page.view.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleYearsRepository
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import plus.vplan.app.utils.atStartOfDay
import plus.vplan.app.utils.currentThreadName
import plus.vplan.app.utils.now
import plus.vplan.app.utils.until
import kotlin.time.Duration.Companion.days

class GradesViewModel(
    private val calculateAverageUseCase: CalculateAverageUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val requestGradeUnlockUseCase: RequestGradeUnlockUseCase,
    private val lockUseCase: LockGradesUseCase,
    private val vppIdRepository: VppIdRepository
) : ViewModel(), KoinComponent {
    private val gradeState = MutableStateFlow(GradesState())
    val state = gradeState.asStateFlow()

    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()
    private val besteSchuleYearsRepository by inject<BesteSchuleYearsRepository>()

    private val getGradeLockStateUseCase by inject<GetGradeLockStateUseCase>()

    private val currentSchulverwalterUser =
        MutableStateFlow<VppId.Active.SchulverwalterConnection?>(null)

    init {
        viewModelScope.launch subscribeToGradeLock@{
            getGradeLockStateUseCase().collectLatest { lockState ->
                gradeState.update { it.copy(gradeLockState = lockState) }
            }
        }

        viewModelScope.launch subscribeToSelectedYearAndIntervals@{
            Logger.d("subscribeToSelectedYearAndIntervals") {"thread: ${currentThreadName()}"}
            combine(
                gradeState.map { it.selectedYear },
                gradeState.map { it.availableIntervals },
                currentSchulverwalterUser
            ) { selectedYear, intervals, schulverwalterUser ->
                Triple(selectedYear, intervals, schulverwalterUser)
            }
                .distinctUntilChangedBy { (year, intervals, user) ->
                    year?.id.hashCode() + intervals.map { it.id }.sorted().hashCode() + user?.userId.hashCode()
                }
                .collectLatest { (selectedYear, allIntervals, schulverwalterUser) ->
                    if (selectedYear == null || schulverwalterUser == null) {
                        gradeState.update { it.copy(intervalsForSelectedYear = emptyMap()) }
                        return@collectLatest
                    }

                    val intervalsForYear = allIntervals.filter { it.yearId == selectedYear.id }

                    if (!getGradeLockStateUseCase().first().canAccess) {
                        gradeState.update {
                            it.copy(
                                intervalsForSelectedYear = intervalsForYear.associateWith { _ ->
                                    IntervalData(
                                        avg = null,
                                        subjects = emptyList(),
                                        latestGrades = emptyList()
                                    )
                                }
                            )
                        }
                        return@collectLatest
                    }

                    besteSchuleGradesRepository.getGrades(
                        responsePreference = ResponsePreference.Fast,
                        contextBesteschuleUserId = schulverwalterUser.userId,
                        contextBesteschuleAccessToken = schulverwalterUser.accessToken
                    )
                        .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                        .map { it.data }
                        .collectLatest { allGrades ->
                            val intervalDataMap = intervalsForYear.associateWith { interval ->
                                calculateIntervalData(interval, intervalsForYear, allGrades)
                            }

                            gradeState.update { it.copy(intervalsForSelectedYear = intervalDataMap) }
                        }
                }
        }

        viewModelScope.launch {
            currentSchulverwalterUser
                .distinctUntilChangedBy { it?.userId }
                .collectLatest collectUser@{ currentSchulverwalterUser ->
                    gradeState.update { GradesState(gradeLockState = it.gradeLockState) }
                    Logger.d { "Switching to schulverwalter user ${currentSchulverwalterUser?.userId}" }

                    if (currentSchulverwalterUser == null) return@collectUser

                    combine(
                        besteSchuleYearsRepository.getYears(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleAccessToken = currentSchulverwalterUser.accessToken
                        )
                            .filterIsInstance<Response.Success<List<BesteSchuleYear>>>()
                            .map { it.data },
                        besteSchuleIntervalsRepository.getIntervals(
                            responsePreference = ResponsePreference.Fast,
                            contextBesteschuleAccessToken = currentSchulverwalterUser.accessToken,
                            contextBesteschuleUserId = currentSchulverwalterUser.userId
                        )
                    ) { years, intervals ->
                        val intervalIdsForUser =
                            (intervals as? Response.Success)?.data?.map { it.id }.orEmpty()
                        years.filter { year -> year.intervalIds.any { it in intervalIdsForUser } }
                    }.collectLatest { years ->
                        gradeState.update { gradesState ->
                            val isFirstLoad = gradesState.isLoading
                            val selectedYear =
                                if (isFirstLoad) years.firstOrNull { year -> LocalDate.now() in year.from..year.to } ?: years.maxByOrNull { it.to }
                                else gradesState.selectedYear

                            gradesState.copy(
                                availableYears = years,
                                selectedYear = selectedYear,
                                availableIntervals = emptyList(),
                            )
                        }

                        combine(years.map { it.intervals }) {
                            it.toList().flatten()
                        }.collectLatest { intervals ->
                            gradeState.update { gradesState ->
                                gradesState.copy(
                                    availableIntervals = intervals,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }



    private suspend fun calculateIntervalData(
        interval: BesteSchuleInterval,
        allIntervalsInYear: List<BesteSchuleInterval>,
        allGrades: List<BesteSchuleGrade>
    ): IntervalData {
        val relevantIntervalIds = buildSet {
            add(interval.id)
            interval.includedIntervalId?.let { add(it) }

            allIntervalsInYear
                .filter { it.includedIntervalId == interval.id }
                .forEach { add(it.id) }
        }

        val gradesForInterval = allGrades.filter { grade ->
            grade.collection.first()!!.intervalId in relevantIntervalIds
        }

        if (gradesForInterval.isEmpty()) {
            return IntervalData(
                avg = Double.NaN,
                subjects = emptyList(),
                latestGrades = emptyList()
            )
        }

        val subjects = gradesForInterval
            .groupBy { grade -> grade.collection.first()!!.subjectId }
            .map { (subjectId, gradesForSubject) ->
                val subject = besteSchuleSubjectsRepository.getSubjectFromCache(subjectId).first()!!

                val categoriesMap = gradesForSubject.groupBy { grade ->
                    grade.collection.first()!!.type
                }

                val categories = categoriesMap.map { (type, gradesForType) ->
                    val weight = 1.0

                    Subject.Category(
                        id = (subjectId.toString() + type).hashCode(),
                        name = type,
                        average = null,
                        count = gradesForType.size,
                        weight = weight,
                        grades = gradesForType
                            .filter { it.value != null }
                            .associateWith { it.isSelectedForFinalGrade }
                            .toList()
                            .sortedBy { it.first.givenAt }
                            .toMap(),
                        calculatorGrades = emptyList()
                    )
                }.sortedBy { it.name }

                Subject(
                    id = subjectId,
                    average = null,
                    name = subject.fullName,
                    categories = categories
                )
            }
            .sortedBy { it.name }

        val subjectsWithAverages = subjects.map { subject ->
            val gradesForSubject = subject.categories
                .flatMap { it.grades.filterValues { selected -> selected == true }.keys }

            val subjectAverage = if (gradesForSubject.isNotEmpty()) {
                calculateAverageUseCase(
                    grades = gradesForSubject,
                    interval = interval,
                    additionalGrades = emptyList()
                )
            } else null

            val categoriesWithAverages = subject.categories.map { category ->
                val gradesForCategory = category.grades.filterValues { it == true }.keys.toList()
                val categoryAverage = if (gradesForCategory.isNotEmpty()) {
                    calculateAverageUseCase(
                        grades = gradesForCategory,
                        interval = interval,
                        additionalGrades = emptyList()
                    )
                } else null

                category.copy(average = categoryAverage)
            }

            subject.copy(
                average = subjectAverage,
                categories = categoriesWithAverages
            )
        }

        val fullAverage = if (subjectsWithAverages.isNotEmpty()) {
            val allSelectedGrades = subjectsWithAverages
                .flatMap { it.categories.flatMap { category -> category.grades.filterValues { it == true }.keys } }

            if (allSelectedGrades.isNotEmpty()) {
                calculateAverageUseCase(
                    grades = allSelectedGrades,
                    interval = interval,
                    additionalGrades = emptyList()
                )
            } else null
        } else null

        return IntervalData(
            avg = fullAverage,
            subjects = subjectsWithAverages,
            latestGrades = gradesForInterval
                .filter { it.givenAt.atStartOfDay() until LocalDateTime.now() < 14.days }
                .sortedByDescending { it.givenAt }
                .take(4)
        )
    }

    fun init(vppIdId: Int) {
        viewModelScope.launch {
            val vppId = vppIdRepository.getById(vppIdId, ResponsePreference.Fast)
                .getFirstValueOld() as? VppId.Active
            val schulverwalterConnection = vppId?.schulverwalterConnection ?: return@launch
            currentSchulverwalterUser.value = schulverwalterConnection
        }
    }

    fun onEvent(event: GradeDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is GradeDetailEvent.ToggleConsiderForFinalGrade -> {
                    val intervalId = event.grade.collection.first()!!.intervalId

                    gradeState.update { state ->
                        val updatedIntervalsMap = state.intervalsForSelectedYear.mapValues { (interval, data) ->
                            if (interval.id == intervalId || interval.includedIntervalId == intervalId) {
                                val updatedSubjects = data.subjects.map { subject ->
                                    subject.copy(
                                        categories = subject.categories.map { category ->
                                            category.copy(
                                                grades = category.grades.mapKeys { (grade, _) ->
                                                    grade
                                                }.mapValues { (grade, currentSelection) ->
                                                    if (grade.id == event.grade.id) {
                                                        val newValue = !(currentSelection ?: true)
                                                        newValue
                                                    } else currentSelection
                                                }
                                            )
                                        }
                                    )
                                }
                                data.copy(subjects = updatedSubjects)
                            } else data
                        }
                        state.copy(intervalsForSelectedYear = updatedIntervalsMap)
                    }

                    recalculateInterval(intervalId)
                }

                is GradeDetailEvent.ToggleEditMode -> {
                    gradeState.update { it.copy(isInEditMode = !it.isInEditMode) }
                }

                is GradeDetailEvent.AddGrade -> {
                    gradeState.update { state ->
                        val updatedIntervalsMap = state.intervalsForSelectedYear.mapValues { (_, data) ->
                            val updatedSubjects = data.subjects.map { subject ->
                                subject.copy(
                                    categories = subject.categories.map { category ->
                                        if (category.id == event.categoryId) {
                                            category.copy(calculatorGrades = category.calculatorGrades + event.grade)
                                        } else category
                                    }
                                )
                            }
                            data.copy(subjects = updatedSubjects)
                        }
                        state.copy(intervalsForSelectedYear = updatedIntervalsMap)
                    }

                    recalculateAllIntervals()
                }

                is GradeDetailEvent.RemoveGrade -> {
                    gradeState.update { state ->
                        val updatedIntervalsMap = state.intervalsForSelectedYear.mapValues { (_, data) ->
                            val updatedSubjects = data.subjects.map { subject ->
                                subject.copy(
                                    categories = subject.categories.map { category ->
                                        if (category.id == event.categoryId) {
                                            category.copy(calculatorGrades = category.calculatorGrades - event.grade)
                                        } else category
                                    }
                                )
                            }
                            data.copy(subjects = updatedSubjects)
                        }
                        state.copy(intervalsForSelectedYear = updatedIntervalsMap)
                    }

                    recalculateAllIntervals()
                }

                is GradeDetailEvent.ResetAdditionalGrades -> {
                    gradeState.update { state ->
                        val updatedIntervalsMap = state.intervalsForSelectedYear.mapValues { (_, data) ->
                            val updatedSubjects = data.subjects.map { subject ->
                                subject.copy(
                                    categories = subject.categories.map { category ->
                                        category.copy(calculatorGrades = emptyList())
                                    }
                                )
                            }
                            data.copy(subjects = updatedSubjects)
                        }
                        state.copy(intervalsForSelectedYear = updatedIntervalsMap)
                    }

                    recalculateAllIntervals()
                }

                is GradeDetailEvent.Refresh -> {
                    gradeState.update { it.copy(isUpdating = true) }
                    try {
                        syncGradesUseCase(
                            allowNotifications = true,
                            yearId = state.value.selectedYear?.id
                        )
                    } finally {
                        gradeState.update { it.copy(isUpdating = false) }
                    }
                }

                is GradeDetailEvent.RequestGradeUnlock -> requestGradeUnlockUseCase()
                is GradeDetailEvent.RequestGradeLock -> lockUseCase()
                is GradeDetailEvent.SelectYear -> {
                    gradeState.update { it.copy(selectedYear = event.year) }
                }
            }
        }
    }

    private suspend fun recalculateInterval(intervalId: Int) {
        val currentState = gradeState.value
        val interval = currentState.intervalsForSelectedYear.keys.find {
            it.id == intervalId
        } ?: return

        val allIntervalsInYear = currentState.intervalsForSelectedYear.keys.toList()
        val schulverwalterUser = currentSchulverwalterUser.value ?: return

        val allGrades = besteSchuleGradesRepository.getGrades(
            responsePreference = ResponsePreference.Fast,
            contextBesteschuleUserId = schulverwalterUser.userId,
            contextBesteschuleAccessToken = schulverwalterUser.accessToken
        )
            .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
            .first()
            .data

        val newData = calculateIntervalData(interval, allIntervalsInYear, allGrades)

        gradeState.update { state ->
            state.copy(
                intervalsForSelectedYear = state.intervalsForSelectedYear.toMutableMap().apply {
                    put(interval, newData)
                }
            )
        }
    }

    private suspend fun recalculateAllIntervals() {
        val currentState = gradeState.value
        val allIntervalsInYear = currentState.intervalsForSelectedYear.keys.toList()
        val schulverwalterUser = currentSchulverwalterUser.value ?: return

        val allGrades = withContext(Dispatchers.IO) {
            besteSchuleGradesRepository.getGrades(
                responsePreference = ResponsePreference.Fast,
                contextBesteschuleUserId = schulverwalterUser.userId,
                contextBesteschuleAccessToken = schulverwalterUser.accessToken
            )
                .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                .first()
                .data
        }

        val updatedIntervalsMap = withContext(Dispatchers.Default) {
            allIntervalsInYear.associateWith { interval ->
                calculateIntervalData(interval, allIntervalsInYear, allGrades)
            }
        }

        gradeState.update { it.copy(intervalsForSelectedYear = updatedIntervalsMap) }
    }
}

/**
 * @param isLoading If the ViewModel is preparing the data to be shown. If true, don't show content in UI.
 * @param availableYears All years that can be viewed by the current user.
 * @param selectedYear The year that has been selected by the drawer.
 * @param availableIntervals All available intervals for the current user.
 * @param intervalsForSelectedYear Map of intervals to their data for the selected year.
 * @param isInEditMode Whether the user is in edit mode (for calculator grades).
 * @param isUpdating Whether a refresh is currently in progress.
 * @param gradeLockState The current lock state of grades.
 */
data class GradesState(
    val isLoading: Boolean = true,
    val availableYears: List<BesteSchuleYear> = emptyList(),
    val selectedYear: BesteSchuleYear? = null,
    val availableIntervals: List<BesteSchuleInterval> = emptyList(),
    val intervalsForSelectedYear: Map<BesteSchuleInterval, IntervalData> = emptyMap(),
    val isInEditMode: Boolean = false,
    val isUpdating: Boolean = false,
    val gradeLockState: GradeLockState? = null,
)

data class IntervalData(
    val avg: Double?,
    val subjects: List<Subject>,
    val latestGrades: List<BesteSchuleGrade>
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

    data class SelectYear(val year: BesteSchuleYear) : GradeDetailEvent()
}
