package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.utils.isContinuous
import plus.vplan.app.utils.lastContinuousBy
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.until
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client

private const val TAG = "UpdateLessonTimesUseCase"
private val LOGGER = Logger.withTag(TAG)

class UpdateLessonTimesUseCase(
    private val groupRepository: GroupRepository,
    private val lessonTimeRepository: LessonTimeRepository
) {
    suspend operator fun invoke(school: School.AppSchool, providedClient: Stundenplan24Client? = null) {
        val client = providedClient ?: Stundenplan24Client(authentication = Authentication(
            sp24SchoolId = school.sp24Id,
            username = school.username,
            password = school.password
        ))
        val lessonTimes = (client.lessonTime.getLessonTime(contextSchoolWeek = null) as? plus.vplan.lib.sp24.source.Response.Success)?.data

        val groups = groupRepository.getBySchool(schoolId = school.id).first()
        val existingLessonTimes = lessonTimeRepository.getBySchool(schoolId = school.id)
            .map { it.filter { lessonTime -> !lessonTime.interpolated } }

        val downloadedLessonTimes = lessonTimes
            ?.mapNotNull { lessonTime ->
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

        if (downloadedLessonTimes == null) {
            LOGGER.e { "Downloaded lesson times are null" }
            return
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
    }
}