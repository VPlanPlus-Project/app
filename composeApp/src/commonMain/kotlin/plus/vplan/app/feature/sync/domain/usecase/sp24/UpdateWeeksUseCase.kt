package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client

private val LOGGER = Logger.withTag("UpdateWeeksUseCase")

class UpdateWeeksUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val weekRepository: WeekRepository
) {
    suspend operator fun invoke(school: School.AppSchool, providedClient: Stundenplan24Client? = null): Response.Error? {
        val client = providedClient ?: stundenplan24Repository.getSp24Client(
            Authentication(school.sp24Id, school.username, school.password),
            withCache = true
        )

        val weeks = run {
            val weeks = client.week.getWeeks()
            if (weeks is plus.vplan.lib.sp24.source.Response.Error) {
                LOGGER.e { "Failed to get weeks: $weeks" }
                return Response.Error.fromSp24KtError(weeks)
            }
            require(weeks is plus.vplan.lib.sp24.source.Response.Success)
            weeks.data
        }

        val existingWeeks = weekRepository.getBySchool(schoolId = school.id).first()
        val downloadedWeeks = weeks.map { baseDataWeek ->
            Week(
                calendarWeek = baseDataWeek.calendarWeek,
                start = baseDataWeek.start,
                end = baseDataWeek.end,
                weekType = baseDataWeek.weekType,
                weekIndex = baseDataWeek.weekIndex,
                school = school.id
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