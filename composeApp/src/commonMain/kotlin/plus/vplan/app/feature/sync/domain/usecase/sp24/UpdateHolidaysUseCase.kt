package plus.vplan.app.feature.sync.domain.usecase.sp24

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Holiday
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client

class UpdateHolidaysUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val dayRepository: DayRepository
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

        val existingHolidays = dayRepository.getHolidays(school.id).first()

        existingHolidays.let { existing ->
            val downloadedDates = downloadedHolidays.data
            dayRepository.deleteHolidaysByIds(existing.filter { it.date !in downloadedDates }.map { it.id })
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
            .let { dayRepository.upsert(it) }

        return null
    }
}