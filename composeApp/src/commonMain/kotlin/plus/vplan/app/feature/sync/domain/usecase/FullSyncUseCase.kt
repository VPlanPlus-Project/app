package plus.vplan.app.feature.sync.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateDefaultLessonsUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class FullSyncUseCase(
    private val schoolRepository: SchoolRepository,
    private val dayRepository: DayRepository,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateDefaultLessonsUseCase: UpdateDefaultLessonsUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase,
) {
    suspend operator fun invoke() {
        schoolRepository.getAll().first().filterIsInstance<School.IndiwareSchool>().forEach { school ->
            updateDefaultLessonsUseCase(school)
            updateHolidaysUseCase(school)
            updateWeeksUseCase(school)
            val today = LocalDate.now()
            val nextDay = run {
                val holidayDates = dayRepository.getHolidays(school.id).first().map { it.date }
                var start = today + 1.days
                while (start.dayOfWeek.isoDayNumber > 5 || start in holidayDates) {
                    start += 1.days
                }
                start
            }

            updateSubstitutionPlanUseCase(school, today)
            updateSubstitutionPlanUseCase(school, nextDay)

        }
        updateHomeworkUseCase()
        updateAssessmentUseCase()
    }
}