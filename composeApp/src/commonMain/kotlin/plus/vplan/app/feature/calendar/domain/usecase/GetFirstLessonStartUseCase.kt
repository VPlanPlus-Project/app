package plus.vplan.app.feature.calendar.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.LessonTimeRepository
import kotlin.uuid.Uuid

class GetFirstLessonStartUseCase(
    private val lessonTimeRepository: LessonTimeRepository
) {
    suspend operator fun invoke(profile: Profile): LocalTime {
        return (if (profile is Profile.StudentProfile) lessonTimeRepository.getByGroup(profile.groupId).first()
        else lessonTimeRepository.getBySchool(Uuid.parseHex(profile.getSchool().first().entityId)).first()).minOfOrNull { it.start } ?: LocalTime(0, 0)
    }
}