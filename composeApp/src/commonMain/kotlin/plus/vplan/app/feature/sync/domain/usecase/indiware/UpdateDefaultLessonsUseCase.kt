package plus.vplan.app.feature.sync.domain.usecase.indiware

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository

class UpdateDefaultLessonsUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val defaultLessonRepository: DefaultLessonRepository,
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
        if (baseData is Response.Error) return baseData
        if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")

        val groups = groupRepository.getBySchool(schoolId = school.id).first()
        baseData.data.classes.forEach { baseDataClass ->
            val group = groups.firstOrNull { it.name == baseDataClass.name } ?: throw NoSuchElementException("Group ${baseDataClass.name} not found")
            val defaultLessons = defaultLessonRepository.getByGroup(groupId = group.id).first()

            val downloadedDefaultLessons = baseDataClass.defaultLessons.map { baseDataDefaultLesson ->

            }
        }


        return null
    }
}