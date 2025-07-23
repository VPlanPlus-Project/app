package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.isContinuous
import plus.vplan.app.utils.lastContinuousBy
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.until
import plus.vplan.lib.sp24.source.Authentication

private const val TAG = "UpdateLessonTimesUseCase"
private val LOGGER = Logger.withTag(TAG)

class UpdateLessonTimesUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val groupRepository: GroupRepository,
    private val weekRepository: WeekRepository,
    private val lessonTimeRepository: LessonTimeRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val weeks = weekRepository.getBySchool(school.id).first()
        val weeksInPastOrCurrent = weeks
            .filter { it.start < Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .sortedBy { it.weekIndex }

        val currentWeek = weeks.firstOrNull { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date in it.start..it.end }

        var weekIndex =
            (if (weeksInPastOrCurrent.isEmpty()) weeks.maxOfOrNull { it.weekIndex }
            else weeksInPastOrCurrent.lastOrNull()?.weekIndex)

        while (weekIndex != null && weekIndex >= 0) {
            weeks.firstOrNull { week -> week.weekIndex == weekIndex } ?: run {
                weekIndex -= 1
                continue
            }

            val sPlan = indiwareRepository.getWPlanSplan(school.getSp24LibAuthentication(), weekIndex)
            if (sPlan is plus.vplan.lib.sp24.source.Response.Success) break

            LOGGER.w { "Failed to download SPlan for week $weekIndex, trying next week" }
            weekIndex -= 1
        }
        if (weekIndex == -1) weekIndex = null

        val groups = groupRepository.getBySchool(schoolId = school.id).first()
        val existingLessonTimes = lessonTimeRepository.getBySchool(schoolId = school.id)
            .map { it.filter { lessonTime -> !lessonTime.interpolated } }

        val downloadedLessonTimes = indiwareRepository
            .downloadLessonTimes(school.getSp24LibAuthentication(), weekIndex)
            .let { (it as? plus.vplan.lib.sp24.source.Response.Success) }
            ?.data
            ?.mapNotNull { lessonTime ->
                val group = groups.firstOrNull { it.name == lessonTime.className } ?: return@mapNotNull null
                LessonTime(
                    id = "${school.id}/${group.id}/${lessonTime.lessonNumber}",
                    start = lessonTime.start,
                    end = lessonTime.end,
                    lessonNumber = lessonTime.lessonNumber,
                    group = group.id,
                    interpolated = false
                )
            }

        if (downloadedLessonTimes == null) {
            LOGGER.e { "Downloaded lesson times are null" }
            return null
        }

        existingLessonTimes.first().let { existing ->
            val downloadedLessonTimesToDelete =
                existing.filter { existingLessonTime -> downloadedLessonTimes.none { it.id == existingLessonTime.id } }
            LOGGER.d { "Delete ${downloadedLessonTimesToDelete.size} lesson times" }
            lessonTimeRepository.deleteById(downloadedLessonTimesToDelete.map { it.id })
        }

        downloadedLessonTimes.let {
            val existingLessonTimesIds = existingLessonTimes.first()
            val downloadedLessonTimesToUpsert =
                downloadedLessonTimes.filter { downloadedLessonTime -> existingLessonTimesIds.none { it.hashCode() == downloadedLessonTime.hashCode() } }
            LOGGER.d { "Upsert ${downloadedLessonTimesToUpsert.size} lesson times" }
            lessonTimeRepository.upsert(downloadedLessonTimesToUpsert)
        }

        val lessonsToInterpolate = mutableListOf<LessonTime>()
        groups.forEach { group ->
            val lessonTimes = lessonTimeRepository
                .getByGroup(group.id).first()
                .sortedBy { it.lessonNumber }
                .toMutableList()

            if (lessonTimes.isEmpty()) return@forEach
            while (!lessonTimes.map { it.lessonNumber }.isContinuous() || lessonTimes.size < 10) {
                val last = lessonTimes.lastContinuousBy { it.lessonNumber } ?: lessonTimes.last()
                val lessonDuration = (last.start until last.end)
                val next = LessonTime(
                    id = "${school.id}/${group.id}/${last.lessonNumber + 1}",
                    start = last.end,
                    end = last.end + lessonDuration,
                    lessonNumber = last.lessonNumber + 1,
                    group = group.id,
                    interpolated = true
                )
                lessonTimes.add(next)
                lessonTimes.sortBy { it.lessonNumber }
                lessonsToInterpolate.add(next)
            }
        }
        lessonTimeRepository.upsert(lessonsToInterpolate)

        LOGGER.i { "Lesson times updated" }

        return null
    }
}