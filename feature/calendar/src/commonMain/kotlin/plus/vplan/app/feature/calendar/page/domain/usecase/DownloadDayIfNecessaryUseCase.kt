@file:OptIn(FlowPreview::class)

package plus.vplan.app.feature.calendar.page.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Timetable
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.core.utils.date.now

class DownloadDayIfNecessaryUseCase(
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val timetableRepository: TimetableRepository,
) {

    private val mutex = Mutex()
    val isRunning: StateFlow<Boolean>
        field = MutableStateFlow(false)
    suspend operator fun invoke(date: LocalDate, school: School.AppSchool) = withContext(Dispatchers.Default) {
        if (date > LocalDate.now()) return@withContext
        mutex.withLock {
            val timetables = timetableRepository.getTimetables(school).first()
                .filter { it.week.start <= date }
            if (timetables.isEmpty() || timetables.maxBy { it.week.start }.dataState == Timetable.HasData.Unknown) {
                try {
                    isRunning.value = true
                    updateTimetableUseCase.updateTimetableRelatedToDate(date, school)
                } finally {
                    isRunning.value = false
                }
            }
        }
    }
}