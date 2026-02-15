package plus.vplan.app.feature.search.subfeature.room_search.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.LessonTimeRepository

class GetLessonTimesForProfileUseCase(
    private val lessonTimeRepository: LessonTimeRepository
) {
    operator fun invoke(profile: Profile): Flow<List<LessonTime>> {
        if (profile !is Profile.StudentProfile) return flowOf(emptyList())
        return lessonTimeRepository.getByGroup(profile.group.id)
    }
}