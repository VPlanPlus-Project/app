package plus.vplan.app.core.sync.domain.usecase

import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.model.Profile

class UpdateProfileLessonIndexUseCase(
    private val timetableRepository: TimetableRepository,
) {
    suspend operator fun invoke(profile: Profile, timetableVersion: Int) {
        val timetableLessonIds = timetableRepository.getTimetableLessonIdsForProfile(
            profile = profile,
            version = timetableVersion
        )
        timetableRepository.replaceLessonIndex(profile.id, timetableLessonIds)
    }
}
