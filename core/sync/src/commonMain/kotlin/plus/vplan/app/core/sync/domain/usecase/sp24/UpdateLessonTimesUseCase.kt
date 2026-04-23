package plus.vplan.app.core.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.lesson_times.LessonTimeRepository
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.School
import plus.vplan.app.core.utils.date.plus
import plus.vplan.app.core.utils.date.until
import plus.vplan.app.core.utils.list.isContinuous
import plus.vplan.app.core.utils.list.lastContinuousBy
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.Response as Sp24Response

private const val TAG = "UpdateLessonTimesUseCase"
private val LOGGER = Logger.withTag(TAG)

class UpdateLessonTimesUseCase(
    private val groupRepository: GroupRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend operator fun invoke(school: School.AppSchool, providedClient: Stundenplan24Client? = null) {
        val client = providedClient ?: Stundenplan24Client(authentication = Authentication(
            sp24SchoolId = school.sp24Id,
            username = school.username,
            password = school.password
        ))

        val lessonTimesResult = client.lessonTime.getLessonTime(contextSchoolWeek = null)
        if (lessonTimesResult !is Sp24Response.Success) {
            val message = "Failed to download lesson time data: $lessonTimesResult"
            LOGGER.e { message }
            analyticsRepository.captureError(
                location = "UpdateLessonTimesUseCase",
                message = message
            )
            return
        }

        val lessonTimes = lessonTimesResult.data

        val groups = groupRepository.getBySchool(school).first()
        val existingLessonTimes = lessonTimeRepository.getBySchool(school)
            .map { it.filter { lessonTime -> !lessonTime.interpolated } }

        val downloadedLessonTimes = lessonTimes
            .mapNotNull { lessonTime ->
                val group = groups.firstOrNull { it.name == lessonTime.className } ?: return@mapNotNull null
                LessonTime(
                    id = LessonTime.buildId(school.id, group.id, lessonTime.lessonNumber),
                    start = lessonTime.start,
                    end = lessonTime.end,
                    lessonNumber = lessonTime.lessonNumber,
                    group = group.id,
                    interpolated = lessonTime.interpolated
                )
            }

        existingLessonTimes.first().let { existing ->
            val downloadedLessonTimesToDelete =
                existing.filter { existingLessonTime -> downloadedLessonTimes.none { it.id == existingLessonTime.id } }
            LOGGER.d { "Delete ${downloadedLessonTimesToDelete.size} lesson times" }
            lessonTimeRepository.delete(downloadedLessonTimesToDelete)
        }

        downloadedLessonTimes.let {
            val existingLessonTimesIds = existingLessonTimes.first()
            val downloadedLessonTimesToUpsert =
                downloadedLessonTimes.filter { downloadedLessonTime -> existingLessonTimesIds.none { it.hashCode() == downloadedLessonTime.hashCode() } }
            LOGGER.d { "Upsert ${downloadedLessonTimesToUpsert.size} lesson times" }
            lessonTimeRepository.save(downloadedLessonTimesToUpsert)
        }

        val lessonsToInterpolate = mutableListOf<LessonTime>()
        groups.forEach { group ->
            val lessonTimes = lessonTimeRepository
                .getByGroup(group).first()
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
        lessonTimeRepository.save(lessonsToInterpolate)

        LOGGER.i { "Lesson times updated" }
    }
}
