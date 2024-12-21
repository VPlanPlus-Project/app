package plus.vplan.app.feature.sync.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.WeekRepository

private val LOGGER = Logger.withTag("UpdateWeeksUseCase")

class UpdateWeeksUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val weekRepository: WeekRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
        if (baseData is Response.Error) return baseData
        if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")
        if (baseData.data.weeks == null) return Response.Error.Other("No weeks found")
        val existingWeeks = weekRepository.getBySchool(schoolId = school.id).first()
        val downloadedWeeks = baseData.data.weeks.map { baseDataWeek ->
            Week(
                id = school.id.toString() + "/" + baseDataWeek.calendarWeek.toString(),
                calendarWeek = baseDataWeek.calendarWeek,
                start = baseDataWeek.start,
                end = baseDataWeek.end,
                weekType = baseDataWeek.weekType,
                weekIndex = baseDataWeek.weekIndex,
                school = school
            )
        }
        existingWeeks.let { existing ->
            val downloadedIds = downloadedWeeks.map { it.id }
            val weeksToDelete = existing.filter { existingWeek -> downloadedIds.none { it == existingWeek.id } }
            LOGGER.d { "Delete ${weeksToDelete.size} weeks" }
            weekRepository.deleteById(weeksToDelete.map { it.id })
        }
        existingWeeks.let { existing ->
            val weeksToUpsert = downloadedWeeks.filter { downloadedWeek -> existing.none { it.hashCode() == downloadedWeek.hashCode() } }
            LOGGER.d { "Upsert ${weeksToUpsert.size} weeks" }
            weekRepository.upsert(weeksToUpsert)
        }
        return null
    }
}