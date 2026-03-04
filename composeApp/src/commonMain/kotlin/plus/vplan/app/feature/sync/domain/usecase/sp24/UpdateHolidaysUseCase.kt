package plus.vplan.app.feature.sync.domain.usecase.sp24

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.School
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client

class UpdateHolidaysUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val holidayRepository: HolidayRepository,
) {
    suspend operator fun invoke(
        school: School.AppSchool,
        providedClient: Stundenplan24Client? = null,
    ): Response.Error? {
        val client = providedClient ?: stundenplan24Repository.getSp24Client(
            Authentication(school.sp24Id, school.username, school.password),
            withCache = true
        )

        val downloadedHolidays = client.holiday.getHolidays()

        if (downloadedHolidays is plus.vplan.lib.sp24.source.Response.Error) {
            return Response.Error.fromSp24KtError(downloadedHolidays)
        }
        require(downloadedHolidays is plus.vplan.lib.sp24.source.Response.Success)

        val existingHolidays = holidayRepository.getBySchool(school).first()

        existingHolidays.let { existing ->
            val downloadedDates = downloadedHolidays.data
            holidayRepository.delete(existing.filter { it.date !in downloadedDates })
        }

        val existingDates = existingHolidays.map { it.date }
        downloadedHolidays.data
            .filter { it !in existingDates }
            .map {
                Holiday(
                    date = it,
                    school = school.id
                )
            }
            .let { holidayRepository.save(it) }

        return null
    }
}