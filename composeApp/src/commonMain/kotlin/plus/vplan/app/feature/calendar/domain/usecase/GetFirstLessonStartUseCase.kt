package plus.vplan.app.feature.calendar.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.LessonTimeRepository

class GetFirstLessonStartUseCase(
    private val lessonTimeRepository: LessonTimeRepository
) {
    suspend operator fun invoke(profile: Profile): LocalTime {
        return (if (profile is Profile.StudentProfile) lessonTimeRepository.getByGroup(profile.groupId).first()
        else lessonTimeRepository.getBySchool(profile.getSchool().first().entityId.toInt()).first()).minOfOrNull { it.start } ?: LocalTime(0, 0)
    }
}