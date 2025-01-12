package plus.vplan.app.feature.sync.domain.usecase.indiware

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Holiday
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.utils.latest

class UpdateHolidaysUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val dayRepository: DayRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
        if (baseData is Response.Error) return baseData
        if (baseData !is Response.Success) throw IllegalStateException("Unexpected response type")

        val existingHolidays = dayRepository.getHolidays(school.id).latest()
        val downloadedHolidays = baseData.data.holidays.map { holiday ->
            Holiday(
                date = holiday,
                school = school.id
            )
        }

        existingHolidays.let { existing ->
            val downloadedDates = downloadedHolidays.map { it.date }
            dayRepository.deleteHolidaysByIds(existing.filter { it.date !in downloadedDates }.map { it.id })
        }

        downloadedHolidays.let { downloaded ->
            dayRepository.upsert(downloaded)
        }

        return null
    }
}