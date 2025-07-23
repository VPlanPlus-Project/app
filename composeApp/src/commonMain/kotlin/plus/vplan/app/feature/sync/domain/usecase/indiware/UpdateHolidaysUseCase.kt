package plus.vplan.app.feature.sync.domain.usecase.indiware

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Holiday
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.IndiwareBaseData
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.lib.sp24.source.Authentication

class UpdateHolidaysUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val dayRepository: DayRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool, indiwareBaseData: IndiwareBaseData? = null): Response.Error? {
        val baseData = indiwareBaseData ?: run {
            val baseData = indiwareRepository.getBaseData(Authentication(school.sp24Id, school.username, school.password))
            if (baseData is Response.Error) return baseData
            if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")
            baseData.data
        }

        val existingHolidays = dayRepository.getHolidays(school.id).first()
        val downloadedHolidays = baseData.holidays.map { holiday ->
            Holiday(
                date = holiday,
                school = school.id
            )
        }

        existingHolidays.let { existing ->
            val downloadedDates = downloadedHolidays.map { it.date }
            dayRepository.deleteHolidaysByIds(existing.filter { it.date !in downloadedDates }.map { it.id })
        }

        val existingDates = existingHolidays.map { it.date }
        downloadedHolidays
            .filter { it.date !in existingDates }
            .let { dayRepository.upsert(it) }

        return null
    }
}