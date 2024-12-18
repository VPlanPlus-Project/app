package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

class GetProfileOptionsUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val defaultLessonRepository: DefaultLessonRepository
) {
    operator fun invoke(): Flow<List<OnboardingProfile>> = channelFlow {
        onboardingRepository.getSp24OnboardingSchool().collectLatest { currentOnboardingSchool ->
            if (currentOnboardingSchool == null) return@collectLatest send(emptyList())
            combine(
                groupRepository.getBySchool(currentOnboardingSchool.schoolId!!),
                teacherRepository.getBySchool(currentOnboardingSchool.schoolId),
                roomRepository.getBySchool(currentOnboardingSchool.schoolId)
            ) { groups, teachers, rooms ->
                groups.map {
                    OnboardingProfile.StudentProfile(
                        id = it.id,
                        name = it.name,
                        defaultLessons = defaultLessonRepository.getByGroup(it.id).first()
                    )
                } + teachers.map {
                    OnboardingProfile.TeacherProfile(
                        id = it.id,
                        name = it.name
                    )
                } + rooms.map {
                    OnboardingProfile.RoomProfile(
                        id = it.id,
                        name = it.name
                    )
                }
            }.collect {
                send(it)
            }
        }
    }
}