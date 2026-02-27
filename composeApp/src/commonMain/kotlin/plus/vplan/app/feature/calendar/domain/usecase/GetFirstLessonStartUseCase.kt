package plus.vplan.app.feature.calendar.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.data.lesson_times.LessonTimeRepository

class GetFirstLessonStartUseCase(
    private val lessonTimeRepository: LessonTimeRepository
) {
    suspend operator fun invoke(profile: Profile): LocalTime {
        return (if (profile is Profile.StudentProfile) lessonTimeRepository.getByGroup(profile.group).first()
        else lessonTimeRepository.getBySchool(profile.school).first()).minOfOrNull { it.start } ?: LocalTime(0, 0)
    }
}