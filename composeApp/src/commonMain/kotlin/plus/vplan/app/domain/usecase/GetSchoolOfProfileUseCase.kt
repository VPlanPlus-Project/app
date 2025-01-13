package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Teacher

class GetSchoolOfProfileUseCase {
    suspend operator fun invoke(profile: Profile): Int {
        return when (profile) {
            is Profile.StudentProfile -> App.groupSource.getById(profile.group).filterIsInstance<CacheState.Done<Group>>().first().data.school
            is Profile.TeacherProfile -> App.teacherSource.getById(profile.teacher).filterIsInstance<CacheState.Done<Teacher>>().first().data.school
            is Profile.RoomProfile -> TODO()
            else -> throw IllegalStateException("Profile type not found")
        }
    }
}