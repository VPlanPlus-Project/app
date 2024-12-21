package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.utils.latest

private val LOGGER = Logger.withTag("UpdateLessonTimesUseCase")

class UpdateLessonTimesUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val groupRepository: GroupRepository,
    private val lessonTimeRepository: LessonTimeRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
        if (baseData is Response.Error) return baseData
        if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")

        val groups = groupRepository.getBySchool(schoolId = school.id).first()
        val existingLessonTimes = lessonTimeRepository.getBySchool(schoolId = school.id).map { it.filter { lessonTime -> !lessonTime.interpolated } }
        val downloadedLessonTimes = baseData.data.classes.flatMap { baseDataClass ->
            val group = groups.firstOrNull { it.name == baseDataClass.name } ?: throw NoSuchElementException("Group ${baseDataClass.name} not found")
            baseDataClass.lessonTimes.map { baseDataLessonTime ->
                LessonTime(
                    id = "${school.id}/${group.id}/${baseDataLessonTime.lessonNumber}",
                    start = baseDataLessonTime.start,
                    end = baseDataLessonTime.end,
                    lessonNumber = baseDataLessonTime.lessonNumber,
                    group = group,
                    interpolated = false
                )
            }
        }

        existingLessonTimes.latest().let { existing ->
            val downloadedLessonTimesToDelete = existing.filter { existingLessonTime -> downloadedLessonTimes.none { it.id == existingLessonTime.id } }
            LOGGER.d { "Delete ${downloadedLessonTimesToDelete.size} lesson times" }
            lessonTimeRepository.deleteById(downloadedLessonTimesToDelete.map { it.id })
        }

        downloadedLessonTimes.let {
            val existingLessonTimesIds = existingLessonTimes.latest()
            val downloadedLessonTimesToUpsert = downloadedLessonTimes.filter { downloadedLessonTime -> existingLessonTimesIds.none { it.hashCode() == downloadedLessonTime.hashCode() } }
            LOGGER.d { "Upsert ${downloadedLessonTimesToUpsert.size} lesson times" }
            lessonTimeRepository.upsert(downloadedLessonTimesToUpsert)
        }

        return null
    }
}